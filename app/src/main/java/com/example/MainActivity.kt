package com.example

// رصيدي 4G. البرمجة والتصميم: هاشم القديمي.
// Native single-purpose balance UI. Subscriber and session data live only in RAM.
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OfficialBalanceClient
import com.example.data.OfficialBalanceResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val Green = Color(0xFF0C7156)
private val Ink = Color(0xFF16382F)
private val Muted = Color(0xFF567067)
private val Paper = Color(0xFFF4F9F6)
private val Mist = Color(0xFFE3EEE7)

private class InquirySession {
    val client = OfficialBalanceClient()
    var number by mutableStateOf("")
    var code by mutableStateOf("")
    var captcha by mutableStateOf<Bitmap?>(null)
    var result by mutableStateOf<OfficialBalanceResult?>(null)
    var error by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
    var generation by mutableStateOf(0)
    var active = false
    var job: Job? = null
    fun clear() {
        generation++
        job?.cancel()
        job = null
        client.clear()
        number = ""
        code = ""
        captcha = null
        result = null
        error = null
        busy = false
    }
}

class MainActivity : ComponentActivity() {
    private val session = InquirySession()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Green, onPrimary = Color.White,
                background = Paper, onBackground = Ink,
                surface = Color.White, onSurface = Ink,
                onSurfaceVariant = Muted, secondaryContainer = Mist,
                onSecondaryContainer = Green, outline = Color(0xFFBFD4C8)
            )) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    BalanceInquiryApp(session)
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        session.active = true
        session.generation++
    }
    override fun onStop() {
        session.active = false
        session.clear()
        super.onStop()
    }
}

@Composable
private fun BalanceInquiryApp(session: InquirySession) {
    var showInfo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun loadCaptcha() {
        if (session.busy || !session.active) return
        val generation = session.generation
        session.job = scope.launch {
            session.busy = true
            session.error = null
            session.code = ""
            session.captcha = null
            try {
                val image = session.client.loadCaptcha()
                if (generation == session.generation) session.captcha = image
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (generation == session.generation) {
                    session.error = "تعذّر تحميل الكابتشا بأمان من الموقع الرسمي. تحقّق من الاتصال ثم أعد المحاولة."
                }
            } finally {
                if (generation == session.generation) session.busy = false
            }
        }
    }
    LaunchedEffect(session.generation) { loadCaptcha() }
    fun query() {
        if (session.busy || session.captcha == null) return
        if (!session.number.matches(Regex("[0-9]{8,20}")) || session.code.isBlank()) {
            session.error = "أدخل رقم الاشتراك ورمز التحقق الظاهر في الصورة."
            return
        }
        val generation = session.generation
        session.job = scope.launch {
            session.busy = true
            session.error = null
            session.result = null
            try {
                val result = session.client.query(session.number, session.code.trim())
                if (generation == session.generation) {
                    session.result = result
                    session.number = ""
                    session.client.clear()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (generation == session.generation) {
                    session.error = "لم يكتمل الاستعلام. تحقّق من الرقم ورمز التحقق والاتصال، ثم حدّث الكابتشا. إذا استمر الخطأ فقد تكون استجابة الموقع تغيّرت."
                    session.client.clear()
                }
            } finally {
                if (generation == session.generation) {
                    session.captcha = null
                    session.code = ""
                    session.busy = false
                }
            }
        }
    }
    Scaffold(containerColor = Paper) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 520.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("رصيدي 4G", Modifier.weight(1f), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showInfo = true }) { Icon(Icons.Default.Info, "عن التطبيق والخصوصية", tint = Green) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("رصيد مودمك،\nبوضوح.", fontSize = 36.sp, lineHeight = 49.sp, color = Ink, fontWeight = FontWeight.Bold)
                    Text("استعلام رصيد يمن فورجي", fontSize = 17.sp, color = Muted)
                }
                Surface(color = Green, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (session.result == null) "الاستعلام فقط" else "رصيد البيانات المتاح",
                            color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        session.result?.dataRemaining?.let {
                            Text(it, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(if (session.result == null) "الكابتشا من الموقع الرسمي، والنتيجة داخل التطبيق" else "نتيجة آخر استعلام في هذه الجلسة",
                            color = Color(0xFFD5E9DF), fontSize = 14.sp, lineHeight = 23.sp)
                    }
                }
                OutlinedTextField(
                    value = session.number,
                    onValueChange = { value ->
                        session.number = value.mapNotNull { char ->
                            Character.digit(char, 10).takeIf { it >= 0 }?.toString()
                        }.joinToString("").take(20)
                    },
                    modifier = Modifier.fillMaxWidth(), enabled = !session.busy,
                    label = { Text("رقم الاشتراك") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("رمز التحقق المرئي", Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        TextButton(onClick = { loadCaptcha() }, enabled = !session.busy) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(6.dp))
                            Text("تحديث")
                        }
                    }
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Box(Modifier.fillMaxWidth().heightIn(min = 100.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                            val bitmap = session.captcha
                            when {
                                bitmap != null -> Image(bitmap.asImageBitmap(), "الكابتشا الرسمية، أدخل الحروف الظاهرة",
                                    Modifier.fillMaxWidth().height(90.dp))
                                session.busy -> CircularProgressIndicator()
                                else -> Text("اضغط تحديث لتحميل الكابتشا الرسمية", fontSize = 14.sp, color = Muted, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = session.code, onValueChange = { session.code = it.take(20) },
                        modifier = Modifier.fillMaxWidth(), enabled = !session.busy && session.captcha != null,
                        label = { Text("الرمز الظاهر في الصورة") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(autoCorrect = false),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Button(onClick = { query() }, enabled = !session.busy && session.captcha != null,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (session.busy) "جارٍ الاتصال…" else "استعلام عن الرصيد", fontSize = 16.sp)
                    }
                }
                session.error?.let {
                    Surface(color = Color(0xFFFFEAE5), shape = RoundedCornerShape(14.dp)) {
                        Text(it, Modifier.padding(16.dp), color = Color(0xFF78352A), fontSize = 15.sp, lineHeight = 24.sp)
                    }
                }
                session.result?.let { result ->
                    Text("تفاصيل الرصيد", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Surface(color = Color.White, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            result.rows.forEach { row ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(row.label, fontSize = 14.sp, color = Muted)
                                    Text(row.value, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
                                }
                            }
                        }
                    }
                }
                Text("رقمك لا يُحفظ في التطبيق. يُرسل مباشرة للموقع الرسمي عند الاستعلام فقط، وتُمسح بيانات الجلسة عند مغادرة التطبيق.",
                    fontSize = 14.sp, lineHeight = 23.sp, color = Muted)
                Text("تطبيق مستقل وغير رسمي\nالبرمجة والتصميم: هاشم القديمي\n© 2026 · جميع الحقوق محفوظة",
                    Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 24.sp, color = Muted)
            }
        }
    }
    if (showInfo) AlertDialog(
        onDismissRequest = { showInfo = false },
        title = { Text("عن رصيدي 4G") },
        text = { Text("تطبيق مستقل وغير تابع للمؤسسة العامة للاتصالات.\n\nالكابتشا من الخدمة الرسمية ويحلّها المستخدم يدويًا. رقم الاشتراك يُرسل مباشرة عبر HTTPS عند الاستعلام، ولا يُحفظ في التطبيق أو سجلاته.\n\nجلسة الكابتشا والنتائج في الذاكرة فقط وتُمسح عند مغادرة التطبيق. الأرقام المحفوظة من الإصدارات القديمة لم تُحذف؛ هذا المسار لا يقرأها أو يعدّلها.\n\nالربط مبني على صفحتي الموقع المرفقتين؛ اكتمال الاختبار الحي يعتمد على وصول جهازك للموقع وثبات النموذج.\n\nالبرمجة والتصميم: هاشم القديمي") },
        confirmButton = { TextButton(onClick = { showInfo = false }) { Text("حسنًا") } }
    )
}
