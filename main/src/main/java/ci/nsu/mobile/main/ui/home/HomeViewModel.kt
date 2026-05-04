package ci.nsu.mobile.main.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.data.db.QuoteEntity
import ci.nsu.mobile.main.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val quoteOfDay: QuoteEntity? = null,
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadQuoteOfDay()
    }

    private fun loadQuoteOfDay() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            val quote = repository.getNextQuoteForHome()
            _uiState.value = HomeUiState(quoteOfDay = quote, isLoading = false)
        }
    }
}
