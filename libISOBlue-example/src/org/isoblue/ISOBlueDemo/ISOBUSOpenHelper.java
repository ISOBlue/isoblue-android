package org.isoblue.ISOBlueDemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ISOBUSOpenHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "isobus_messages.db";
    public static final String TABLE_MESSAGES = "isobus_messages";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PGN = "pgn";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_SRC = "src";
    public static final String COLUMN_DEST = "dest";
    public static final String COLUMN_BUS = "bus";
    public static final String COLUMN_TIME = "time";

    private static final int DATABASE_VERSION = 1;
    private static final String MESSAGE_TABLE_CREATE =
                "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PGN + " INTEGER, " +
                COLUMN_DATA + " BLOB, " +
                COLUMN_SRC + " INTEGER, " +
                COLUMN_DEST + " INTEGER, " +
                COLUMN_BUS + " TEXT, " +
                COLUMN_TIME + " INTEGER);";

    ISOBUSOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MESSAGE_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
