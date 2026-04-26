package com.adrianosilva.simply_works.ui.usagestats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.ui.components.StatCard
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun UsageStatsScreenRoot(viewModel: UsageStatsViewModel) {
    UsageStatsScreen(state = viewModel.state)
}

@Composable
private fun UsageStatsScreen(state: UsageStatsUiState) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    WaveBackground {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                    text = "Usage Statistics",
                    style = MaterialTheme.typography.headlineMedium.copy(color = SoftWhite),
                    modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.stats) { index, (name, count) ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 50)) +
                                slideInHorizontally(
                                    initialOffsetX = { 50 },
                                    animationSpec = tween(durationMillis = 300, delayMillis = index * 50)
                                )
                    ) {
                        UsageStatItem(name = name, count = count)
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageStatItem(name: String, count: Int, modifier: Modifier = Modifier) {
    // Reuse StatCard but ensure it fills width and has a reasonable height
    StatCard(
            label = name,
            value = count.toString(),
            modifier = modifier.fillMaxWidth().height(100.dp) // Slightly taller for grid
    )
}

@Preview(showBackground = true)
@Composable
private fun UsageStatsScreenPreview() {
    val stats: List<Pair<String, Int>> =
            listOf(
                    Pair("Eco 40-60", 10),
                    Pair("Jeans", 5),
                    Pair("Cotton", 20),
                    Pair("Wool", 2),
                    Pair("Quick Wash", 15),
                    Pair("Delicates", 8)
            )
    SimplyworksTheme {
        UsageStatsScreen(
                state = UsageStatsUiState(stats),
        )
    }
}
