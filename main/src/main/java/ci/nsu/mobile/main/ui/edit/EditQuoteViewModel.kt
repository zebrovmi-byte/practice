package ci.nsu.mobile.main.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditQuoteUiState(
    val text: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

class EditQuoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(application)

    private val _uiState = MutableStateFlow(EditQuoteUiState())
    val uiState: StateFlow<EditQuoteUiState> = _uiState

    private var editingId: Long? = null

    fun loadQuote(id: Long?) {
        if (id == null) {
            _uiState.value = EditQuoteUiState()
            return
        }
        editingId = id
        viewModelScope.launch {
            _uiState.value = EditQuoteUiState(isLoading = true)
            val quote = repository.getById(id)
            _uiState.value = EditQuoteUiState(text = quote?.text ?: "")
        }
    }

    fun onTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(text = newText)
    }

    fun save() {
        val text = _uiState.value.text.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            val id = editingId
            if (id == null) {
                repository.insert(text)
            } else {
                val existing = repository.getById(id) ?: return@launch
                repository.update(existing.copy(text = text))
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
