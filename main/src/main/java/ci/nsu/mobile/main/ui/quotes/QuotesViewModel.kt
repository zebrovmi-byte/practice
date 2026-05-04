package ci.nsu.mobile.main.ui.quotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.data.db.QuoteEntity
import ci.nsu.mobile.main.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class QuotesUiState(
    val quotes: List<QuoteEntity> = emptyList(),
    val isEditMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val showDeleteSingleDialog: Boolean = false,
    val pendingDeleteId: Long? = null,
    val showDeleteMultipleDialog: Boolean = false
)

class QuotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(application)

    private val _uiState = MutableStateFlow(QuotesUiState())
    val uiState: StateFlow<QuotesUiState> = _uiState

    init {
        repository.allQuotesFlow
            .onEach { quotes ->
                _uiState.value = _uiState.value.copy(quotes = quotes)
            }
            .launchIn(viewModelScope)
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            selectedIds = emptySet()
        )
    }

    fun toggleSelection(id: Long) {
        val current = _uiState.value.selectedIds.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _uiState.value = _uiState.value.copy(selectedIds = current)
    }

    // Single swipe-to-delete: show confirmation dialog
    fun requestDeleteSingle(id: Long) {
        _uiState.value = _uiState.value.copy(
            showDeleteSingleDialog = true,
            pendingDeleteId = id
        )
    }

    fun confirmDeleteSingle() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            val quote = repository.getById(id) ?: return@launch
            repository.delete(quote)
        }
        _uiState.value = _uiState.value.copy(
            showDeleteSingleDialog = false,
            pendingDeleteId = null
        )
    }

    fun cancelDeleteSingle() {
        _uiState.value = _uiState.value.copy(
            showDeleteSingleDialog = false,
            pendingDeleteId = null
        )
    }

    // Multiple delete
    fun requestDeleteSelected() {
        if (_uiState.value.selectedIds.isEmpty()) return
        _uiState.value = _uiState.value.copy(showDeleteMultipleDialog = true)
    }

    fun confirmDeleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        viewModelScope.launch {
            repository.deleteByIds(ids)
        }
        _uiState.value = _uiState.value.copy(
            showDeleteMultipleDialog = false,
            selectedIds = emptySet(),
            isEditMode = false
        )
    }

    fun cancelDeleteSelected() {
        _uiState.value = _uiState.value.copy(showDeleteMultipleDialog = false)
    }
}
