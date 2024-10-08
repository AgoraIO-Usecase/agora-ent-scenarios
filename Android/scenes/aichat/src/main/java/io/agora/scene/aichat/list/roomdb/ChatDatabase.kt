package io.agora.scene.aichat.list.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.agora.scene.aichat.BuildConfig
import io.agora.scene.aichat.ext.MD5

@Database(entities = [ChatUserEntity::class], version = 1)
abstract class ChatDatabase: RoomDatabase() {

    /**
     * Get the user data access object.
     */
    abstract fun userDao(): ChatUserDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        // 以下数据库升级设置，为升级数据库将清掉之前的数据，如果要保留数据，慎重采用此种方式
        // 可以采用addMigrations()的方式，进行数据库的升级
        fun getDatabase(context: Context, userId: String): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = (BuildConfig.IM_APP_KEY + userId).MD5()
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChatDatabase::class.java,
                        dbName
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}