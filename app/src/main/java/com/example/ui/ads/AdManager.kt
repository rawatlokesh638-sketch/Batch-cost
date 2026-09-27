package com.example.ui.ads

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ActiveAdNetwork {
    BOTH,
    ADSTERRA,
    MONETAG
}

data class AdConfigState(
    val isAdsEnabled: Boolean = true,
    val adNetwork: ActiveAdNetwork = ActiveAdNetwork.BOTH,
    val adsterraBannerKey: String = "",
    val adsterraDirectLink: String = "",
    val monetagZoneId: String = "11904884",
    val monetagDomain: String = "3nbf4.com"
)

object AdManager {
    private const val PREFS_NAME = "ad_manager_prefs"
    private const val KEY_ADS_ENABLED = "ads_enabled"
    private const val KEY_AD_NETWORK = "ad_network"
    private const val KEY_ADSTERRA_BANNER_KEY = "adsterra_banner_key"
    private const val KEY_ADSTERRA_DIRECT_LINK = "adsterra_direct_link"
    private const val KEY_MONETAG_ZONE_ID = "monetag_zone_id"
    private const val KEY_MONETAG_DOMAIN = "monetag_domain"

    private val _configState = MutableStateFlow(AdConfigState())
    val configState: StateFlow<AdConfigState> = _configState.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_ADS_ENABLED, true)
        val networkStr = prefs.getString(KEY_AD_NETWORK, ActiveAdNetwork.BOTH.name) ?: ActiveAdNetwork.BOTH.name
        val network = try {
            ActiveAdNetwork.valueOf(networkStr)
        } catch (_: Exception) {
            ActiveAdNetwork.BOTH
        }
        val adsterraKey = prefs.getString(KEY_ADSTERRA_BANNER_KEY, "") ?: ""
        val adsterraDirect = prefs.getString(KEY_ADSTERRA_DIRECT_LINK, "") ?: ""
        val monetagZone = prefs.getString(KEY_MONETAG_ZONE_ID, "11904884") ?: "11904884"
        val monetagDom = prefs.getString(KEY_MONETAG_DOMAIN, "3nbf4.com") ?: "3nbf4.com"

        _configState.value = AdConfigState(
            isAdsEnabled = enabled,
            adNetwork = network,
            adsterraBannerKey = adsterraKey,
            adsterraDirectLink = adsterraDirect,
            monetagZoneId = monetagZone,
            monetagDomain = monetagDom
        )
        isInitialized = true
    }

    fun updateConfig(
        context: Context,
        enabled: Boolean,
        network: ActiveAdNetwork,
        adsterraBannerKey: String,
        adsterraDirectLink: String,
        monetagZoneId: String,
        monetagDomain: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ADS_ENABLED, enabled)
            putString(KEY_AD_NETWORK, network.name)
            putString(KEY_ADSTERRA_BANNER_KEY, adsterraBannerKey.trim())
            putString(KEY_ADSTERRA_DIRECT_LINK, adsterraDirectLink.trim())
            putString(KEY_MONETAG_ZONE_ID, monetagZoneId.trim())
            putString(KEY_MONETAG_DOMAIN, monetagDomain.trim())
        }

        _configState.value = AdConfigState(
            isAdsEnabled = enabled,
            adNetwork = network,
            adsterraBannerKey = adsterraBannerKey.trim(),
            adsterraDirectLink = adsterraDirectLink.trim(),
            monetagZoneId = monetagZoneId.trim(),
            monetagDomain = monetagDomain.trim()
        )
    }

    /**
     * Generates a self-contained HTML page designed for WebView banner rendering.
     */
    fun buildBannerHtml(state: AdConfigState): String {
        val adsterraKey = state.adsterraBannerKey.trim()
        val monetagZone = state.monetagZoneId.trim()
        val monetagDomain = state.monetagDomain.trim().ifEmpty { "3nbf4.com" }

        val adContent = StringBuilder()

        when (state.adNetwork) {
            ActiveAdNetwork.ADSTERRA -> {
                if (adsterraKey.isNotEmpty()) {
                    adContent.append(buildAdsterraSnippet(adsterraKey))
                } else {
                    adContent.append(buildDefaultFallbackAd("Adsterra Banner Ready (Add Banner Key in Settings)"))
                }
            }
            ActiveAdNetwork.MONETAG -> {
                if (monetagZone.isNotEmpty()) {
                    adContent.append(buildMonetagSnippet(monetagDomain, monetagZone))
                } else {
                    adContent.append(buildDefaultFallbackAd("Monetag Zone $monetagZone Active"))
                }
            }
            ActiveAdNetwork.BOTH -> {
                if (adsterraKey.isNotEmpty()) {
                    adContent.append(buildAdsterraSnippet(adsterraKey))
                } else if (monetagZone.isNotEmpty()) {
                    adContent.append(buildMonetagSnippet(monetagDomain, monetagZone))
                } else {
                    adContent.append(buildDefaultFallbackAd("Bakery Deals & Ads Partner Network Active"))
                }
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        background: transparent;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    }
                    .banner-container {
                        width: 100%;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }
                </style>
            </head>
            <body>
                <div class="banner-container">
                    $adContent
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildAdsterraSnippet(key: String): String {
        return """
            <script type="text/javascript">
                atOptions = {
                    'key' : '$key',
                    'format' : 'iframe',
                    'height' : 50,
                    'width' : 320,
                    'params' : {}
                };
            </script>
            <script type="text/javascript" src="//www.topcreativeformat.com/$key/invoke.js"></script>
        """.trimIndent()
    }

    private fun buildMonetagSnippet(domain: String, zoneId: String): String {
        return """
            <script src="https://$domain/act/files/tag.min.js?z=$zoneId" data-cfasync="false" async></script>
            <div style="font-size: 11px; color: #64748b; padding: 4px; text-align: center;">
                ✨ Monetag Partner Zone #$zoneId Connected
            </div>
        """.trimIndent()
    }

    private fun buildDefaultFallbackAd(text: String): String {
        return """
            <div style="display: inline-flex; align-items: center; gap: 8px; background: linear-gradient(135deg, #1e293b, #0f172a); color: #f8fafc; padding: 8px 14px; border-radius: 8px; font-size: 12px; font-weight: 500; border: 1px solid #334155; max-width: 95%;">
                <span style="background: #3b82f6; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: bold;">AD</span>
                <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">$text</span>
            </div>
        """.trimIndent()
    }

    fun openDirectLink(context: Context, customUrl: String? = null) {
        val targetUrl = customUrl?.ifEmpty { null }
            ?: _configState.value.adsterraDirectLink.ifEmpty { null }
            ?: "https://github.com/rawatlokesh638-sketch/Batch-cost"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if browser not available
        }
    }
}
