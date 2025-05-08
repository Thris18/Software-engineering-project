package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.SearchSuggestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SokeKnapp(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit,
    onQueryChange: (String) -> Unit = {},
    suggestions: List<SearchSuggestion> = emptyList(),
    onSuggestionSelected: (SearchSuggestion) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var debouncedQuery by remember { mutableStateOf("") }

    // Debounce søkeforslag
    LaunchedEffect(query) {
        scope.launch {
            delay(100) // 100ms debounce
            debouncedQuery = query
            if (query.isNotEmpty()) {
                Log.d("SokeKnapp", "Calling onQueryChange with query: $query")
                onQueryChange(query)
            }
        }
    }

    // Log når forslagene endres
    LaunchedEffect(suggestions) {
        Log.d("SokeKnapp", "Suggestions updated: ${suggestions.size} suggestions")
        suggestions.forEach { suggestion ->
            Log.d("SokeKnapp", "Suggestion: ${suggestion.name}")
        }
    }

    Column(modifier = modifier) {
        TextField(
            value = query,
            onValueChange = { 
                query = it
                showSuggestions = true
            },
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            placeholder = { Text("Søk etter sted...") },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            trailingIcon = {
                IconButton(onClick = {
                    onSearch(query)
                    showSuggestions = false
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Søk")
                }
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(query)
                    showSuggestions = false
                }
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            )
        )

        if (showSuggestions && suggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                LazyColumn {
                    items(suggestions) { suggestion ->
                        Text(
                            text = suggestion.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSuggestionSelected(suggestion)
                                    showSuggestions = false
                                    query = suggestion.name
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}