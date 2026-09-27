package com.example.ui.ads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentConfig by AdManager.configState.collectAsStateWithLifecycle()

    var isEnabled by remember { mutableStateOf(currentConfig.isAdsEnabled) }
    var banner468Key by remember { mutableStateOf(currentConfig.banner468x60Key) }
    var nativeKey by remember { mutableStateOf(currentConfig.nativeBannerKey) }
    var popunderUrl by remember { mutableStateOf(currentConfig.popunderScriptUrl) }
    var socialBarUrl by remember { mutableStateOf(currentConfig.socialBarScriptUrl) }
    var smartlinkUrl by remember { mutableStateOf(currentConfig.smartlinkUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Adsterra 5-Ad Suite", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Configured & Active in App", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Enable/Disable Switch
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Ads in App", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Displays ads on all mobile devices", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            modifier = Modifier.testTag("ads_enable_toggle")
                        )
                    }
                }

                // 1. Native Banner
                OutlinedTextField(
                    value = nativeKey,
                    onValueChange = { nativeKey = it },
                    label = { Text("1. Native Banner Key (Dashboard)") },
                    modifier = Modifier.fillMaxWidth().testTag("native_banner_key_input"),
                    singleLine = true
                )

                // 2. 468x60 Banner
                OutlinedTextField(
                    value = banner468Key,
                    onValueChange = { banner468Key = it },
                    label = { Text("2. 468x60 Banner Key (Bottom Bar)") },
                    modifier = Modifier.fillMaxWidth().testTag("banner_468_key_input"),
                    singleLine = true
                )

                // 3. Smartlink
                OutlinedTextField(
                    value = smartlinkUrl,
                    onValueChange = { smartlinkUrl = it },
                    label = { Text("3. Smartlink URL (Partner Deals)") },
                    modifier = Modifier.fillMaxWidth().testTag("smartlink_url_input"),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { AdManager.openSmartlink(context, smartlinkUrl) },
                    modifier = Modifier.fillMaxWidth().testTag("test_smartlink_btn")
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Test Smartlink (Direct Link)")
                }

                // 4. Popunder
                OutlinedTextField(
                    value = popunderUrl,
                    onValueChange = { popunderUrl = it },
                    label = { Text("4. Popunder Script URL") },
                    modifier = Modifier.fillMaxWidth().testTag("popunder_url_input"),
                    singleLine = true
                )

                // 5. Social Bar
                OutlinedTextField(
                    value = socialBarUrl,
                    onValueChange = { socialBarUrl = it },
                    label = { Text("5. Social Bar Script URL") },
                    modifier = Modifier.fillMaxWidth().testTag("social_bar_url_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AdManager.updateConfig(
                        context = context,
                        enabled = isEnabled,
                        banner468x60Key = banner468Key,
                        nativeBannerKey = nativeKey,
                        popunderScriptUrl = popunderUrl,
                        socialBarScriptUrl = socialBarUrl,
                        smartlinkUrl = smartlinkUrl
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_ad_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
