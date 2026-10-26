package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.ui.compose.components.BackButton
import com.carthigan.droidconuganda.ui.compose.components.ConferenceIcon
import com.carthigan.droidconuganda.ui.compose.components.ConferenceMessage
import com.carthigan.droidconuganda.ui.compose.components.PageHeading
import com.carthigan.droidconuganda.ui.search.SearchResultType
import com.carthigan.droidconuganda.ui.search.SearchViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchRoute(viewModel: SearchViewModel, onBack: () -> Unit,
    onSession: (String) -> Unit, onSpeaker: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var submittedQuery by rememberSaveable { mutableStateOf("") }
    val results by viewModel.searchResults.observeAsState(emptyList())
    val loading by viewModel.isLoading.observeAsState(false)
    val failed by viewModel.loadFailed.observeAsState(false)
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(query) {
        if (query.trim().length >= 2) delay(250)
        viewModel.onSearchQueryChanged(query)
        submittedQuery = query.trim()
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) { BackButton(onBack) }
        PageHeading(stringResource(R.string.ui_search_title))
        OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
            label = { Text(stringResource(R.string.ui_search_hint)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            leadingIcon = { ConferenceIcon(R.drawable.ic_search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                    ConferenceIcon(R.drawable.ic_clear_all, stringResource(R.string.ui_clear_search))
                }
            }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                query.trim().length < 2 -> item { ConferenceMessage(stringResource(R.string.ui_search_prompt),
                    stringResource(R.string.ui_search_prompt_body)) }
                loading || query.trim() != submittedQuery -> item { ConferenceMessage(stringResource(R.string.ui_loading),
                    stringResource(R.string.ui_loading_details), loading = true) }
                failed -> item { ConferenceMessage(stringResource(R.string.ui_search_error),
                    stringResource(R.string.ui_search_error_body), actionLabel = stringResource(R.string.ui_retry),
                    onAction = { viewModel.onSearchQueryChanged(query) }) }
                results.isEmpty() -> item { ConferenceMessage(stringResource(R.string.search_schedule_no_results),
                    stringResource(R.string.ui_search_empty_body)) }
                else -> items(results, key = { "${it.type}:${it.objectId}" }) { result ->
                    Surface(onClick = {
                        keyboard?.hide()
                        if (result.type == SearchResultType.SESSION) onSession(result.objectId)
                        else onSpeaker(result.objectId)
                    }, color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(result.title, style = MaterialTheme.typography.titleLarge)
                            Text(result.subtitle, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
