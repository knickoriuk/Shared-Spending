package db_control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;

public class get {

    /**
     * Creates a list of all unique names in the database.
     * @return (ArrayList<String>) An array of names of individuals in the database.
     */
    private static ArrayList<String> names() {
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
     * Finds the total amount of money spent by the given person.
     * @param name (String) The name of the person to find total purchases of
     * @return (float) Total value of all purchases made by {name}
     */
    public static float individual_sum(String name) {
        float sum = 0;

        // Verify that the name is valid
        ArrayList<String> valid_names = get.names();
        if (!valid_names.contains(name.toUpperCase())) {
            System.err.println("Name " + name + " not found in database.");

        } else { // Now find the total in the database
            ResultSet rs;
            String sql = "SELECT SUM(Cost) FROM Purchases WHERE Name = '";
            sql = sql.concat(name.toUpperCase() + "'");
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
     * Calculates the total of all spending recorded in the database.
     * @return (float) Total for all purchases recorded in the database.
     */
    public static float all_time_total() {
        float sum = 0;
        ResultSet rs;
        String sql = "SELECT SUM(Cost) FROM Purchases";
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
     * Obtains the date of the first purchase in the database
     * @return (String) The earliest date stored in the database
     */
    public static String earliest_date() {
        String date = "";
        String sql = "SELECT MIN(Date) FROM Purchases";
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
     * Creates an ordered list of names by each person's total spending, in descending order.
     * @return (ArrayList<String>) Array of names, sorted by total spent, descending
     */
    private static ArrayList<String> ordered_names() {
        ArrayList<String> names_by_cost = new ArrayList<>();
        String sql = "Select Name FROM Purchases GROUP BY Name ORDER BY Sum(Cost) DESC";
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
     * Finds the last 10 purchases made. Formats into a 2D array, to be used in a JTable constructor.
     * @return (String[][]) Array of {{Date, name, cost}, {Date, name, cost}, ...}
     */
    public static String[][] most_recent_purchases() {
        String name;
        String date;
        String cost;
        int index = 0;
        String[][] most_recent = new String[10][3];
        String sql = "SELECT * FROM Purchases ORDER BY Date DESC LIMIT 10";
        ResultSet rs = db.query(sql);

        try {
            while (rs.next()) {
                name = to_titlecase(rs.getString("Name"));
                date = rs.getString("Date");
                cost = String.format("$%.2f", rs.getFloat("Cost"));
                most_recent[index] = new String[] {date, name, cost}; // Append this row to the most_recent array
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
                csv_writer.append(String.format("%s,%s,%s\n", date, name, cost));
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
     * Converts the database's BLOCK CASE strings to Title Case.
     * @param input (String) Some text to be converted to title case.
     * @return (String) The given string, now in Title Case
     */
    private static String to_titlecase(String input) {
        String input_lowercase = input.toLowerCase();
        StringBuilder output = new StringBuilder(input.length());
        boolean capitalize_next = true; // Capitalize first letter always
        for (char c : input_lowercase.toCharArray()) {

            // If space, capitalize next char
            if (Character.isSpaceChar(c)) {
                capitalize_next = true;

            // Otherwise if this should be a capital, capitalize it.
            } else if (capitalize_next) {
                c = Character.toTitleCase(c);
                capitalize_next = false;
            }
            output.append(c);
        }
        return output.toString();
    }

    /**
     * Generates a weighted average of money spent per day, weighted by how many years ago the purchase was.
     * @return (double) A weighted average of spending per day
     */
    private static double weighted_average() {
        double avg = 0;
        double weights = 0;
        String date = earliest_date();

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
            String sql = String.format("SELECT Sum(Cost * %.4f) FROM Purchases WHERE Date BETWEEN '%s' AND '%s'",
                    year_weight, earlier_date, later_date);
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

    /**
     * Generates a summary of all purchases in the database as a printable string.
     * @return (String) A summary of purchase history in the database.
     */
    public static String summary() {
        float all_time = all_time_total();
        String date = earliest_date();
        ArrayList<String> names_list = ordered_names();
        String summary_output = "";

        // If no data so far, return a 'welcome!' string
        if (all_time == 0.0) {
            return "Welcome to the Shared Spending Manager.\n\nAdd transaction history with " +
                    "\"Add New Purchase\" and a summary will appear here.";
        }

        // Individual Sums
        for (String s : names_list) {
            summary_output = summary_output.concat(String.format("%s has spent $%.2f.\n",
                    to_titlecase(s),
                    individual_sum(s)
            ));
        }
        summary_output = summary_output.concat("\n");

        // Differences in sums
        if (names_list.size() > 1) { // Only bother if there's more than one person in the database
            for (int i=names_list.size()-1; i>0; i--) {
                float difference = individual_sum(names_list.get(0)) - individual_sum(names_list.get(i));
                summary_output = summary_output.concat(String.format("%s has spent $%.2f less than %s.\n",
                        to_titlecase(names_list.get(i)),
                        difference,
                        to_titlecase(names_list.get(0))
                ));
            }
        }
        summary_output = summary_output.concat("\n");

        // Daily Average and Total
        LocalDate earliest = LocalDate.parse(date);
        summary_output = summary_output.concat(String.format("Since %s, $%.2f has been spent, averaging at $%.2f per day.",
                earliest.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                all_time,
                weighted_average()
        ));

        return summary_output;
    }
}
