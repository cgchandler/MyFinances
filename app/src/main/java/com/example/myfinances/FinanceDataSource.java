package com.example.myfinances;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FinanceDataSource {

    private SQLiteDatabase database;
    private FinanceDBHelper dbHelper;

    public FinanceDataSource(Context context) {
        dbHelper = new FinanceDBHelper(context);
    }

    public void Open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void Close() {
        dbHelper.close();
    }

    public boolean insertFinance(Finance f) {
        boolean didSucceed = false;
        try {
            ContentValues initialValues = new ContentValues();

            initialValues.put("AccountType", f.getAccountType());
            initialValues.put("AccountNumber", f.getAccountNumber());
            initialValues.put("InitialBalance", f.getInitialBalance());
            initialValues.put("CurrentBalance", f.getCurrentBalance());
            initialValues.put("PaymentAmount", f.getPaymentAmount());
            initialValues.put("InterestRate", f.getInterestRate());

            didSucceed = database.insert("Finance", null, initialValues) > 0;
        }
        catch (Exception e) {
            // do nothing -will return false if there's an exception
        }
        return didSucceed;
    }

    public boolean updateFinance(Finance f) {
        boolean didSucceed = false;
        try {
            Long rowId = (long) f.getFinanceID();
            ContentValues updateValues = new ContentValues();

            updateValues.put("AccountType", f.getAccountType());
            updateValues.put("AccountNumber", f.getAccountNumber());
            updateValues.put("InitialBalance", f.getInitialBalance());
            updateValues.put("CurrentBalance", f.getCurrentBalance());
            updateValues.put("PaymentAmount", f.getPaymentAmount());
            updateValues.put("InterestRate", f.getInterestRate());

            didSucceed = database.update("Finance", updateValues, "_ID=" + rowId, null) > 0;
        }
        catch (Exception e) {
            // do nothing -will return false if there's an exception
        }
        return didSucceed;
    }
}
