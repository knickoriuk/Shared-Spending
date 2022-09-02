package db_control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import except.*;

public class add {

    /**
     * Given some date string, confirm the date exists, is not in the future, and is correctly formatted.
     * @param date_string (String) Date to verify correctness of
     * @return (boolean) Returns true if date is a valid YYYY-MM-DD format, throws exception otherwise.
     */
    public static boolean confirm_date_format(String date_string) throws future_date_exception, date_format_exception {
        try {
            LocalDate now = LocalDate.now();
            LocalDate date = LocalDate.parse(date_string);

            // Check if the date is in the future
            if (date.isAfter(now)) {
                throw new future_date_exception("Invalid Date: " + date_string + "\nDate must not be in the future");
            }

        // If an exception is thrown, it is because the string was incorrectly formatted
        } catch (DateTimeParseException e) {
            throw new date_format_exception("Invalid Date: " + date_string + "\nFormat the date as YYYY-MM-DD");
        }

        return true;
    }

    /**
     * Adds the purchase to the database after confirming an accurate input format.
     * @param cost_str (String) Cost of the purchase.
     * @param date (String) Date the purchase was made. Will not process it unless formatted as YYYY-MM-DD
     * @param name (String) Name of the person who made the purchase
     * @param category (String) Category of the purchase. No category / an empty string is allowed.
     */
    public static void purchase(String cost_str, String date, String name, String category) throws future_date_exception, date_format_exception, empty_input_exception, invalid_cost_exception {
        // Check if any of the inputs were empty strings
        if (name.isBlank() | cost_str.isBlank() | date.isBlank()) {
            throw new empty_input_exception("Fields for date, name and cost are required.");
        }

        // Replace category if blank given
        if (category.isBlank()) {
            category = "UNCATEGORIZED";
        }

        float cost = Float.parseFloat(cost_str);

        // Check that the date is formatted correctly. If not, it throws exceptions here.
        if (!confirm_date_format(date)) {
            return;
        }

        // Check that the cost is valid.
        if (cost <= 0) {
            throw new invalid_cost_exception("Invalid cost: $" + String.format(cost_str, "%.2f") + "\nPlease check and try again.");
        }

        // Now we can safely add purchase to the db
        db.add_purchase(cost_str, date, name, category);
    }

    /**
     * Given a path to a .csv file, add the contents of the .csv to the database.
     * First confirms the file is accurately structured. Throws exception otherwise.
     * @param filepath (String) Path to the .csv file to be added.
     */
    public static void from_csv(String filepath) throws file_format_exception,
            future_date_exception, invalid_cost_exception, empty_input_exception, date_format_exception {
        BufferedReader csv_reader = null;
        try {
            csv_reader = new BufferedReader(new FileReader(filepath));
            String row = csv_reader.readLine(); // Read first line
            String[] header = row.split(","); // Split line into array values

            if (Arrays.stream(header).count() != 4) { // Check if there is the correct number of columns
                throw new file_format_exception("The .csv file must have 4 columns: Date, Name, Cost, and Category.");

            // Check if columns are in the right order: date, name, cost, category.
            } else if (!header[0].equals("Date") | !header[1].equals("Name") | !header[2].equals("Cost") | !header[3].equals("Category")) {
                throw new file_format_exception("The .csv file must have appropriate data stored\nwithin 3 columns," +
                        " in this order: Date, Name, Cost.");
            }

            // Empty the database.
            db.clear_table();
            db.initialize();

            // Read in and add info from the file
            while ((row = csv_reader.readLine()) != null) {
                String[] data = row.split(",");
                purchase(data[2], data[0], data[1], data[3]);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());

        } finally { // Close the BufferedReader
            try{
                if (csv_reader != null) {
                    csv_reader.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
