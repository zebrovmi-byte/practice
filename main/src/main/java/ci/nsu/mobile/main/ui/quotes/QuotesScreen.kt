package ci.nsu.mobile.main.ui.quotes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.mobile.main.data.db.QuoteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    onBack: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    viewModel: QuotesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Single delete confirmation dialog
    if (state.showDeleteSingleDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteSingle() },
            title = { Text("Удалить цитату?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteSingle() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteSingle() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Multiple delete confirmation dialog
    if (state.showDeleteMultipleDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteSelected() },
            title = { Text("Удалить выбранные?") },
            text = { Text("Будет удалено ${state.selectedIds.size} цитат(ы). Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteSelected() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteSelected() }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Цитаты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state.isEditMode && state.selectedIds.isNotEmpty()) {
                        IconButton(onClick = { viewModel.requestDeleteSelected() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить выбранные",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.toggleEditMode() }) {
                        Text(if (state.isEditMode) "Готово" else "Редактировать")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Добавить цитату")
            }
        }
    ) { padding ->
        if (state.quotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет цитат. Нажмите «+» чтобы добавить.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(
                    items = state.quotes,
                    key = { it.id }
                ) { quote ->
                    QuoteListItem(
                        quote = quote,
                        isEditMode = state.isEditMode,
                        isSelected = state.selectedIds.contains(quote.id),
                        onItemClick = {
                            if (state.isEditMode) {
                                viewModel.toggleSelection(quote.id)
                            } else {
                                onQuoteClick(quote.id)
                            }
                        },
                        onSwipeDelete = { viewModel.requestDeleteSingle(quote.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteListItem(
    quote: QuoteEntity,
    isEditMode: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onSwipeDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart ||
                value == SwipeToDismissBoxValue.StartToEnd
            ) {
                onSwipeDelete()
                // Return false so the item stays until confirmed
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart,
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "swipe_bg_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        val bgColor by animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface,
            label = "item_bg_color"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .clickable { onItemClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (isEditMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onItemClick() }
                )
            } else {
                Icon(
                    imageVector = if (quote.isShown) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (quote.isShown) "Показана" else "Не показана",
                    tint = if (quote.isShown) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
