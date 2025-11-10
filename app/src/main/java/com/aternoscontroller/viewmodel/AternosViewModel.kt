package com.aternoscontroller.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aternoscontroller.data.PreferencesManager
import com.aternoscontroller.worker.ServerCheckWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ServerState(
    val isOnline: Boolean = false,
    val playersOnline: Int = 0,
    val queuePosition: Int? = null,
    val isInQueue: Boolean = false,
    val isLoading: Boolean = false
)

class AternosViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val workManager = WorkManager.getInstance(application)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _serverState = MutableStateFlow(ServerState())
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    private val _autoAcceptQueue = MutableStateFlow(false)
    val autoAcceptQueue: StateFlow<Boolean> = _autoAcceptQueue.asStateFlow()

    private val _autoStartServer = MutableStateFlow(false)
    val autoStartServer: StateFlow<Boolean> = _autoStartServer.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.isLoggedIn.collect { loggedIn ->
                _isLoggedIn.value = loggedIn
            }
        }
        viewModelScope.launch {
            preferencesManager.autoAcceptQueue.collect { autoAccept ->
                _autoAcceptQueue.value = autoAccept
            }
        }
        viewModelScope.launch {
            preferencesManager.autoStartServer.collect { autoStart ->
                _autoStartServer.value = autoStart
                if (autoStart) {
                    scheduleServerCheck()
                } else {
                    cancelServerCheck()
                }
            }
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLoggedIn(loggedIn)
            if (!loggedIn) {
                cancelServerCheck()
            }
        }
    }

    fun startServer() {
        viewModelScope.launch {
            _serverState.value = _serverState.value.copy(isLoading = true)
            try {
                Thread.sleep(1000)
                _serverState.value = _serverState.value.copy(
                    isOnline = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _serverState.value = _serverState.value.copy(isLoading = false)
            }
        }
    }

    fun stopServer() {
        viewModelScope.launch {
            _serverState.value = _serverState.value.copy(isLoading = true)
            try {
                Thread.sleep(1000)
                _serverState.value = ServerState(isOnline = false, isLoading = false)
            } catch (e: Exception) {
                _serverState.value = _serverState.value.copy(isLoading = false)
            }
        }
    }

    fun acceptQueue() {
        viewModelScope.launch {
            _serverState.value = _serverState.value.copy(
                queuePosition = null,
                isInQueue = false
            )
        }
    }

    fun updateServerState(online: Boolean, players: Int, queue: Int? = null) {
        _serverState.value = _serverState.value.copy(
            isOnline = online,
            playersOnline = players,
            queuePosition = queue,
            isInQueue = queue != null
        )
    }

    fun setAutoAcceptQueue(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoAcceptQueue(enabled)
        }
    }

    fun setAutoStartServer(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoStartServer(enabled)
        }
    }

    private fun scheduleServerCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ServerCheckWorker>(
            3, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "server_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cancelServerCheck() {
        workManager.cancelUniqueWork("server_check")
    }
}
