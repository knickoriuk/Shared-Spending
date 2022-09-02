package db_control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;
import static app.string_reformatter.to_titlecase;

public class get {

    /**
     * Creates a list of all unique names in the database. Names will be in UPPERCASE.
     * @return (ArrayList<String>) An array of names of individuals in the database.
     */
    public static ArrayList<String> names() {
        ArrayList<String> names = new ArrayList<>();
        String sql = "SELECT DISTINCT Name FROM Purchases";
        ResultSet rs = db.query(sql);

        try {
            while (rs.next()) {
                names.add(rs.getString("Name"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the ResultSet
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return names;
    }

    /**
     * Creates an ordered list of names sorted by each person's total spending, in descending order.
     * @param category (String) The category by which to determine total spending in. If null, includes all categories.
     * @return (ArrayList<String>) Array of names, sorted by total spent, descending
     */
    public static ArrayList<String> ordered_names(String category) {
        ArrayList<String> names_by_cost = new ArrayList<>();
        String sql;
        if (category == null) {
            sql = "Select Name FROM Purchases GROUP BY Name ORDER BY Sum(Cost) DESC";
        } else {
            sql = String.format("Select Name FROM Purchases WHERE Category = '%s' GROUP BY Name ORDER BY Sum(Cost) DESC", category.toUpperCase());
        }
        ResultSet rs = db.query(sql);

        try {
            while(rs.next()) {
                names_by_cost.add(rs.getString("Name"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the ResultSet
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return names_by_cost;
    }

    /**
     * Creates a list of all unique categories in the database. Categories will be in UPPERCASE.
     * @return (ArrayList<String>) An array of categories of purchases in the database.
     */
    public static ArrayList<String> categories() {
        ArrayList<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT Category FROM Purchases";
        ResultSet rs = db.query(sql);

        try {
            while (rs.next()) {
                categories.add(rs.getString("Category"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the ResultSet
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return categories;
    }

    /**
     * Finds the total amount of money spent by the given person in the listed category.
     * @param name (String) The name of the person to find total purchases of
     * @param category (String) The category of purchases to total. If null, total over all categories
     * @return (float) Total value of all purchases made by {name}
     */
    public static float individual_sum(String name, String category) {
        float sum = 0;
        ArrayList<String> valid_names = get.names();
        ArrayList<String> valid_cats = get.categories();

        // Verify that the name is valid
        if (!valid_names.contains(name.toUpperCase())) {
            System.err.println("Name " + name + " not found in database.");

        // Verify the category is valid OR null
        } else if (category != null && !valid_cats.contains(category.toUpperCase())) {
            System.err.println("Category " + category + " not found in database.");

        } else { // Now find the total in the database
            ResultSet rs;
            String sql;
            if (category == null) {
                sql = String.format("SELECT SUM(Cost) FROM Purchases WHERE Name = '%s'",
                        name.toUpperCase());
            } else {
                sql = String.format("SELECT SUM(Cost) FROM Purchases WHERE Name = '%s' AND Category = '%s'",
                        name.toUpperCase(), category.toUpperCase());
            }
            rs = db.query(sql);

            try {
                sum = rs.getFloat(1);
            } catch (SQLException e) {
                System.out.println(e.getMessage());

            } finally { // Close the ResultSet
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return sum;
    }

    /**
     * Calculates the total of spending recorded in the database for the given category.
     * @param category (String) Category to determine total of. If null, total over all categories.
     * @return (float) Total for all purchases in that category.
     */
    public static float total(String category) {
        float sum = 0;
        ResultSet rs;
        String sql;
        if (category == null) {
            sql = "SELECT SUM(Cost) FROM Purchases";
        } else {
            sql = String.format("SELECT SUM(Cost) FROM Purchases WHERE Category = '%s'", category.toUpperCase());
        }
        rs = db.query(sql);

        try {
            sum = rs.getFloat(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the ResultSet
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return sum;
    }

    /**
     * Obtains the date of the first purchase in the database within the specified category
     * @param category (String) The category of transactions to look into. Pass category = null to query over all categories
     * @return (String) The earliest date
     */
    public static String earliest_date(String category) {
        String date = "";
        String sql;
        if (category == null) {
            sql = "SELECT MIN(Date) FROM Purchases";
        } else {
            sql = String.format("SELECT MIN(Date) FROM Purchases WHERE Category = '%s'", category);
        }
        ResultSet rs = db.query(sql);

        try {
            date = rs.getString(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the ResultSet
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return date;
    }

    /**
     * Finds the last 20 purchases made. Formats into a 2D array, to be used in a JTable constructor.
     * @return (String[][]) Array of {{Date, name, cost}, {Date, name, cost}, ...}
     */
    public static String[][] most_recent_purchases() {
        String name;
        String date;
        String cost;
        String category;
        int index = 0;
        String[][] most_recent = new String[20][4];
        String sql = "SELECT * FROM Purchases ORDER BY Date DESC LIMIT 20";
        ResultSet rs = db.query(sql);

        try {
            while (rs.next()) {
                name = to_titlecase(rs.getString("Name"));
                date = rs.getString("Date");
                cost = String.format("$%.2f", rs.getFloat("Cost"));
                category = to_titlecase(rs.getString("Category"));
                most_recent[index] = new String[] {date, name, cost, category}; // Append this row to the most_recent array
                index += 1;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the ResultSet
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return most_recent;
    }

    /**
     * Exports the current database to a csv file.
     * @return (String) The path of the new .csv file
     */
    public static String db_to_csv() {
        String name;
        String date;
        String cost;
        String category;
        String path_out = "";
        FileWriter csv_writer = null;
        String sql = "SELECT * FROM Purchases";
        ResultSet rs = db.query(sql);

        try {
            csv_writer = new FileWriter("shared_spending_export.csv");
            path_out = new File("shared_spending_export.csv").getAbsolutePath(); // Get path of new file

            // Create Header
            csv_writer.append("Date,Name,Cost\n");

            while (rs.next()) { // Write Contents
                name = rs.getString("Name");
                date = rs.getString("Date");
                cost = rs.getString("Cost");
                category = rs.getString("Category");
                csv_writer.append(String.format("%s,%s,%s,%s\n", date, name, cost, category));
            }

        } catch (IOException | SQLException e) {
            System.out.println(e.getMessage());

        } finally { // Close the FileWriter and ResultSet
            try{
                rs.close();
                if (csv_writer != null) {
                    csv_writer.flush();
                    csv_writer.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return path_out;
    }

    /**
     * Generates a weighted average of money spent per day, weighted by how many years ago the purchase was. Used to
     * prevent old entries from affecting the average too much.
     * @param category (String) The category to determine the weighted average across
     * @return (double) A weighted average of spending per day
     */
    public static double weighted_average(String category) {
        double avg = 0;
        double weights = 0;
        String date = earliest_date(category);

        // How many years are recorded?
        LocalDate today = LocalDate.now();
        LocalDate earliest = LocalDate.parse(date);
        int num_years = (int) (ChronoUnit.YEARS.between(earliest, today)) + 1;

        // Get weighted sum per year
        for (int i=1; i<=num_years; i++) {
            double year_weight = 1./i;
            String earlier_date = today.minusYears(i).plusDays(1).toString();
            String later_date = today.minusYears(i-1).toString();

            double days_between = 365.2425; // Days in the year
            if (i == num_years) { // If first year, only count how many days since the first entry
                days_between = ChronoUnit.DAYS.between(earliest, today.minusYears(i-1)) + 1;
            }

            // Get sum of costs in the year, each multiplied by the weight
            String sql;
            if (category == null) {
                sql = String.format("SELECT Sum(Cost * %.4f) FROM Purchases WHERE Date BETWEEN '%s' AND '%s'",
                        year_weight, earlier_date, later_date);
            } else {
                sql = String.format("SELECT Sum(Cost * %.4f) FROM Purchases WHERE Date BETWEEN '%s' AND '%s' AND Category = '%s'",
                        year_weight, earlier_date, later_date, category);
            }

            ResultSet rs = db.query(sql);

            try {
                // This year's average spent per day: rs.getFloat(1)/(days_between * year_weight)
                avg += rs.getFloat(1)/days_between; // Sum of (1/n * purchase_cost) for n from 1 to (# of years)
                weights += year_weight; // Sum of weights of (1/n) for n from 1 to (# of years)

            } catch (SQLException e) {
                System.out.println(e.getMessage());

            } finally { // Close the ResultSet
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return (avg/weights);
    }

}
