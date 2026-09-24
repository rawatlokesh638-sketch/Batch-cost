package com.example.ui.screens.orders

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.AIParsingConfig
import com.example.util.GeminiService
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualOrderScannerScreen(
    parsingConfig: AIParsingConfig,
    onBack: () -> Unit,
    onImportOrders: (List<com.example.ui.RecordedOrder>) -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        capturedBitmap = bitmap
    }

    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            capturedBitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visual Bill Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Scan handwritten bills or order notes to extract details automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            val result = GeminiService.parseOrderFromImage(capturedBitmap!!, parsingConfig, context)
                            try {
                                val orders = mutableListOf<com.example.ui.RecordedOrder>()
                                if (result.startsWith("[")) {
                                    val array = JSONArray(result)
                                    for (i in 0 until array.length()) {
                                        val obj = array.getJSONObject(i)
                                        orders.add(parseJsonToOrder(obj))
                                    }
                                } else if (result.startsWith("{")) {
                                    val obj = JSONObject(result)
                                    orders.add(parseJsonToOrder(obj))
                                }
                                if (orders.isNotEmpty()) {
                                    onImportOrders(orders)
                                }
                            } catch (e: Exception) {
                                // Error parsing
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyzing Bill...")
                    } else {
                        Text("Extract Orders with AI")
                    }
                }

                OutlinedButton(
                    onClick = {
                        capturedBitmap = null
                        selectedImageUri = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Text("Clear and Try Another")
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(48.dp))
                            Text("Pick from Gallery")
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onClick = { cameraLauncher.launch(null) }
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp))
                            Text("Take Photo of Bill")
                        }
                    }
                }
            }
        }
    }
}

private fun parseJsonToOrder(obj: JSONObject): com.example.ui.RecordedOrder {
    return com.example.ui.RecordedOrder(
        customerName = obj.optString("customerName", "Unknown"),
        productName = obj.optString("productName", "Uncategorized"),
        quantity = obj.optInt("quantity", 1),
        totalRevenue = obj.optDouble("totalRevenue", 0.0),
        totalCost = 0.0, // Default for now
        status = obj.optString("status", "New"),
        deliveryDate = obj.optString("deliveryDate", "Pending"),
        date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    )
}
