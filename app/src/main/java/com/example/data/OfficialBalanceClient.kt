package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

data class OfficialBalanceRow(val label: String, val value: String)
data class OfficialBalanceResult(val rows: List<OfficialBalanceRow>, val dataRemaining: String?)

/**
 * Protocol observed in the user's before/after MHTML captures.
 * No SharedPreferences, disk cache, WebView, logging or SSL bypass.
 * All session cookies, hidden fields, captcha and subscriber input are memory-only.
 */
class OfficialBalanceClient {
    private val portal = "https://svc.ptc.gov.ye/4g/"
    private val captchaUrl = "https://svc.ptc.gov.ye/wp-content/plugins/query4g-bill-api/securimage/securimage_show.php?namespace=q4g_one_captcha"
    private val cookies = mutableListOf<Cookie>()
    private var hiddenFields: Map<String, String> = emptyMap()
    private val jar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, incoming: List<Cookie>) {
            synchronized(cookies) {
                incoming.forEach { cookie ->
                    cookies.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
                    if (cookie.expiresAt > System.currentTimeMillis()) cookies.add(cookie)
                }
            }
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(cookies) {
            cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
            cookies.filter { it.matches(url) }
        }
    }
    private val client = OkHttpClient.Builder()
        .cookieJar(jar)
        .cache(null)
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    fun clear() {
        client.dispatcher.cancelAll()
        synchronized(cookies) { cookies.clear() }
        hiddenFields = emptyMap()
    }

    suspend fun loadCaptcha(): Bitmap = withContext(Dispatchers.IO) {
        clear()
        val page = String(fetch(request(portal).build()), Charsets.UTF_8)
        coroutineContext.ensureActive()
        val form = blocks(page, "form").firstOrNull { block ->
            attribute(block, "id") == "qbill_formnew"
        } ?: throw IOException("نموذج الموقع غير متاح أو تغيّر. لم يتم إرسال رقم.")
        if (attribute(form, "method")?.lowercase() != "post" ||
            attribute(form, "action") != portal) {
            throw IOException("تغيّرت وجهة نموذج الاستعلام؛ أوقف التطبيق الإرسال لحماية بياناتك.")
        }
        val inputs = Regex("<input\\b[^>]*>", options).findAll(form).map { it.value }.toList()
        if (!inputs.any { attribute(it, "name") == "phone4gidnew" } ||
            !inputs.any { attribute(it, "name") == "captcha_code_q4Gbill" } ||
            !inputs.any { attribute(it, "name") == "qsubmitnew" }) {
            throw IOException("حقول النموذج الرسمي تغيّرت؛ يلزم تحديث الربط.")
        }
        val image = Regex("<img\\b[^>]*>", options).findAll(form).map { it.value }
            .firstOrNull { attribute(it, "id") == "qp4g-captcha_img" }
        if (image == null || attribute(image, "src") != captchaUrl) {
            throw IOException("تعذّر التحقق من مصدر الكابتشا الرسمي.")
        }
        hiddenFields = inputs.filter { attribute(it, "type")?.lowercase() == "hidden" }
            .mapNotNull { input -> attribute(input, "name")?.let { it to (attribute(input, "value") ?: "") } }
            .toMap()
        val bytes = fetch(request("$captchaUrl&t=${System.currentTimeMillis()}").build())
        coroutineContext.ensureActive()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth !in 1..2048 || bounds.outHeight !in 1..1024) {
            throw IOException("استجابة الكابتشا ليست صورة صالحة.")
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IOException("تعذّر عرض صورة الكابتشا الرسمية.")
    }

    suspend fun query(number: String, code: String): OfficialBalanceResult = withContext(Dispatchers.IO) {
        require(number.matches(Regex("[0-9]{8,20}"))) { "أدخل رقم الاشتراك بأرقام صحيحة." }
        require(code.isNotBlank() && code.length <= 20) { "أدخل رمز التحقق الظاهر في الصورة." }
        val body = FormBody.Builder()
        hiddenFields.filterKeys { it !in setOf("phone4gidnew", "captcha_code_q4Gbill", "qsubmitnew") }
            .forEach { (name, value) -> body.add(name, value) }
        body.add("phone4gidnew", number)
            .add("captcha_code_q4Gbill", code)
            .add("qsubmitnew", "استعلام")
        val html = String(fetch(request(portal).header("Origin", "https://svc.ptc.gov.ye")
            .post(body.build()).build()), Charsets.UTF_8)
        coroutineContext.ensureActive()
        parseResult(html, number)
    }

    private fun request(url: String) = Request.Builder().url(url)
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36")
        .header("Referer", portal)
        .header("Cache-Control", "no-store")

    private fun fetch(request: Request): ByteArray {
        // Never follow redirects: the subscriber is sent only to the verified HTTPS endpoint.
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("لم يستجب الموقع الرسمي بنجاح. حاول لاحقًا.")
            val body = response.body ?: throw IOException("استجابة الموقع فارغة.")
            if (body.contentLength() > 1_048_576) throw IOException("استجابة الموقع أكبر من المتوقع.")
            val output = ByteArrayOutputStream()
            body.byteStream().use { stream ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    if (output.size() + count > 1_048_576) throw IOException("استجابة الموقع أكبر من المتوقع.")
                    output.write(buffer, 0, count)
                }
            }
            return output.toByteArray()
        }
    }

    companion object {
        private val options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        private fun blocks(html: String, tag: String) =
            Regex("<$tag\\b[^>]*>.*?</$tag\\s*>", options).findAll(html).map { it.value }.toList()
        private fun attribute(tag: String, name: String): String? {
            val opening = tag.substringBefore('>') + ">"
            return Regex("\\b${Regex.escape(name)}\\s*=\\s*([\"'])(.*?)\\1", options)
                .find(opening)?.groupValues?.get(2)?.let { text(it) }
        }
        private fun text(html: String) = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            .toString().replace('\u00a0', ' ').replace(Regex("\\s+"), " ").trim()

        fun parseResult(html: String, expectedNumber: String): OfficialBalanceResult {
            val table = blocks(html, "table").firstOrNull {
                attribute(it, "class")?.split(Regex("\\s+"))?.contains("transdetail") == true
            } ?: throw IOException("لم يرجع الموقع نتيجة رصيد. تحقّق من الرقم والرمز، ثم حدّث الكابتشا وحاول مجددًا.")
            var section = ""
            var returnedNumber: String? = null
            var dataRemaining: String? = null
            val rows = mutableListOf<OfficialBalanceRow>()
            blocks(table, "tr").forEach { row ->
                val header = blocks(row, "th").firstOrNull()
                val cell = blocks(row, "td").firstOrNull()
                if (header == null) {
                    section = cell?.let { text(it) }.orEmpty()
                } else if (cell != null) {
                    val label = text(header)
                    val value = text(cell)
                    if (label == "رقم الهاتف") {
                        returnedNumber = value.filter { it.isDigit() }.map {
                            Character.digit(it, 10).toString()
                        }.joinToString("")
                    } else if (value.isNotBlank()) {
                        val displayLabel = if (section.isNotBlank() && label in setOf("الرصيد المتاح", "الرصيد الإجمالي")) {
                            "$section · $label"
                        } else label
                        rows.add(OfficialBalanceRow(displayLabel, value))
                        if (section.contains("رصيد البيانات") && label == "الرصيد المتاح") dataRemaining = value
                    }
                }
            }
            if (returnedNumber != expectedNumber || rows.none { it.label == "الباقة" } ||
                rows.none { it.label == "تاريخ انتهاء الرصيد" } || dataRemaining == null) {
                throw IOException("بنية النتيجة لا تطابق الاستعلام؛ لم تُعرض بيانات غير مؤكدة.")
            }
            return OfficialBalanceResult(rows, dataRemaining)
        }
    }
}
