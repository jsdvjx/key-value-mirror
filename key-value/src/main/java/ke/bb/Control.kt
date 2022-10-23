package ke.bb

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

//Int,Long,Float,Double,String,Boolean
class Control(context: Context) {
    data class KeyValueItem(
        val group: String,
        val key: String,
        var value: String?,
        val type: String,
        var updatedAt: Long
    ) {
        fun get(): Any? {
            return when (type) {
                "Int" -> value?.toInt() ?: 0
                "Long" -> value?.toLong() ?: 0L
                "Float" -> value?.toFloat() ?: 0F
                "Double" -> value?.toDouble() ?: "0.0"
                "String" -> value
                "Boolean" -> value.toBoolean()
                "String?" -> value
                "Boolean?" -> value?.toBoolean()
                "Int?"-> value?.toInt()
                "Long?"-> value?.toLong()
                "Float?"-> value?.toFloat()
                "Double?"-> value?.toDouble()
                else -> null
            }
        }
    }

    class SyncMap(
        private val map: MutableMap<String, Any?> = mutableMapOf(),
        private val onUpdate: (KeyValueItem?, value: Any?) -> Unit,
    ) : MutableMap<String, Any?> by map {
        override fun put(key: String, value: Any?): Any? {
            if (map.containsKey(key)) {
                val item = map[key] as KeyValueItem
                onUpdate(item, value)
                return item.get()
            }
            throw Throwable("No such key: $key")
        }

        override fun putAll(from: Map<out String, Any?>) {
            for (entry in from) {
                put(entry.key, entry.value)
            }
        }

        override fun remove(key: String): Any? {
            val result = get(key)
            put(key, null)
            return result
        }

        override fun clear() {
            for (mutableEntry in map) {
                remove(mutableEntry.key)
            }
        }

        override fun get(key: String): Any? {
            return (map[key] as KeyValueItem).get()
        }
    }

    private val keyValueDbHelper = KeyValueDbHelper(context)
    private val db = keyValueDbHelper.writableDatabase

    private val groups: MutableMap<String, SyncMap> =
        all().groupBy { it.group }.mapValues {
            SyncMap(it.value.associateBy { item -> item.key }.toMutableMap()) { item, value ->
                set(item!!, value?.toString())
            }
        }.toMutableMap()

    fun getGroup(group: String): SyncMap {
        return groups[group]!!
    }

    private fun set(item: KeyValueItem, value: String?): Boolean {
        return upsert(item, value)
    }

    private fun setIfNotExists(item: KeyValueItem, value: String?): Boolean {
        if (exists(item)) {
            return false
        }
        return upsert(item, value)
    }

    fun initKeyValue(group: String, key: String, type: String, value: String? = null): Boolean {
        return setIfNotExists(KeyValueItem(group, key, value, type, System.currentTimeMillis()), value)
    }

    private fun exists(item: KeyValueItem): Boolean {
//        return properties.containsKey(group + "_" + key)
        val cursor = db.query(
            KeyValueEntry.TABLE_NAME,
            arrayOf(KeyValueEntry.COLUMN_NAME_GROUP, KeyValueEntry.COLUMN_NAME_KEY),
            "${KeyValueEntry.COLUMN_NAME_GROUP} = ? AND ${KeyValueEntry.COLUMN_NAME_KEY} = ?",
            arrayOf(item.group, item.key),
            null,
            null,
            null
        )
        return (cursor.count > 0).apply {
            cursor.close()
        }
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


    private fun upsert(item: KeyValueItem, value: String?): Boolean {
        item.updatedAt = System.currentTimeMillis()
        item.value = value
        val values = ContentValues().apply {
            put(KeyValueEntry.COLUMN_NAME_GROUP, item.group)
            put(KeyValueEntry.COLUMN_NAME_KEY, item.key)
            put(KeyValueEntry.COLUMN_NAME_VALUE, item.value)
            put(KeyValueEntry.COLUMN_NAME_TYPE, item.type)
            put(KeyValueEntry.COLUMN_NAME_UPDATED_AT, item.updatedAt)
        }
        return db.insertWithOnConflict(
            KeyValueEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        ) > 0
    }
}