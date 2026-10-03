package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Yemen4GAccentGold
import com.example.ui.theme.Yemen4GPrimaryBlue
import com.example.ui.theme.Yemen4GPrimaryLight
import com.example.ui.theme.Yemen4GSuccessGreen

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Developer Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_profile_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E223D)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(Yemen4GAccentGold, Yemen4GPrimaryBlue))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Yemen4GAccentGold, Yemen4GPrimaryLight))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "المطور هاشم القديمي",
                            tint = Color(0xFF071326),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Text(
                        text = "هاشم القديمي",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Yemen4GAccentGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "حقوق البرمجة والتصميم والتطوير",
                            color = Yemen4GAccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "مطور ومصمم برمجيات وتطبيقات أندرويد متخصصة",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 0.8.dp)

                    // Contact action
                    OutlinedButton(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:hashem7droid23@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "تواصل بخصوص تطبيق يمن فورجي")
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "إرسال بريد إلكتروني"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Yemen4GPrimaryLight
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "hashem7droid23@gmail.com",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            // GitHub & APK Build Guide Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_build_guide_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1F38))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Yemen4GSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بناء ونشر ملف التطبيق (APK) عبر GitHub",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 0.6.dp)

                    Text(
                        text = "تم تضمين ملف GitHub Actions جاهز (.github/workflows/build-apk.yml) في المشروع لبناء ملف الـ APK تلقائياً!",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "خطوات الحصول على ملف APK:\n1. اضغط على أيقونة الإعدادات أو زر 'Export' في شريط AI Studio العلوي.\n2. اختر 'Push to GitHub' أو قم بتحميل ملف الـ ZIP للمشروع ورفعه على حسابك في GitHub.\n3. بمجرد رفع الكود، ستبدأ خدمة GitHub Actions تلقائياً ببناء التطبيق وتقديم ملف APK جاهز للتثبيت في قسم Actions (Artifacts)!\n4. كما يمكنك أيضاً توليد وتحميل الـ APK مباشرة من قائمة خيارات AI Studio.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            // App Specifications Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09172B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "معلومات الإصدار والنظام:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "اسم التطبيق:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(text = "يمن فورجي (Yemen 4G)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "رقم الإصدار:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(text = "1.0.0 (Build 1)", color = Color.White, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "واجهة المستخدم:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(text = "Jetpack Compose M3", color = Color.White, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "الموقع الرسمي المربوط به:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(text = "https://svc.ptc.gov.ye/4g/", color = Yemen4GPrimaryLight, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
