package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.repository.FootballRepository
import com.example.viewmodel.FootballViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { DashboardScreen() } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun matchCenterOverlay_screenshot() {
    FootballRepository.resetMatches()
    val matches = FootballRepository.matches.value
    val liveMatch = matches.first { it.status == com.example.model.MatchStatus.LIVE }
    
    composeTestRule.setContent {
      MyApplicationTheme {
        MatchCenterOverlay(
          match = liveMatch,
          isHungarian = true,
          onDismiss = {},
          viewModel = FootballViewModel()
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/match_center.png")
  }
}
