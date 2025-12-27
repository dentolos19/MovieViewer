package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.User

data class ProfileUiState(
    val user: User? = null,
    val preferredName: String = "",
    val preferredNameError: String? = null,
    val dateOfBirth: Long? = null,
    val dateOfBirthError: String? = null,
    val profilePicturePath: String? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val showDatePicker: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

