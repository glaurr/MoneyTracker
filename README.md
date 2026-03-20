# 💰 MoneyTracker

A native Android application for smart personal income and expense management.

## ✨ Key Features

* **💸 Balance Tracking:** Automatic and precise calculation of the total balance based on entered income and expenses.
* **🛍️ Category System:** Transactions are intuitively grouped by emoji icons (e.g., 🍔, 🚗, 📝) for immediate visual identification.
* **📈 Fluid Interface:** Optimized display of the transaction history using an efficient list system.
* **📊 Visual Statistics:** A dedicated stats tab that provides clear insights and summaries of your spending habits and income over time.
* **📁 Solid Architecture:** Visual resources (texts, theme colors) are managed natively and securely, following Android development best practices.
* **💾 Backup & Restore:** Export your local database to your phone storage and import it anytime so you never lose your history.
* **🌙 Hardcore Dark Mode:** A fully dark interface, optimized to reduce eye strain and save battery life on OLED screens.

## 📸 Screenshots

| Main Screen | Statistics |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/c717a9ad-26b4-4b9a-afcd-87eebae1ac94" width="435" /> | <img src="https://github.com/user-attachments/assets/853990d5-af14-443e-8d3e-e12d90cd7626" width="435" /> |

## 🛠️ Built With

* **Language:** Java
* **UI/Design:** XML (Native Android Views)
* **Database:** SQLite (Local database management)
* **Architecture:** MVC (Model-View-Controller adapted for Android)

## ⚙️ How to Install

1. Download the latest `.apk` file from the **Releases** section (or build the project locally).
2. Transfer the file to your Android phone.
3. Allow installation from "Unknown sources" in your phone's security settings.
4. Install and enjoy the app!

## 📜 License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## 🗺️ Roadmap (Upcoming Features)

Here are some of the planned features and improvements for future releases:

* **🌍 Multi-language Support:** Add full bilingual support (English & Romanian) by extracting and managing all text resources within localized `values/strings.xml` and `values-en/strings.xml` directories.
* **🚉 Better Filtering:** Add the option to search and filter by category and/or description.
* **🛡️ Safe Database Migrations:** Refactor the `onUpgrade` method in the SQLite `DatabaseHelper` to perform safe schema migrations (e.g., using `ALTER TABLE`) instead of dropping the database, ensuring zero data loss during app updates.
