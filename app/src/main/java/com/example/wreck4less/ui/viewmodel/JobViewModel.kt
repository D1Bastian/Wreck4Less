package com.example.wreck4less.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wreck4less.data.model.WreckIntel
import com.example.wreck4less.data.model.WreckSubmission
import com.example.wreck4less.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val data: com.example.wreck4less.data.model.WreckResponse) : UiState()
    data class Error(val message: String) : UiState()
}

class JobViewModel : ViewModel() {

    private val repository = JobRepository()

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun submitWreck(
        make: String,
        model: String,
        year: String,
        damage: String,
        location: String
    ) {
        viewModelScope.launch {

            _state.value = UiState.Loading

            try {

                val submission = WreckSubmission(
                    intel = WreckIntel(
                        make = make,
                        model = model,
                        year = year,
                        damage_description = damage,
                        image_keys = emptyList(),
                        location_label = location
                    )
                )

                val response = repository.submitWreck(submission)

                if (response.isSuccessful && response.body() != null) {
                    _state.value = UiState.Success(response.body()!!)
                } else {
                    _state.value = UiState.Error("Server error: ${response.code()}")
                }

            } catch (e: Exception) {
                _state.value = UiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}
