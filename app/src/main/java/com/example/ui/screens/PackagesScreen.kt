package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Yemen4GPackage
import com.example.ui.theme.Yemen4GAccentAmber
import com.example.ui.theme.Yemen4GAccentGold
import com.example.ui.theme.Yemen4GPrimaryBlue
import com.example.ui.theme.Yemen4GPrimaryLight
import com.example.ui.theme.Yemen4GSuccessGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PackagesScreen(
    packages: List<Yemen4GPackage>,
    modifier: Modifier = Modifier
) {
    var selectedPackage by remember { mutableStateOf(packages.firstOrNull { it.isPopular } ?: packages.firstOrNull()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Packages Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C203B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "دليل باقات يمن فورجي الرسمية",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "أسعار الباقات الرسمية شاملة ضريبة القيمة المضافة ومصاريف السداد",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Live Calculator Card for Selected Package
        if (selectedPackage != null) {
            item {
                selectedPackage?.let { pkg ->
                    TaxCalculatorCard(pkg = pkg)
                }
            }
        }

        item {
            Text(
                text = "اختر باقة لمعرفة التفاصيل وحساب الإجمالي:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(packages) { pkg ->
            val isSelected = selectedPackage?.id == pkg.id
            PackageItemCard(
                pkg = pkg,
                isSelected = isSelected,
                onClick = { selectedPackage = pkg }
            )
        }

        item {
            // Recharge info & methods
            RechargeMethodsInfoCard()
        }
    }
}

@Composable
private fun TaxCalculatorCard(pkg: Yemen4GPackage) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("package_calculator_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E223D)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Yemen4GPrimaryLight, Yemen4GAccentGold)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Yemen4GAccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حاسبة التجديد: ${pkg.name}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Yemen4GPrimaryBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${pkg.gigabytes} GB",
                        color = Yemen4GPrimaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 0.6.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "سعر الباقة الأساسي:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text(text = "${formatter.format(pkg.priceYer)} ريال يمني", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "ضريبة الاتصالات والمبيعات (14%):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text(text = "+ ${formatter.format(pkg.taxYer)} ريال", color = Yemen4GAccentAmber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 0.6.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "المبلغ المطلوب في المحفظة/الحساب:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${formatter.format(pkg.totalWithTax)} ر.ي",
                    color = Yemen4GSuccessGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PackageItemCard(
    pkg: Yemen4GPackage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("package_card_${pkg.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF102647) else Color(0xFF0C1B30)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isSelected) Brush.linearGradient(listOf(Yemen4GPrimaryLight, Yemen4GPrimaryBlue))
            else Brush.linearGradient(listOf(Color(0xFF1E3A5F), Color(0xFF1E3A5F)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pkg.isPopular) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Yemen4GAccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = pkg.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${formatter.format(pkg.priceYer)} ريال",
                    color = Yemen4GAccentGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = pkg.speedLimit,
                color = Yemen4GPrimaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            // Features bullets
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                pkg.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Yemen4GSuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = feature,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RechargeMethodsInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A172B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = Yemen4GAccentAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "طرق سداد وتجديد رصيد يمن فورجي:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "• عبر تطبيق ومحافظ البنوك (الكريمي تميز، ون كاش، محفظة جوالي، كاش، فلوسك، مري، سبأ كاش).\n• عبر نقاط خدمات الاتصالات ومحلات الصرافة بإعطائهم رقم شريحة يمن فورجي.\n• عبر موقع بوابة تسديد الاتصالات أو كروت يمن نت / يمن فورجي.",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 18.sp
            )
        }
    }
}
