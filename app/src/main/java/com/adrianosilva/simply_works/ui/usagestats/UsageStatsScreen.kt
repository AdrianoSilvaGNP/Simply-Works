package com.adrianosilva.simply_works.ui.usagestats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme

@Composable
fun UsageStatsScreenRoot(
    viewModel: UsageStatsViewModel
) {
    UsageStatsScreen(
        state = viewModel.state
    )
}

@Composable
private fun UsageStatsScreen(
    state: UsageStatsUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(top = 32.dp)
    ) {
        items(state.stats) {
            UsageStat(
                name = it.first,
                count = it.second
            )
        }
    }
}

@Composable
private fun UsageStat(
    name: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$name: $count",
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun UsageStatsScreenPreview() {
    val stats: List<Pair<String, Int>> = listOf(
        Pair("ProgramA", 10),
        Pair("ProgramB", 5),
        Pair("ProgramC", 20),
    )
    SimplyworksTheme {
        UsageStatsScreen(
            state = UsageStatsUiState(stats),
        )
    }
}