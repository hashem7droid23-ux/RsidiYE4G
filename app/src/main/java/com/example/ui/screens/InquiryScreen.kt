package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubscriberAccount
import com.example.model.Yemen4GScreen
import com.example.ui.ConnectionCheckState
import com.example.ui.Yemen4GMainUiState
import com.example.ui.Yemen4GViewModel
import com.example.ui.theme.Yemen4GAccentAmber
import com.example.ui.theme.Yemen4GAccentGold
import com.example.ui.theme.Yemen4GErrorRed
import com.example.ui.theme.Yemen4GPrimaryBlue
import com.example.ui.theme.Yemen4GPrimaryLight
import com.example.ui.theme.Yemen4GSuccessGreen

@Composable
fun InquiryScreen(
    state: Yemen4GMainUiState,
    viewModel: Yemen4GViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Developer Rights Card
        HeaderCard()

        // Inquiry & Connection Form Card
        QueryFormCard(
            state = state,
            viewModel = viewModel
        )

        // Connection State Banner (Real Server Status)
        when (val conn = state.connectionState) {
            is ConnectionCheckState.Checking -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2647))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Yemen4GPrimaryLight,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جاري فحص الاتصال بسيرفر يمن فورجي الرسمي (svc.ptc.gov.ye)...",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            is ConnectionCheckState.Connected -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3328)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Yemen4GSuccessGreen, Yemen4GSuccessGreen)))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Yemen4GSuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = conn.message,
                            color = Color(0xFFA7F3D0),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissConnectionState() }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }
                }
            }
            is ConnectionCheckState.Failed -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF38151E)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Yemen4GErrorRed, Yemen4GErrorRed)))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Yemen4GErrorRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = conn.error,
                            color = Color(0xFFFECDD3),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissConnectionState() }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }
                }
            }
            ConnectionCheckState.Idle -> { }
        }

        // Saved accounts quick picker
        if (state.savedAccounts.isNotEmpty()) {
            SavedAccountsSection(
                accounts = state.savedAccounts,
                currentNumber = state.modemNumber,
                onSelect = { viewModel.selectAccount(it) },
                onDelete = { viewModel.deleteAccount(it) }
            )
        }

        // Official Portal Info & Authenticity Guarantee Banner
        OfficialPortalInfoBanner(
            onOpenPortal = { viewModel.setScreen(Yemen4GScreen.OFFICIAL_PORTAL) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C213D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF13325B),
                            Color(0xFF09192E)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF0A182E))
                        .border(1.dp, Color(0xFF1E3A6E), RoundedCornerShape(30.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Yemen4GPrimaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "بوابة استعلام يمن فورجي الرسمية (PTC)",
                        color = Color(0xFFBFDBFE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Yemen4GPrimaryBlue, Yemen4GAccentGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = "مودم يمن فورجي",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "يمن فورجي - استعلام الرصيد",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الاستعلام المباشر عبر بوابة المؤسسة العامة للاتصالات",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF1E3A6E), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E3A63).copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Yemen4GAccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "برمجة وتصميم: هاشم القديمي",
                        color = Yemen4GAccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QueryFormCard(
    state: Yemen4GMainUiState,
    viewModel: Yemen4GViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("query_form_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E223D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Yemen4GSuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إدخال رقم المودم والاستعلام المباشر",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.checkServerOnly() },
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Yemen4GPrimaryLight)
                ) {
                    Text(text = "فحص السيرفر", fontSize = 11.sp)
                }
            }

            Text(
                text = "أدخل رقم هاتف شريحة يمن فورجي (يبدأ عادةً بـ 1 أو 10). يتم فتح بوابة المؤسسة العامة للاتصالات الرسمية مع التعبئة التلقائية للرقم، لإدخال كابتشا السيرفر الرسمية وعرض رصيدك الفعلي الموثوق:",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            OutlinedTextField(
                value = state.modemNumber,
                onValueChange = { viewModel.onModemNumberChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("modem_number_input"),
                label = { Text("رقم مودم يمن فورجي (9 - 10 أرقام)") },
                placeholder = { Text("مثال: 100234567") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = Yemen4GPrimaryLight
                    )
                },
                trailingIcon = {
                    if (state.modemNumber.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onModemNumberChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح الرقم",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.verifyAndOpenOfficialPortal() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yemen4GPrimaryLight,
                    unfocusedBorderColor = Color(0xFF1E3A5F),
                    focusedContainerColor = Color(0xFF071224),
                    unfocusedContainerColor = Color(0xFF071224),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Yemen4GPrimaryLight,
                    unfocusedLabelColor = Color(0xFF94A3B8)
                )
            )

            // Primary Action Button
            Button(
                onClick = { viewModel.verifyAndOpenOfficialPortal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_query_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yemen4GPrimaryBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "فتح البوابة الرسمية والاستعلام الفوري",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SavedAccountsSection(
    accounts: List<SubscriberAccount>,
    currentNumber: String,
    onSelect: (SubscriberAccount) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "الأرقام المحفوظة:",
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(accounts) { account ->
                val isSelected = account.number == currentNumber
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Yemen4GPrimaryBlue.copy(alpha = 0.25f) else Color(0xFF0E223D))
                        .border(
                            1.dp,
                            if (isSelected) Yemen4GPrimaryLight else Color(0xFF1E3A5F),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(account) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = if (isSelected) Yemen4GPrimaryLight else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = account.label,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = account.number,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficialPortalInfoBanner(onOpenPortal: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPortal() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF09172B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF1E3A5F), Color(0xFF1E3A5F))))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Yemen4GPrimaryLight,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ضمان الشفافية ومصداقية الرصيد:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "الاستعلام يتم حصراً ومباشرة من سيرفر المؤسسة العامة للاتصالات اليمنية (svc.ptc.gov.ye/4g) لمنع أي تلاعب أو بيانات غير دقيقة.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
