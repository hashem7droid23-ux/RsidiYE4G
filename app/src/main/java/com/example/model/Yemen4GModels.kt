package com.example.model

/**
 * Data structures for Yemen 4G Modem Portal and Balance Inquiry
 * Programmed & Designed for: هاشم القديمي (Hashem Al-Qadimi)
 */

data class SubscriberAccount(
    val number: String,
    val label: String,
    val lastChecked: Long = System.currentTimeMillis()
)

data class Yemen4GBalance(
    val accountNumber: String,
    val subscriberName: String,
    val packageName: String,
    val totalGigabytes: Double,
    val remainingGigabytes: Double,
    val usedGigabytes: Double,
    val expirationDate: String,
    val daysRemaining: Int,
    val status: String,
    val balanceYer: Double = 0.0,
    val queryTimestamp: String,
    val isOfficialServerVerified: Boolean = true
) {
    val usagePercentage: Float
        get() = if (totalGigabytes > 0.0) {
            ((usedGigabytes / totalGigabytes) * 100.0).coerceIn(0.0, 100.0).toFloat()
        } else 0f

    val remainingPercentage: Float
        get() = if (totalGigabytes > 0.0) {
            ((remainingGigabytes / totalGigabytes) * 100.0).coerceIn(0.0, 100.0).toFloat()
        } else 0f
}

data class Yemen4GPackage(
    val id: String,
    val name: String,
    val gigabytes: Double,
    val priceYer: Int,
    val validityDays: Int,
    val speedLimit: String,
    val isPopular: Boolean = false,
    val features: List<String>
) {
    val taxYer: Int get() = (priceYer * 0.14).toInt() // 14% Telecommunication VAT
    val totalWithTax: Int get() = priceYer + taxYer
}

data class ModemGatewayInfo(
    val brand: String,
    val modelName: String,
    val gatewayIp: String,
    val defaultUsername: String,
    val defaultPassword: String,
    val instructions: String
)

sealed interface QueryUiState {
    data object Idle : QueryUiState
    data object Loading : QueryUiState
    data class Success(val balance: Yemen4GBalance) : QueryUiState
    data class Error(val message: String, val canRetry: Boolean = true) : QueryUiState
}

enum class Yemen4GScreen {
    INQUIRY,
    OFFICIAL_PORTAL,
    PACKAGES,
    MODEM_GATEWAY,
    DEVELOPER_INFO
}
