package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QueryUiState
import com.example.model.SubscriberAccount
import com.example.model.Yemen4GBalance
import com.example.ui.Yemen4GMainUiState
import com.example.ui.Yemen4GViewModel
import com.example.ui.components.CustomCaptchaView
import com.example.ui.theme.Yemen4GAccentAmber
import com.example.ui.theme.Yemen4GAccentGold
import com.example.ui.theme.Yemen4GElevatedNavy
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
    val context = LocalContext.current
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

        // If results are available, show the balance dashboard directly, else show the login/query form
        when (val queryState = state.queryState) {
            is QueryUiState.Success -> {
                BalanceDashboardCard(
                    balance = queryState.balance,
                    onReset = { viewModel.resetQuery() },
                    onCopy = { viewModel.copyToClipboard(context, queryState.balance) },
                    onShare = { viewModel.shareBalance(context, queryState.balance) }
                )
            }
            else -> {
                // Inquiry Form Card
                QueryFormCard(
                    state = state,
                    viewModel = viewModel
                )
            }
        }

        // Error message notification
        if (state.queryState is QueryUiState.Error) {
            ErrorAlertCard(
                message = state.queryState.message,
                onDismiss = { viewModel.dismissError() },
                onRetry = { viewModel.executeQuery() }
            )
        }

        // Saved accounts quick picker
        if (state.savedAccounts.isNotEmpty() && state.queryState !is QueryUiState.Success) {
            SavedAccountsSection(
                accounts = state.savedAccounts,
                currentNumber = state.modemNumber,
                onSelect = { viewModel.selectAccount(it) },
                onDelete = { viewModel.deleteAccount(it) }
            )
        }

        // Official Portal Info & Features Strip
        OfficialPortalInfoBanner()

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
                // Top Badge
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

                // App Title
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
                            text = "فحص الرصيد والباقات وموعد انتهاء الاشتراك",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF1E3A6E), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Mandatory Programmer & Designer Rights
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
            // Section Title
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
                        text = "تسجيل الدخول والاستعلام",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Demo button
                OutlinedButton(
                    onClick = { viewModel.loadQuickDemo() },
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("quick_demo_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Yemen4GPrimaryLight
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "تجربة فورية", fontSize = 12.sp)
                }
            }

            Text(
                text = "أدخل رقم هاتف شريحة المودم (يبدأ عادةً بـ 1 أو 10) مع كتابة رمز التحقق المرئي بدقة:",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            // Modem Number Input Field
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
                    imeAction = ImeAction.Next
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

            // Captcha Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A182E))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Yemen4GAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "رمز التحقق المرئي (كابتشا):",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "انقر على السهم للتحديث",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                // Captcha Canvas + Reload button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CustomCaptchaView(
                        captchaCode = state.activeCaptchaCode,
                        onRefresh = { viewModel.refreshCaptcha() },
                        isLoading = state.isRefreshingCaptcha
                    )
                }

                // Captcha Input Field
                OutlinedTextField(
                    value = state.captchaInput,
                    onValueChange = { viewModel.onCaptchaChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("captcha_input"),
                    label = { Text("أدخل الرمز الظاهر أعلاه") },
                    placeholder = { Text("اكتب الرمز المكون من 5 خانات") },
                    trailingIcon = {
                        val isMatched = state.captchaInput.equals(state.activeCaptchaCode, ignoreCase = true)
                        if (isMatched) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "كود صحيح",
                                tint = Yemen4GSuccessGreen
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.executeQuery() }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Yemen4GAccentGold,
                        unfocusedBorderColor = Color(0xFF1E3A5F),
                        focusedContainerColor = Color(0xFF071224),
                        unfocusedContainerColor = Color(0xFF071224),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Yemen4GAccentGold,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )
            }

            // Primary Action Button (استعلام)
            Button(
                onClick = { viewModel.executeQuery() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_query_button"),
                enabled = state.queryState !is QueryUiState.Loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yemen4GPrimaryBlue,
                    disabledContainerColor = Color(0xFF1E3A6E)
                )
            ) {
                if (state.queryState is QueryUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "جاري الاتصال بالنظام والاستعلام...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "استعلام عن الرصيد والباقة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceDashboardCard(
    balance: Yemen4GBalance,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_dashboard_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E223D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header with status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Yemen4GSuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تفاصيل الرصيد الرسمي",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Yemen4GSuccessGreen.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Yemen4GSuccessGreen, Yemen4GSuccessGreen)))
                ) {
                    Text(
                        text = balance.status,
                        color = Yemen4GSuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Circular Balance Gauge
            CircularBalanceGauge(
                remainingGb = balance.remainingGigabytes,
                totalGb = balance.totalGigabytes,
                remainingPercent = balance.remainingPercentage
            )

            // Expiry countdown pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF132A4A))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Yemen4GAccentAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "متبقي ${balance.daysRemaining} يوم حتى انتهاء الصلاحية (${balance.expirationDate})",
                    color = Color(0xFFFFE082),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Detailed Specs Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF071224))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(label = "رقم المودم / الشريحة", value = balance.accountNumber, isHighlight = true)
                HorizontalDivider(color = Color(0xFF162B47), thickness = 0.6.dp)
                DetailRow(label = "اسم الباقة المشترك بها", value = balance.packageName, isHighlight = false)
                HorizontalDivider(color = Color(0xFF162B47), thickness = 0.6.dp)
                DetailRow(label = "إجمالي سعة الباقة", value = "${balance.totalGigabytes} جيجابايت", isHighlight = false)
                HorizontalDivider(color = Color(0xFF162B47), thickness = 0.6.dp)
                DetailRow(label = "الرصيد المستهلك", value = "${balance.usedGigabytes} جيجابايت", isHighlight = false)
                HorizontalDivider(color = Color(0xFF162B47), thickness = 0.6.dp)
                DetailRow(label = "الرصيد المتبقي", value = "${balance.remainingGigabytes} جيجابايت", isHighlight = true, valueColor = Yemen4GSuccessGreen)
                HorizontalDivider(color = Color(0xFF162B47), thickness = 0.6.dp)
                DetailRow(label = "تاريخ ووقت الاستعلام", value = balance.queryTimestamp, isHighlight = false)
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("copy_balance_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Yemen4GPrimaryLight
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "نسخ", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("share_balance_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Yemen4GAccentGold
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مشاركة", fontSize = 13.sp)
                }
            }

            // Query Another Number Button
            Button(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("query_another_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF163259)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "استعلام عن مودم آخر",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CircularBalanceGauge(
    remainingGb: Double,
    totalGb: Double,
    remainingPercent: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = remainingPercent / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "balanceGauge"
    )

    Box(
        modifier = Modifier
            .size(190.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()

            // Background circle track
            drawArc(
                color = Color(0xFF132845),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic progress arc
            val sweep = 270f * animatedProgress
            val gaugeColor = when {
                remainingPercent > 50f -> Yemen4GSuccessGreen
                remainingPercent > 20f -> Yemen4GAccentAmber
                else -> Yemen4GErrorRed
            }

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Yemen4GPrimaryBlue,
                        gaugeColor
                    )
                ),
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner stats
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$remainingGb",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "جيجابايت متبقية",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Yemen4GPrimaryLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "من إجمالي $totalGb GB (${remainingPercent.toInt()}%)",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = if (isHighlight) valueColor else Color.White,
            fontSize = if (isHighlight) 14.sp else 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )
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
            text = "المودمات المحفوظة للاستعلام السريع:",
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
private fun ErrorAlertCard(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("error_alert_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B151E)
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Yemen4GErrorRed, Yemen4GErrorRed)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Yemen4GErrorRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تنبيه في الاستعلام",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    color = Color(0xFFFECDD3),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "إغلاق التنبيه",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun OfficialPortalInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF09172B))
            .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Yemen4GPrimaryLight,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "الموقع الرسمي للاستعلام: svc.ptc.gov.ye/4g/ التابع للمؤسسة العامة للاتصالات السلكية واللاسلكية بالجمهورية اليمنية.",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
