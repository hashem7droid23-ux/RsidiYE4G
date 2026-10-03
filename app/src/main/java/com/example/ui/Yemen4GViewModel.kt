package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Yemen4GRepository
import com.example.model.ModemGatewayInfo
import com.example.model.QueryUiState
import com.example.model.SubscriberAccount
import com.example.model.Yemen4GBalance
import com.example.model.Yemen4GPackage
import com.example.model.Yemen4GScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Yemen4GMainUiState(
    val currentScreen: Yemen4GScreen = Yemen4GScreen.INQUIRY,
    val modemNumber: String = "",
    val captchaInput: String = "",
    val activeCaptchaCode: String = "",
    val isRefreshingCaptcha: Boolean = false,
    val queryState: QueryUiState = QueryUiState.Idle,
    val savedAccounts: List<SubscriberAccount> = emptyList(),
    val packages: List<Yemen4GPackage> = emptyList(),
    val routers: List<ModemGatewayInfo> = emptyList(),
    val selectedPackageForCalc: Yemen4GPackage? = null,
    val showSaveAccountDialog: Boolean = false
)

class Yemen4GViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Yemen4GRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        Yemen4GMainUiState(
            activeCaptchaCode = repository.getCurrentCaptcha(),
            savedAccounts = repository.getSavedAccounts(),
            packages = repository.getOfficialPackages(),
            routers = repository.getModemRouters(),
            selectedPackageForCalc = repository.getOfficialPackages().firstOrNull { it.isPopular }
        )
    )
    val uiState: StateFlow<Yemen4GMainUiState> = _uiState.asStateFlow()

    fun setScreen(screen: Yemen4GScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun onModemNumberChange(number: String) {
        val filtered = number.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(modemNumber = filtered) }
    }

    fun onCaptchaChange(captcha: String) {
        val filtered = captcha.uppercase().filter { it.isLetterOrDigit() }.take(5)
        _uiState.update { it.copy(captchaInput = filtered) }
    }

    fun refreshCaptcha() {
        val newCode = repository.regenerateCaptcha()
        _uiState.update {
            it.copy(
                activeCaptchaCode = newCode,
                captchaInput = ""
            )
        }
    }

    fun selectAccount(account: SubscriberAccount) {
        _uiState.update {
            it.copy(
                modemNumber = account.number,
                captchaInput = ""
            )
        }
    }

    fun loadQuickDemo() {
        val demoNumbers = listOf("100889922", "101445566", "102773311")
        val chosen = demoNumbers.random()
        _uiState.update {
            it.copy(
                modemNumber = chosen,
                captchaInput = it.activeCaptchaCode
            )
        }
        executeQuery()
    }

    fun executeQuery() {
        val number = _uiState.value.modemNumber.trim()
        val captcha = _uiState.value.captchaInput.trim()

        if (number.isEmpty()) {
            _uiState.update {
                it.copy(
                    queryState = QueryUiState.Error("يرجى إدخال رقم هاتف أو مودم يمن فورجي (يبدأ بـ 1)")
                )
            }
            return
        }

        if (captcha.isEmpty()) {
            _uiState.update {
                it.copy(
                    queryState = QueryUiState.Error("يرجى كتابة رمز التحقق (الكابتشا) الظاهر في الصورة")
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(queryState = QueryUiState.Loading) }

            val result = repository.queryBalance(number, captcha)
            result.fold(
                onSuccess = { balance ->
                    _uiState.update {
                        it.copy(
                            queryState = QueryUiState.Success(balance),
                            savedAccounts = repository.getSavedAccounts(),
                            activeCaptchaCode = repository.getCurrentCaptcha(),
                            captchaInput = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            queryState = QueryUiState.Error(error.localizedMessage ?: "فشل الاستعلام"),
                            activeCaptchaCode = repository.getCurrentCaptcha(),
                            captchaInput = ""
                        )
                    }
                }
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(queryState = QueryUiState.Idle) }
    }

    fun resetQuery() {
        _uiState.update {
            it.copy(
                queryState = QueryUiState.Idle,
                captchaInput = "",
                activeCaptchaCode = repository.regenerateCaptcha()
            )
        }
    }

    fun selectPackageForCalc(pkg: Yemen4GPackage) {
        _uiState.update { it.copy(selectedPackageForCalc = pkg) }
    }

    fun saveAccountWithLabel(number: String, label: String) {
        if (number.isNotBlank()) {
            repository.saveAccount(SubscriberAccount(number, label.ifBlank { "مودم $number" }))
            _uiState.update { it.copy(savedAccounts = repository.getSavedAccounts()) }
        }
    }

    fun deleteAccount(number: String) {
        repository.removeAccount(number)
        _uiState.update { it.copy(savedAccounts = repository.getSavedAccounts()) }
    }

    fun copyToClipboard(context: Context, balance: Yemen4GBalance) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = buildString {
            appendLine("📶 تقرير رصيد يمن فورجي الرسمي 📶")
            appendLine("رقم المودم: ${balance.accountNumber}")
            appendLine("الباقة الحالية: ${balance.packageName}")
            appendLine("الرصيد المتبقي: ${balance.remainingGigabytes} جيجابايت")
            appendLine("الرصيد المستهلك: ${balance.usedGigabytes} جيجابايت من أصل ${balance.totalGigabytes} جيجابايت")
            appendLine("تاريخ الانتهاء: ${balance.expirationDate} (متبقي ${balance.daysRemaining} يوم)")
            appendLine("الحالة: ${balance.status}")
            appendLine("تاريخ الاستعلام: ${balance.queryTimestamp}")
            appendLine("---")
            appendLine("تم الاستعلام عبر تطبيق يمن فورجي")
            appendLine("برمجة وتصميم: هاشم القديمي")
        }
        val clip = ClipData.newPlainText("Yemen4G Balance", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ تفاصيل الرصيد بنجاح", Toast.LENGTH_SHORT).show()
    }

    fun shareBalance(context: Context, balance: Yemen4GBalance) {
        val text = buildString {
            appendLine("📶 استعلام رصيد يمن فورجي 📶")
            appendLine("رقم المشترك: ${balance.accountNumber}")
            appendLine("الباقة: ${balance.packageName}")
            appendLine("الرصيد المتبقي: ${balance.remainingGigabytes} GB / ${balance.totalGigabytes} GB")
            appendLine("تاريخ الانتهاء: ${balance.expirationDate} (باقي ${balance.daysRemaining} يوم)")
            appendLine("---")
            appendLine("برمجة وتصميم: هاشم القديمي")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "رصيد يمن فورجي - ${balance.accountNumber}")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الرصيد عبر").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
