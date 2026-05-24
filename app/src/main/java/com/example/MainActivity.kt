package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.repository.FootballRepository
import com.example.ui.theme.*
import com.example.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBg)
                ) { innerPadding ->
                    DashboardScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: FootballViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedLeagueId by viewModel.selectedLeagueId.collectAsState()
    val selectedMatch by viewModel.selectedMatch.collectAsState()
    val isHungarian by viewModel.isHungarian.collectAsState()
    val isSimRunning by viewModel.isSimulationRunning.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val standings by viewModel.standings.collectAsState()
    val isApiKeyPresent = remember { com.example.network.GeminiApiClient.isApiKeyAvailable() }

    Box(modifier = modifier.background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- HEADER PANEL ---
            HeaderPanel(
                isHungarian = isHungarian,
                isSimRunning = isSimRunning,
                isApiKeyPresent = isApiKeyPresent,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onToggleSim = { viewModel.toggleSimulation() },
                onReset = { viewModel.resetMatches() }
            )

            // --- TAB NAV SELECTOR ---
            TabNavigation(
                selectedTab = currentTab,
                isHungarian = isHungarian,
                onTabSelected = { viewModel.selectTab(it) }
            )

            HorizontalDivider(color = CardOutline, thickness = 1.dp)

            // --- MAIN CONTENT AREA ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    0 -> MatchesLounge(
                        matches = matches,
                        selectedLeagueId = selectedLeagueId,
                        isHungarian = isHungarian,
                        onLeagueChanged = { viewModel.selectLeague(it) },
                        onMatchSelected = { viewModel.selectMatch(it) },
                        viewModel = viewModel
                    )
                    1 -> StandingsLounge(
                        standings = standings,
                        selectedLeagueId = selectedLeagueId,
                        isHungarian = isHungarian,
                        onLeagueChanged = { viewModel.selectLeague(it) },
                        viewModel = viewModel
                    )
                    2 -> TopScorersLounge(
                        topScorers = viewModel.topScorers,
                        isHungarian = isHungarian
                    )
                    3 -> AiPredictorLounge(
                        matches = matches,
                        isHungarian = isHungarian,
                        onMatchSelected = { viewModel.selectMatch(it) }
                    )
                }
            }
        }

        // --- INTERACTIVE MATCH CENTER OVERLAY ---
        selectedMatch?.let { match ->
            MatchCenterOverlay(
                match = match,
                isHungarian = isHungarian,
                onDismiss = { viewModel.selectMatch(null) },
                viewModel = viewModel
            )
        }
    }
}

// ==========================================
// COMPONENT: HEADER PANEL
// ==========================================
@Composable
fun HeaderPanel(
    isHungarian: Boolean,
    isSimRunning: Boolean,
    isApiKeyPresent: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleSim: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, CardOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isHungarian) "EURO FOCI ELEMZŐ" else "EURO MATCH ANALYST",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isApiKeyPresent) SportsGreen else WarningOrange)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isApiKeyPresent) "✦ Gemini AI Active" else "⚠ Fallback Formula Mode",
                            color = if (isApiKeyPresent) SportsGreen else WarningOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hungarian / English flag toggle buttons
                    IconButton(onClick = onToggleLanguage) {
                        Text(
                            text = if (isHungarian) "🇭🇺" else "🇬🇧",
                            fontSize = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))

                    // Simulation power toggle using custom pause / core play icons
                    IconButton(onClick = onToggleSim) {
                        if (isSimRunning) {
                            Row(
                                modifier = Modifier.height(18.dp).width(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.fillMaxHeight().width(5.dp).background(SportsGreen, RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.fillMaxHeight().width(5.dp).background(SportsGreen, RoundedCornerShape(1.dp)))
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Toggle Live Simulation",
                                tint = WarningOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Reset Match state
                    IconButton(onClick = onReset) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart simulation matches",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            
            // Subtitle banner with running tickers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryCard)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSimRunning) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(LiveDotRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHungarian) "Élő adathalmaz szimulálva (valós időben)" else "Live simulation feed matches running (real-time)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = if (isHungarian) "A szimulációs motor megállítva" else "Simulation engine paused",
                        color = WarningOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: TAB NAVIGATION
// ==========================================
@Composable
fun TabNavigation(
    selectedTab: Int,
    isHungarian: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        if (isHungarian) "Mérkőzések" else "Matches",
        if (isHungarian) "Tabella" else "Standings",
        if (isHungarian) "Góllövők" else "Top Scorers",
        if (isHungarian) "AI Elemző" else "AI Predictor"
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = SportsBlue,
        edgePadding = 12.dp,
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        color = if (selectedTab == index) SportsBlue else TextSecondary,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

// ==========================================
// COMPONENT: TEAM BADGE CREST COMPOSABLE
// ==========================================
@Composable
fun TeamBadge(
    team: Team,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(team.primaryColor)
            .border(2.dp, team.secondaryColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = team.secondaryColor.copy(alpha = 0.25f),
                radius = size.dp.toPx() / 3.2f,
                center = Offset(size.dp.toPx() / 2, size.dp.toPx() / 2)
            )
        }
        Text(
            text = team.shortCode,
            color = if (team.primaryColor == Color(0xFFF0F2F5) || team.primaryColor == Color.White) Color.Black else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size * 0.35f).sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.5).sp
        )
    }
}

// ==========================================
// VIEW: MATCHES LOUNGE SCREEN
// ==========================================
@Composable
fun MatchesLounge(
    matches: List<Match>,
    selectedLeagueId: String,
    isHungarian: Boolean,
    onLeagueChanged: (String) -> Unit,
    onMatchSelected: (Match) -> Unit,
    viewModel: FootballViewModel
) {
    val leagues = viewModel.leagues
    val filteredMatches = matches.filter { it.leagueId == selectedLeagueId }

    Column(modifier = Modifier.fillMaxSize()) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leagues.forEach { lg ->
                val isSelected = lg.id == selectedLeagueId
                Button(
                    onClick = { onLeagueChanged(lg.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) SportsBlue else DarkCard,
                        contentColor = if (isSelected) Color.White else TextSecondary
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .border(1.dp, if (isSelected) SportsBlue else CardOutline, RoundedCornerShape(12.dp)),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "${lg.logoChar} ${lg.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            
            val liveMatches = filteredMatches.filter { it.status == MatchStatus.LIVE }
            if (liveMatches.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHungarian) "ÉLŐ MÉRKŐZÉSEK" else "LIVE MATCHES ONGOING",
                        color = LiveDotRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
                items(liveMatches) { match ->
                    MatchCard(match = match, isHungarian = isHungarian, onClick = { onMatchSelected(match) })
                }
            }

            val upcomingMatches = filteredMatches.filter { it.status == MatchStatus.UPCOMING }
            if (upcomingMatches.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHungarian) "KÖZELGŐ MÉRKŐZÉSEK" else "UPCOMING FIXTURES",
                        color = SportsBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
                items(upcomingMatches) { match ->
                    MatchCard(match = match, isHungarian = isHungarian, onClick = { onMatchSelected(match) })
                }
            }

            val finishedMatches = filteredMatches.filter { it.status == MatchStatus.FINISHED }
            if (finishedMatches.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHungarian) "BEFEJEZETT MÉRKŐZÉSEK" else "COMPLETED MATCHES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
                items(finishedMatches) { match ->
                    MatchCard(match = match, isHungarian = isHungarian, onClick = { onMatchSelected(match) })
                }
            }

            if (filteredMatches.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        isHungarian = isHungarian,
                        tip = if (isHungarian) "Nincs mérkőzés ebben a ligában jelenleg." else "No matches in this league right now."
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: INDIVIDUAL MATCH CARD
// ==========================================
@Composable
fun MatchCard(
    match: Match,
    isHungarian: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (match.status == MatchStatus.LIVE) 1.dp else 0.5.dp,
                color = if (match.status == MatchStatus.LIVE) LiveDotRed.copy(alpha = 0.6f) else CardOutline,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (match.status == MatchStatus.LIVE) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiveDotRed.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${match.minute}'",
                            color = LiveDotRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            color = LiveDotRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (match.status == MatchStatus.UPCOMING) {
                    Text(
                        text = "TODAY ${match.scheduledTime}",
                        color = SportsBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = if (isHungarian) "VÉGE" else "FULL TIME",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // AI win projections peak preview
                val pre = remember(match) { com.example.network.GeminiApiClient.calculateLocalAnalysis(match) }
                val peakText = if (pre.homeWinChance > pre.awayWinChance && pre.homeWinChance > pre.drawChance) {
                    "${match.homeTeam.shortCode} ${pre.homeWinChance.toInt()}%"
                } else if (pre.awayWinChance > pre.homeWinChance && pre.awayWinChance > pre.drawChance) {
                    "${match.awayTeam.shortCode} ${pre.awayWinChance.toInt()}%"
                } else {
                    "Draw ${pre.drawChance.toInt()}%"
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI Win Indicator",
                        tint = SportsGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHungarian) "Favorit: $peakText" else "Favored: $peakText",
                        color = SportsGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(0.42f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamBadge(team = match.homeTeam, size = 32)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.weight(0.16f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (match.status == MatchStatus.UPCOMING) {
                        Text(
                            text = "VS",
                            color = CardOutline,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            color = if (match.status == MatchStatus.LIVE) SportsGreen else TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    modifier = Modifier.weight(0.42f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamBadge(team = match.awayTeam, size = 32)
                }
            }

            if (match.events.isNotEmpty()) {
                val latest = match.events.first()
                val eventSymbol = when (latest.type) {
                    EventType.GOAL -> "⚽"
                    EventType.YELLOW_CARD -> "🟨"
                    EventType.RED_CARD -> "🟥"
                    EventType.SUBSTITUTION -> "🔄"
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TextPrimary.copy(alpha = 0.05f))
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$eventSymbol ${latest.minute}' ${latest.primaryPlayer} (${if (latest.isHomeTeam) match.homeTeam.shortCode else match.awayTeam.shortCode})",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// VIEW: LEAGUE STANDINGS LOUNGE SCREEN
// ==========================================
@Composable
fun StandingsLounge(
    standings: Map<String, List<StandingItem>>,
    selectedLeagueId: String,
    isHungarian: Boolean,
    onLeagueChanged: (String) -> Unit,
    viewModel: FootballViewModel
) {
    val leagues = viewModel.leagues
    val currentStandings = standings[selectedLeagueId] ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leagues.forEach { lg ->
                val isSelected = lg.id == selectedLeagueId
                Button(
                    onClick = { onLeagueChanged(lg.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) SportsBlue else DarkCard,
                        contentColor = if (isSelected) Color.White else TextSecondary
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .border(1.dp, if (isSelected) SportsBlue else CardOutline, RoundedCornerShape(12.dp)),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "${lg.logoChar} ${lg.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryCard),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = if (isHungarian) "CSAPAT" else "TEAM", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = "M", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text(text = "GY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text(text = "D", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text(text = "V", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text(text = "P", color = SportsGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(currentStandings) { standing ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, CardOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${standing.position}",
                            color = if (standing.position <= 2) SportsGreen else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeamBadge(team = standing.team, size = 26)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = standing.team.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    standing.team.form.forEach { f ->
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (f) {
                                                        "W" -> SportsGreenMuted
                                                        "D" -> TextSecondary
                                                        else -> ErrorRed
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = f, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Text(text = "${standing.played}", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text(text = "${standing.won}", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text(text = "${standing.drawn}", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text(text = "${standing.lost}", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text(
                            text = "${standing.points}",
                            color = SportsGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// VIEW: TOP GOAL SCORERS LOUNGE SCREEN
// ==========================================
@Composable
fun TopScorersLounge(
    topScorers: List<PlayerStats>,
    isHungarian: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Text(
                text = if (isHungarian) "GÓLLÖVŐLISTA VEZETŐK" else "LEAGUE GOAL LEADERS",
                color = WarningOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 6.dp),
                letterSpacing = 1.sp
            )
        }

        items(topScorers) { player ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(0.5.dp, CardOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = player.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = player.teamName, color = TextSecondary, fontSize = 12.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "G", color = WarningOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${player.goals}",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "A", color = SportsBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${player.assists}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// VIEW: AI PREDICTOR HOME LOUNGE
// ==========================================
@Composable
fun AiPredictorLounge(
    matches: List<Match>,
    isHungarian: Boolean,
    onMatchSelected: (Match) -> Unit
) {
    val liveAndForm = matches.filter { it.status == MatchStatus.LIVE || it.status == MatchStatus.FINISHED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SportsGreenMuted),
                border = BorderStroke(1.dp, SportsBlue.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHungarian) "✦ Taktikai AI Elemzőközpont" else "✦ Tactical AI Predictor Hub",
                        color = SportsGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHungarian) 
                            "Válassz ki egy aktív vagy korábbi mérkőzést az élő szimulációból. A Gemini AI valós időben elemzi a statisztikai változásokat, kiállításokat és gólokat, majd részletes nyerési valószínűségeket és taktikai prognózist készít."
                            else 
                            "Select any live or finished match from the feed. Gemini AI analyzes match flow events (goals, red cards, corners) and provides instant percentage projections & coaching summaries.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = if (isHungarian) "VÁLASSZ MÉRKŐZÉST ELEMZÉSRE" else "SELECT MATCH TO EVALUATE",
                color = WarningOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                letterSpacing = 1.sp
            )
        }

        items(liveAndForm) { match ->
            MatchCard(match = match, isHungarian = isHungarian, onClick = { onMatchSelected(match) })
        }

        if (liveAndForm.isEmpty()) {
            item {
                Text(
                    text = if (isHungarian) "Nincs aktív élő meccs jelenleg." else "No active simulation matches at this moment.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

// ==========================================
// VIEW: DETAILS & INTERACTIVE MATCH CENTER OVERLAY
// ==========================================
@Composable
fun MatchCenterOverlay(
    match: Match,
    isHungarian: Boolean,
    onDismiss: () -> Unit,
    viewModel: FootballViewModel
) {
    val aiState by viewModel.aiAnalysisState.collectAsState()
    val localResult = remember(match) { com.example.network.GeminiApiClient.calculateLocalAnalysis(match) }
    val resolvedProb = remember(match, aiState) {
        when (aiState) {
            is AIAnalysisUiState.Success -> (aiState as AIAnalysisUiState.Success).result
            else -> localResult
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clickable(enabled = false) {}
                .border(BorderStroke(1.dp, CardOutline), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHungarian) "Mérkőzés-Központ" else "Live Match Center",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close overlay", tint = TextPrimary)
                    }
                }

                HorizontalDivider(color = CardOutline, thickness = 0.5.dp)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(0.5.dp, CardOutline)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (match.status == MatchStatus.LIVE) "LIVE • ${match.minute}'" else if (match.status == MatchStatus.FINISHED) "FINISHED" else "UPCOMING",
                                    color = if (match.status == MatchStatus.LIVE) LiveDotRed else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        TeamBadge(team = match.homeTeam, size = 52)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = match.homeTeam.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Text(text = "Form: " + match.homeTeam.form.joinToString(""), color = TextSecondary, fontSize = 10.sp)
                                    }

                                    Text(
                                        text = if (match.status == MatchStatus.UPCOMING) "VS" else "${match.homeScore} - ${match.awayScore}",
                                        color = if (match.status == MatchStatus.LIVE) SportsGreen else TextPrimary,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        TeamBadge(team = match.awayTeam, size = 52)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = match.awayTeam.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Text(text = "Form: " + match.awayTeam.form.joinToString(""), color = TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isHungarian) "DYNAMIKUS ESÉLYMÉRLEGELŐ (%)" else "LIVE DYNAMIC PROBABILITIES (%)",
                                color = SportsGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardOutline)
                            ) {
                                val homePercent = resolvedProb.homeWinChance / 100f
                                val drawPercent = resolvedProb.drawChance / 100f
                                val awayPercent = resolvedProb.awayWinChance / 100f

                                if (homePercent > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(homePercent.coerceAtLeast(0.01f))
                                            .background(ProbHomeColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${resolvedProb.homeWinChance.toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                if (drawPercent > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(drawPercent.coerceAtLeast(0.01f))
                                            .background(ProbDrawColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${resolvedProb.drawChance.toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                if (awayPercent > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(awayPercent.coerceAtLeast(0.01f))
                                            .background(ProbAwayColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${resolvedProb.awayWinChance.toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProbHomeColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${match.homeTeam.shortCode} Win", color = TextSecondary, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProbDrawColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Draw", color = TextSecondary, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProbAwayColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${match.awayTeam.shortCode} Win", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SportsBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = SportsGreenMuted),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isHungarian) "✦ Taktikai AI Elemző" else "✦ Tactical AI Briefing",
                                        color = SportsGreen,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    
                                    val src = when(aiState) {
                                        is AIAnalysisUiState.Success -> (aiState as AIAnalysisUiState.Success).result.safetyStatus
                                        else -> "Formula Feed"
                                    }
                                    Text(text = src, color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                when (aiState) {
                                    is AIAnalysisUiState.Idle -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (isHungarian) "ALAP STATISZTIKAI PROGNÓZIS:" else "QUICK STATS PREDICTION:",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = if (isHungarian) "Várható eredmény: " else "Expected score: ",
                                                        color = TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = localResult.predictedScore,
                                                        color = WarningOrange,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }

                                            // --- CARDS & CORNERS PREDICTION ROW (FAST FALLBACK) ---
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Corners prediction card
                                                Card(
                                                    modifier = Modifier.weight(1f).border(0.5.dp, CardOutline, RoundedCornerShape(12.dp)),
                                                    colors = CardDefaults.cardColors(containerColor = SecondaryCard),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = if (isHungarian) "VÁRHATÓ SZÖGLETEK" else "PREDICTED CORNERS",
                                                            color = TextSecondary,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(text = "📐 ", fontSize = 14.sp)
                                                            Text(
                                                                text = "${localResult.predictedHomeCorners + localResult.predictedAwayCorners}",
                                                                color = SportsBlue,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }
                                                        Text(
                                                            text = "${match.homeTeam.shortCode}: ${localResult.predictedHomeCorners} | ${match.awayTeam.shortCode}: ${localResult.predictedAwayCorners}",
                                                            color = TextSecondary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }

                                                // Cards prediction card
                                                Card(
                                                    modifier = Modifier.weight(1f).border(0.5.dp, CardOutline, RoundedCornerShape(12.dp)),
                                                    colors = CardDefaults.cardColors(containerColor = SecondaryCard),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = if (isHungarian) "VÁRHATÓ LAPOK" else "PREDICTED CARDS",
                                                            color = TextSecondary,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(text = "🟨 ", fontSize = 13.sp)
                                                            Text(
                                                                text = "${localResult.predictedHomeYellowCards + localResult.predictedAwayYellowCards}",
                                                                color = WarningOrange,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                            if (localResult.predictedHomeRedCards + localResult.predictedAwayRedCards > 0) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(text = "🟥 ", fontSize = 13.sp)
                                                                Text(
                                                                    text = "${localResult.predictedHomeRedCards + localResult.predictedAwayRedCards}",
                                                                    color = ErrorRed,
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.Black
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${match.homeTeam.shortCode}: ${localResult.predictedHomeYellowCards}🟨 | ${match.awayTeam.shortCode}: ${localResult.predictedAwayYellowCards}🟨",
                                                            color = TextSecondary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Button(
                                                onClick = { viewModel.requestAiPrediction() },
                                                colors = ButtonDefaults.buttonColors(containerColor = SportsBlue),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isHungarian) "Mélyreható Gemini AI Elemzés" else "Deep Gemini AI Projections",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    is AIAnalysisUiState.Loading -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(color = SportsGreen, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = if (isHungarian) "A Gemini model mérlegeli az esélyeket..." else "Gemini is analyzing match flow metrics...",
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    is AIAnalysisUiState.Success -> {
                                        val result = (aiState as AIAnalysisUiState.Success).result
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isHungarian) "VÁRHATÓ VÉGEREDMÉNY:" else "PREDICTED FINAL SCORE:",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = result.predictedScore,
                                                    color = WarningOrange,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            // --- CARDS & CORNERS PREDICTION ROW (PREMIUM GEMINI AI) ---
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Corners prediction card
                                                Card(
                                                    modifier = Modifier.weight(1f).border(0.5.dp, CardOutline, RoundedCornerShape(12.dp)),
                                                    colors = CardDefaults.cardColors(containerColor = SecondaryCard),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = if (isHungarian) "VÁRHATÓ SZÖGLETEK" else "PREDICTED CORNERS",
                                                            color = TextSecondary,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(text = "📐 ", fontSize = 14.sp)
                                                            Text(
                                                                text = "${result.predictedHomeCorners + result.predictedAwayCorners}",
                                                                color = SportsBlue,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }
                                                        Text(
                                                            text = "${match.homeTeam.shortCode}: ${result.predictedHomeCorners} | ${match.awayTeam.shortCode}: ${result.predictedAwayCorners}",
                                                            color = TextSecondary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }

                                                // Cards prediction card
                                                Card(
                                                    modifier = Modifier.weight(1f).border(0.5.dp, CardOutline, RoundedCornerShape(12.dp)),
                                                    colors = CardDefaults.cardColors(containerColor = SecondaryCard),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = if (isHungarian) "VÁRHATÓ LAPOK" else "PREDICTED CARDS",
                                                            color = TextSecondary,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(text = "🟨 ", fontSize = 14.sp)
                                                            Text(
                                                                text = "${result.predictedHomeYellowCards + result.predictedAwayYellowCards}",
                                                                color = WarningOrange,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                            if (result.predictedHomeRedCards + result.predictedAwayRedCards > 0) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(text = "🟥 ", fontSize = 14.sp)
                                                                Text(
                                                                    text = "${result.predictedHomeRedCards + result.predictedAwayRedCards}",
                                                                    color = ErrorRed,
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.Black
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${match.homeTeam.shortCode}: ${result.predictedHomeYellowCards}🟨${if(result.predictedHomeRedCards > 0) " ${result.predictedHomeRedCards}🟥" else ""} | ${match.awayTeam.shortCode}: ${result.predictedAwayYellowCards}🟨${if(result.predictedAwayRedCards > 0) " ${result.predictedAwayRedCards}🟥" else ""}",
                                                            color = TextSecondary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            val verdict = if (isHungarian) result.analystVerdictHu else result.analystVerdictEn
                                            Text(
                                                text = "“ $verdict ”",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                fontWeight = FontWeight.Medium
                                            )

                                            val keyFactor = if (isHungarian) result.keyFactorHu else result.keyFactorEn
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SportsBlue.copy(alpha = 0.1f))
                                                    .border(0.5.dp, SportsBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.Info, contentDescription = "Key Factor icon", tint = SportsBlue, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = keyFactor,
                                                    color = SportsBlue,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = 14.sp
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { viewModel.requestAiPrediction() }) {
                                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = if (isHungarian) "Elemzés frissítése" else "Recalculate Projections", fontSize = 11.sp, color = SportsGreen)
                                                }
                                            }
                                        }
                                    }
                                    is AIAnalysisUiState.Error -> {
                                        val err = if (isHungarian) (aiState as AIAnalysisUiState.Error).messageHu else (aiState as AIAnalysisUiState.Error).messageEn
                                        Text(text = err, color = ErrorRed, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isHungarian) "MUTATÓK & STATISZTIKA" else "MATCH FLOW STATISTICS",
                                color = WarningOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            StatisticBarRow(
                                label = if (isHungarian) "Labdabirtoklás" else "Possession",
                                homeValLabel = "${match.stats.ballPossessionHome}%",
                                awayValLabel = "${match.stats.ballPossessionAway}%",
                                scoreRatio = match.stats.ballPossessionHome / 100f
                            )
                            val totalShots = (match.stats.shotsHome + match.stats.shotsAway).coerceAtLeast(1)
                            StatisticBarRow(
                                label = if (isHungarian) "Lövések" else "Total Shots",
                                homeValLabel = "${match.stats.shotsHome}",
                                awayValLabel = "${match.stats.shotsAway}",
                                scoreRatio = match.stats.shotsHome.toFloat() / totalShots
                            )
                            val totalOnTarget = (match.stats.shotsOnTargetHome + match.stats.shotsOnTargetAway).coerceAtLeast(1)
                            StatisticBarRow(
                                label = if (isHungarian) "Kaput eltaláló lövések" else "Shots on Target",
                                homeValLabel = "${match.stats.shotsOnTargetHome}",
                                awayValLabel = "${match.stats.shotsOnTargetAway}",
                                scoreRatio = match.stats.shotsOnTargetHome.toFloat() / totalOnTarget
                            )
                            val totalCorners = (match.stats.cornersHome + match.stats.cornersAway).coerceAtLeast(1)
                            StatisticBarRow(
                                label = if (isHungarian) "Szöglet" else "Corners Conceded",
                                homeValLabel = "${match.stats.cornersHome}",
                                awayValLabel = "${match.stats.cornersAway}",
                                scoreRatio = match.stats.cornersHome.toFloat() / totalCorners
                            )
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isHungarian) "MÉRKŐZÉS ESEMÉNYEK" else "MATCH EVENT CHRONOLOGY",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            if (match.events.isEmpty()) {
                                Text(
                                    text = if (isHungarian) "Még nem történt rögzített esemény ezen a meccsen." else "Tactical play yet to execute field events.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            } else {
                                match.events.forEach { ev ->
                                    EventTimelineRow(event = ev, homeTeam = match.homeTeam, awayTeam = match.awayTeam)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, CardOutline), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = SecondaryCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isHungarian) "⚒ Live Coach Lab: Esemény Előidéző" else "⚒ Live Coach Lab: Scenario Injector",
                                    color = WarningOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHungarian) 
                                        "Tesztre szabhatod a predikciót! Kattints egy gombra egy gól, sárga lap vagy kiállítás beírásához, és figyeld meg, hogyan tolódnak el azonnal az erőviszonyok és az AI esélyek."
                                        else 
                                        "Simulate events to test prediction odds! Click below to inject a Goal, Card, or Red Card and see probabilities dynamically shift in real-time.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))

                                var selectedTeamIsHome by remember { mutableStateOf(true) }
                                var playerNameInput by remember { mutableStateOf("") }

                                Row(
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { selectedTeamIsHome = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedTeamIsHome) SportsGreenMuted else DarkCard
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = match.homeTeam.shortCode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { selectedTeamIsHome = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!selectedTeamIsHome) SportsBlue else DarkCard
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = match.awayTeam.shortCode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = playerNameInput,
                                    onValueChange = { playerNameInput = it },
                                    label = { Text(text = if (isHungarian) "Játékos neve" else "Player Name", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SportsGreen,
                                        unfocusedBorderColor = CardOutline
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val targetPlayer = playerNameInput.ifBlank { "Player" }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.triggerInteractiveEvent(EventType.GOAL, selectedTeamIsHome, targetPlayer)
                                            playerNameInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SportsGreenMuted),
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "⚽ GÓL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.triggerInteractiveEvent(EventType.YELLOW_CARD, selectedTeamIsHome, targetPlayer)
                                            playerNameInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "🟨 LAP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.triggerInteractiveEvent(EventType.RED_CARD, selectedTeamIsHome, targetPlayer)
                                            playerNameInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "🟥 PIROS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticBarRow(
    label: String,
    homeValLabel: String,
    awayValLabel: String,
    scoreRatio: Float
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = homeValLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = awayValLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { scoreRatio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = SportsGreen,
            trackColor = SportsBlue
        )
    }
}

@Composable
fun EventTimelineRow(
    event: MatchEvent,
    homeTeam: Team,
    awayTeam: Team
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val symbol = when (event.type) {
            EventType.GOAL -> "⚽"
            EventType.YELLOW_CARD -> "🟨"
            EventType.RED_CARD -> "🟥"
            EventType.SUBSTITUTION -> "🔄"
        }
        val teamAbbr = if (event.isHomeTeam) homeTeam.shortCode else awayTeam.shortCode

        Text(
            text = "${event.minute}'",
            color = LiveDotRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp),
            fontFamily = FontFamily.Monospace
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(TextPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = symbol, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "${event.primaryPlayer} ($teamAbbr)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            event.secondaryPlayer?.let { sec ->
                Text(
                    text = if (event.type == EventType.SUBSTITUTION) "Replaced: $sec" else "Assist: $sec",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(
    isHungarian: Boolean,
    tip: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚽", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = tip, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
