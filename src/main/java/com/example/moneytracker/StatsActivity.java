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

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {
    LinearLayout layoutYearly, layoutMonthly;
    PieChart pieChart;
    DatabaseHelper dbHelper;

    /**
     * Initializes the activity, sets up the user interface components, and populates the
     * statistics sections with data from the database.
     *
     * <p>This method performs the following actions:
     * <ul>
     *     <li>Sets the content view to the activity_stats layout.</li>
     *     <li>Initializes references to the UI layouts and the PieChart.</li>
     *     <li>Instantiates the database helper to retrieve financial records.</li>
     *     <li>Configures and displays the PieChart for the current month's categories.</li>
     *     <li>Populates the yearly and monthly scrollable sections with aggregated financial data.</li>
     * </ul>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        layoutYearly = findViewById(R.id.layoutYearly);
        layoutMonthly = findViewById(R.id.layoutMonthly);
        pieChart = findViewById(R.id.pieChart);

        dbHelper = new DatabaseHelper(this);

        setupPieChart();

        populateSection(layoutYearly, dbHelper.getYearlyStats(), true);
        populateSection(layoutMonthly, dbHelper.getMonthlyStats(), false);
    }

    /**
     * Configures and populates the PieChart with category-based expense statistics for the current month.
     *
     * <p>This method retrieves transaction data grouped by category from the database for the current
     * year and month. It processes this data into {@link PieEntry} objects, applies visual styling
     * (colors, text sizes, animations), and updates the visibility of the chart. If no data is
     * available for the current month, the chart is hidden.</p>
     */
    private void setupPieChart() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        ArrayList<String> data = dbHelper.getCategoryStats(currentMonth);

        List<PieEntry> pieEntries = new ArrayList<>();

        for (String item : data) {
            String[] parts = item.split(";");
            String category = parts[0];
            float amount = Float.parseFloat(parts[1]);
            if (category.isEmpty()) category = getString(R.string.cat_other);
            pieEntries.add(new PieEntry(amount, category));
        }

        if (pieEntries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        } else {
            pieChart.setVisibility(View.VISIBLE);
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, getString(R.string.lbl_empty));
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.white));
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(getString(R.string.lbl_this_month));
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.black));
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.white));
        pieChart.setEntryLabelColor(ContextCompat.getColor(this, R.color.white));
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    /**
     * Populates a layout container with visual cards representing financial statistics for specific periods.
     *
     * <p>This method iterates through a list of formatted string records (expected format: "period;income;expense"),
     * calculates the net balance, and dynamically creates UI components for each entry. If the data list
     * is empty, a default "no data" message is displayed.</p>
     *
     * <p>Visual styling and behavior differ based on the {@code isYearly} flag:
     * <ul>
     *     <li><b>Yearly:</b> Larger font sizes and a distinct background color for broad overviews.</li>
     *     <li><b>Monthly:</b> Smaller font sizes and an interactive click listener that allows
     *     users to expand the card to view detailed transaction breakdowns.</li>
     * </ul>
     * </p>
     *
     * @param container The {@link LinearLayout} where the generated statistic cards will be added.
     */
    private void populateSection(LinearLayout container, ArrayList<String> dataList, boolean isYearly) {
        if (dataList.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.msg_error_no_transaction);
            empty.setTextColor(ContextCompat.getColor(this, R.color.gray));
            container.addView(empty);
            return;
        }

        for (String record : dataList) {
            String[] parts = record.split(";");
            final String periodLabel = parts[0];
            double income = Double.parseDouble(parts[1]);
            double expense = Double.parseDouble(parts[2]);
            double balance = income + expense;

            LinearLayout card = new LinearLayout(this);
            int color = isYearly ? R.color.mettalic_black : R.color.blackout;
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(40, 40, 40, 40);
            card.setBackgroundColor(ContextCompat.getColor(this, color));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 20);
            card.setLayoutParams(params);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(periodLabel);
            tvTitle.setTextSize(isYearly ? 22 : 18);
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
            tvTitle.setTypeface(null, Typeface.BOLD);

            TextView tvDetails = new TextView(this);
            tvDetails.setText(String.format(getString(R.string.lbl_income_expense), income, expense));
            tvDetails.setTextColor(ContextCompat.getColor(this, R.color.lightgray));
            tvDetails.setTextSize(14);
            tvDetails.setPadding(0, 10, 0, 10);

            TextView tvBalance = new TextView(this);
            int colorBalance = balance >= 0 ? R.color.green : R.color.red;
            tvBalance.setText(String.format(getString(R.string.lbl_total), String.format(getString(R.string.lbl_balance), balance)));
            tvBalance.setTextSize(16);
            tvBalance.setTypeface(null, Typeface.BOLD);
            tvBalance.setTextColor(ContextCompat.getColor(this, colorBalance));
            tvBalance.setGravity(Gravity.END);

            card.addView(tvTitle);
            card.addView(tvDetails);
            card.addView(tvBalance);

            if (!isYearly) {
                LinearLayout hiddenDetails = new LinearLayout(this);
                hiddenDetails.setOrientation(LinearLayout.VERTICAL);
                hiddenDetails.setVisibility(View.GONE);
                hiddenDetails.setPadding(10, 20, 10, 0);

                card.addView(hiddenDetails);

                card.setOnClickListener(v -> {
                    if (hiddenDetails.getVisibility() == View.VISIBLE) {
                        hiddenDetails.setVisibility(View.GONE);
                    } else {
                        loadDetailsIntoView(hiddenDetails, periodLabel);
                        hiddenDetails.setVisibility(View.VISIBLE);
                    }
                });
            }
            container.addView(card);
        }
    }

    /**
     * Dynamically populates a container with a detailed list of transactions for a specific period.
     *
     * <p>This method clears the existing views in the provided container and fetches all
     * {@link Transaction} objects associated with the given period from the database.
     * For each transaction, it creates a horizontal layout displaying the day of the month,
     * the transaction description, and the amount (color-coded green for income and red
     * for expenses).</p>
     *
     * <p>If no transactions are found for the period, a message indicating the absence of
     * data is displayed instead.</p>
     *
     * @param container The {@link LinearLayout} (usually a hidden details pane) where the
     *                  transaction rows will be injected.
     * @param period    The time period (e.g., "yyyy-MM") used to filter transactions from the database.
     */
    private void loadDetailsIntoView(LinearLayout container, String period) {
        container.removeAllViews();

        View line = new View(this);
        line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray));
        container.addView(line);

        ArrayList<Transaction> list = dbHelper.getTransactionsForPeriod(period);

        if (list.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText(R.string.msg_error_no_transaction);
            tv.setTextColor(ContextCompat.getColor(this, R.color.gray));
            tv.setPadding(0, 20, 0, 0);
            container.addView(tv);
            return;
        }

        for (Transaction t : list) {
            String day = t.date.length() >= 10 ? t.date.substring(8, 10) : getString(R.string.lbl_empty);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 15, 0, 15);

            TextView tvDay = new TextView(this);
            tvDay.setText(day);
            tvDay.setTextColor(ContextCompat.getColor(this, R.color.lightgray));
            tvDay.setTextSize(14);
            tvDay.setTypeface(null, Typeface.BOLD);
            tvDay.setPadding(0, 0, 15, 0);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(t.description);
            tvDesc.setTextColor(ContextCompat.getColor(this, R.color.white));
            tvDesc.setTextSize(15);
            tvDesc.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format(getString(R.string.lbl_amount), t.amount));
            tvAmount.setTextSize(15);
            tvAmount.setTypeface(null, Typeface.BOLD);

            if(t.amount < 0) {
                tvAmount.setTextColor(ContextCompat.getColor(this, R.color.red));
            } else {
                tvAmount.setTextColor(ContextCompat.getColor(this, R.color.green));
                tvAmount.setText(getString(R.string.lbl_plus_sign) + String.format(getString(R.string.lbl_amount), t.amount));
            }

            row.addView(tvDay);
            row.addView(tvDesc);
            row.addView(tvAmount);
            container.addView(row);
        }
    }
}