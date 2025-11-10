package com.aternoscontroller.ui.screens

import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.aternoscontroller.viewmodel.AternosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AternosViewModel,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val serverState by viewModel.serverState.collectAsState()
    val autoAcceptQueue by viewModel.autoAcceptQueue.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aternos Controller") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Sair",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            
                            allowFileAccess = true
                            allowContentAccess = true
                            
                            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onServerStatusChange(isOnline: Boolean, players: Int) {
                                viewModel.updateServerState(isOnline, players)
                            }
                            
                            @JavascriptInterface
                            fun onQueueDetected(position: Int) {
                                viewModel.updateServerState(false, 0, position)
                                if (autoAcceptQueue) {
                                    this@apply.post {
                                        evaluateJavascript("document.querySelector('.queue-button')?.click();", null)
                                    }
                                }
                            }
                        }, "AndroidInterface")

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                
                                view?.evaluateJavascript("""
                                    (function() {
                                        setInterval(function() {
                                            try {
                                                var statusElement = document.querySelector('.statuslabel-label');
                                                if (statusElement) {
                                                    var isOnline = statusElement.textContent.includes('Online');
                                                    var playersElement = document.querySelector('.statuslabel-players');
                                                    var players = playersElement ? parseInt(playersElement.textContent) : 0;
                                                    AndroidInterface.onServerStatusChange(isOnline, players || 0);
                                                }
                                                
                                                var queueElement = document.querySelector('.queue-position');
                                                if (queueElement) {
                                                    var position = parseInt(queueElement.textContent);
                                                    AndroidInterface.onQueueDetected(position || 0);
                                                }
                                            } catch(e) {
                                                console.log('Error:', e);
                                            }
                                        }, 2000);
                                    })();
                                """.trimIndent(), null)
                            }
                        }

                        loadUrl("https://aternos.org/server/")
                        webView = this
                    }
                }
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sair") },
            text = { Text("Deseja realmente sair?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        webView?.clearCache(true)
                        webView?.clearHistory()
                        CookieManager.getInstance().removeAllCookies(null)
                        onLogout()
                    }
                ) {
                    Text("Sim")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Não")
                }
            }
        )
    }
}
