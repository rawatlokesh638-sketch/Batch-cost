package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun TopOverflowMenu(
    onSelectTab: (Int) -> Unit,
    onOpenPriceList: () -> Unit,
    onOpenCrm: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenMultiBusiness: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenMonetization: () -> Unit,
    onOpenWhatsAppLink: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("top_overflow_menu_button")
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("top_overflow_dropdown")
        ) {
            DropdownMenuItem(
                text = { Text("📊 Dashboard") },
                onClick = { expanded = false; onSelectTab(0) },
                leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("📦 Products & Recipes") },
                onClick = { expanded = false; onSelectTab(1) },
                leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("📋 Orders Management") },
                onClick = { expanded = false; onSelectTab(2) },
                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("🏷️ Label Generator") },
                onClick = { expanded = false; onSelectTab(3) },
                leadingIcon = { Icon(Icons.Default.ListAlt, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("🍳 Ingredients Pantry") },
                onClick = { expanded = false; onSelectTab(4) },
                leadingIcon = { Icon(Icons.Default.Kitchen, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("🧮 Batch Calculator") },
                onClick = { expanded = false; onSelectTab(5) },
                leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("📋 Price List Generator") },
                onClick = { expanded = false; onOpenPriceList() },
                leadingIcon = { Icon(Icons.Default.ListAlt, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("👥 Customer CRM") },
                onClick = { expanded = false; onOpenCrm() },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("🔔 Smart Reminders") },
                onClick = { expanded = false; onOpenReminders() },
                leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("🏪 Multi-Business Switcher") },
                onClick = { expanded = false; onOpenMultiBusiness() },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("👨💼 Team & Roles") },
                onClick = { expanded = false; onOpenTeam() },
                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("🔐 Firebase Auth") },
                onClick = { expanded = false; onOpenAuth() },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("💬 AI Business Assistant") },
                onClick = { expanded = false; onOpenAiAssistant() },
                leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("📲 WhatsApp Smart Link") },
                onClick = { expanded = false; onOpenWhatsAppLink() },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("💳 Plans & Monetization") },
                onClick = { expanded = false; onOpenMonetization() },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("⚙️ Settings") },
                onClick = { expanded = false; onSelectTab(6) },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
        }
    }
}
