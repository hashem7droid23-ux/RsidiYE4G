package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Html
import kotlinx.coroutines.CancellationException
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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import javax.net.ssl.SSLException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

data class OfficialBalanceRow(val label: String, val value: String)
data class OfficialBalanceResult(val rows: List<OfficialBalanceRow>, val dataRemaining: String?)

private enum class CaptchaStage(val label: String) {
    PAGE("فتح صفحة الاستعلام"),
    FORM("قراءة النموذج والحقول"),
    SOURCE("التحقق من رابط الكابتشا"),
    IMAGE("تحميل صورة الكابتشا"),
    DECODE("قراءة الصورة وعرضها")
}

private class SafePortalFailure(val detail: String) : IOException()
private class CaptchaDiagnostic(val stage: CaptchaStage, val detail: String) : IOException()

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
        var stage = CaptchaStage.PAGE
        try {
        clear()
        val page = String(fetch(request(portal).build()), Charsets.UTF_8)
        coroutineContext.ensureActive()
        stage = CaptchaStage.FORM
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
        stage = CaptchaStage.SOURCE
        val image = Regex("<img\\b[^>]*>", options).findAll(form).map { it.value }
            .firstOrNull { attribute(it, "id") == "qp4g-captcha_img" }
        if (image == null || attribute(image, "src") != captchaUrl) {
            throw IOException("تعذّر التحقق من مصدر الكابتشا الرسمي.")
        }
        hiddenFields = inputs.filter { attribute(it, "type")?.lowercase() == "hidden" }
            .mapNotNull { input -> attribute(input, "name")?.let { it to (attribute(input, "value") ?: "") } }
            .toMap()
        stage = CaptchaStage.IMAGE
        val bytes = fetch(request("$captchaUrl&t=${System.currentTimeMillis()}").build())
        coroutineContext.ensureActive()
        stage = CaptchaStage.DECODE
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth !in 1..2048 || bounds.outHeight !in 1..1024) {
            throw IOException("استجابة الكابتشا ليست صورة صالحة.")
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IOException("تعذّر عرض صورة الكابتشا الرسمية.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val detail = when (e) {
                is SafePortalFailure -> e.detail
                is SSLException, is CertificateException ->
                    "فشل التحقق من اتصال HTTPS أو شهادة الموقع. لم يتم تجاوز الأمان."
                is UnknownHostException -> "تعذّر العثور على عنوان خادم الموقع (DNS)."
                is SocketTimeoutException -> "انتهت مهلة الاتصال قبل اكتمال هذه المرحلة."
                is ConnectException -> "تعذّر إنشاء اتصال بالخادم."
                else -> when (stage) {
                    CaptchaStage.FORM -> "النموذج أو وجهته أو حقوله لا تطابق البنية التي تم التحقق منها."
                    CaptchaStage.SOURCE -> "رابط صورة الكابتشا غائب أو لا يطابق المصدر الرسمي المتوقع."
                    CaptchaStage.DECODE -> "الاستجابة ليست صورة قابلة للعرض، أو أبعادها خارج الحد المسموح."
                    else -> "توقّف اتصال الشبكة أو لم تكتمل قراءة الاستجابة."
                }
            }
            // No raw exception message, response body, URL, headers or cookie values.
            throw CaptchaDiagnostic(stage, detail)
        }
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
            if (response.code in 300..399) {
                throw SafePortalFailure("أعاد الخادم تحويلًا للرابط (HTTP ${response.code}). التطبيق لم يتبعه؛ يلزم التحقق من وجهته.")
            }
            if (!response.isSuccessful) {
                throw SafePortalFailure("رفض الخادم الطلب أو تعذّر تنفيذه (HTTP ${response.code}).")
            }
            val body = response.body ?: throw SafePortalFailure("وصلت استجابة بلا محتوى.")
            if (body.contentLength() > 1_048_576) throw SafePortalFailure("حجم الاستجابة تجاوز الحد الآمن.")
            val output = ByteArrayOutputStream()
            body.byteStream().use { stream ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    if (output.size() + count > 1_048_576) throw SafePortalFailure("حجم الاستجابة تجاوز الحد الآمن.")
                    output.write(buffer, 0, count)
                }
            }
            return output.toByteArray()
        }
    }

    companion object {
        fun captchaFailureMessage(error: Exception): String {
            val diagnosis = error as? CaptchaDiagnostic
                ?: return "تعذّر تحميل الكابتشا. فشل غير مصنّف؛ حاول التحديث."
            return "فشل الكابتشا في مرحلة: ${diagnosis.stage.label}\n${diagnosis.detail}\nرمز التشخيص: CAPTCHA_${diagnosis.stage.name}\nلا يحتوي هذا التشخيص على رقمك أو بيانات الجلسة."
        }
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
