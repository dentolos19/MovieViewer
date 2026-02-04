package com.it2161.s231292a.movieviewer.ui.states

data class LoginUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val passwordVisible: Boolean = false,
)
