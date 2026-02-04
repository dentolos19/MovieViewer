package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val profilePicturePath: String? = null,
    val preferredName: String = "",
    val preferredNameError: String? = null,
    val dateOfBirth: Long? = null,
    val dateOfBirthError: String? = null,
    val showDatePicker: Boolean = false,
    val saveSuccess: Boolean = false,
)

