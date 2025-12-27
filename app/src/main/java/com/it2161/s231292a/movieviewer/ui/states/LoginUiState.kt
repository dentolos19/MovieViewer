package com.it2161.s231292a.movieviewer.ui.states

data class LoginUiState(
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordVisible: Boolean = false,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)
