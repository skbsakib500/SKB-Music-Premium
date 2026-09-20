package com.skb.music.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

class AutoEqRepository(private val context: Context) {

    data class Headphone(val name: String)

    private val dbFile: File by lazy {
        File(context.filesDir, "auto_eq.db").also { f ->
            if (!f.exists()) {
                runCatching {
                    context.assets.open("auto_eq.db").use { input ->
                        FileOutputStream(f).use { output -> input.copyTo(output) }
                    }
                }
            }
        }
    }

    fun loadHeadphones(): List<Headphone> = runCatching {
        if (!dbFile.exists()) return emptyList()
        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath, null,
            SQLiteDatabase.OPEN_READONLY
        )
        val result = mutableListOf<Headphone>()

        val tables = mutableListOf<String>()
        db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { c ->
            while (c.moveToNext()) tables.add(c.getString(0))
        }

        for (t in tables) {
            runCatching {
                db.rawQuery("SELECT * FROM $t LIMIT 1", null).use { c ->
                    if ("name" in c.columnNames) {
                        db.rawQuery("SELECT name FROM $t LIMIT 500", null).use { c2 ->
                            while (c2.moveToNext()) {
                                result.add(Headphone(c2.getString(0)))
                            }
                        }
                    }
                }
            }
            if (result.isNotEmpty()) break
        }
        db.close()
        result
    }.getOrDefault(emptyList())
}
