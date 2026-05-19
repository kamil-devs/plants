package com.example.pruningapp.ui.components

import com.example.pruningapp.repository.PruningGuideResult

fun PruningGuideResult.toUiState(): UiState<PruningGuideResult.Success> = when (this) {
    is PruningGuideResult.Loading -> UiState.Loading
    is PruningGuideResult.Error -> UiState.Error(message)
    is PruningGuideResult.NotFound -> UiState.Error("Brak danych dla tej rosliny")
    is PruningGuideResult.Success -> UiState.Success(this)
}
