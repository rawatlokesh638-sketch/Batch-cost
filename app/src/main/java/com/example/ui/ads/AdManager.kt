package com.example.ui.ads

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdConfigState(
    val isAdsEnabled: Boolean = true,
    val banner468x60Key: String = AdManager.OWNER_BANNER_468X60_KEY,
    val nativeBannerKey: String = AdManager.OWNER_NATIVE_BANNER_KEY,
    val popunderScriptUrl: String = AdManager.OWNER_POPUNDER_URL,
    val socialBarScriptUrl: String = AdManager.OWNER_SOCIAL_BAR_URL,
    val smartlinkUrl: String = AdManager.OWNER_SMARTLINK_URL
)

/**
 * AdManager manages hardcoded, embedded monetization ads.
 * Ads are permanently enabled for all users and cannot be disabled or overridden by app users.
 */
object AdManager {
    // Owner's Permanent Ad Keys & URLs
    const val OWNER_BANNER_468X60_KEY = "0e2646541d90aca6dc3d2cd09bc02a41"
    const val OWNER_NATIVE_BANNER_KEY = "edd460171965647f099030c848d0ce48"
    const val OWNER_POPUNDER_URL = "https://pl31001085.profitableratecpmnetwork.com/0a/46/b8/0a46b8ebc82f8e7138db80c9b0781365.js"
    const val OWNER_SOCIAL_BAR_URL = "https://pl31001086.profitableratecpmnetwork.com/23/41/b3/2341b3e281555c9b44ed1d1557374bc3.js"
    const val OWNER_SMARTLINK_URL = "https://www.profitableratecpmnetwork.com/e2905fw619?key=f95b245bc9c99d1770e9aa518049431b"

    private val _configState = MutableStateFlow(
        AdConfigState(
            isAdsEnabled = true,
            banner468x60Key = OWNER_BANNER_468X60_KEY,
            nativeBannerKey = OWNER_NATIVE_BANNER_KEY,
            popunderScriptUrl = OWNER_POPUNDER_URL,
            socialBarScriptUrl = OWNER_SOCIAL_BAR_URL,
            smartlinkUrl = OWNER_SMARTLINK_URL
        )
    )
    val configState: StateFlow<AdConfigState> = _configState.asStateFlow()

    private var isInitialized = false
    private var lastPopunderTime = 0L

    fun init(context: Context) {
        if (isInitialized) return
        // Ensure ads are strictly enabled with owner's monetization keys
        _configState.value = AdConfigState(
            isAdsEnabled = true,
            banner468x60Key = OWNER_BANNER_468X60_KEY,
            nativeBannerKey = OWNER_NATIVE_BANNER_KEY,
            popunderScriptUrl = OWNER_POPUNDER_URL,
            socialBarScriptUrl = OWNER_SOCIAL_BAR_URL,
            smartlinkUrl = OWNER_SMARTLINK_URL
        )
        isInitialized = true
    }

    /**
     * 1. 468x60 / Responsive Banner HTML for Bottom Bar (Optimized for High CPM)
     */
    fun buildBanner468x60Html(state: AdConfigState): String {
        val key = OWNER_BANNER_468X60_KEY

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <meta name="referrer" content="always">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    html, body {
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
     * 2. Native Banner HTML for Dashboard Screen (Optimized for High CPM & Full Viewability)
     */
    fun buildNativeBannerHtml(state: AdConfigState): String {
        val key = OWNER_NATIVE_BANNER_KEY

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <meta name="referrer" content="always">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    html, body {
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
     * 3. Social Bar HTML (Highest CTR & CPM format)
     */
    fun buildSocialBarHtml(state: AdConfigState): String {
        val scriptUrl = OWNER_SOCIAL_BAR_URL

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="referrer" content="always">
                <style>body { margin:0; padding:0; background:transparent; overflow:hidden; }</style>
            </head>
            <body>
                <script src="$scriptUrl"></script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 4. Popunder trigger - Safely disabled to comply with App Store quality policies
     */
    fun triggerActionPopunder(context: Context) {
        // Disabled to prevent unwanted external popups and store rejection
    }

    /**
     * 5. Smartlink / Direct Link (Intentional CTA click)
     */
    fun openSmartlink(context: Context, customUrl: String? = null) {
        val targetUrl = customUrl?.ifEmpty { null } ?: OWNER_SMARTLINK_URL

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
