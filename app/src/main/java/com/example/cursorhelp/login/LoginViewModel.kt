package com.example.cursorhelp.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 登录界面 UI 状态
 */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 登录界面 ViewModel。
 * 使用 StateFlow 暴露 [LoginUiState]，通过模拟的异步挂起函数执行登录。
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * 模拟异步登录：延迟 1.5 秒后，仅当用户名为非空且密码长度 >= 6 时成功。
     */
    suspend fun login(username: String, password: String): Boolean {
        delay(1500)
        return username.isNotBlank() && password.length >= 6
    }

    fun onUsernameChange(value: String) {
        _uiState.update {
            it.copy(username = value, errorMessage = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, errorMessage = null)
        }
    }

    /**
     * 执行登录：在 viewModelScope 中调用挂起函数并更新 StateFlow 状态。
     */
    fun performLogin() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, loginSuccess = false)
            }
            runCatching {
                login(_uiState.value.username, _uiState.value.password)
            }.onSuccess { success ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccess = success,
                        errorMessage = if (success) null else "用户名或密码无效（模拟：密码至少 6 位）"
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccess = false,
                        errorMessage = e.message ?: "登录失败"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
