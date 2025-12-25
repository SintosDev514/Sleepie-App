package com.example.sleepie.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.data.db.SleepieDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SleepViewModel(application: Application) : AndroidViewModel(application) {

    private val sleepSessionDao = SleepieDatabase.getDatabase(application).sleepSessionDao()

    val allSleepSessions: Flow<List<SleepSession>> = sleepSessionDao.getAllSleepSessions()

    fun insertSleepSession(session: SleepSession) {
        viewModelScope.launch {
            sleepSessionDao.insertSleepSession(session)
        }
    }
}
