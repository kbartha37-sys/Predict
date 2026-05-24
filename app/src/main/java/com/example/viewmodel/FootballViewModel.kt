package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.network.GeminiApiClient
import com.example.repository.FootballRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AIAnalysisUiState {
    object Idle : AIAnalysisUiState
    object Loading : AIAnalysisUiState
    data class Success(val result: AIAnalysisResult) : AIAnalysisUiState
    data class Error(val messageEn: String, val messageHu: String) : AIAnalysisUiState
}

class FootballViewModel : ViewModel() {

    // --- Tab state ---
    private val _currentTab = MutableStateFlow(0) // 0: Matches, 1: Standings, 2: Top Scorers, 3: AI Predictor Lounge
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // --- Selected League ---
    private val _selectedLeagueId = MutableStateFlow("CL")
    val selectedLeagueId: StateFlow<String> = _selectedLeagueId.asStateFlow()

    // --- Selected Match in details sheet ---
    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    // --- AI Prediction state ---
    private val _aiAnalysisState = MutableStateFlow<AIAnalysisUiState>(AIAnalysisUiState.Idle)
    val aiAnalysisState: StateFlow<AIAnalysisUiState> = _aiAnalysisState.asStateFlow()

    // --- Language Selection ---
    private val _isHungarian = MutableStateFlow(true) // true: HU, false: EN
    val isHungarian: StateFlow<Boolean> = _isHungarian.asStateFlow()

    // --- Live ticker simulation toggle ---
    private val _isSimulationRunning = MutableStateFlow(true)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    // --- Hot-updating repository values ---
    val matches: StateFlow<List<Match>> = FootballRepository.matches
    val standings: StateFlow<Map<String, List<StandingItem>>> = FootballRepository.standings
    val topScorers = FootballRepository.topScorers
    val leagues = FootballRepository.leagues

    private var simulationJob: Job? = null

    init {
        startSimulation()
        
        // Match selection sync: make sure if the currently selected match is updated in repository, it updates in detail pane
        viewModelScope.launch {
            matches.collect { refreshedMatches ->
                _selectedMatch.value?.let { current ->
                    refreshedMatches.find { it.id == current.id }?.let { updated ->
                        _selectedMatch.value = updated
                    }
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    fun selectLeague(leagueId: String) {
        _selectedLeagueId.value = leagueId
    }

    fun selectMatch(match: Match?) {
        _selectedMatch.value = match
        _aiAnalysisState.value = AIAnalysisUiState.Idle
    }

    fun toggleLanguage() {
        _isHungarian.value = !_isHungarian.value
    }

    fun toggleSimulation() {
        _isSimulationRunning.value = !_isSimulationRunning.value
        if (_isSimulationRunning.value) {
            startSimulation()
        } else {
            simulationJob?.cancel()
        }
    }

    fun resetMatches() {
        FootballRepository.resetMatches()
        _selectedMatch.value = null
        _aiAnalysisState.value = AIAnalysisUiState.Idle
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(7000) // tick simulation minute every 7 seconds
                FootballRepository.tickSimulation()
            }
        }
    }

    fun triggerInteractiveEvent(eventType: EventType, isHome: Boolean, player: String) {
        val current = _selectedMatch.value ?: return
        FootballRepository.injectCustomEvent(current.id, eventType, isHome, player)
        // Automatically re-trigger prediction locally to show instant reactivity
        viewModelScope.launch {
            // Give a short delay to let repository update apply
            delay(150)
            _selectedMatch.value?.let { updated ->
                analyzeMatchState(updated)
            }
        }
    }

    fun requestAiPrediction() {
        val currentMatch = _selectedMatch.value ?: return
        viewModelScope.launch {
            _aiAnalysisState.value = AIAnalysisUiState.Loading
            try {
                val result = GeminiApiClient.analyzeMatch(currentMatch)
                _aiAnalysisState.value = AIAnalysisUiState.Success(result)
            } catch (e: Exception) {
                _aiAnalysisState.value = AIAnalysisUiState.Error(
                    messageEn = "AI Analysis failed to resolve: ${e.message}",
                    messageHu = "Sikertelen AI elemzés: ${e.message}"
                )
            }
        }
    }

    private fun analyzeMatchState(match: Match) {
        // Automatically updates local math projection in real-time when custom events fire,
        // unless they click the explicit Gemini button for live detailed tactical briefing.
        if (_aiAnalysisState.value is AIAnalysisUiState.Success) {
            val local = GeminiApiClient.calculateLocalAnalysis(match)
            _aiAnalysisState.value = AIAnalysisUiState.Success(local)
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
