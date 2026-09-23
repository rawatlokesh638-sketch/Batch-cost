package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TeamMember(
    val name: String,
    val email: String,
    val role: TeamRole,
    val permissions: String
)

enum class TeamRole {
    OWNER, MANAGER, STAFF
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val teamMembers = remember {
        mutableStateListOf(
            TeamMember("Lokesh Rawat", "rawatlokesh638@gmail.com", TeamRole.OWNER, "Full Access: Billing, Settings, Reports, Products, Orders"),
            TeamMember("Amit Kumar", "amit@bakery.com", TeamRole.MANAGER, "Can manage Products, Pricing, Orders, and Analytics reports"),
            TeamMember("Rohan Singh", "rohan@bakery.com", TeamRole.STAFF, "Can create production batches & view assigned task queues")
        )
    }

    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf(TeamRole.STAFF) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("👨💼 Team Accounts & Roles", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Owner, Manager & Staff granular permissions", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("team_members_list")
            ) {
                items(teamMembers) { member ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(member.email, fontSize = 12.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (member.role) {
                                                TeamRole.OWNER -> Color(0xFFFEF3C7)
                                                TeamRole.MANAGER -> Color(0xFFDBEAFE)
                                                TeamRole.STAFF -> Color(0xFFF1F5F9)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = member.role.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (member.role) {
                                            TeamRole.OWNER -> Color(0xFFB45309)
                                            TeamRole.MANAGER -> Color(0xFF1D4ED8)
                                            TeamRole.STAFF -> Color(0xFF475569)
                                        }
                                    )
                                }
                            }

                            Text("🔒 Permissions: ${member.permissions}", fontSize = 12.sp, color = Color(0xFF334155))
                        }
                    }
                }
            }

            if (showInviteDialog) {
                AlertDialog(
                    onDismissRequest = { showInviteDialog = false },
                    title = { Text("Invite Team Member") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inviteEmail,
                                onValueChange = { inviteEmail = it },
                                label = { Text("Staff Email Address") },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (inviteEmail.isNotBlank()) {
                                    teamMembers.add(TeamMember(inviteEmail.substringBefore("@"), inviteEmail, TeamRole.STAFF, "Can create production batches & view assigned task queues"))
                                    inviteEmail = ""
                                    showInviteDialog = false
                                }
                            }
                        ) {
                            Text("Send Invite")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showInviteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Button(
                onClick = { showInviteDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("invite_team_member_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Invite New Staff Member")
            }
        }
    }
}
