package app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import db_control.get;
import static app.string_reformatter.to_titlecase;

public class summary {

    /**
     * Generates a summary of all purchases in the database as a printable string.
     * @param simple (Boolean) true if output should be shortened
     * @return (String) A summary of purchase history in the database.
     */
    public static String summarize_db(Boolean simple) {
        ArrayList<String> all_cats = get.categories();
        ArrayList<String> names_list = get.ordered_names(null);

        // If no data so far, return a 'welcome!' string
        if (names_list.isEmpty()) {
            return "Welcome to the Shared Spending Manager.\n\nAdd transaction history with " +
                    "\"Add New Purchase\" and a summary will appear here.";
        }

        // Generate summary across all categories
        String summary_output = summary_snippet(null, simple);

        // If categories are being used, make summaries for each category
        if (all_cats.size() != 1) {

            for (String category : all_cats) {
                summary_output = summary_output.concat("\n~~~~~~~~~~~~~~~~~~~~~~~~\n");
                summary_output = summary_output.concat(summary_snippet(category, simple));
            }
        }
        return summary_output;
    }

    /**
     *
     * @param category (String) category to summarize, if null, summarize over all categories.
     * @param simple (Boolean) true if output should be shortened
     * @return a string containing a summary of purchase details for the given category.
     */
    private static String summary_snippet(String category, Boolean simple) {
        String snippet;
        String date;
        LocalDate earliest;
        float total;
        ArrayList<String> names_list = get.ordered_names(category);

        // Create a Category Header
        if (category == null) {
            snippet = "TOTAL:\n\n";
        } else {
            snippet = String.format("%s:\n\n", category);
        }

        // Individual Sums
        for (String name : names_list) {
            snippet = snippet.concat(String.format("%s has spent $%.2f.\n",
                    to_titlecase(name),
                    get.individual_sum(name, category)
            ));
        }

        // Only add next sections if Simple Summary is off
        if (!simple) {

            snippet = snippet.concat("\n");
            // Differences in Sums
            if (names_list.size() > 1) { // Only bother if there's more than one person in the database
                for (int i = names_list.size() - 1; i > 0; i--) {
                    float difference = get.individual_sum(names_list.get(0), category) -
                            get.individual_sum(names_list.get(i), category);
                    snippet = snippet.concat(String.format("%s has spent $%.2f less than %s.\n",
                            to_titlecase(names_list.get(i)),
                            difference,
                            to_titlecase(names_list.get(0))
                    ));
                }
                snippet = snippet.concat("\n");
            }

            // Daily Average and Total
            date = get.earliest_date(category);
            earliest = LocalDate.parse(date);
            total = get.total(category);
            if (category == null) { // If no category, get all-time totals
                snippet = snippet.concat(String.format("Since %s, $%.2f has been spent, averaging at $%.2f per day.\n",
                        earliest.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                        total,
                        get.weighted_average(null)
                ));

            } else {
                snippet = snippet.concat(String.format("Since %s, $%.2f has been spent on %s, averaging at $%.2f per day.\n",
                        earliest.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                        total,
                        to_titlecase(category),
                        get.weighted_average(category)
                ));
            }
        }
        return snippet;
    }

}
