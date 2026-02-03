package com.it2161.s231292a.movieviewer.ui.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.Session
import com.it2161.s231292a.movieviewer.data.entities.User
import com.it2161.s231292a.movieviewer.data.repositories.UserRepository
import com.it2161.s231292a.movieviewer.ui.states.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, usernameError = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun updatePreferredName(name: String) {
        _uiState.update { it.copy(preferredName = name, preferredNameError = null) }
    }

    fun updateDateOfBirth(timestamp: Long) {
        _uiState.update { it.copy(dateOfBirth = timestamp, dateOfBirthError = null, showDatePicker = false) }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    fun setProfilePicturePath(path: String?) {
        _uiState.update { it.copy(profilePicturePath = path) }
    }

    fun saveProfilePicture(context: Context, imageBytes: ByteArray): String? {
        return try {
            val filename = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { output ->
                output.write(imageBytes)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "User ID is required") }
            isValid = false
        } else if (state.username.length < 3) {
            _uiState.update { it.copy(usernameError = "User ID must be at least 3 characters") }
            isValid = false
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        if (state.confirmPassword != state.password) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        if (state.preferredName.isBlank()) {
            _uiState.update { it.copy(preferredNameError = "Preferred name is required") }
            isValid = false
        }

        if (state.dateOfBirth == null) {
            _uiState.update { it.copy(dateOfBirthError = "Date of birth is required") }
            isValid = false
        }

        return isValid
    }

    fun register(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            val user = User(
                username = state.username,
                password = state.password,
                preferredName = state.preferredName,
                dateOfBirth = state.dateOfBirth!!,
                profilePicturePath = state.profilePicturePath
            )

            val result = userRepository.registerUser(user)

            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    // Auto-login after registration
                    val loginResult = userRepository.login(state.username, state.password)
                    loginResult.fold(
                        onSuccess = { loggedInUser ->
                            Session.login(loggedInUser)
                            onSuccess()
                        },
                        onFailure = {
                            onSuccess() // Still redirect to login
                        }
                    )
                },
                onFailure = { exception ->
                    onError(exception.message ?: "Registration failed")
                }
            )
        }
    }

    companion object {
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(userRepository) as T
                }
            }
        }
    }
}
