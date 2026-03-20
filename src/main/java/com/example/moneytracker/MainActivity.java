/**
 *  -------------------------------------------------------------
 *     ██████╗ ██╗      █████╗ ██╗   ██╗██████╗ ██████╗
 *    ██╔════╝ ██║     ██╔══██╗██║   ██║██╔══██╗██╔══██╗
 *    ██║  ███╗██║     ███████║██║   ██║██████╔╝██████╔╝
 *    ██║   ██║██║     ██╔══██║██║   ██║██╔══██╗██╔══██╗
 *    ╚██████╔╝███████╗██║  ██║╚██████╔╝██║  ██║██║  ██║
 *     ╚═════╝ ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝
 *  -------------------------------------------------------------
 *  Copyright 2026 Popescu "GLauRR" Laurentiu-Georgian
 *  Github page of the project: https://github.com/glaurr/MoneyTracker
 *  -------------------------------------------------------------
 */

package com.example.moneytracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TransactionAdapter adapter;
    ArrayList<Transaction> transactionList;
    DatabaseHelper dbHelper;
    TextView tvTotalBalance, tvIncome, tvExpense;
    EditText etDesc, etAmount;
    Spinner spinnerCategory;
    Button btnAdd, btnDate;
    RadioButton rbExpense;
    String[] categories;

    private String selectedDateString = "";
    private ActivityResultLauncher<Intent> backupLauncher;
    private ActivityResultLauncher<Intent> restoreLauncher;

    /**
     * Initializes the activity, sets up the user interface, and configures data handling logic.
     * <p>
     * This method performs the following setup tasks:
     * <ul>
     *     <li>Inflates the layout and initializes UI components (TextViews, EditTexts, Spinner, etc.).</li>
     *     <li>Configures the Toolbar and sets it as the Action Bar.</li>
     *     <li>Registers {@code ActivityResultLauncher} instances for database backup and restore operations via JSON.</li>
     *     <li>Initializes the {@link DatabaseHelper} and the {@link RecyclerView} with a {@link TransactionAdapter}.</li>
     *     <li>Sets up the category spinner with predefined values and custom styling.</li>
     *     <li>Configures event listeners for the date picker and transaction submission.</li>
     *     <li>Loads existing transaction data from the database to populate the dashboard.</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                String json = dbHelper.getAllDataAsJSON();
                                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                                if (outputStream != null) {
                                    outputStream.write(json.getBytes());
                                    outputStream.close();
                                    Toast.makeText(this, R.string.msg_export_success, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(this, R.string.msg_error_export, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        restoreLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                if (inputStream != null) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                    StringBuilder stringBuilder = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        stringBuilder.append(line);
                                    }
                                    inputStream.close();
                                    dbHelper.importDataFromJSON(stringBuilder.toString());
                                    loadData();
                                    Toast.makeText(this, R.string.msg_import_success, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(this, R.string.msg_error_import, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        recyclerView = findViewById(R.id.recyclerView);
        etDesc = findViewById(R.id.etDesc);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAdd = findViewById(R.id.btnAdd);
        btnDate = findViewById(R.id.btnDate);
        rbExpense = findViewById(R.id.rbExpense);

        dbHelper = new DatabaseHelper(this);

        categories = new String[]{
                getString(R.string.cat_food), getString(R.string.cat_salary),
                getString(R.string.cat_rent), getString(R.string.cat_transport),
                getString(R.string.cat_distraction), getString(R.string.cat_other)
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }
        };
        spinnerCategory.setAdapter(spinnerAdapter);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList, dbHelper, this::updateDashboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        resetDateToToday();
        btnDate.setOnClickListener(v -> showDatePicker());

        loadData();
        btnAdd.setOnClickListener(v -> addTransaction());
    }

    /**
     * Resets the selected date to the current system date and updates the associated UI button.
     * <p>
     * This method formats today's date using the "yyyy-MM-dd" pattern, assigns it to
     * {@code selectedDateString}, and refreshes the text on {@code btnDate} to reflect
     * the change.
     */
    private void resetDateToToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDateString = sdf.format(new Date());
        btnDate.setText(getString(R.string.btn_date_format, selectedDateString));
    }

    /**
     * Displays a {@link DatePickerDialog} to allow the user to select a transaction date.
     * <p>
     * The dialog is initialized with the current date. It enforces a selection range
     * limited to the last 5 days, including today. Upon selecting a date, the
     * {@code selectedDateString} is updated in "yyyy-MM-dd" format and the
     * date selection button text is refreshed to show the new value.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        long todayInMillis = calendar.getTimeInMillis();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDateString = String.format(Locale.US, "%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    btnDate.setText(getString(R.string.btn_date_format, selectedDateString));
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(todayInMillis);

        calendar.add(Calendar.DAY_OF_MONTH, -5);
        long fiveDaysAgoInMillis = calendar.getTimeInMillis();
        datePickerDialog.getDatePicker().setMinDate(fiveDaysAgoInMillis);

        datePickerDialog.show();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * <p>
     * This method inflates the menu resource {@code R.menu.main_menu} into the provided
     * {@link Menu} object, which populates the Action Bar with actions such as
     * statistics, export, and import.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * <p>
     * Based on the selected menu item, this method performs one of the following actions:
     * <ul>
     *     <li>{@code action_stats}: Navigates to the {@link StatsActivity} to display financial charts.</li>
     *     <li>{@code action_export}: Triggers the data backup process to a JSON file.</li>
     *     <li>{@code action_import}: Triggers the data restoration process from a JSON file.</li>
     * </ul>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        } else if (id == R.id.action_export) {
            backupData();
            return true;
        } else if (id == R.id.action_import) {
            restoreData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initiates the process of backing up the application data to a JSON file.
     * <p>
     * This method creates an intent with {@link Intent#ACTION_CREATE_DOCUMENT} to prompt the user
     * to select a destination and filename (defaulting to "money_backup.json").
     * The intent is configured to handle "application/json" files and is launched via
     * {@code backupLauncher}, which handles the subsequent file writing operation.
     */
    private void backupData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "money_backup.json");
        backupLauncher.launch(intent);
    }

    /**
     * Initiates the process of restoring transaction data from an external JSON file.
     * <p>
     * This method creates and launches an intent using {@link Intent#ACTION_OPEN_DOCUMENT}
     * to allow the user to select a compatible JSON file from the system's file picker.
     * The actual file processing and database updating are handled by the
     * {@code restoreLauncher} callback.
     */
    private void restoreData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        restoreLauncher.launch(intent);
    }

    /**
     * Validates and saves a new transaction to the database based on user input.
     * <p>
     * This method retrieves the description, amount, and category from the UI components.
     * It performs the following steps:
     * <ul>
     *     <li>Checks if description or amount fields are empty.</li>
     *     <li>Parses the amount and determines its sign based on whether the "Expense" radio button is checked.</li>
     *     <li>Persists the transaction to the database using {@link DatabaseHelper}.</li>
     *     <li>Refreshes the transaction list and dashboard totals.</li>
     *     <li>Resets the input fields and the date picker to the current day.</li>
     * </ul>
     * Displays a {@link Toast} message if validation fails or if the amount format is invalid.
     */
    private void addTransaction() {
        String desc = etDesc.getText().toString();
        String amountStr = etAmount.getText().toString();
        String category = "";
        if (spinnerCategory.getSelectedItem() != null) {
            category = spinnerCategory.getSelectedItem().toString();
        }

        if (desc.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.msg_error_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amountInput = Math.abs(Double.parseDouble(amountStr));
            if (rbExpense.isChecked()) {
                amountInput = -amountInput;
            }

            dbHelper.addTransaction(desc, amountInput, category, selectedDateString);
            loadData();

            etDesc.setText(R.string.lbl_empty);
            etAmount.setText(R.string.lbl_empty);
            resetDateToToday();

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.msg_error_amount, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Synchronizes the in-memory transaction list with the database and refreshes the UI.
     * <p>
     * This method clears the current {@code transactionList}, populates it with all records
     * retrieved from the {@link DatabaseHelper}, and notifies the {@link TransactionAdapter}
     * of the data change. Finally, it calls {@link #updateDashboard()} to recalculate
     * and display the latest financial totals.
     */
    private void loadData() {
        transactionList.clear();
        transactionList.addAll(dbHelper.getAllTransactions());
        if (adapter != null) adapter.notifyDataSetChanged();
        updateDashboard();
    }

    /**
     * Updates the financial overview displayed on the main dashboard.
     * <p>
     * This method iterates through the current {@code transactionList} to calculate the
     * total balance, aggregate income (positive amounts), and aggregate expenses
     * (negative amounts). Once calculated, it refreshes the text values of
     * {@code tvTotalBalance}, {@code tvIncome}, and {@code tvExpense}.
     */
    private void updateDashboard() {
        double total = 0, income = 0, expense = 0;
        for (Transaction t : transactionList) {
            total += t.amount;
            if (t.amount > 0) income += t.amount;
            else expense += t.amount;
        }

        tvTotalBalance.setText(getString(R.string.lbl_balance, total));
        tvIncome.setText(getString(R.string.lbl_income, income));
        tvExpense.setText(getString(R.string.lbl_expense, expense));
    }
}