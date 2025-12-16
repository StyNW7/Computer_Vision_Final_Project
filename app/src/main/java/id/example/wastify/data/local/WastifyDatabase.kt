package id.example.wastify.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- 1. User Entity ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val password: String // In a real app, hash this!
)

// --- 2. History Entity ---
@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,     // Path to the saved image file
    val resultTitle: String,   // "Recyclable", "Organic", etc.
    val resultColor: Long,     // Store Color as Long (ARGB)
    val timestamp: Long        // System.currentTimeMillis()
)

// --- 3. User DAO ---
@Dao
interface UserDao {
    @Insert
    suspend fun registerUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?
}

// --- 4. History DAO ---
@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(history: ScanHistory)

    // Return a Flow so UI updates automatically when data changes
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScanHistory>>
}

// --- 5. Database Setup ---
@Database(entities = [User::class, ScanHistory::class], version = 1, exportSchema = false)
abstract class WastifyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: WastifyDatabase? = null

        fun getDatabase(context: Context): WastifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WastifyDatabase::class.java,
                    "wastify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}