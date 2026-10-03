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
import com.example.model.SubscriberAccount
import com.example.model.Yemen4GPackage
import com.example.model.Yemen4GScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ConnectionCheckState {
    data object Idle : ConnectionCheckState
    data object Checking : ConnectionCheckState
    data class Connected(val message: String, val checkedAt: Long = System.currentTimeMillis()) : ConnectionCheckState
    data class Failed(val error: String, val checkedAt: Long = System.currentTimeMillis()) : ConnectionCheckState
}

data class Yemen4GMainUiState(
    val currentScreen: Yemen4GScreen = Yemen4GScreen.INQUIRY,
    val modemNumber: String = "",
    val connectionState: ConnectionCheckState = ConnectionCheckState.Idle,
    val savedAccounts: List<SubscriberAccount> = emptyList(),
    val packages: List<Yemen4GPackage> = emptyList(),
    val routers: List<ModemGatewayInfo> = emptyList(),
    val selectedPackageForCalc: Yemen4GPackage? = null
)

class Yemen4GViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Yemen4GRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        Yemen4GMainUiState(
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

    fun selectAccount(account: SubscriberAccount) {
        _uiState.update { it.copy(modemNumber = account.number) }
    }

    fun verifyAndOpenOfficialPortal() {
        val number = _uiState.value.modemNumber.trim()
        if (number.length < 8) {
            _uiState.update {
                it.copy(
                    connectionState = ConnectionCheckState.Failed(
                        "يرجى إدخال رقم مودم يمن فورجي صحيح (9 إلى 10 أرقام) قبل الانتقال للبوابة"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(connectionState = ConnectionCheckState.Checking) }

            // Save account
            repository.saveAccount(SubscriberAccount(number, "مودم $number"))
            _uiState.update { it.copy(savedAccounts = repository.getSavedAccounts()) }

            val connResult = repository.verifyOfficialPortalConnection()
            connResult.fold(
                onSuccess = { isOnline ->
                    if (isOnline) {
                        _uiState.update {
                            it.copy(
                                connectionState = ConnectionCheckState.Connected(
                                    "خادم المؤسسة العامة للاتصالات (svc.ptc.gov.ye) متصل وجاهز للاستعلام الرسمي المباشر."
                                ),
                                currentScreen = Yemen4GScreen.OFFICIAL_PORTAL
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                connectionState = ConnectionCheckState.Failed(
                                    "خادم البوابة الرسمية لم يستجب برمز نجاح، يمكنك تجربة فتح البوابة مباشرة عبر المتصفح المدمج."
                                ),
                                currentScreen = Yemen4GScreen.OFFICIAL_PORTAL
                            )
                        }
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionCheckState.Failed(
                                "تعذر الوصول المباشر لخوادم الاتصالات (قد يكون بسبب الحظر الجغرافي لخوادم اليمن أو بطء الشبكة). سيتم فتح المتصفح المدمج الآن لتجاوز أي قيود شبكة."
                            ),
                            currentScreen = Yemen4GScreen.OFFICIAL_PORTAL
                        )
                    }
                }
            )
        }
    }

    fun checkServerOnly() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionState = ConnectionCheckState.Checking) }
            val connResult = repository.verifyOfficialPortalConnection()
            connResult.fold(
                onSuccess = { isOnline ->
                    _uiState.update {
                        it.copy(
                            connectionState = if (isOnline) {
                                ConnectionCheckState.Connected("سيرفر يمن فورجي الرسمي (svc.ptc.gov.ye/4g/) متصل ومتاح حالياً.")
                            } else {
                                ConnectionCheckState.Failed("سيرفر يمن فورجي الرسمي لا يستجيب في الوقت الحالي.")
                            }
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionCheckState.Failed(
                                "فشل الاتصال بسيرفر الاتصالات: ${err.localizedMessage ?: "مهلة الاتصال انتهت"}"
                            )
                        )
                    }
                }
            )
        }
    }

    fun dismissConnectionState() {
        _uiState.update { it.copy(connectionState = ConnectionCheckState.Idle) }
    }

    fun selectPackageForCalc(pkg: Yemen4GPackage) {
        _uiState.update { it.copy(selectedPackageForCalc = pkg) }
    }

    fun deleteAccount(number: String) {
        repository.removeAccount(number)
        _uiState.update { it.copy(savedAccounts = repository.getSavedAccounts()) }
    }
}
