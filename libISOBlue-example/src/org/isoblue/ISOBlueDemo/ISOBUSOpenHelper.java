/*
 * Author: Alex Layton <awlayton@purdue.edu>
 *
 * Copyright (c) 2013 Purdue University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.isoblue.ISOBlueDemo;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.os.Environment;

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
	private static final String MESSAGE_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_MESSAGES + " (" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_PGN
			+ " INTEGER, " + COLUMN_DATA + " BLOB, " + COLUMN_SRC
			+ " INTEGER, " + COLUMN_DEST + " INTEGER, " + COLUMN_BUS
			+ " TEXT, " + COLUMN_TIME + " INTEGER);";

	ISOBUSOpenHelper(Context context) {
		super(context, Environment.getExternalStorageDirectory()
				+ File.separator + DATABASE_NAME, null, DATABASE_VERSION);
		MediaScannerConnection.scanFile(context,
				new String[] { Environment.getExternalStorageDirectory()
						+ File.separator + DATABASE_NAME }, null, null);
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
