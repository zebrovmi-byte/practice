package ci.nsu.mobile.main.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isShown: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
