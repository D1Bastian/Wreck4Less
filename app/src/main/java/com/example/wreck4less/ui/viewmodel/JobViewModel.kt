package com.example.wreck4less.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wreck4less.data.repository.JobRepository
import com.example.wreck4less.data.repository.TowRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobViewModel(private val repository: JobRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<String>("Idle")
    val uiState: StateFlow<String> = _uiState

    fun requestTow(customerId: String, lat: Double, lng: Double, address: String) {
        viewModelScope.launch {
            _uiState.value = "Requesting..."
            try {
                val request = TowRequest(customer_id = customerId, lat = lat, lng = lng, address = address)
                val response = repository.createJob(request)
                if (response.isSuccessful) {
                    _uiState.value = "Searching for Drivers..."
                } else {
                    _uiState.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _uiState.value = "Network Failure"
            }
        }
    }
}