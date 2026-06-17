package com.thpl.naviagtion3demo.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thpl.naviagtion3demo.room.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // We fetch ALL users as a Flow. We will filter them in the ViewModel.
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert
    suspend fun insertUser(user: User)

    @Insert
    suspend fun insertAll(users: List<User>)
}