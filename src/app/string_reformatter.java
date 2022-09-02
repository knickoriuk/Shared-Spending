package app;

import java.util.ArrayList;

public class string_reformatter {

    /**
     * Converts strings to Title Case.
     * @param input (String) Some text to be converted to title case.
     * @return (String) The given string, now in Title Case
     */
    public static String to_titlecase(String input) {
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
     * Converts an array of strings to Title Case
     * @param input_array (ArrayList<String>) array of strings to convert to Title Case
     * @return a new array of strings in Title Case
     */
    public static ArrayList<String> to_titlecase(ArrayList<String> input_array) {
        ArrayList<String> output_array = new ArrayList<>();
        for (String element : input_array) {
            output_array.add(to_titlecase(element));
        }
        return output_array;
    }
}
