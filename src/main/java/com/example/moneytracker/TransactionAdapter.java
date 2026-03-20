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

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    Context context;
    ArrayList<Transaction> list;
    DatabaseHelper dbHelper;
    Runnable onUpdateListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvDate, tvAmount, tvCategory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }

    /**
     * Constructs a new TransactionAdapter to manage and display transaction data in a RecyclerView.
     *
     * @param context          The context used for layout inflation and UI components.
     * @param list             The list of transaction objects to be displayed.
     * @param dbHelper         The database helper used for performing delete operations.
     * @param onUpdateListener A callback triggered when the data set changes (e.g., after deletion)
     *                         to refresh UI elements outside the adapter.
     */
    public TransactionAdapter(Context context, ArrayList<Transaction> list, DatabaseHelper dbHelper, Runnable onUpdateListener) {
        this.context = context;
        this.list = list;
        this.dbHelper = dbHelper;
        this.onUpdateListener = onUpdateListener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item. This method inflates the "item_transaction" layout and creates a new
     * ViewHolder instance to hold the view references.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View (not used in this implementation).
     * @return A new ViewHolder that holds the View for a transaction item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data from a specific {@link Transaction} object to the views held by the {@link ViewHolder}.
     * This method updates the UI components for the transaction's description, date, amount, and category,
     * sets text colors based on the transaction value, and attaches a long-click listener to facilitate
     * record deletion via a confirmation dialog.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item
     *                 at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = list.get(position);

        holder.tvDesc.setText(t.description);
        holder.tvDate.setText(t.date);

        if (t.amount < 0) {
            holder.tvAmount.setText(String.format(context.getString(R.string.lbl_balance), t.amount));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.tvAmount.setText(context.getString(R.string.lbl_plus_sign) + String.format(context.getString(R.string.lbl_balance), t.amount));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
        }

        holder.tvCategory.setText(getCategoryIcon(t.category));
        holder.tvCategory.setBackgroundColor(getCategoryColor(t.category));
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_delete)
                    .setMessage(R.string.dialog_delete_msg)
                    .setPositiveButton(R.string.dialog_btn_yes, (dialog, which) -> {
                        dbHelper.deleteTransaction(t.id);
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        onUpdateListener.run();
                    })
                    .setNegativeButton(R.string.dialog_btn_no, null)
                    .show();
            return true;
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The size of the transaction list.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Returns a visual representation (emoji) for a given transaction category.
     *
     * @param category The name of the category to map to an icon.
     * @return A string containing the emoji icon corresponding to the category;
     *         returns "?" if the category is null or empty, or a default icon "📝"
     *         if the category is not recognized.
     */
    private String getCategoryIcon(String category) {
        if (category == null || category.isEmpty()) return "?";
        if (category.equals(context.getString(R.string.cat_food))) return "🍔";
        if (category.equals(context.getString(R.string.cat_salary))) return "💰";
        if (category.equals(context.getString(R.string.cat_rent))) return "🏠";
        if (category.equals(context.getString(R.string.cat_transport))) return "🚗";
        if (category.equals(context.getString(R.string.cat_distraction))) return "🎉";
        if (category.equals(context.getString(R.string.cat_other))) return "📦";
        return "📝";
    }

    /**
     * Determines the ARGB color value associated with a specific transaction category.
     *
     * @param category The name of the category to map to a color.
     * @return The resolved color integer; returns black if the category is null or empty,
     *         or gray if the category is not recognized.
     */
    private int getCategoryColor(String category) {
        if (category == null || category.isEmpty()) return ContextCompat.getColor(context, R.color.black);
        if (category.equals(context.getString(R.string.cat_food))) return ContextCompat.getColor(context, R.color.orange);
        if (category.equals(context.getString(R.string.cat_salary))) return ContextCompat.getColor(context, R.color.green);
        if (category.equals(context.getString(R.string.cat_rent))) return ContextCompat.getColor(context, R.color.red);
        if (category.equals(context.getString(R.string.cat_transport))) return ContextCompat.getColor(context, R.color.blue);
        if (category.equals(context.getString(R.string.cat_distraction))) return ContextCompat.getColor(context, R.color.violet);
        return ContextCompat.getColor(context, R.color.gray);
    }
}