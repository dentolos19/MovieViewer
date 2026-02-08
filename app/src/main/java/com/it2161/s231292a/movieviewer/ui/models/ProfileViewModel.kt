package com.it2161.s231292a.movieviewer.ui.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.Session
import com.it2161.s231292a.movieviewer.data.repositories.UserRepository
import com.it2161.s231292a.movieviewer.ui.states.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val user = Session.getCurrentUser()
            if (user != null) {
                _uiState.update {
                    it.copy(
                        user = user,
                        preferredName = user.preferredName,
                        dateOfBirth = user.dateOfBirth,
                        profilePicturePath = user.profilePicturePath,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No user logged in"
                    )
                }
            }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun cancelEditing() {
        val user = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditing = false,
                preferredName = user?.preferredName ?: "",
                dateOfBirth = user?.dateOfBirth,
                profilePicturePath = user?.profilePicturePath,
                preferredNameError = null,
                dateOfBirthError = null
            )
        }
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

    fun saveProfile(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = _uiState.value.user
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "No user found") }
                onError("No user found")
                return@launch
            }

            val state = _uiState.value
            val updatedUser = currentUser.copy(
                preferredName = state.preferredName,
                dateOfBirth = state.dateOfBirth!!,
                profilePicturePath = state.profilePicturePath
            )

            val result = userRepository.updateUser(updatedUser)

            result.fold(
                onSuccess = {
                    Session.login(updatedUser) // Update session
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            isLoading = false,
                            isEditing = false,
                            saveSuccess = true
                        )
                    }
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                    onError(exception.message ?: "Failed to save profile")
                }
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    companion object {
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(userRepository) as T
                }
            }
        }
    }
}
