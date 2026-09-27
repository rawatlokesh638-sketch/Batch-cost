package com.example.ui.ads

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
    var selectedNetwork by remember { mutableStateOf(currentConfig.adNetwork) }
    var adsterraBannerKey by remember { mutableStateOf(currentConfig.adsterraBannerKey) }
    var adsterraDirectLink by remember { mutableStateOf(currentConfig.adsterraDirectLink) }
    var monetagZoneId by remember { mutableStateOf(currentConfig.monetagZoneId) }
    var monetagDomain by remember { mutableStateOf(currentConfig.monetagDomain) }

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
                    Text("Adsterra & Monetag Ads", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Mobile Ads & Monetization Setup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("Displays banner & partner offers on all mobiles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            modifier = Modifier.testTag("ads_enable_toggle")
                        )
                    }
                }

                // Network Selector
                Text("Select Ad Network:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedNetwork == ActiveAdNetwork.BOTH,
                        onClick = { selectedNetwork = ActiveAdNetwork.BOTH },
                        label = { Text("Both") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedNetwork == ActiveAdNetwork.ADSTERRA,
                        onClick = { selectedNetwork = ActiveAdNetwork.ADSTERRA },
                        label = { Text("Adsterra") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedNetwork == ActiveAdNetwork.MONETAG,
                        onClick = { selectedNetwork = ActiveAdNetwork.MONETAG },
                        label = { Text("Monetag") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Monetag Config Box (Preconfigured with user's sw.js values)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Monetag Setup (sw.js Connected)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = monetagZoneId,
                            onValueChange = { monetagZoneId = it },
                            label = { Text("Monetag Zone ID") },
                            placeholder = { Text("11904884") },
                            modifier = Modifier.fillMaxWidth().testTag("monetag_zone_id_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = monetagDomain,
                            onValueChange = { monetagDomain = it },
                            label = { Text("Monetag Script Domain") },
                            placeholder = { Text("3nbf4.com") },
                            modifier = Modifier.fillMaxWidth().testTag("monetag_domain_input"),
                            singleLine = true
                        )
                    }
                }

                // Adsterra Config Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Adsterra Setup", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)

                        OutlinedTextField(
                            value = adsterraBannerKey,
                            onValueChange = { adsterraBannerKey = it },
                            label = { Text("Adsterra Banner Key (320x50 / 300x250)") },
                            placeholder = { Text("e.g. c032ab48df9...") },
                            modifier = Modifier.fillMaxWidth().testTag("adsterra_banner_key_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = adsterraDirectLink,
                            onValueChange = { adsterraDirectLink = it },
                            label = { Text("Adsterra Direct Link (SmartLink)") },
                            placeholder = { Text("https://www.profitablecpmrate.com/...") },
                            modifier = Modifier.fillMaxWidth().testTag("adsterra_direct_link_input"),
                            singleLine = true
                        )

                        if (adsterraDirectLink.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    AdManager.openDirectLink(context, adsterraDirectLink)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("test_direct_link_btn")
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Test Direct Link Now")
                            }
                        }
                    }
                }

                // Helpful Guide
                Text(
                    text = "💡 Guide: Adsterra se 320x50 banner banakar uska 'Key' daalein aur Direct Link banakar SmartLink daalein. Monetag ka sw.js Zone #$monetagZoneId pehle se configured hai.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AdManager.updateConfig(
                        context = context,
                        enabled = isEnabled,
                        network = selectedNetwork,
                        adsterraBannerKey = adsterraBannerKey,
                        adsterraDirectLink = adsterraDirectLink,
                        monetagZoneId = monetagZoneId,
                        monetagDomain = monetagDomain
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
