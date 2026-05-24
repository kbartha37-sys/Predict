package com.example.model

import androidx.compose.ui.graphics.Color

enum class MatchStatus {
    LIVE,
    UPCOMING,
    FINISHED
}

enum class EventType {
    GOAL,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION
}

data class Team(
    val id: String,
    val name: String,
    val shortCode: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val form: List<String> = listOf("W", "D", "W", "L", "W"), // Recent form (W, D, L)
    val goalsScored: Int = 0,
    val goalsConceded: Int = 0,
    val cleanSheets: Int = 0
)

data class StandingItem(
    val position: Int,
    val team: Team,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
)

data class League(
    val id: String,
    val name: String,
    val country: String,
    val logoChar: String,
    val logoColor: Color
)

data class MatchEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: EventType,
    val minute: Int,
    val isHomeTeam: Boolean,
    val primaryPlayer: String,
    val secondaryPlayer: String? = null // For assists or players replaced
)

data class MatchStats(
    val ballPossessionHome: Int,
    val ballPossessionAway: Int,
    val shotsHome: Int,
    val shotsAway: Int,
    val shotsOnTargetHome: Int,
    val shotsOnTargetAway: Int,
    val foulsHome: Int,
    val foulsAway: Int,
    val cornersHome: Int,
    val cornersAway: Int,
    val yellowCardsHome: Int,
    val yellowCardsAway: Int,
    val redCardsHome: Int,
    val redCardsAway: Int
) {
    companion object {
        fun default() = MatchStats(
            ballPossessionHome = 50,
            ballPossessionAway = 50,
            shotsHome = 0,
            shotsAway = 0,
            shotsOnTargetHome = 0,
            shotsOnTargetAway = 0,
            foulsHome = 0,
            foulsAway = 0,
            cornersHome = 0,
            cornersAway = 0,
            yellowCardsHome = 0,
            yellowCardsAway = 0,
            redCardsHome = 0,
            redCardsAway = 0
        )
    }
}

data class Match(
    val id: String,
    val leagueId: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int,
    val awayScore: Int,
    val minute: Int,
    val status: MatchStatus,
    val events: List<MatchEvent>,
    val stats: MatchStats,
    val scheduledTime: String = "19:45"
)

data class PlayerStats(
    val name: String,
    val teamName: String,
    val goals: Int,
    val assists: Int,
    val matchesPlayed: Int
)

data class AIAnalysisResult(
    val homeWinChance: Float, // 0 to 100
    val drawChance: Float,    // 0 to 100
    val awayWinChance: Float, // 0 to 100
    val predictedScore: String,
    val predictedHomeYellowCards: Int,
    val predictedAwayYellowCards: Int,
    val predictedHomeRedCards: Int,
    val predictedAwayRedCards: Int,
    val predictedHomeCorners: Int,
    val predictedAwayCorners: Int,
    val analystVerdictEn: String,
    val analystVerdictHu: String,
    val keyFactorEn: String,
    val keyFactorHu: String,
    val safetyStatus: String // "Safe", "Risky", etc.
)
