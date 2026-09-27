package com.example.ui.ads

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdConfigState(
    val isAdsEnabled: Boolean = true,
    val banner468x60Key: String = "0e2646541d90aca6dc3d2cd09bc02a41",
    val nativeBannerKey: String = "edd460171965647f099030c848d0ce48",
    val popunderScriptUrl: String = "https://pl31001085.profitableratecpmnetwork.com/0a/46/b8/0a46b8ebc82f8e7138db80c9b0781365.js",
    val socialBarScriptUrl: String = "https://pl31001086.profitableratecpmnetwork.com/23/41/b3/2341b3e281555c9b44ed1d1557374bc3.js",
    val smartlinkUrl: String = "https://www.profitableratecpmnetwork.com/e2905fw619?key=f95b245bc9c99d1770e9aa518049431b"
)

object AdManager {
    private const val PREFS_NAME = "adsterra_manager_prefs"
    private const val KEY_ADS_ENABLED = "ads_enabled"
    private const val KEY_BANNER_468X60 = "banner_468x60_key"
    private const val KEY_NATIVE_BANNER = "native_banner_key"
    private const val KEY_POPUNDER_URL = "popunder_script_url"
    private const val KEY_SOCIAL_BAR_URL = "social_bar_script_url"
    private const val KEY_SMARTLINK_URL = "smartlink_url"

    const val DEFAULT_BANNER_468X60_KEY = "0e2646541d90aca6dc3d2cd09bc02a41"
    const val DEFAULT_NATIVE_BANNER_KEY = "edd460171965647f099030c848d0ce48"
    const val DEFAULT_POPUNDER_URL = "https://pl31001085.profitableratecpmnetwork.com/0a/46/b8/0a46b8ebc82f8e7138db80c9b0781365.js"
    const val DEFAULT_SOCIAL_BAR_URL = "https://pl31001086.profitableratecpmnetwork.com/23/41/b3/2341b3e281555c9b44ed1d1557374bc3.js"
    const val DEFAULT_SMARTLINK_URL = "https://www.profitableratecpmnetwork.com/e2905fw619?key=f95b245bc9c99d1770e9aa518049431b"

    private val _configState = MutableStateFlow(AdConfigState())
    val configState: StateFlow<AdConfigState> = _configState.asStateFlow()

    private var isInitialized = false
    private var lastPopunderTime = 0L

    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_ADS_ENABLED, true)
        val b468 = prefs.getString(KEY_BANNER_468X60, DEFAULT_BANNER_468X60_KEY) ?: DEFAULT_BANNER_468X60_KEY
        val nativeKey = prefs.getString(KEY_NATIVE_BANNER, DEFAULT_NATIVE_BANNER_KEY) ?: DEFAULT_NATIVE_BANNER_KEY
        val popunder = prefs.getString(KEY_POPUNDER_URL, DEFAULT_POPUNDER_URL) ?: DEFAULT_POPUNDER_URL
        val socialBar = prefs.getString(KEY_SOCIAL_BAR_URL, DEFAULT_SOCIAL_BAR_URL) ?: DEFAULT_SOCIAL_BAR_URL
        val smartlink = prefs.getString(KEY_SMARTLINK_URL, DEFAULT_SMARTLINK_URL) ?: DEFAULT_SMARTLINK_URL

        _configState.value = AdConfigState(
            isAdsEnabled = enabled,
            banner468x60Key = b468,
            nativeBannerKey = nativeKey,
            popunderScriptUrl = popunder,
            socialBarScriptUrl = socialBar,
            smartlinkUrl = smartlink
        )
        isInitialized = true
    }

    fun updateConfig(
        context: Context,
        enabled: Boolean,
        banner468x60Key: String,
        nativeBannerKey: String,
        popunderScriptUrl: String,
        socialBarScriptUrl: String,
        smartlinkUrl: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ADS_ENABLED, enabled)
            putString(KEY_BANNER_468X60, banner468x60Key.trim())
            putString(KEY_NATIVE_BANNER, nativeBannerKey.trim())
            putString(KEY_POPUNDER_URL, popunderScriptUrl.trim())
            putString(KEY_SOCIAL_BAR_URL, socialBarScriptUrl.trim())
            putString(KEY_SMARTLINK_URL, smartlinkUrl.trim())
        }

        _configState.value = AdConfigState(
            isAdsEnabled = enabled,
            banner468x60Key = banner468x60Key.trim(),
            nativeBannerKey = nativeBannerKey.trim(),
            popunderScriptUrl = popunderScriptUrl.trim(),
            socialBarScriptUrl = socialBarScriptUrl.trim(),
            smartlinkUrl = smartlinkUrl.trim()
        )
    }

    /**
     * 1. 468x60 Responsive Banner HTML for Bottom Bar
     */
    fun buildBanner468x60Html(state: AdConfigState): String {
        val key = state.banner468x60Key.ifEmpty { DEFAULT_BANNER_468X60_KEY }

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
                    }
                    .banner-wrapper {
                        width: 100%;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        transform: scale(min(1, calc((100vw - 16px) / 468)));
                        transform-origin: center center;
                    }
                </style>
            </head>
            <body>
                <div class="banner-wrapper">
                    <script type="text/javascript">
                        atOptions = {
                            'key' : '$key',
                            'format' : 'iframe',
                            'height' : 60,
                            'width' : 468,
                            'params' : {}
                        };
                    </script>
                    <script type="text/javascript" src="https://www.highrevenueformat.com/$key/invoke.js"></script>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 2. Native Banner HTML for Dashboard Screen
     */
    fun buildNativeBannerHtml(state: AdConfigState): String {
        val key = state.nativeBannerKey.ifEmpty { DEFAULT_NATIVE_BANNER_KEY }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        background: transparent;
                        width: 100%;
                        min-height: 100%;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    }
                    #container-$key {
                        width: 100%;
                        max-width: 100%;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }
                </style>
            </head>
            <body>
                <div id="container-$key"></div>
                <script async="async" data-cfasync="false" src="https://pl31537282.profitableratecpmnetwork.com/$key/invoke.js"></script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 3. Social Bar HTML
     */
    fun buildSocialBarHtml(state: AdConfigState): String {
        val scriptUrl = state.socialBarScriptUrl.ifEmpty { DEFAULT_SOCIAL_BAR_URL }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body { margin:0; padding:0; background:transparent; overflow:hidden; }</style>
            </head>
            <body>
                <script src="$scriptUrl"></script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 4. Popunder trigger (Controlled frequency: e.g. at most once per 3 minutes after successful action)
     */
    fun triggerActionPopunder(context: Context) {
        if (!_configState.value.isAdsEnabled) return
        val now = System.currentTimeMillis()
        // 3 minutes cooldown between popunders
        if (now - lastPopunderTime < 180_000L) {
            return
        }
        lastPopunderTime = now

        val smartlink = _configState.value.smartlinkUrl.ifEmpty { DEFAULT_SMARTLINK_URL }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(smartlink)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if browser not available
        }
    }

    /**
     * 5. Smartlink / Direct Link (Intentional CTA click)
     */
    fun openSmartlink(context: Context, customUrl: String? = null) {
        val targetUrl = customUrl?.ifEmpty { null }
            ?: _configState.value.smartlinkUrl.ifEmpty { DEFAULT_SMARTLINK_URL }

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
