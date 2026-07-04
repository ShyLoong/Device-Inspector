package com.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val deviceInfoState = MutableStateFlow<List<DeviceInfoItem>>(emptyList())
    private val compareInfoState = MutableStateFlow<List<DeviceInfoItem>?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        loadDeviceData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        loadDeviceData()

        setContent {
            MyApplicationTheme {
                var showImportDialog by remember { mutableStateOf(false) }
                var currentLanguage by remember { mutableStateOf(AppTranslator.Language.ENGLISH) }
                val context = LocalContext.current

                if (showImportDialog) {
                    ImportDialog(
                        language = currentLanguage,
                        onDismiss = { showImportDialog = false },
                        onImport = { jsonString ->
                            compareInfoState.value = JsonUtils.importFromJson(jsonString)
                            showImportDialog = false
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text(AppTranslator.translate("Device Risk Inspector", currentLanguage)) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            actions = {
                                IconButton(onClick = {
                                    currentLanguage = if (currentLanguage == AppTranslator.Language.ENGLISH) {
                                        AppTranslator.Language.CHINESE
                                    } else {
                                        AppTranslator.Language.ENGLISH
                                    }
                                }) {
                                    Icon(Icons.Filled.Translate, contentDescription = "Switch Language")
                                }
                                val currentData by deviceInfoState.collectAsState()
                                val compareData by compareInfoState.collectAsState()

                                if (compareData != null) {
                                    IconButton(onClick = { compareInfoState.value = null }) {
                                        Icon(Icons.Filled.Close, contentDescription = AppTranslator.translate("Clear Comparison", currentLanguage))
                                    }
                                } else {
                                    IconButton(onClick = { showImportDialog = true }) {
                                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = AppTranslator.translate("Import & Compare", currentLanguage))
                                    }
                                    IconButton(onClick = {
                                        val jsonString = JsonUtils.exportToJson(currentData)
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, jsonString)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }) {
                                        Icon(Icons.Filled.Share, contentDescription = AppTranslator.translate("Export JSON", currentLanguage))
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { requestPermissionsAndLoadData() }) {
                            Text(AppTranslator.translate("Scan", currentLanguage))
                        }
                    }
                ) { innerPadding ->
                    val data by deviceInfoState.collectAsState(initial = emptyList())
                    val compareData by compareInfoState.collectAsState()

                    if (compareData != null) {
                        ComparisonList(
                            localData = data,
                            remoteData = compareData!!,
                            language = currentLanguage,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    } else {
                        DeviceDataList(
                            data = data,
                            language = currentLanguage,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun loadDeviceData() {
        val collector = DeviceDataCollector(this)
        deviceInfoState.value = collector.collectAllData()
    }

    private fun requestPermissionsAndLoadData() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

@Composable
fun ImportDialog(language: AppTranslator.Language, onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppTranslator.translate("Import Device Data", language)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(AppTranslator.translate("Paste JSON data here", language)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = { onImport(text) }) {
                Text(AppTranslator.translate("Compare", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppTranslator.translate("Cancel", language))
            }
        }
    )
}

@Composable
fun ComparisonList(localData: List<DeviceInfoItem>, remoteData: List<DeviceInfoItem>, language: AppTranslator.Language, modifier: Modifier = Modifier) {
    val allCategories = (localData.map { it.category } + remoteData.map { it.category }).distinct()
    
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        allCategories.forEach { category ->
            item {
                Text(
                    text = AppTranslator.translate(category, language),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }
            
            val localItems = localData.filter { it.category == category }
            val remoteItems = remoteData.filter { it.category == category }
            val allKeys = (localItems.map { it.key } + remoteItems.map { it.key }).distinct()

            items(allKeys) { key ->
                val localValue = localItems.find { it.key == key }?.value ?: "N/A"
                val remoteValue = remoteItems.find { it.key == key }?.value ?: "N/A"
                val isDifferent = localValue != remoteValue

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDifferent) MaterialTheme.colorScheme.errorContainer 
                                         else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = AppTranslator.translate(key, language),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDifferent) MaterialTheme.colorScheme.onErrorContainer 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppTranslator.translate("This Device", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDifferent) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = AppTranslator.translate(localValue, language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDifferent) MaterialTheme.colorScheme.onErrorContainer 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppTranslator.translate("Other Device", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDifferent) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = AppTranslator.translate(remoteValue, language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDifferent) MaterialTheme.colorScheme.onErrorContainer 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DeviceDataList(data: List<DeviceInfoItem>, language: AppTranslator.Language, modifier: Modifier = Modifier) {
    val groupedData = data.groupBy { it.category }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedData.forEach { (category, items) ->
            item {
                Text(
                    text = AppTranslator.translate(category, language),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = AppTranslator.translate(item.key, language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppTranslator.translate(item.value, language),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
