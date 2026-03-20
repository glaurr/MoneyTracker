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

public class Transaction {
    int id;
    String description;
    double amount;
    String category;
    String date;

    public Transaction(int id, String description, double amount, String category, String date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }
}