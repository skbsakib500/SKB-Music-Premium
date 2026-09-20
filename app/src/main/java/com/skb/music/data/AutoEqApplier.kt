package com.skb.music.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

/**
 * Reads AutoEq DB and extracts the actual filter bands for a headphone.
 * Applies pre-computed 5-band approximation.
 */
object AutoEqApplier {

    private var cachedDb: SQLiteDatabase? = null

    private fun open(context: Context): SQLiteDatabase? {
        if (cachedDb?.isOpen == true) return cachedDb
        val dbFile = File(context.filesDir, "auto_eq.db")
        if (!dbFile.exists()) {
            runCatching {
                context.assets.open("auto_eq.db").use { input ->
                    FileOutputStream(dbFile).use { input.copyTo(it) }
                }
            }
        }
        if (!dbFile.exists()) return null
        cachedDb = runCatching {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
            )
        }.getOrNull()
        return cachedDb
    }

    /**
     * Fetches 5-band approximation for a headphone name.
     * Returns list of 5 gain values in dB.
     */
    fun bandsFor(context: Context, headphone: String): List<Float> {
        val db = open(context) ?: return zeros()
        return runCatching {
            // Try common table names
            val candidates = listOf("headphones", "autoeq", "presets", "data")
            for (tbl in candidates) {
                val cursor = db.rawQuery(
                    "SELECT * FROM $tbl WHERE name = ? LIMIT 1",
                    arrayOf(headphone)
                )
                cursor.use { c ->
                    if (c.moveToFirst()) {
                        val cols = c.columnNames
                        val bandCols = cols.filter {
                            it.contains("band", true) || it.startsWith("f")
                        }.sorted()
                        val gains = mutableListOf<Float>()
                        for (col in bandCols) {
                            val v = c.getFloat(c.getColumnIndexOrThrow(col))
                            gains.add(v)
                        }
                        if (gains.isNotEmpty()) {
                            return@use downsample(gains, 5)
                        }
                    }
                }
            }
            // If nothing found, give a genre-neutral curve
            return zeros()
        }.getOrDefault(zeros())
    }

    /** Reduce arbitrary N-band curve into 5 bands by averaging. */
    private fun downsample(src: List<Float>, target: Int): List<Float> {
        if (src.size <= target) {
            val out = MutableList(target) { 0f }
            src.forEachIndexed { i, v -> out[i] = v }
            return out
        }
        val step = src.size.toFloat() / target
        return (0 until target).map { i ->
            val start = (i * step).toInt()
            val end = ((i + 1) * step).toInt().coerceAtMost(src.size)
            if (end <= start) 0f
            else src.subList(start, end).average().toFloat()
        }
    }

    private fun zeros() = listOf(0f, 0f, 0f, 0f, 0f)
}
