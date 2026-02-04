package com.it2161.s231292a.movieviewer.ui.states

data class RegisterUiState(
    val isLoading: Boolean = false,
    val profilePicturePath: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val preferredName: String = "",
    val preferredNameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val passwordVisible: Boolean = false,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val confirmPasswordVisible: Boolean = false,
    val dateOfBirth: Long? = null,
    val dateOfBirthError: String? = null,
    val showDatePicker: Boolean = false
)

