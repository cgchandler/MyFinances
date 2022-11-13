package com.example.myfinances;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FinanceDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myfinances.db";
    private static final int DATABASE_VERSION = 1;

    // Database Creation Statement
    private static final String CREATE_TABLE_FINANCE =
            "CREATE TABLE Finance ("
                    + "_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "AccountType TEXT not null, "
                    + "AccountNumber TEXT not null, "
                    + "InitialBalance REAL, "
                    + "CurrentBalance REAL, "
                    + "PaymentAmount REAL, "
                    + "InterestRate REAL);";

    public FinanceDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FINANCE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(FinanceDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destory old data");
        db.execSQL("DROP TABLE IF EXISTS Finance;");
        onCreate(db);
    }
}
