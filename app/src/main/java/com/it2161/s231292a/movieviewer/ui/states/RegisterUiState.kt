package com.it2161.s231292a.movieviewer.ui.states

data class RegisterUiState(
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val preferredName: String = "",
    val preferredNameError: String? = null,
    val dateOfBirth: Long? = null,
    val dateOfBirthError: String? = null,
    val profilePicturePath: String? = null,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val showDatePicker: Boolean = false
)

