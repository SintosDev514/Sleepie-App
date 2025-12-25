package com.example.sleepie.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SleepViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
