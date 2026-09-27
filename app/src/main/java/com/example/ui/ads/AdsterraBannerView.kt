package com.example.ui.ads

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Global helper to optimize WebView for the highest possible CPM / eCPM in Adsterra & Monetag networks:
 * 1. Disguises Android WebView as a genuine Chrome Mobile Browser by stripping '; wv' and 'Version/4.0',
 *    which prevents ad networks from downgrading traffic to low-floor in-app bids.
 * 2. Enables 3rd-party cookies for high-paying retargeting advertiser bids (Finance, E-comm, Crypto).
 * 3. Enables Hardware Acceleration, DOM storage, Database storage, and always-allow mixed content.
 * 4. Ensures external links open in device browser to track conversions and highest affiliate rates.
 */
@SuppressLint("SetJavaScriptEnabled")
fun optimizeWebViewForHighCpm(webView: WebView) {
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(webView, true)
    }

    webView.apply {
        setBackgroundColor(Color.TRANSPARENT)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            mediaPlaybackRequiresUserGesture = false

            // Disguise WebView as Chrome Mobile Browser for Tier-1 eCPM bidding
            val currentUa = userAgentString ?: ""
            if (currentUa.contains("; wv") || currentUa.contains("Version/4.0")) {
                userAgentString = currentUa.replace("; wv", "").replace("Version/4.0 ", "")
            }
        }
    }
}

/**
 * 1. Bottom 468x60 Responsive Banner across main tabs with CPM Optimization
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdBannerBottomBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AdManager.init(context)
    }

    val configState by AdManager.configState.collectAsStateWithLifecycle()

    if (!configState.isAdsEnabled) {
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("adsterra_banner_container"),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SPONSORED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Bakery Partner Network",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                val htmlData = remember(configState.banner468x60Key) {
                    AdManager.buildBanner468x60Html(configState)
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            optimizeWebViewForHighCpm(this)
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        ctx.startActivity(intent)
                                        true
                                    } catch (_: Exception) {
                                        false
                                    }
                                }
                            }
                            loadDataWithBaseURL("https://www.highrevenueformat.com/", htmlData, "text/html", "UTF-8", null)
                        }
                    },
                    update = { view ->
                        view.loadDataWithBaseURL("https://www.highrevenueformat.com/", htmlData, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }
        }
    }
}

/**
 * 2. Native Banner Card for Dashboard Screen (Optimized Viewability & High CPM)
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdsterraNativeBannerCard(
    modifier: Modifier = Modifier
) {
    val configState by AdManager.configState.collectAsStateWithLifecycle()

    if (!configState.isAdsEnabled) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("adsterra_native_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📢 SPONSORED PARTNER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Adsterra Native",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            val htmlData = remember(configState.nativeBannerKey) {
                AdManager.buildNativeBannerHtml(configState)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            optimizeWebViewForHighCpm(this)
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        ctx.startActivity(intent)
                                        true
                                    } catch (_: Exception) {
                                        false
                                    }
                                }
                            }
                            loadDataWithBaseURL("https://pl31537282.profitableratecpmnetwork.com/", htmlData, "text/html", "UTF-8", null)
                        }
                    },
                    update = { view ->
                        view.loadDataWithBaseURL("https://pl31537282.profitableratecpmnetwork.com/", htmlData, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }
        }
    }
}

/**
 * 3. Social Bar Composable (Adsterra's #1 Highest CTR & eCPM format)
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdsterraSocialBarWidget(
    modifier: Modifier = Modifier
) {
    val configState by AdManager.configState.collectAsStateWithLifecycle()
    if (!configState.isAdsEnabled) return

    val htmlData = remember(configState.socialBarScriptUrl) {
        AdManager.buildSocialBarHtml(configState)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("adsterra_social_bar_widget"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    optimizeWebViewForHighCpm(this)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            return try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                ctx.startActivity(intent)
                                true
                            } catch (_: Exception) {
                                false
                            }
                        }
                    }
                    loadDataWithBaseURL("https://pl31001086.profitableratecpmnetwork.com/", htmlData, "text/html", "UTF-8", null)
                }
            },
            update = { view ->
                view.loadDataWithBaseURL("https://pl31001086.profitableratecpmnetwork.com/", htmlData, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxWidth().height(58.dp)
        )
    }
}

/**
 * 4. Smartlink High-Revenue Intentional CTA Card (Payout $3.00 - $15.00+ CPM)
 * High CTR directly multiplies Adsterra account eCPM!
 */
@Composable
fun AdsterraSmartlinkCard(
    modifier: Modifier = Modifier,
    title: String = "🔥 Bakery Supplies & Wholesale Deals",
    subtitle: String = "Save up to 40% on packaging, ovens & bulk raw ingredients (Daily Offers)"
) {
    val context = LocalContext.current
    val configState by AdManager.configState.collectAsStateWithLifecycle()

    if (!configState.isAdsEnabled) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { AdManager.openSmartlink(context) }
            .testTag("adsterra_smartlink_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFEF3C7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color(0xFFFDE68A),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFB45309),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = androidx.compose.ui.graphics.Color(0xFF92400E)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = androidx.compose.ui.graphics.Color(0xFFB45309),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { AdManager.openSmartlink(context) },
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFD97706)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Claim", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }
    }
}
