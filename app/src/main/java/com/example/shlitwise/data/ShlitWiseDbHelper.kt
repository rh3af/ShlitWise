package com.example.shlitwise.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.shlitwise.model.DbUser

class ShlitWiseDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL,
                $COLUMN_PHONE_NUMBER TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun insertUser(
        fullName: String,
        email: String,
        passwordHash: String,
        phoneNumber: String
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD_HASH, passwordHash)
            put(COLUMN_PHONE_NUMBER, phoneNumber)
            put(COLUMN_CREATED_AT, System.currentTimeMillis())
        }

        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): DbUser? {
        val cursor = readableDatabase.query(
            TABLE_USERS,
            arrayOf(
                COLUMN_ID,
                COLUMN_FULL_NAME,
                COLUMN_EMAIL,
                COLUMN_PASSWORD_HASH,
                COLUMN_PHONE_NUMBER
            ),
            "$COLUMN_EMAIL = ?",
            arrayOf(email.trim().lowercase()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return DbUser(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    fullName = it.getString(it.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)),
                    phoneNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER))
                )
            }
        }

        return null
    }

    companion object {
        private const val DATABASE_NAME = "shlitwise.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
        private const val COLUMN_PHONE_NUMBER = "phone_number"
        private const val COLUMN_CREATED_AT = "created_at"
    }
}