package de.poljansek.budgetbook.core.config

object AppConfig {
    const val ALL_CATEGORIES: String = "Alle"
    const val DEFAULT_CURRENCY: String = "EUR"

    fun isProd(): Boolean = BuildConfig.FLAVOR.equals("prod", ignoreCase = true)

    fun flavorDefaultBaseUrl(): String {
        if (isProd()) {
            return BuildConfig.PROD_BASE_URL.trimEnd('/')
        }
        return "http://${loopbackHost()}:8080"
    }
}
