package db_control;

import java.sql.*;

public class db {

    /**
     * Create a connection to the database file
     * @return (Connection) The connection to purchase_history.db database
     */
    private static Connection connect() {
        String url = "jdbc:sqlite:purchase_history.db";
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     *  Creates the table if it doesn't exist. Run this function every time the app is run.
     */
    public static void initialize() {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS Purchases (Cost FLOAT, Date TEXT, Name TEXT)";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Drop the table. Delete the database.
     */
    public static void clear_table() {
        Connection conn = connect();
        String sql = "DROP TABLE IF EXISTS Purchases";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Run a specified SQL query on the database.
     * @param sql (String) An SQLite Query statement.
     * @return (ResultSet) The results from the query.
     */
    protected static ResultSet query(String sql) {
        Connection conn = connect();
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    /**
     * Add a purchase to the database. Must ensure the parameters are valid before passing to this function.
     * @param cost (String) Cost of purchase
     * @param date (String) Date of purchase, formatted in YYYY-MM-DD
     * @param name (String) Name of person who made the purchase
     */
    protected static void add_purchase(String cost, String date, String name) {
        Connection conn = connect();
        String sql = String.format("INSERT INTO Purchases (Cost, Date, Name) VALUES ('%s', DATE('%s'), '%s')",
                cost, date, name.toUpperCase());

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Remove all database entries of the given date.
     * @param date (String) The date of which to remove all database entries, in the form YYYY-MM-DD
     */
    public static void clear_date(String date) {
        Connection conn = connect();
        String sql = String.format("DELETE FROM Purchases WHERE Date = '%s'", date);

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
