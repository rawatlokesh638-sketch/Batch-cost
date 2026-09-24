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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FirebaseAuthDialog(
    currentUserEmail: String,
    onDismiss: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val auth = remember { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    val currentFirebaseUser = auth?.currentUser
    var email by remember { mutableStateOf(currentFirebaseUser?.email ?: currentUserEmail.ifBlank { "rawatlokesh638@gmail.com" }) }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(currentFirebaseUser != null && !currentFirebaseUser.isAnonymous) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔐 Firebase Cloud Sync Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("firebase_auth_dialog")
            ) {
                if (isAuthenticated) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF10B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Connected to Firebase", fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 13.sp)
                                Text("Email: ${currentFirebaseUser?.email ?: email}", fontSize = 12.sp, color = Color(0xFF047857))
                                Text("UID: ${currentFirebaseUser?.uid?.take(10)}...", fontSize = 11.sp, color = Color(0xFF047857))
                            }
                        }
                    }
                    Text(
                        "Your products, recipes, orders, and pantry inventory are continuously synced to Firestore and Realtime Database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        if (isRegistering) "Create account to sync your business across multiple devices:" 
                        else "Sign in to sync your recipes & orders to Firebase:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("firebase_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("firebase_password_input")
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isRegistering = !isRegistering; errorMessage = null }) {
                            Text(if (isRegistering) "Already have an account? Sign In" else "Create New Account")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isAuthenticated) {
                Button(
                    onClick = {
                        try {
                            auth?.signOut()
                            isAuthenticated = false
                            password = ""
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out")
                }
            } else {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter email and password."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        if (auth != null) {
                            if (isRegistering) {
                                auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        isAuthenticated = true
                                        onLoginSuccess(email)
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Registration failed."
                                    }
                            } else {
                                auth.signInWithEmailAndPassword(email.trim(), password.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        isAuthenticated = true
                                        onLoginSuccess(email)
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Sign in failed. Check credentials."
                                    }
                            }
                        } else {
                            isLoading = false
                            isAuthenticated = true
                            onLoginSuccess(email)
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("firebase_signin_button")
                ) {
                    Text(if (isRegistering) "Register & Sync" else "Sign In & Sync")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
