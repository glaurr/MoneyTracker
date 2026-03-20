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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "MoneyDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "transactions";

    /**
     * Constructs a new instance of DatabaseHelper.
     * Initializes the SQLite database with the predefined name and version.
     *
     * @param context The context used to open or create the database.
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called when the database is created for the first time. This method
     * executes the SQL command to create the transactions table with the
     * required schema (id, description, amount, category, and date).
     *
     * @param db The database in which the table will be created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "description TEXT, "
                + "amount REAL, "
                + "category TEXT, "
                + "date TEXT)";
        db.execSQL(query);
    }

    /**
     * Called when the database needs to be upgraded. This implementation
     * drops the existing transactions table and recreates it.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Adds a new transaction record to the database.
     *
     * @param desc     The description of the transaction.
     * @param amount   The monetary value of the transaction (positive for income, negative for expense).
     * @param category The category classification for the transaction.
     * @param date     The date the transaction occurred, typically in YYYY-MM-DD format.
     */
    public void addTransaction(String desc, double amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", desc);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /**
     * Retrieves all transactions from the database, ordered by date and then by ID in descending order.
     * This ensures that the most recent entries appear first in the list.
     *
     * @return An {@link ArrayList} containing all {@link Transaction} objects stored in the database.
     */
    public ArrayList<Transaction> getAllTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY date DESC, id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * Deletes a specific transaction from the database based on its unique identifier.
     *
     * @param id The unique ID of the transaction to be removed.
     */
    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Exports all transaction data from the database into a JSON formatted string.
     * Each transaction is represented as a JSON object containing the description,
     * amount, category, and date.
     *
     * @return A {@link String} representing a {@link JSONArray} of all transactions,
     *         or an empty array string if an error occurs.
     */
    public String getAllDataAsJSON() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        JSONArray jsonArray = new JSONArray();

        try {
            if (cursor.moveToFirst()) {
                do {
                    JSONObject obj = new JSONObject();
                    obj.put("desc", cursor.getString(1));
                    obj.put("amnt", cursor.getDouble(2));
                    obj.put("cat", cursor.getString(3));
                    obj.put("date", cursor.getString(4));
                    jsonArray.put(obj);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        cursor.close();
        db.close();
        return jsonArray.toString();
    }

    /**
     * Imports transaction data from a JSON string into the database.
     * This method clears all existing records in the transactions table before
     * parsing the JSON array and inserting the new records. The operation is
     * performed within a database transaction to ensure data integrity.
     *
     * @param jsonString A JSON-formatted string representing a {@link JSONArray}
     *                   of transactions to be imported.
     */
    public void importDataFromJSON(String jsonString) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_NAME, null, null);
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("description", obj.getString("desc"));
                values.put("amount", obj.getDouble("amnt"));
                values.put("category", obj.getString("cat"));
                values.put("date", obj.getString("date"));
                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Calculates and retrieves financial statistics grouped by month.
     * The method aggregates total income (positive amounts) and total expenses (negative amounts)
     * for each month found in the database.
     *
     * @return An {@link ArrayList} of strings, where each entry is formatted as "YYYY-MM;totalIncome;totalExpense",
     *         ordered by month in descending order.
     */
    public ArrayList<String> getMonthlyStats() {
        ArrayList<String> stats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT substr(date, 1, 7) as month, " +
                "SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) as income, " +
                "SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) as expense " +
                "FROM " + TABLE_NAME +
                " GROUP BY month ORDER BY month DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                stats.add(cursor.getString(0) + ";" + cursor.getDouble(1) + ";" + cursor.getDouble(2));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stats;
    }

    /**
     * Calculates and retrieves annual financial statistics from the database.
     * This method aggregates income (positive amounts) and expenses (negative amounts)
     * grouped by year, based on the transaction dates.
     *
     * @return An {@link ArrayList} of {@link String} objects, where each string is formatted
     *         as "year;total_income;total_expenses", ordered by year in descending order.
     */
    public ArrayList<String> getYearlyStats() {
        ArrayList<String> stats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT substr(date, 1, 4) as year, " +
                "SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) as income, " +
                "SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) as expense " +
                "FROM " + TABLE_NAME +
                " GROUP BY year ORDER BY year DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                stats.add(cursor.getString(0) + ";" + cursor.getDouble(1) + ";" + cursor.getDouble(2));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stats;
    }
    /**
     * Retrieves all transactions from the database that occurred within a specific time period.
     * The method filters records using a prefix match on the date string (e.g., "YYYY-MM" or "YYYY"),
     * ordering the results by date and ID in descending order.
     *
     * @param period The time period prefix to filter by (e.g., "2026-01" for a specific month
     *               or "2026" for a full year).
     * @return An {@link ArrayList} of {@link Transaction} objects that fall within the specified period.
     */
    public ArrayList<Transaction> getTransactionsForPeriod(String period) {
        ArrayList<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE date LIKE '" + period + "%' " +
                " ORDER BY date DESC, id DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * Calculates the total expenses for each category within a specific month.
     * This method is primarily used for generating chart data, aggregating only
     * negative transaction amounts (expenses) filtered by the provided month string.
     *
     * @param month The time period to filter by, typically in "YYYY-MM" format.
     * @return An {@link ArrayList} of strings, where each entry is formatted as
     *         "categoryName;absoluteAmount", representing the total spent in that category.
     */
    public ArrayList<String> getCategoryStats(String month) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT category, SUM(amount) FROM " + TABLE_NAME +
                " WHERE amount < 0 AND date LIKE '" + month + "%'" +
                " GROUP BY category";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                double amount = cursor.getDouble(1);
                list.add(cat + ";" + Math.abs(amount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}