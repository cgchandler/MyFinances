package com.example.myfinances;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Finance currentFinance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Limit data entry to 7 digits with 2 digits after decimal
        EditText editInitialBalance = findViewById(R.id.editInitialBalance);
        editInitialBalance.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        EditText editCurrentBalance = findViewById(R.id.editCurrentBalance);
        editCurrentBalance.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        EditText editPaymentAmount = findViewById(R.id.editPaymentAmount);
        editPaymentAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        EditText editInterestRate = findViewById(R.id.editInterestRate);
        editInterestRate.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 2)});

        // Instantiate the finance object for storing data
        currentFinance = new Finance();

        // Initialize the button
        initButtonSave();
        initButtonCancel();
        initAccountType();

        // Save user entered data in the finance object
        initTextChangedEvents();

    }

    private void initAccountType() {

        // Set the default Account Type to Checking
        RadioButton rbChecking = findViewById(R.id.radioChecking);
        rbChecking.setChecked(true);
        initChecking();

        // Setup the Listener for onCheckedChanged
        RadioGroup rgAccountType = findViewById(R.id.radioGroupAccountType);
        rgAccountType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rbCD = findViewById(R.id.radioCD);
                RadioButton rbLoan = findViewById(R.id.radioLoan);
                RadioButton rbChecking = findViewById(R.id.radioChecking);

                // Alwasy clear the data entry values when a new account type is selected
                clearScreen();

                if (rbCD.isChecked()) {
                    initCD();
                }
                else if (rbLoan.isChecked()) {
                    initLoan();
                }
                else if (rbChecking.isChecked()) {
                    initChecking();
                }
            }
        });

    }

    private void initCD() {
        initAvailbleButtons(getResources().getString(R.string.account_type_cd),
                true, true, true, false, true);
    }

    private void initLoan() {
        initAvailbleButtons(getResources().getString(R.string.account_type_loan),
                true, true, true, true, true);
    }
    private void initChecking() {
        initAvailbleButtons(getResources().getString(R.string.account_type_checking),
                true, false, true, false, false);
    }

    private void initAvailbleButtons(String accountType,
                                     boolean accountNumberEnabled,
                                     boolean initialBalanceEnabled,
                                     boolean currentBalanceEnabled,
                                     boolean paymentAmountEnabled,
                                     boolean interestRateEnabled)
    {
        // Set the AccountType
        currentFinance.setAccountType(accountType);
        // Enable/Disable buttons on current selection
        EditText editAccountNumber = findViewById(R.id.editAccountNumber);
        editAccountNumber.setEnabled(accountNumberEnabled);
        EditText editInitialBalance = findViewById(R.id.editInitialBalance);
        editInitialBalance.setEnabled(initialBalanceEnabled);
        EditText editCurrentBalance = findViewById(R.id.editCurrentBalance);
        editCurrentBalance.setEnabled(currentBalanceEnabled);
        EditText editPaymentAmount = findViewById(R.id.editPaymentAmount);
        editPaymentAmount.setEnabled(paymentAmountEnabled);
        EditText editInterestRate = findViewById(R.id.editInterestRate);
        editInterestRate.setEnabled(interestRateEnabled);
    }

    private void initButtonSave() {
        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setEnabled(false);   // Save button is initially disabled
        buttonSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean wasSuccessful;
                hideKeyboard(); // automatically hide the soft-keyboard when save is pressed
                FinanceDataSource ds = new FinanceDataSource(MainActivity.this);
                try {
                    ds.Open();
                    initTextChangedEvents();
                    if (currentFinance.getFinanceID() == -1) {
                        currentFinance.setFinanceID(ds.insertFinance(currentFinance));
                        wasSuccessful = (currentFinance.getFinanceID() > 0);
                    }
                    else {
                        wasSuccessful = ds.updateFinance(currentFinance);
                    }
                    ds.Close();
                }
                catch (Exception ex)
                {
                    wasSuccessful = false;
                }

                if (wasSuccessful) {
                    // Display Success Message!
                    String alertTitle = getResources().getString(R.string.success_title);
                    String alertValue = getResources().getString(R.string.success_message, currentFinance.getAccountType(), currentFinance.getAccountNumber(), currentFinance.getFinanceID());
                    displayAlertDialog(alertTitle, alertValue);
                    // Clear the Data Entry Screen
                    clearScreen();
                    // Reset the Finance Object
                    currentFinance = new Finance();
                    // Set the default Account Type to Checking
                    initChecking();
                }
                else {
                    // Display Failure Message!
                    String alertTitle = getResources().getString(R.string.failure_title);
                    String alertValue = getResources().getString(R.string.failure_message);
                    displayAlertDialog(alertTitle, alertValue);
                }

            }
        });
    }

    private void displayAlertDialog(String titleValue, String alertValue){
        AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
        ad.setTitle(titleValue);
        ad.setCancelable(false);
        ad.setMessage(alertValue);
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ad.cancel();
            }
        });
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                int textSize = getResources().getDimensionPixelSize(R.dimen.alert_text_size);
                ((TextView)ad.findViewById(android.R.id.message)).setTextSize(textSize);
            }
        });
        ad.show();
    }

    private void initButtonCancel() {
        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearScreen();
            }
        });
    }

    private void initTextChangedEvents() {

        final EditText editAccountNumber = findViewById(R.id.editAccountNumber);
        editAccountNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                currentFinance.setAccountNumber(editAccountNumber.getText().toString());
                // Only enable the Save button if an account number is entered
                Button buttonSave = findViewById(R.id.buttonSave);
                buttonSave.setEnabled(currentFinance.getAccountNumber().length() > 0);
            }
        });

        final EditText editInitialBalance = findViewById(R.id.editInitialBalance);
        editInitialBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                currentFinance.setInitialBalance(getEditTextDouble(editInitialBalance));
            }
        });

        EditText editCurrentBalance = findViewById(R.id.editCurrentBalance);
        editCurrentBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                currentFinance.setCurrentBalance(getEditTextDouble(editCurrentBalance));
            }
        });

        EditText editPaymentAmount = findViewById(R.id.editPaymentAmount);
        editPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                currentFinance.setPaymentAmount(getEditTextDouble(editPaymentAmount));
            }
        });

        EditText editInterestRate = findViewById(R.id.editInterestRate);
        editInterestRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                currentFinance.setInterestRate(getEditTextDouble(editInterestRate));
            }
        });

    }

    private void clearScreen()
    {
        // Set the Initial Text Values
        EditText editAccountNumber = findViewById(R.id.editAccountNumber);
        editAccountNumber.setText("");
        EditText editInitialBalance = findViewById(R.id.editInitialBalance);
        editInitialBalance.setText("");
        EditText editCurrentBalance = findViewById(R.id.editCurrentBalance);
        editCurrentBalance.setText("");
        EditText editPaymentAmount = findViewById(R.id.editPaymentAmount);
        editPaymentAmount.setText("");
        EditText editInterestRate = findViewById(R.id.editInterestRate);
        editInterestRate.setText("");
    }

    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        EditText editAccountNumber = findViewById(R.id.editAccountNumber);
        imm.hideSoftInputFromWindow(editAccountNumber.getWindowToken(), 0);
        EditText editInitialBalance = findViewById(R.id.editInitialBalance);
        imm.hideSoftInputFromWindow(editInitialBalance.getWindowToken(), 0);
        EditText editCurrentBalance = findViewById(R.id.editCurrentBalance);
        imm.hideSoftInputFromWindow(editCurrentBalance.getWindowToken(), 0);
        EditText editPaymentAmount = findViewById(R.id.editPaymentAmount);
        imm.hideSoftInputFromWindow(editPaymentAmount.getWindowToken(), 0);
        EditText editInterestRate = findViewById(R.id.editInterestRate);
        imm.hideSoftInputFromWindow(editInterestRate.getWindowToken(), 0);
    }

    private double getEditTextDouble(EditText editValue)
    {
        try {
            return Double.parseDouble(editValue.getText().toString());
        }
        catch (Exception e){
            return 0.0;
        }
    }

    // Limit decimal input from: https://www.tutorialspoint.com/how-to-limit-decimal-places-in-android-edittext
    class DecimalDigitsInputFilter implements InputFilter {
        private Pattern mPattern;
        DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }
    }

}