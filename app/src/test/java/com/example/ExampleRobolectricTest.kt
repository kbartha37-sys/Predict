package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.repository.FootballRepository
import com.example.network.GeminiApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Euro Football Analyst", appName)
  }

  @Test
  fun testSimulationTicksAndFormula() {
    // Reset matches to clean slate
    FootballRepository.resetMatches()
    
    // Perform 250 simulation ticks
    for (i in 1..250) {
      FootballRepository.tickSimulation()
      
      // Check each match
      val matches = FootballRepository.matches.value
      for (match in matches) {
        assertNotNull(match)
        
        // Calculate dynamic formulas
        val analysis = GeminiApiClient.calculateLocalAnalysis(match)
        assertNotNull(analysis)
        
        // Check probabilities behave correctly
        val sum = analysis.homeWinChance + analysis.drawChance + analysis.awayWinChance
        assertEquals("Probabilities should sum to approximately 100", 100f, sum, 1.5f)
      }
    }
  }
}
