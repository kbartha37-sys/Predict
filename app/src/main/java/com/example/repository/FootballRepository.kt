package com.example.repository

import androidx.compose.ui.graphics.Color
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

object FootballRepository {

    // --- Static Leagues ---
    val leagues = listOf(
        League("CL", "Champions League", "Europe", "★", Color(0xFFFFD700)),
        League("EPL", "Premier League", "England", "🦁", Color(0xFF3F1052)),
        League("LALIGA", "La Liga", "Spain", "🇪🇸", Color(0xFFEE242C)),
        League("BUNDESLIGA", "Bundesliga", "Germany", "🇩🇪", Color(0xFFD3010F)),
        League("SERIEA", "Serie A", "Italy", "🇮🇹", Color(0xFF00529B))
    )

    // --- Static Teams by League ---
    private val teamsMap = mapOf(
        "CL" to listOf(
            Team("rma", "Real Madrid", "RMA", Color(0xFFF0F2F5), Color(0xFF4A148C), listOf("W", "W", "D", "W", "W")),
            Team("mci", "Manchester City", "MCI", Color(0xFF81D4FA), Color(0xFF01579B), listOf("W", "L", "W", "W", "D")),
            Team("bay", "Bayern München", "FCB", Color(0xFFD32F2F), Color(0xFFFFFFFF), listOf("D", "W", "W", "L", "W")),
            Team("psg", "Paris Saint-Germain", "PSG", Color(0xFF0D47A1), Color(0xFFB71C1C), listOf("W", "W", "D", "D", "W"))
        ),
        "EPL" to listOf(
            Team("ars", "Arsenal", "ARS", Color(0xFFE53935), Color(0xFFECEFF1), listOf("W", "W", "L", "W", "W")),
            Team("liv", "Liverpool", "LIV", Color(0xFFB71C1C), Color(0xFFFBC02D), listOf("W", "D", "W", "W", "W")),
            Team("mun", "Manchester United", "MUN", Color(0xFFD50000), Color(0xFF000000), listOf("L", "W", "D", "L", "W")),
            Team("che", "Chelsea", "CHE", Color(0xFF2962FF), Color(0xFFFFFFFF), listOf("W", "D", "W", "L", "L"))
        ),
        "LALIGA" to listOf(
            Team("bar", "Barcelona", "FCB", Color(0xFF004D40), Color(0xFF880E4F), listOf("W", "W", "W", "W", "L")),
            Team("atm", "Atlético Madrid", "ATM", Color(0xFFD32F2F), Color(0xFF0D47A1), listOf("W", "D", "L", "W", "W")),
            Team("rso", "Real Sociedad", "RSO", Color(0xFF1976D2), Color(0xFFFFFFFF), listOf("D", "L", "W", "W", "D")),
            Team("sev", "Sevilla FC", "SEV", Color(0xFFC62828), Color(0xFFFFFFFF), listOf("L", "D", "L", "W", "L"))
        ),
        "BUNDESLIGA" to listOf(
            Team("bvb", "Borussia Dortmund", "BVB", Color(0xFFFFEB3B), Color(0xFF000000), listOf("W", "D", "W", "W", "L")),
            Team("b04", "Bayer Leverkusen", "B04", Color(0xFFB71C1C), Color(0xFF000000), listOf("W", "W", "W", "D", "W")),
            Team("rbl", "RB Leipzig", "RBL", Color(0xFF0D47A1), Color(0xFFD32F2F), listOf("W", "L", "D", "W", "W")),
            Team("sge", "Eintracht Frankfurt", "SGE", Color(0xFF212121), Color(0xFFD32F2F), listOf("D", "W", "L", "W", "D"))
        ),
        "SERIEA" to listOf(
            Team("int", "Inter Milan", "INT", Color(0xFF0D47A1), Color(0xFF000000), listOf("W", "W", "W", "D", "W")),
            Team("juv", "Juventus", "JUV", Color(0xFF212121), Color(0xFFFFFFFF), listOf("D", "W", "D", "W", "W")),
            Team("mil", "AC Milan", "MIL", Color(0xFFC62828), Color(0xFF000000), listOf("L", "W", "W", "L", "W")),
            Team("nap", "Napoli", "NAP", Color(0xFF03A9F4), Color(0xFFFFFFFF), listOf("W", "W", "L", "W", "D"))
        )
    )

    // Get all teams in the system
    fun getTeamsForLeague(leagueId: String): List<Team> {
        return teamsMap[leagueId] ?: emptyList()
    }

    // --- State-managed Matches Flow ---
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    // --- Standings Map ---
    private val _standings = MutableStateFlow<Map<String, List<StandingItem>>>(emptyMap())
    val standings: StateFlow<Map<String, List<StandingItem>>> = _standings.asStateFlow()

    // --- Top Scorers (static details) ---
    val topScorers = listOf(
        PlayerStats("Erling Haaland", "Manchester City", 27, 5, 29),
        PlayerStats("Kylian Mbappé", "Real Madrid", 22, 7, 28),
        PlayerStats("Robert Lewandowski", "Barcelona", 20, 4, 30),
        PlayerStats("Harry Kane", "Bayern München", 31, 8, 31),
        PlayerStats("Mohamed Salah", "Liverpool", 19, 9, 27),
        PlayerStats("Bukayo Saka", "Arsenal", 16, 12, 29),
        PlayerStats("Lautaro Martínez", "Inter Milan", 24, 4, 30),
        PlayerStats("Jude Bellingham", "Real Madrid", 15, 10, 26)
    )

    init {
        generateInitialData()
    }

    private fun generateInitialData() {
        val initialMatches = mutableListOf<Match>()

        // 1. Live Champions League Match (Real Madrid vs Man City)
        val rma = teamsMap["CL"]!![0]
        val mci = teamsMap["CL"]!![1]
        initialMatches.add(
            Match(
                id = "live_cl_1",
                leagueId = "CL",
                homeTeam = rma,
                awayTeam = mci,
                homeScore = 2,
                awayScore = 2,
                minute = 65,
                status = MatchStatus.LIVE,
                events = listOf(
                    MatchEvent(type = EventType.GOAL, minute = 12, isHomeTeam = true, primaryPlayer = "V. Júnior", secondaryPlayer = "J. Bellingham"),
                    MatchEvent(type = EventType.GOAL, minute = 28, isHomeTeam = false, primaryPlayer = "E. Haaland", secondaryPlayer = "K. De Bruyne"),
                    MatchEvent(type = EventType.YELLOW_CARD, minute = 35, isHomeTeam = true, primaryPlayer = "A. Rüdiger"),
                    MatchEvent(type = EventType.GOAL, minute = 49, isHomeTeam = false, primaryPlayer = "P. Foden"),
                    MatchEvent(type = EventType.GOAL, minute = 58, isHomeTeam = true, primaryPlayer = "K. Mbappé", secondaryPlayer = "F. Valverde")
                ),
                stats = MatchStats(
                    ballPossessionHome = 48,
                    ballPossessionAway = 52,
                    shotsHome = 12,
                    shotsAway = 14,
                    shotsOnTargetHome = 6,
                    shotsOnTargetAway = 7,
                    foulsHome = 8,
                    foulsAway = 9,
                    cornersHome = 4,
                    cornersAway = 6,
                    yellowCardsHome = 1,
                    yellowCardsAway = 0,
                    redCardsHome = 0,
                    redCardsAway = 0
                )
            )
        )

        // 2. Live Premier League Match (Arsenal vs Chelsea)
        val ars = teamsMap["EPL"]!![0]
        val che = teamsMap["EPL"]!![3]
        initialMatches.add(
            Match(
                id = "live_epl_1",
                leagueId = "EPL",
                homeTeam = ars,
                awayTeam = che,
                homeScore = 1,
                awayScore = 0,
                minute = 40,
                status = MatchStatus.LIVE,
                events = listOf(
                    MatchEvent(type = EventType.GOAL, minute = 22, isHomeTeam = true, primaryPlayer = "B. Saka", secondaryPlayer = "M. Ødegaard"),
                    MatchEvent(type = EventType.YELLOW_CARD, minute = 31, isHomeTeam = false, primaryPlayer = "Moises Caicedo")
                ),
                stats = MatchStats(
                    ballPossessionHome = 58,
                    ballPossessionAway = 42,
                    shotsHome = 9,
                    shotsAway = 3,
                    shotsOnTargetHome = 4,
                    shotsOnTargetAway = 1,
                    foulsHome = 5,
                    foulsAway = 11,
                    cornersHome = 5,
                    cornersAway = 2,
                    yellowCardsHome = 0,
                    yellowCardsAway = 1,
                    redCardsHome = 0,
                    redCardsAway = 0
                )
            )
        )

        // 3. Live La Liga Match (Sociedad vs Sevilla)
        val rso = teamsMap["LALIGA"]!![2]
        val sev = teamsMap["LALIGA"]!![3]
        initialMatches.add(
            Match(
                id = "live_la_1",
                leagueId = "LALIGA",
                homeTeam = rso,
                awayTeam = sev,
                homeScore = 0,
                awayScore = 1,
                minute = 82,
                status = MatchStatus.LIVE,
                events = listOf(
                    MatchEvent(type = EventType.GOAL, minute = 24, isHomeTeam = false, primaryPlayer = "I. Romero"),
                    MatchEvent(type = EventType.YELLOW_CARD, minute = 44, isHomeTeam = true, primaryPlayer = "M. Zubimendi"),
                    MatchEvent(type = EventType.RED_CARD, minute = 71, isHomeTeam = true, primaryPlayer = "A. Elustondo")
                ),
                stats = MatchStats(
                    ballPossessionHome = 45,
                    ballPossessionAway = 55,
                    shotsHome = 6,
                    shotsAway = 11,
                    shotsOnTargetHome = 2,
                    shotsOnTargetAway = 5,
                    foulsHome = 14,
                    foulsAway = 12,
                    cornersHome = 3,
                    cornersAway = 7,
                    yellowCardsHome = 1,
                    yellowCardsAway = 2,
                    redCardsHome = 1,
                    redCardsAway = 0
                )
            )
        )

        // 4. Upcoming Bundesliga match (Bayern vs Dortmund)
        val bay = teamsMap["CL"]!![2]
        val bvb = teamsMap["BUNDESLIGA"]!![0]
        initialMatches.add(
            Match(
                id = "up_bun_1",
                leagueId = "BUNDESLIGA",
                homeTeam = bay,
                awayTeam = bvb,
                homeScore = 0,
                awayScore = 0,
                minute = 0,
                status = MatchStatus.UPCOMING,
                events = emptyList(),
                stats = MatchStats.default(),
                scheduledTime = "18:30"
            )
        )

        // 5. Featured Upcoming Serie A match (Inter vs Juventus)
        val inter = teamsMap["SERIEA"]!![0]
        val juv = teamsMap["SERIEA"]!![1]
        initialMatches.add(
            Match(
                id = "up_it_1",
                leagueId = "SERIEA",
                homeTeam = inter,
                awayTeam = juv,
                homeScore = 0,
                awayScore = 0,
                minute = 0,
                status = MatchStatus.UPCOMING,
                events = emptyList(),
                stats = MatchStats.default(),
                scheduledTime = "20:45"
            )
        )

        // 6. Finished EPL match (Liverpool vs Man United)
        val liv = teamsMap["EPL"]!![1]
        val mun = teamsMap["EPL"]!![2]
        initialMatches.add(
            Match(
                id = "fin_epl_1",
                leagueId = "EPL",
                homeTeam = liv,
                awayTeam = mun,
                homeScore = 3,
                awayScore = 1,
                minute = 90,
                status = MatchStatus.FINISHED,
                events = listOf(
                    MatchEvent(type = EventType.GOAL, minute = 15, isHomeTeam = true, primaryPlayer = "M. Salah", secondaryPlayer = "L. Díaz"),
                    MatchEvent(type = EventType.GOAL, minute = 42, isHomeTeam = false, primaryPlayer = "B. Fernandes"),
                    MatchEvent(type = EventType.GOAL, minute = 63, isHomeTeam = true, primaryPlayer = "C. Gakpo", secondaryPlayer = "A. Mac Allister"),
                    MatchEvent(type = EventType.YELLOW_CARD, minute = 75, isHomeTeam = false, primaryPlayer = "L. Martínez"),
                    MatchEvent(type = EventType.GOAL, minute = 88, isHomeTeam = true, primaryPlayer = "D. Núñez", secondaryPlayer = "M. Salah")
                ),
                stats = MatchStats(
                    ballPossessionHome = 61,
                    ballPossessionAway = 39,
                    shotsHome = 19,
                    shotsAway = 8,
                    shotsOnTargetHome = 8,
                    shotsOnTargetAway = 3,
                    foulsHome = 10,
                    foulsAway = 13,
                    cornersHome = 8,
                    cornersAway = 4,
                    yellowCardsHome = 1,
                    yellowCardsAway = 3,
                    redCardsHome = 0,
                    redCardsAway = 0
                )
            )
        )

        _matches.value = initialMatches

        // Generate Standing tables
        val standingsMap = mutableMapOf<String, List<StandingItem>>()
        leagues.forEach { league ->
            val teams = teamsMap[league.id] ?: emptyList()
            var pos = 1
            val items = teams.map { team ->
                val w = Random.nextInt(15, 25)
                val d = Random.nextInt(4, 9)
                val l = Random.nextInt(2, 8)
                val gf = w * 2 + d + Random.nextInt(5, 15)
                val ga = l * 2 + d + Random.nextInt(2, 10)
                StandingItem(
                    position = pos++,
                    team = team,
                    played = w + d + l,
                    won = w,
                    drawn = d,
                    lost = l,
                    goalsFor = gf,
                    goalsAgainst = ga,
                    points = w * 3 + d
                )
            }.sortedByDescending { it.points }
            
            // Re-assign positions based on sorted order
            standingsMap[league.id] = items.mapIndexed { index, item ->
                item.copy(position = index + 1)
            }
        }
        _standings.value = standingsMap
    }

    /**
     * Ticks matches forward in time, randomly simulating actual match events in real-time.
     */
    fun tickSimulation() {
        val currentMatches = _matches.value.map { match ->
            if (match.status != MatchStatus.LIVE) return@map match

            val nextMinute = match.minute + 1
            if (nextMinute > 90) {
                // End match
                match.copy(minute = 90, status = MatchStatus.FINISHED)
            } else {
                // Trigger casual live events (goals, cards, stats changes)
                val roll = Random.nextInt(100)
                val isHome = Random.nextBoolean()
                val newEvents = match.events.toMutableList()
                var homeScore = match.homeScore
                var awayScore = match.awayScore
                var homeStats = match.stats

                when {
                    // 1. Goal scored (3% chance)
                    roll < 3 -> {
                        val scorer = if (isHome) {
                            homeScore++
                            getRandomPlayerName(match.homeTeam.id)
                        } else {
                            awayScore++
                            getRandomPlayerName(match.awayTeam.id)
                        }
                        
                        val assister = if (Random.nextBoolean()) getRandomPlayerName(if (isHome) match.homeTeam.id else match.awayTeam.id) else null
                        
                        newEvents.add(
                            MatchEvent(
                                type = EventType.GOAL,
                                minute = nextMinute,
                                isHomeTeam = isHome,
                                primaryPlayer = scorer,
                                secondaryPlayer = assister
                            )
                        )
                        
                        // Increment shots
                        homeStats = if (isHome) {
                            homeStats.copy(
                                shotsHome = homeStats.shotsHome + 1,
                                shotsOnTargetHome = homeStats.shotsOnTargetHome + 1
                            )
                        } else {
                            homeStats.copy(
                                shotsAway = homeStats.shotsAway + 1,
                                shotsOnTargetAway = homeStats.shotsOnTargetAway + 1
                            )
                        }
                    }

                    // 2. Yellow Card bookings (4% chance)
                    roll in 3..6 -> {
                        val player = getRandomPlayerName(if (isHome) match.homeTeam.id else match.awayTeam.id)
                        newEvents.add(
                            MatchEvent(
                                type = EventType.YELLOW_CARD,
                                minute = nextMinute,
                                isHomeTeam = isHome,
                                primaryPlayer = player
                            )
                        )
                        
                        homeStats = if (isHome) {
                            homeStats.copy(yellowCardsHome = homeStats.yellowCardsHome + 1)
                        } else {
                            homeStats.copy(yellowCardsAway = homeStats.yellowCardsAway + 1)
                        }
                    }

                    // 3. Red Card eject (0.5% chance)
                    roll == 7 -> {
                        val player = getRandomPlayerName(if (isHome) match.homeTeam.id else match.awayTeam.id)
                        newEvents.add(
                            MatchEvent(
                                type = EventType.RED_CARD,
                                minute = nextMinute,
                                isHomeTeam = isHome,
                                primaryPlayer = player
                            )
                        )
                        
                        homeStats = if (isHome) {
                            homeStats.copy(redCardsHome = homeStats.redCardsHome + 1)
                        } else {
                            homeStats.copy(redCardsAway = homeStats.redCardsAway + 1)
                        }
                    }

                    // 4. Player Substitution (5% chance)
                    roll in 8..12 -> {
                        val teamId = if (isHome) match.homeTeam.id else match.awayTeam.id
                        val playerOut = getRandomPlayerName(teamId)
                        val playerIn = getRandomPlayerName(teamId)
                        newEvents.add(
                            MatchEvent(
                                type = EventType.SUBSTITUTION,
                                minute = nextMinute,
                                isHomeTeam = isHome,
                                primaryPlayer = playerIn,
                                secondaryPlayer = playerOut
                            )
                        )
                    }
                }

                // Randomize minor shot statistics & alternating ball possessions
                val ballShift = Random.nextInt(-3, 4)
                val homePoss = (homeStats.ballPossessionHome + ballShift).coerceIn(30, 70)
                val awayPoss = 100 - homePoss

                val addShot = Random.nextInt(100) < 15
                val addCorner = Random.nextInt(100) < 10
                val addFoul = Random.nextInt(100) < 18

                homeStats = homeStats.copy(
                    ballPossessionHome = homePoss,
                    ballPossessionAway = awayPoss,
                    shotsHome = homeStats.shotsHome + if (addShot && isHome) 1 else 0,
                    shotsAway = homeStats.shotsAway + if (addShot && !isHome) 1 else 0,
                    shotsOnTargetHome = homeStats.shotsOnTargetHome + if (addShot && isHome && Random.nextBoolean()) 1 else 0,
                    shotsOnTargetAway = homeStats.shotsOnTargetAway + if (addShot && !isHome && Random.nextBoolean()) 1 else 0,
                    cornersHome = homeStats.cornersHome + if (addCorner && isHome) 1 else 0,
                    cornersAway = homeStats.cornersAway + if (addCorner && !isHome) 1 else 0,
                    foulsHome = homeStats.foulsHome + if (addFoul && isHome) 1 else 0,
                    foulsAway = homeStats.foulsAway + if (addFoul && !isHome) 1 else 0
                )

                match.copy(
                    minute = nextMinute,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    events = newEvents.sortedByDescending { it.minute },
                    stats = homeStats
                )
            }
        }
        _matches.value = currentMatches
    }

    /**
     * Users can interactively inject events to run as Live Match Analyst
     */
    fun injectCustomEvent(matchId: String, eventType: EventType, isHome: Boolean, playerName: String) {
        val current = _matches.value.map { match ->
            if (match.id != matchId) return@map match

            val newEvents = match.events.toMutableList()
            var homeScore = match.homeScore
            var awayScore = match.awayScore
            var homeStats = match.stats
            val currentMin = if (match.minute == 0) 1 else match.minute

            when (eventType) {
                EventType.GOAL -> {
                    if (isHome) homeScore++ else awayScore++
                    newEvents.add(
                        MatchEvent(
                            type = EventType.GOAL,
                            minute = currentMin,
                            isHomeTeam = isHome,
                            primaryPlayer = playerName,
                            secondaryPlayer = "Custom Event"
                        )
                    )
                    homeStats = if (isHome) {
                        homeStats.copy(
                            shotsHome = homeStats.shotsHome + 1,
                            shotsOnTargetHome = homeStats.shotsOnTargetHome + 1
                        )
                    } else {
                        homeStats.copy(
                            shotsAway = homeStats.shotsAway + 1,
                            shotsOnTargetAway = homeStats.shotsOnTargetAway + 1
                        )
                    }
                }
                EventType.YELLOW_CARD -> {
                    newEvents.add(
                        MatchEvent(
                            type = EventType.YELLOW_CARD,
                            minute = currentMin,
                            isHomeTeam = isHome,
                            primaryPlayer = playerName
                        )
                    )
                    homeStats = if (isHome) {
                        homeStats.copy(yellowCardsHome = homeStats.yellowCardsHome + 1)
                    } else {
                        homeStats.copy(yellowCardsAway = homeStats.yellowCardsAway + 1)
                    }
                }
                EventType.RED_CARD -> {
                    newEvents.add(
                        MatchEvent(
                            type = EventType.RED_CARD,
                            minute = currentMin,
                            isHomeTeam = isHome,
                            primaryPlayer = playerName
                        )
                    )
                    homeStats = if (isHome) {
                        homeStats.copy(redCardsHome = homeStats.redCardsHome + 1)
                    } else {
                        homeStats.copy(redCardsAway = homeStats.redCardsAway + 1)
                    }
                }
                EventType.SUBSTITUTION -> {
                    newEvents.add(
                        MatchEvent(
                            type = EventType.SUBSTITUTION,
                            minute = currentMin,
                            isHomeTeam = isHome,
                            primaryPlayer = playerName,
                            secondaryPlayer = "Player Out"
                        )
                    )
                }
            }

            match.copy(
                homeScore = homeScore,
                awayScore = awayScore,
                events = newEvents.sortedByDescending { it.minute },
                stats = homeStats,
                // Automatically activate upcoming match if user triggers custom event on it
                status = if (match.status == MatchStatus.UPCOMING) MatchStatus.LIVE else match.status,
                minute = if (match.status == MatchStatus.UPCOMING) 1 else match.minute
            )
        }
        _matches.value = current
    }

    /**
     * Change match status (e.g. restart game, speeds it up, etc.)
     */
    fun resetMatches() {
        generateInitialData()
    }

    private fun getRandomPlayerName(teamId: String): String {
        return when (teamId) {
            "rma" -> listOf("Vinícius Jr", "Jude Bellingham", "Rodrygo", "Luka Modrić", "Aurélien Tchouaméni", "F. Valverde", "Dani Carvajal", "Eder Militão").random()
            "mci" -> listOf("E. Haaland", "Kevin De Bruyne", "Phil Foden", "Bernardo Silva", "Rodri", "Jack Grealish", "Kyle Walker", "Ruben Dias").random()
            "bay" -> listOf("Harry Kane", "Leroy Sané", "Jamal Musiala", "Thomas Müller", "Joshua Kimmich", "Leon Goretzka", "Alphonso Davies").random()
            "psg" -> listOf("O. Dembélé", "Randal Kolo Muani", "Vitinha", "Warren Zaïre-Emery", "Achraf Hakimi", "Marquinhos", "Bradley Barcola").random()
            "ars" -> listOf("Bukayo Saka", "Martin Ødegaard", "Gabriel Martinelli", "Kai Havertz", "Declan Rice", "Gabriel Jesus", "William Saliba").random()
            "liv" -> listOf("Mohamed Salah", "Luis Díaz", "Darwin Núñez", "Alexis Mac Allister", "Dominik Szoboszlai", "Virgil van Dijk").random()
            "mun" -> listOf("Bruno Fernandes", "Marcus Rashford", "Alejandro Garnacho", "Rasmus Højlund", "Casemiro", "Kobbie Mainoo").random()
            "che" -> listOf("Cole Palmer", "Nicolas Jackson", "Enzo Fernández", "Noni Madueke", "Raheem Sterling", "Reece James").random()
            "bar" -> listOf("R. Lewandowski", "Lamine Yamal", "Pedri", "Gavi", "Frenkie de Jong", "Raphinha", "Ronald Araújo", "Ilkay Gündogan").random()
            "atm" -> listOf("Antoine Griezmann", "Alvaro Morata", "Koke", "Rodrigo De Paul", "Marcos Llorente", "Jan Oblak").random()
            "juv" -> listOf("Dushan Vlahović", "Federico Chiesa", "Manuel Locatelli", "Adrien Rabiot", "Gleison Bremer", "Danilo").random()
            "int" -> listOf("Lautaro Martínez", "Marcus Thuram", "Hakan Çalhanoğlu", "Nicolò Barella", "Federico Dimarco", "Alessandro Bastoni").random()
            else -> listOf("M. Kovács", "P. Szabó", "L. Nagy", "J. Németh", "Z. Tóth", "A. Szilágyi", "G. Varga").random()
        }
    }
}
