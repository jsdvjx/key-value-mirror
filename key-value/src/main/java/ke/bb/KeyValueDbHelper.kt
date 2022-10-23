package ke.bb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object KeyValueEntry {
    const val TABLE_NAME = "key_value"
    const val COLUMN_NAME_GROUP = "group_name"
    const val COLUMN_NAME_KEY = "key_name"
    const val COLUMN_NAME_VALUE = "value"
    const val COLUMN_NAME_TYPE = "type"
    const val COLUMN_NAME_UPDATED_AT = "updated_at"
}

class KeyValueDbHelper(context: Context) : SQLiteOpenHelper(context, "key_value", null, 3) {
    companion object {
        const val SQL_CREATE_ENTRIES = """
            CREATE TABLE ${KeyValueEntry.TABLE_NAME} (
                ${KeyValueEntry.COLUMN_NAME_GROUP} TEXT NOT NULL,
                ${KeyValueEntry.COLUMN_NAME_KEY} TEXT NOT NULL,
                ${KeyValueEntry.COLUMN_NAME_VALUE} TEXT,
                ${KeyValueEntry.COLUMN_NAME_UPDATED_AT} INTEGER NOT NULL,
                ${KeyValueEntry.COLUMN_NAME_TYPE} TEXT NOT NULL,
                PRIMARY KEY (${KeyValueEntry.COLUMN_NAME_GROUP}, ${KeyValueEntry.COLUMN_NAME_KEY})
            )
        """
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${KeyValueEntry.TABLE_NAME}"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(p0)
    }
}