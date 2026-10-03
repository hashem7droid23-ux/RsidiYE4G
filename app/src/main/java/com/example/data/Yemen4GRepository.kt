package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ModemGatewayInfo
import com.example.model.SubscriberAccount
import com.example.model.Yemen4GBalance
import com.example.model.Yemen4GPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class Yemen4GRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("yemen4g_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Checks real live connectivity to the official Yemen Telecom portal (https://svc.ptc.gov.ye/4g/)
     * without fabricating fake balances.
     */
    suspend fun verifyOfficialPortalConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://svc.ptc.gov.ye/4g/")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                .build()
            val response = httpClient.newCall(request).execute()
            val isSuccess = response.isSuccessful || response.code in 200..399
            response.close()
            Result.success(isSuccess)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAccountAndPrepareOfficialQuery(modemNumber: String): Result<SubscriberAccount> =
        withContext(Dispatchers.IO) {
            val cleanNumber = modemNumber.trim().replace(" ", "").replace("-", "")

            if (cleanNumber.length < 8 || cleanNumber.length > 11) {
                return@withContext Result.failure(
                    IllegalArgumentException("رقم المودم غير صحيح، يرجى إدخال رقم يمن فورجي مكون من 9 إلى 10 أرقام (يبدأ بـ 1)")
                )
            }

            val account = SubscriberAccount(
                number = cleanNumber,
                label = "مودم $cleanNumber",
                lastChecked = System.currentTimeMillis()
            )
            saveAccount(account)
            Result.success(account)
        }

    fun getSavedAccounts(): List<SubscriberAccount> {
        val jsonStr = prefs.getString("saved_accounts", null) ?: return defaultAccounts()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<SubscriberAccount>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SubscriberAccount(
                        number = obj.getString("number"),
                        label = obj.getString("label"),
                        lastChecked = obj.optLong("lastChecked", System.currentTimeMillis())
                    )
                )
            }
            if (list.isEmpty()) defaultAccounts() else list
        } catch (e: Exception) {
            defaultAccounts()
        }
    }

    private fun defaultAccounts(): List<SubscriberAccount> = listOf(
        SubscriberAccount("100234567", "مودم تجريبي")
    )

    fun saveAccount(account: SubscriberAccount) {
        val current = getSavedAccounts().toMutableList()
        current.removeAll { it.number == account.number }
        current.add(0, account)
        val limited = current.take(10)

        val jsonArray = JSONArray()
        for (acc in limited) {
            val obj = JSONObject()
            obj.put("number", acc.number)
            obj.put("label", acc.label)
            obj.put("lastChecked", acc.lastChecked)
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_accounts", jsonArray.toString()).apply()
    }

    fun removeAccount(number: String) {
        val current = getSavedAccounts().filter { it.number != number }
        val jsonArray = JSONArray()
        for (acc in current) {
            val obj = JSONObject()
            obj.put("number", acc.number)
            obj.put("label", acc.label)
            obj.put("lastChecked", acc.lastChecked)
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_accounts", jsonArray.toString()).apply()
    }

    fun getOfficialPackages(): List<Yemen4GPackage> = listOf(
        Yemen4GPackage(
            id = "pkg_80",
            name = "باقة سوبر فورجي 80 جيجا",
            gigabytes = 80.0,
            priceYer = 9600,
            validityDays = 30,
            speedLimit = "سرعة تصل حتى 100 ميجابت/ث",
            isPopular = true,
            features = listOf("الأكثر طلباً واستخداماً", "مناسبة للعائلات والتصفح ومشاهدة الفيديو", "تراكم الرصيد عند التجديد قبل الانتهاء")
        ),
        Yemen4GPackage(
            id = "pkg_150",
            name = "باقة ماكس فورجي 150 جيجا",
            gigabytes = 150.0,
            priceYer = 16000,
            validityDays = 30,
            speedLimit = "سرعة تصل حتى 100 ميجابت/ث",
            isPopular = false,
            features = listOf("سعة كبيرة جداً للعمل المكتبي والدراسة", "تحميل غير محدود للسرعة العالية", "دعم ألعاب الأونلاين والبث المباشر")
        ),
        Yemen4GPackage(
            id = "pkg_35",
            name = "باقة التوفير 35 جيجا",
            gigabytes = 35.0,
            priceYer = 4800,
            validityDays = 30,
            speedLimit = "سرعة تصل حتى 60 ميجابت/ث",
            isPopular = false,
            features = listOf("باقة اقتصادية مناسبة للأفراد", "تصفح شبكات التواصل وواتساب", "سعر رمزي ومناسب")
        ),
        Yemen4GPackage(
            id = "pkg_250",
            name = "باقة الأعمال 250 جيجا",
            gigabytes = 250.0,
            priceYer = 26000,
            validityDays = 30,
            speedLimit = "سرعة قصوى بدون قيود",
            isPopular = false,
            features = listOf("مخصصة للمكاتب والشركات والمقاهي", "أعلى استقرار للشبكة مع IP ثابت اختياري", "أولوية في سرعة الاتصال")
        ),
        Yemen4GPackage(
            id = "pkg_15",
            name = "باقة البداية 15 جيجا",
            gigabytes = 15.0,
            priceYer = 2400,
            validityDays = 30,
            speedLimit = "سرعة تصل حتى 40 ميجابت/ث",
            isPopular = false,
            features = listOf("باقة خفيفة للطوارئ", "تصفح البريد وتطبيقات المراسلة", "صلاحية كاملة لمدة شهر")
        ),
        Yemen4GPackage(
            id = "pkg_400",
            name = "باقة ألترا فورجي 400 جيجا",
            gigabytes = 400.0,
            priceYer = 40000,
            validityDays = 30,
            speedLimit = "سرعة قصوى بدون قيود",
            isPopular = false,
            features = listOf("أضخم باقة متوفرة على شبكة يمن فورجي", "سيرفرات مخصصة وكاميرات مراقبة", "دعم فني متميز من الاتصالات")
        )
    )

    fun getModemRouters(): List<ModemGatewayInfo> = listOf(
        ModemGatewayInfo(
            brand = "ZTE (زد تي إي)",
            modelName = "ZTE MF286 / MF283 4G CPE",
            gatewayIp = "http://192.168.0.1",
            defaultUsername = "admin",
            defaultPassword = "admin (أو الملصق أسفل المودم)",
            instructions = "قم بالاتصال بشبكة واي فاي المودم أولاً، ثم افتح الرابط لإدارة وتغيير كلمة السر وقوة الإشارة."
        ),
        ModemGatewayInfo(
            brand = "Huawei (هواوي)",
            modelName = "Huawei B310 / B315 / B535 LTE",
            gatewayIp = "http://192.168.8.1",
            defaultUsername = "admin",
            defaultPassword = "admin (أو كلمة سر الواي فاي)",
            instructions = "يتيح لك فحص ترددات البرج (Band 3 / Band 20 / Band 28) واختيار أفضل إشارة."
        ),
        ModemGatewayInfo(
            brand = "Tozed Kangwei (توزيد)",
            modelName = "Tozed ZLT P21 / X21 4G Router",
            gatewayIp = "http://192.168.1.1",
            defaultUsername = "admin",
            defaultPassword = "admin",
            instructions = "المودم الأكثر انتشاراً من قبل المؤسسة العامة للاتصالات يمن فورجي."
        ),
        ModemGatewayInfo(
            brand = "FiberHome / Urbetter",
            modelName = "FiberHome 4G Wireless Gateway",
            gatewayIp = "http://192.168.100.1",
            defaultUsername = "admin",
            defaultPassword = "admin",
            instructions = "شائع الاستخدام لمودمات يمن فورجي الحديثة ذات الهوائيات الخارجية."
        )
    )
}
