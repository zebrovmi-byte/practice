package ci.nsu.mobile.main.data.repository

import android.content.Context
import ci.nsu.mobile.main.data.db.AppDatabase
import ci.nsu.mobile.main.data.db.QuoteEntity
import kotlinx.coroutines.flow.Flow

class QuoteRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).quoteDao()

    val allQuotesFlow: Flow<List<QuoteEntity>> = dao.getAllQuotesFlow()

    suspend fun getCount(): Int = dao.getCount()

    /**
     * Returns the next quote to show on the home screen.
     * If all are shown — resets flags and picks the first one.
     * If DB is empty — loads defaults from assets first.
     */
    suspend fun getNextQuoteForHome(): QuoteEntity? {
        if (dao.getCount() == 0) {
            loadDefaultQuotes()
        }
        var quote = dao.getFirstUnshown()
        if (quote == null) {
            dao.resetAllShown()
            quote = dao.getFirstUnshown()
        }
        if (quote != null) {
            dao.update(quote.copy(isShown = true))
        }
        return quote
    }

    suspend fun insert(text: String): Long {
        return dao.insert(QuoteEntity(text = text))
    }

    suspend fun update(quote: QuoteEntity) {
        dao.update(quote)
    }

    suspend fun delete(quote: QuoteEntity) {
        dao.delete(quote)
    }

    suspend fun deleteByIds(ids: List<Long>) {
        dao.deleteByIds(ids)
    }

    suspend fun getById(id: Long): QuoteEntity? = dao.getById(id)

    private suspend fun loadDefaultQuotes() {
        val lines = context.assets.open("default_quotes.txt")
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
        val entities = lines.mapIndexed { index, text ->
            QuoteEntity(
                text = text.trim(),
                isShown = false,
                addedAt = System.currentTimeMillis() + index
            )
        }
        dao.insertAll(entities)
    }
}
