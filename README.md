# Java Quiz Application

A desktop quiz app built with Java Swing, backed by a MySQL database via JDBC — with a built-in fallback question set so it still runs even if the database is unavailable.

## Features
- Multiple-choice quiz with real-time answer checking (green = correct option, red = incorrect selection)
- Progress tracking ("Question X of Y")
- Falls back to a small built-in question set automatically if the database can't be reached or has no rows, so the app never fails to run
- Play Again support to restart the quiz from question one
- Questions are stored in and loaded from a relational database rather than hardcoded

## Tech Stack
- **Java** (Swing for the UI)
- **MySQL** for data storage
- **JDBC** (MySQL Connector/J) for database access

## Project Structure
Kept as a single file for simplicity:
- `QuizApp.java` — contains three classes:
  - `DBConnection` — opens the JDBC connection to MySQL
  - `Question` — simple data model for one quiz question
  - `QuizApp` — the Swing UI and all quiz logic (entry point: `main`)

1. Install MySQL and run the schema above to create the database and table.
2. Open `QuizApp.java` and update the `DBConnection` class with your own MySQL credentials:
   ```java
   private static final String USER = "root";
   private static final String PASSWORD = "your_password_here";
   ```
   > ⚠️ If you're pushing this to a public repo, avoid committing your real password — consider moving these values into a `.gitignore`d config file instead.
3. Download [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and add the `.jar` to your classpath.
4. Compile and run:
   ```
   javac -cp ".;mysql-connector-j-9.x.x.jar" QuizApp.java
   java -cp ".;mysql-connector-j-9.x.x.jar" QuizApp
   ```
   (use `:` instead of `;` on Mac/Linux, matching the jar filename to whatever version you downloaded)

## What I Learned
- Building a Swing UI with CardLayout, custom rendering, and event-driven logic
- Connecting a desktop Java app to MySQL using JDBC (PreparedStatement, ResultSet)
- Designing graceful fallback behavior so the app degrades instead of crashing when a dependency (the database) is unavailable
- Debugging classpath and driver-loading issues in a non-Maven Java setup
