package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.model.AIAnalysisResult
import com.example.model.EventType
import com.example.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if there's a valid non-placeholder Gemini API key configured.
     */
    fun isApiKeyAvailable(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    /**
     * Standard local mathematical formula to calculate probabilities and verdicts,
     * as a reliable fallback which also validates sports statistics beautifully.
     */
    fun calculateLocalAnalysis(match: com.example.model.Match): AIAnalysisResult {
        val minute = match.minute
        val homeScore = match.homeScore
        val awayScore = match.awayScore
        
        // Base weights: Home team usually has ~10% home advantage
        var homeWeight = 42f
        var drawWeight = 28f
        var awayWeight = 30f

        // 1. Incorporate Form
        val homeWins = match.homeTeam.form.count { it == "W" }
        val awayWins = match.awayTeam.form.count { it == "W" }
        homeWeight += (homeWins - awayWins) * 2.5f
        awayWeight += (awayWins - homeWins) * 2.5f

        // 2. Incorporate Shots and Possession
        val homeSOnT = match.stats.shotsOnTargetHome
        val awaySOnT = match.stats.shotsOnTargetAway
        homeWeight += (homeSOnT - awaySOnT) * 1.5f
        awayWeight += (awaySOnT - homeSOnT) * 1.5f

        // 3. Incorporate Red Cards (highly penalizing)
        homeWeight -= match.stats.redCardsHome * 12f
        awayWeight -= match.stats.redCardsAway * 12f

        // 4. Incorporate Current Score and Minute
        val goalDiff = homeScore - awayScore
        if (match.status == com.example.model.MatchStatus.FINISHED) {
            if (goalDiff > 0) {
                homeWeight = 100f; drawWeight = 0f; awayWeight = 0f
            } else if (goalDiff < 0) {
                homeWeight = 0f; drawWeight = 0f; awayWeight = 100f
            } else {
                homeWeight = 0f; drawWeight = 100f; awayWeight = 0f
            }
        } else if (match.status == com.example.model.MatchStatus.LIVE) {
            // Live adjustments based on time elapsed
            val progressFactor = minute / 90f
            
            if (goalDiff > 0) {
                // Home team leading
                homeWeight += goalDiff * 25f * (1f + progressFactor)
                drawWeight -= goalDiff * 5f
                awayWeight -= goalDiff * 20f
            } else if (goalDiff < 0) {
                // Away team leading
                awayWeight += (-goalDiff) * 25f * (1f + progressFactor)
                drawWeight -= (-goalDiff) * 5f
                homeWeight -= (-goalDiff) * 20f
            } else {
                // Tied
                drawWeight += progressFactor * 20f
                homeWeight -= progressFactor * 10f
                awayWeight -= progressFactor * 10f
                
                // Add minor edge to who has more shots on target
                if (homeSOnT > awaySOnT) homeWeight += 4f else awayWeight += 4f
            }
        }

        // Clamp values to be non-negative
        if (homeWeight < 0) homeWeight = 0f
        if (drawWeight < 0) drawWeight = 0f
        if (awayWeight < 0) awayWeight = 0f

        val total = homeWeight + drawWeight + awayWeight
        val homeChance = if (total > 0) (homeWeight / total) * 100f else 33.3f
        val drawChance = if (total > 0) (drawWeight / total) * 100f else 33.3f
        val awayChance = if (total > 0) (awayWeight / total) * 100f else 33.3f

        // Localized professional statements based on state
        val predictedScore = if (match.status == com.example.model.MatchStatus.FINISHED) {
            "$homeScore - $awayScore"
        } else {
            val extraHome = if (homeChance > 55f) 1 else 0
            val extraAway = if (awayChance > 55f) 1 else 0
            "${homeScore + extraHome} - ${awayScore + extraAway}"
        }

        val verdictEn: String
        val verdictHu: String
        val keyEn: String
        val keyHu: String

        if (match.status == com.example.model.MatchStatus.FINISHED) {
            if (goalDiff > 0) {
                verdictEn = "The home team secured a well-deserved victory, outperforming their opponents globally."
                verdictHu = "A hazai csapat megérdemelt győzelmet aratott, globálisan felülmúlva ellenfelét."
                keyEn = "Clinical finishing and dominant midfield control clinched the victory."
                keyHu = "A klinikai befejezések és a domináns középpályás játék döntötte el a meccset."
            } else if (goalDiff < 0) {
                verdictEn = "An outstanding away performance shut down the hosts' attack entirely."
                verdictHu = "Kiváló idegenbeli játékkal a vendégek teljesen semlegesítették a hazaiak támadásait."
                keyEn = "Tactical discipline and lightning-fast counter-attacks won the match."
                keyHu = "A taktikai fegyelem és a villámgyors kontra-támadások hozták meg a sikert."
            } else {
                verdictEn = "A fiercely contested match ended in a fair draw, with both defenses ruling supreme."
                verdictHu = "Egy végtelenül szoros mérkőzés igazságos döntetlennel zárult, a védelmek jeleskedtek."
                keyEn = "Low goal conversion despite intense action kept the scoreboard locked."
                keyHu = "Az intenzív játék ellenére a gyenge helyzetkihasználás vezetett pontosztozkodáshoz."
            }
        } else if (match.status == com.example.model.MatchStatus.LIVE) {
            if (goalDiff > 0) {
                verdictEn = "The home team holds the upper hand. Maintaining possession and slowing down tempo will be crucial to seal the win."
                verdictHu = "A hazaiak kezében van az irányítás. A labdabirtoklás megtartása és a tempó lassítása kulcsfontosságú lesz."
                keyEn = "Chasing and breaking the home team's structured low block in the remaining minutes."
                keyHu = "A hazai csapat fegyelmezett, mélyen meghúzódó védekező blokkjának feltörése a hátralévő időben."
            } else if (goalDiff < 0) {
                verdictEn = "The away team is masterfully controlling the game on the break. The hosts look fatigued."
                verdictHu = "A vendégcsapat mesterien kontrollálja a játékot ellentámadásokból. A hazaiak fáradtnak tűnnek."
                keyEn = "Hosts need to commit more players forward, risking deep space counters."
                keyHu = "A hazaiaknak több játékost kell előretolniuk, ami megnyitja a területet az idegenbeli kontráknak."
            } else {
                verdictEn = "The match is currently on a knife's edge. A single defensive mistake or brilliance from substitutes will decide it."
                verdictHu = "A mérkőzés jelenleg hajszálon múlik. Egyetlen védelmi hiba vagy a cserejátékosok villanása fog dönteni."
                keyEn = "Fatigue in late stages and set-piece operations are the primary threat parameters."
                keyHu = "A késői szakaszban kialakuló fáradtság és a rögzített helyzetek jelentik a legnagyobb veszélyt."
            }
        } else {
            // PREVIEW / UPCOMING
            verdictEn = "This match faces extremely balanced initial metrics. Modern history favors the home side, though away pace is threatening."
            verdictHu = "Ez a mérkőzés rendkívül kiegyenlített alapmutatókkal indul. A közelmúlt a hazaiaknak kedvez, de a vendégek gyorsak."
            keyEn = "Early pressing intensity and controlling the half-spaces will set the tone."
            keyHu = "A korai letámadás intenzitása és a félterületek ellenőrzése fogja megadni az alaphangot."
        }

        val predHomeYellow = if (match.status == com.example.model.MatchStatus.FINISHED) {
            match.stats.yellowCardsHome
        } else {
            (match.stats.yellowCardsHome + 1).coerceAtMost(5)
        }
        val predAwayYellow = if (match.status == com.example.model.MatchStatus.FINISHED) {
            match.stats.yellowCardsAway
        } else {
            (match.stats.yellowCardsAway + 1).coerceAtMost(5)
        }
        val predHomeRed = match.stats.redCardsHome
        val predAwayRed = match.stats.redCardsAway

        val predHomeCorners = if (match.status == com.example.model.MatchStatus.FINISHED) {
            match.stats.cornersHome
        } else {
            match.stats.cornersHome + 3
        }
        val predAwayCorners = if (match.status == com.example.model.MatchStatus.FINISHED) {
            match.stats.cornersAway
        } else {
            match.stats.cornersAway + 3
        }

        return AIAnalysisResult(
            homeWinChance = Math.round(homeChance * 10f) / 10f,
            drawChance = Math.round(drawChance * 10f) / 10f,
            awayWinChance = Math.round(awayChance * 10f) / 10f,
            predictedScore = predictedScore,
            predictedHomeYellowCards = predHomeYellow,
            predictedAwayYellowCards = predAwayYellow,
            predictedHomeRedCards = predHomeRed,
            predictedAwayRedCards = predAwayRed,
            predictedHomeCorners = predHomeCorners,
            predictedAwayCorners = predAwayCorners,
            analystVerdictEn = verdictEn,
            analystVerdictHu = verdictHu,
            keyFactorEn = keyEn,
            keyFactorHu = keyHu,
            safetyStatus = "Local Formula"
        )
    }

    /**
     * Calls Gemini API via Direct REST endpoints, utilizing a structured prompt.
     * Falls back to local calculations if key is missing or service yields error.
     */
    suspend fun analyzeMatch(match: Match): AIAnalysisResult = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            Log.d(TAG, "Gemini API key not found or placeholder. Falling back to Local Math model.")
            return@withContext calculateLocalAnalysis(match)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val eventsStr = StringBuilder()
        match.events.forEach { event ->
            val team = if (event.isHomeTeam) match.homeTeam.name else match.awayTeam.name
            eventsStr.append("- ${event.minute}' ${event.type.name} by ${event.primaryPlayer} for $team")
            if (event.secondaryPlayer != null) {
                eventsStr.append(" (Assisted/Replaced: ${event.secondaryPlayer})")
            }
            eventsStr.append("\n")
        }

        val fallbackHomeYellow = (match.stats.yellowCardsHome + 1).coerceAtMost(5)
        val fallbackAwayYellow = (match.stats.yellowCardsAway + 1).coerceAtMost(5)
        val fallbackHomeRed = match.stats.redCardsHome
        val fallbackAwayRed = match.stats.redCardsAway
        val fallbackHomeCorners = match.stats.cornersHome + 3
        val fallbackAwayCorners = match.stats.cornersAway + 3

        val prompt = """You are a professional, expert sports analyst and football match tactical advisor. 
            Evaluate the following European football match in real-time and weigh the probability distribution realistically:
            
            LEAGUE ID: ${match.leagueId}
            HOME TEAM: ${match.homeTeam.name} (Recent form: ${match.homeTeam.form.joinToString("")})
            AWAY TEAM: ${match.awayTeam.name} (Recent form: ${match.awayTeam.form.joinToString("")})
            MATCH STATUS: ${match.status.name}
            CURRENT SCORE: ${match.homeScore} - ${match.awayScore}
            CURRENT MINUTE: ${match.minute}'
            
            MATCH EVENTS:
            ${eventsStr.toString().ifEmpty { "- No events recorded yet" }}
            
            MATCH STATISTICS:
            - Possession: Home ${match.stats.ballPossessionHome}% - Away ${match.stats.ballPossessionAway}%
            - Shots: Home ${match.stats.shotsHome} - Away ${match.stats.shotsAway}
            - Shots on Target: Home ${match.stats.shotsOnTargetHome} - Away ${match.stats.shotsOnTargetAway}
            - Corners: Home ${match.stats.cornersHome} - Away ${match.stats.cornersAway}
            - Fouls: Home ${match.stats.foulsHome} - Away ${match.stats.foulsAway}
            - Red Cards: Home ${match.stats.redCardsHome} - Away ${match.stats.redCardsAway}
            - Yellow Cards: Home ${match.stats.yellowCardsHome} - Away ${match.stats.yellowCardsAway}
            
            Based on these realistic parameters, calculate:
            1. Win probabilities: Home win %, Draw %, Away win % (values must sum to 100).
            2. Final predicted score string (e.g., "2 - 1").
            3. Predicted total yellow cards and red cards for both teams.
            4. Predicted total corners for both teams.
            5. Detailed, professional tactical analyst verdict in English (2-3 sentences).
            6. Detailed, professional tactical analyst verdict in Hungarian (2-3 sentences), matching the English insight and using accurate Hungarian football vocabulary (e.g., "letámadás", "helyzetkihasználás", "kontrajáték").
            7. Key game-deciding factor in English (1 sentence).
            8. Key game-deciding factor in Hungarian (1 sentence).
            
            You must respond ONLY with a clean, single JSON object containing EXACTLY these keys:
            {
               "homeWinChance": float,
               "drawChance": float,
               "awayWinChance": float,
               "predictedScore": "string",
               "predictedHomeYellowCards": integer,
               "predictedAwayYellowCards": integer,
               "predictedHomeRedCards": integer,
               "predictedAwayRedCards": integer,
               "predictedHomeCorners": integer,
               "predictedAwayCorners": integer,
               "analystVerdictEn": "string",
               "analystVerdictHu": "string",
               "keyFactorEn": "string",
               "keyFactorHu": "string"
            }
            Do NOT enclose inside ```json code blocks or output any extra markdown words. Just raw valid JSON.
        """.trimIndent()

        // Construct request JSON
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Configure response format to JSON
            put("generationConfig", JSONObject().apply {
                put("responseFormat", JSONObject().apply {
                    put("type", "RESPONSE_FORMAT_TEXT") // Standard text format receiving parsed JSON or application/json
                })
                put("temperature", 0.3) // Conservative temperature for factual sports analysis
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: "No error message"
                    Log.e(TAG, "Gemini API Call failed: Code ${response.code}, Msg: $errBody")
                    return@withContext calculateLocalAnalysis(match)
                }

                val resBody = response.body?.string()
                if (resBody.isNullOrBlank()) {
                    Log.e(TAG, "Gemini API returned empty body.")
                    return@withContext calculateLocalAnalysis(match)
                }

                Log.d(TAG, "Gemini response: $resBody")
                
                // Parse Gemini Response candidate text
                val rootJson = JSONObject(resBody)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    Log.e(TAG, "No candidates in Gemini response.")
                    return@withContext calculateLocalAnalysis(match)
                }

                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                if (content == null) {
                    Log.e(TAG, "No content in candidate.")
                    return@withContext calculateLocalAnalysis(match)
                }

                val parts = content.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    Log.e(TAG, "No parts in content.")
                    return@withContext calculateLocalAnalysis(match)
                }

                var textResponse = parts.getJSONObject(0).optString("text", "").trim()
                
                // Clean markdown code fence if AI included it anyways
                if (textResponse.startsWith("```")) {
                    textResponse = textResponse.replace("```json", "", ignoreCase = true)
                    textResponse = textResponse.replace("```", "")
                    textResponse = textResponse.trim()
                }

                val parsedObj = JSONObject(textResponse)
                
                AIAnalysisResult(
                    homeWinChance = parsedObj.optDouble("homeWinChance", 33.3).toFloat(),
                    drawChance = parsedObj.optDouble("drawChance", 33.3).toFloat(),
                    awayWinChance = parsedObj.optDouble("awayWinChance", 33.3).toFloat(),
                    predictedScore = parsedObj.optString("predictedScore", "${match.homeScore} - ${match.awayScore}"),
                    predictedHomeYellowCards = parsedObj.optInt("predictedHomeYellowCards", fallbackHomeYellow),
                    predictedAwayYellowCards = parsedObj.optInt("predictedAwayYellowCards", fallbackAwayYellow),
                    predictedHomeRedCards = parsedObj.optInt("predictedHomeRedCards", fallbackHomeRed),
                    predictedAwayRedCards = parsedObj.optInt("predictedAwayRedCards", fallbackAwayRed),
                    predictedHomeCorners = parsedObj.optInt("predictedHomeCorners", fallbackHomeCorners),
                    predictedAwayCorners = parsedObj.optInt("predictedAwayCorners", fallbackAwayCorners),
                    analystVerdictEn = parsedObj.optString("analystVerdictEn", "No analysis content available."),
                    analystVerdictHu = parsedObj.optString("analystVerdictHu", "Nem áll rendelkezésre elemzés."),
                    keyFactorEn = parsedObj.optString("keyFactorEn", "Tactical cohesion in neutral zones."),
                    keyFactorHu = parsedObj.optString("keyFactorHu", "Taktikai kohézió a semleges zónákban."),
                    safetyStatus = "Live Gemini API ($MODEL_NAME)"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini API: ${e.message}", e)
            calculateLocalAnalysis(match)
        }
    }
}
