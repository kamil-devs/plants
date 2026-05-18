package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.repository.PruningGuideRepository
import com.example.pruningapp.repository.PruningGuideResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PruningGuideViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PruningGuideRepository(
        (application as App).database.pruningGuideCacheDao()
    )

    private val _state = MutableStateFlow<PruningGuideResult>(PruningGuideResult.Loading)
    val state: StateFlow<PruningGuideResult> = _state

    fun load(perenualId: Int) {
        viewModelScope.launch {
            _state.value = PruningGuideResult.Loading
            _state.value = repository.getPruningGuide(perenualId)
        }
    }
}
