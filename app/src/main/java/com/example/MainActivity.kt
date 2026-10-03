package com.example

// رصيدي 4G: واجهة استعلام عربية مستقلة. البرمجة والتصميم: هاشم القديمي.
// Design: router signal LEDs, paper #F4F9F6, green #0C7156, ink #16382F,
// muted #567067, copper #EA7428. One screen, no portal navigation or fake balances.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF0C7156)
private val Ink = Color(0xFF16382F)
private val Muted = Color(0xFF567067)
private val Paper = Color(0xFFF4F9F6)
private val Mist = Color(0xFFE3EEE7)
private val Copper = Color(0xFFEA7428)

class MainActivity : ComponentActivity() {
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
                    BalanceInquiryApp()
                }
            }
        }
    }
}

@Composable
private fun BalanceInquiryApp() {
    var showInfo by remember { mutableStateOf(false) }
    Scaffold(containerColor = Paper) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("رصيدي 4G", Modifier.weight(1f), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = "عن التطبيق والخصوصية", tint = Green)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("رصيد مودمك،\nبوضوح.", fontSize = 36.sp, lineHeight = 49.sp, color = Ink, fontWeight = FontWeight.Bold)
                    Text("استعلام رصيد يمن فورجي", fontSize = 17.sp, color = Muted)
                }
                Surface(color = Green, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("YEMEN · 4G", color = Color(0xFFD5E9DF), fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("الاستعلام فقط", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("بدون انتقال لصفحة المؤسسة", color = Color(0xFFD5E9DF), fontSize = 14.sp)
                            }
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Bottom) {
                                    listOf(20, 32, 44, 56).forEachIndexed { index, height ->
                                        Box(Modifier.width(10.dp).height(height.dp).background(
                                            if (index == 3) Copper else Color(0xFFC9E8D9), RoundedCornerShape(5.dp)))
                                    }
                                }
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("بيانات الاستعلام", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("رقم الاشتراك", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
                    Surface(color = Mist, shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("الإدخال ينتظر تفعيل الربط", Modifier.weight(1f), fontSize = 16.sp, color = Muted)
                            Icon(Icons.Default.Lock, "رقم الاشتراك غير مفعّل", tint = Muted, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text("رمز التحقق المرئي", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Refresh, null, tint = Green, modifier = Modifier.size(28.dp))
                            Text("الكابتشا الرسمية غير متاحة بعد", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center, color = Ink)
                            Text("يلزم التحقق من جلسة الموقع وآلية الاستعلام قبل تفعيلها.",
                                fontSize = 14.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, color = Muted)
                        }
                    }
                    Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("الاستعلام غير مفعّل بعد", fontSize = 16.sp)
                    }
                }
                Surface(color = Mist, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("نتيجة الرصيد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("لا توجد نتيجة استعلام", fontSize = 16.sp, color = Muted)
                        Text("عرض الرصيد والباقة وتاريخ الانتهاء داخل التطبيق يتطلب ربطًا موثّقًا بالخدمة. لا تعرض هذه النسخة بيانات تجريبية على أنها حقيقية.",
                            fontSize = 14.sp, lineHeight = 23.sp, color = Muted)
                    }
                }
                Text("تطبيق مستقل وغير رسمي. هذه النسخة تحديث تصميم فقط؛ الاستعلام الفعلي والكابتشا غير مفعّلين.",
                    fontSize = 14.sp, lineHeight = 23.sp, color = Muted)
                Text("البرمجة والتصميم: هاشم القديمي\n© 2026 · جميع الحقوق محفوظة",
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp,
                    lineHeight = 24.sp, color = Muted)
            }
        }
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("عن رصيدي 4G") },
            text = { Text("تطبيق مستقل وغير تابع للمؤسسة العامة للاتصالات.\n\nهذا الإصدار واجهة استعلام فقط. لا يجمع أو يحفظ بيانات جديدة، ولا يفتح صفحة المؤسسة تلقائيًا ولا يطلب الرصيد عبر الشبكة.\n\nالأرقام المحفوظة من الإصدارات السابقة لم تُحذف؛ هذا الإصدار لا يقرأها أو يعرضها.\n\nالكابتشا والرصيد الحقيقي ينتظران التحقق من الربط.\n\nالبرمجة والتصميم: هاشم القديمي") },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("حسنًا") } }
        )
    }
}
