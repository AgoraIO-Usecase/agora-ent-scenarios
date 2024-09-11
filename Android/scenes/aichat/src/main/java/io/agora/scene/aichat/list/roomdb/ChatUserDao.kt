package io.agora.scene.aichat.list.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatUserDao {

    @Query("SELECT * FROM ChatUserEntity")
    fun getAll(): List<ChatUserEntity>

    @Query("SELECT * FROM ChatUserEntity WHERE userId = :userId")
    fun getUserById(userId: String): Flow<ChatUserEntity?>

    @Query("SELECT * FROM ChatUserEntity WHERE userId = :userId")
    fun getUser(userId: String): ChatUserEntity?

    @Query("SELECT * FROM ChatUserEntity WHERE userId IN (:userIds)")
    fun getUsersByIds(userIds: List<String>): Flow<List<ChatUserEntity>>

    @Query("SELECT * FROM ChatUserEntity WHERE name LIKE :name")
    fun getUsersByName(name: String?): Flow<List<ChatUserEntity>>

    // Insert by ChatUserEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: ChatUserEntity)

    // Insert ChatUserEntity list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users: List<ChatUserEntity>)

    // Update
    @Query("UPDATE ChatUserEntity SET name = :name, avatar = :avatar, sign = :sign,prompt =:prompt WHERE userId = :userId")
    fun updateUser(userId: String, name: String, avatar: String, sign: String, prompt: String)

    // Update by ChatUserEntity
    @Update
    fun updateUser(user: ChatUserEntity)

    // Update ChatUserEntity list
    @Update
    fun updateUsers(users: List<ChatUserEntity>)

    // Update name
    @Query("UPDATE ChatUserEntity SET name = :name WHERE userId = :userId")
    fun updateUserName(userId: String, name: String)

    // Update avatar
    @Query("UPDATE ChatUserEntity SET avatar = :avatar WHERE userId = :userId")
    fun updateUserAvatar(userId: String, avatar: String)

    // Update sign
    @Query("UPDATE ChatUserEntity SET sign = :sign WHERE userId = :userId")
    fun updateUserSign(userId: String, sign: String)

    // Update prompt
    @Query("UPDATE ChatUserEntity SET prompt = :prompt WHERE userId = :userId")
    fun updateUserPrompt(userId: String, prompt: String)

    // Update update times
    @Query("UPDATE ChatUserEntity SET update_times = update_times + 1 WHERE userId = :userId")
    fun updateUserTimes(userId: String)

    // Update users update times
    @Query("UPDATE ChatUserEntity SET update_times = update_times + 1 WHERE userId IN (:userIds)")
    fun updateUsersTimes(userIds: List<String>)

    /**
     * Reset the update times of all users.
     */
    @Query("UPDATE ChatUserEntity SET update_times = 0")
    fun resetUsersTimes()

    // Delete
    @Delete
    fun deleteUser(user: ChatUserEntity)

    // Delete user list
    @Delete
    fun deleteUsers(users: List<ChatUserEntity>)

    // Delete by userId
    @Query("DELETE FROM ChatUserEntity WHERE userId = :userId")
    fun deleteUserById(userId: String)

    // Delete by userId list
    @Query("DELETE FROM ChatUserEntity WHERE userId IN (:userIds)")
    fun deleteUsersByIds(userIds: List<String>)


    // Delete all users
    @Query("DELETE FROM ChatUserEntity")
    fun deleteAll()
}