package ci.nsu.mobile.main.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY isShown ASC, addedAt ASC")
    fun getAllQuotesFlow(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY isShown ASC, addedAt ASC")
    suspend fun getAllQuotes(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE isShown = 0 ORDER BY addedAt ASC LIMIT 1")
    suspend fun getFirstUnshown(): QuoteEntity?

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getCount(): Int

    @Query("UPDATE quotes SET isShown = 0")
    suspend fun resetAllShown()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    @Update
    suspend fun update(quote: QuoteEntity)

    @Delete
    suspend fun delete(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getById(id: Long): QuoteEntity?
}
