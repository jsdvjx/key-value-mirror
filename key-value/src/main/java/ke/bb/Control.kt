package ke.bb

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

//Int,Long,Float,Double,String,Boolean
class Control(context: Context) {
    data class KeyValueItem(
        val group: String,
        val key: String,
        val value: String,
        val type: String,
        val updatedAt: Long
    )

    private val keyValueDbHelper = KeyValueDbHelper(context)
    private val db = keyValueDbHelper.writableDatabase

    private val properties: MutableMap<String, KeyValueItem> =
        all().associateBy { it.group + "_" + it.key }.toMutableMap()

    fun get(group: String, key: String): Any {
        if (properties.containsKey(group + "_" + key)) {
            val item = properties[group + "_" + key]!!
            return when (item.type) {
                "Int" -> item.value.toInt()
                "Long" -> item.value.toLong()
                "Float" -> item.value.toFloat()
                "Double" -> item.value.toDouble()
                "String" -> item.value
                "Boolean" -> item.value.toBoolean()
                else -> ""
            }
        }
        throw Throwable("No such key: $group.$key")
    }

    fun set(group: String, key: String, value: String, type: String): Boolean {
        return upsert(group, key, value).apply {
            if (this) {
                properties[group + "_" + key] = KeyValueItem(group, key, value, type, System.currentTimeMillis())
            }
        }
    }

    fun getRaw(): MutableMap<String, KeyValueItem> {
        return properties
    }

    private fun all(): List<KeyValueItem> {
        val cursor = db.query(
            KeyValueEntry.TABLE_NAME,
            KeyValueEntry.run {
                arrayOf(
                    COLUMN_NAME_GROUP,
                    COLUMN_NAME_KEY,
                    COLUMN_NAME_VALUE,
                    COLUMN_NAME_TYPE,
                    COLUMN_NAME_UPDATED_AT
                )
            },
            null,
            null,
            null,
            null,
            null
        )
        val records = mutableListOf<KeyValueItem>()
        while (cursor.moveToNext()) {
            records.add(
                KeyValueItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getLong(4)
                )
            )
        }
        cursor.close()
        return records
    }

    private fun delete(group: String, key: String): Boolean {
        val selection = "${KeyValueEntry.COLUMN_NAME_GROUP} = ? AND ${KeyValueEntry.COLUMN_NAME_KEY} = ?"
        val selectionArgs = arrayOf(group, key)
        return db.delete(KeyValueEntry.TABLE_NAME, selection, selectionArgs) > 0
    }

    private fun upsert(group: String, key: String, value: String): Boolean {
        val values = ContentValues().apply {
            put(KeyValueEntry.COLUMN_NAME_GROUP, group)
            put(KeyValueEntry.COLUMN_NAME_KEY, key)
            put(KeyValueEntry.COLUMN_NAME_VALUE, value)
            put(KeyValueEntry.COLUMN_NAME_UPDATED_AT, System.currentTimeMillis())
        }
        return db.insertWithOnConflict(
            KeyValueEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        ) > 0
    }
}