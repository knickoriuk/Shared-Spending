package app;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.io.File;
import java.text.*;
import java.util.ArrayList;
import java.util.Properties;
import org.jdatepicker.impl.*;
import except.*;
import db_control.*;
import static app.string_reformatter.to_titlecase;
import static app.summary.summarize_db;

public class gui {

    // Fonts
    public static final Font font_1 = new Font(Font.SANS_SERIF, Font.PLAIN,18); // Bigger, for text fields and summaries
    public static final Font font_2 = new Font(Font.MONOSPACED, Font.BOLD,14); // monospace, for tables.

    // Simple Summary Toggle
    public static Boolean simple = false;

    // Important components
    public static JFrame frame = new JFrame("Shared Spending Manager");
    public static JTextArea summary = new JTextArea(summarize_db(simple));
    public static JPanel panel_home = new JPanel();
    public static JPanel panel_summary = new JPanel(new BorderLayout());
    public static JPanel panel_add_new = new JPanel();
    public static JPanel panel_display_latest = new JPanel(new BorderLayout());
    public static JMenuBar menu = new JMenuBar();
    public static JTable table_latest = new JTable();
    public static JScrollPane scroll_pane_latest = new JScrollPane();
    public static JComboBox input_name = new JComboBox();
    public static JComboBox input_category = new JComboBox();

    public static void run_gui() {
        initialize_components();

        // Construct GUI
        frame.getContentPane().add(BorderLayout.CENTER, panel_summary);
        frame.getContentPane().add(BorderLayout.PAGE_END, panel_home);
        frame.getContentPane().add(BorderLayout.PAGE_START, menu);
        frame.setVisible(true);
    }

    /**
     * Builds all components: buttons, text fields, panels, listeners, and menu bar.
     */
    private static void initialize_components() {
        // JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 475);

        // JTextArea summary panel
        summary.setEditable(false);
        summary.setFont(font_1);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        JScrollPane scroll = new JScrollPane(summary);
        panel_summary.add(scroll, BorderLayout.CENTER);

            // ----------MENU BAR----------------------------------------------------------
        //Menu Items
        JMenuItem export_csv = make_menu_item("Export to .csv", 'X');
        JMenuItem import_csv = make_menu_item("Import from .csv", 'I');
        JMenuItem reset_db = make_menu_item("Reset Database", 'R');
        JMenuItem add_one = make_menu_item("Add Entry", 'A');
        JMenuItem remove_one = make_menu_item("Remove Entry", 'R');
        JMenuItem info = make_menu_item("General Usage", 'G');
        JMenuItem info_categories = make_menu_item("Categories", 'C');
        JMenuItem info_export = make_menu_item("Database Backup", 'D');
        JCheckBoxMenuItem toggle_simple = new JCheckBoxMenuItem("Simple Summaries");
        toggle_simple.setMnemonic('S');

        // Menu Options
        JMenu mb_file = make_menu_option("File", 'F');
        JMenu mb_edit = make_menu_option("Edit", 'E');
        JMenu mb_help = make_menu_option("Help", 'H');
        mb_file.add(import_csv);
        mb_file.add(export_csv);
        mb_file.addSeparator();
        mb_file.add(reset_db);
        mb_edit.add(toggle_simple);
        mb_edit.addSeparator();
        mb_edit.add(add_one);
        mb_edit.add(remove_one);
        mb_help.add(info);
        mb_help.add(info_categories);
        mb_help.add(info_export);

        // Menu Listeners
        export_csv.addActionListener(e -> export_csv());
        import_csv.addActionListener(e -> import_csv());
        reset_db.addActionListener(e -> reset_database());
        remove_one.addActionListener(e -> remove_entry());
        add_one.addActionListener(e -> replace_lower_panel(panel_add_new));
        info.addActionListener(e -> get_general_help());
        info_categories.addActionListener(e -> get_category_help());
        info_export.addActionListener(e -> get_export_help());
        toggle_simple.addActionListener(e -> {
            simple = !simple;
            summary.setText(summarize_db(simple));
        });

        // Construct Menu
        menu.add(mb_file);
        menu.add(mb_edit);
        menu.add(mb_help);

            // ----------HOME PAGE---------------------------------------------------------
        // Creating buttons
        JButton b_home_add = make_button("Add New Purchase", 'A');
        JButton b_home_display = make_button("Display Latest", 'D');

        // Button Listeners
        b_home_add.addActionListener(e -> replace_lower_panel(panel_add_new));
        b_home_display.addActionListener(e -> {
            // Rebuild the JTable each time in case database content has changed
            scroll_pane_latest.getViewport().remove(table_latest); // Remove old table
            table_latest = new JTable(get.most_recent_purchases(), new String[] {"Date", "Name", "Cost", "Category"}); // Rebuild table
            table_latest.setFont(font_2);
            table_latest.getTableHeader().setFont(font_1);
            table_latest.setDefaultEditor(Object.class, null); // Prevents editing the table
            scroll_pane_latest.getViewport().add(table_latest); // Add it back to the scroll pane
            replace_lower_panel(panel_display_latest);
        });

        // Construct Home Panel
        panel_home.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel_home.add(b_home_add);
        panel_home.add(Box.createHorizontalStrut(10));
        panel_home.add(b_home_display);

            // ----------ADD NEW PURCHASE---------------------------------------------------
        // Creating buttons
        JButton b_new_done = make_button("Add", 'A');
        JButton b_new_cancel = make_button("Cancel", 'C');

        // Fields for amount and dropdown name/category selection
        JFormattedTextField input_cost = new JFormattedTextField(new DecimalFormat("0.00"));
        input_cost.setPreferredSize(new Dimension(200,20));
        populate_comboboxes();
        input_name.setEditable(true);
        input_name.setPreferredSize(new Dimension(200,20));
        input_category.setEditable(true);
        input_category.setPreferredSize(new Dimension(200,20));

        // Date field
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl date_panel = new JDatePanelImpl(model, p);
        JDatePickerImpl input_date = new JDatePickerImpl(date_panel, new date_label_formatter());
        input_date.setPreferredSize(new Dimension(200,20));
        input_date.getComponent(0).setPreferredSize(new Dimension(200,20)); // TextField Dimensions
        input_date.getComponent(1).setPreferredSize(new Dimension(17,20)); // Button Dimensions

        // Labels
        JLabel l_cost = new JLabel("Cost of Purchase: $");
        JLabel l_name = new JLabel("Name of Buyer:   ");
        JLabel l_date = new JLabel("Date of Purchase:   ");
        JLabel l_cat = new JLabel("Purchase Category:   ");

        // Button Listeners
        b_new_cancel.addActionListener(e -> replace_lower_panel(panel_home));
        b_new_done.addActionListener(e -> {
            // Get cost amount and date as strings
            String cost = input_cost.getText();
            String date = input_date.getJFormattedTextField().getText();

            String category; // Get category from dropbox, unless empty
            if (input_category.getSelectedItem() == null) {
                category = "";
            } else {
                category = input_category.getSelectedItem().toString();
            }

            String name; // Get name from dropbox, unless empty
            if (input_name.getSelectedItem() == null) {
                name = "";
            } else {
                name = input_name.getSelectedItem().toString();
            }

            try {
                add.purchase(cost, date, name, category); // Add to database
                input_cost.setText(null);// Empty fields
                input_date.getJFormattedTextField().setText(null);
                populate_comboboxes();
                replace_lower_panel(panel_home); // Go back to homepage
                summary.setText(summarize_db(simple)); // Reset summary text

            // Show error messages for bad input
            } catch (Exception ex) {
                show_error(ex);
            }
        });

        // Construct FlowLayouts
        int buffer_width = 40; // Space between textboxes and right edge of window
        JPanel flow_cost = new JPanel();
        JPanel flow_name = new JPanel();
        JPanel flow_date = new JPanel();
        JPanel flow_cat = new JPanel();
        JPanel flow_commands = new JPanel();
        flow_cost.setLayout(new FlowLayout(FlowLayout.RIGHT));
        flow_name.setLayout(new FlowLayout(FlowLayout.RIGHT));
        flow_date.setLayout(new FlowLayout(FlowLayout.RIGHT));
        flow_cat.setLayout(new FlowLayout(FlowLayout.RIGHT));
        flow_commands.setLayout(new FlowLayout(FlowLayout.CENTER));
        flow_cost.add(l_cost);
        flow_cost.add(input_cost);
        flow_cost.add(Box.createHorizontalStrut(buffer_width));
        flow_name.add(l_name);
        flow_name.add(input_name);
        flow_name.add(Box.createHorizontalStrut(buffer_width));
        flow_date.add(l_date);
        flow_date.add(input_date);
        flow_date.add(Box.createHorizontalStrut(buffer_width));
        flow_cat.add(l_cat);
        flow_cat.add(input_category);
        flow_cat.add(Box.createHorizontalStrut(buffer_width));
        flow_commands.add(b_new_done);
        flow_commands.add(b_new_cancel);

        // Assemble panel
        panel_add_new.setLayout(new BoxLayout(panel_add_new, BoxLayout.Y_AXIS));
        panel_add_new.add(flow_date);
        panel_add_new.add(flow_name);
        panel_add_new.add(flow_cat);
        panel_add_new.add(flow_cost);
        panel_add_new.add(flow_commands);

            // ----------DISPLAY LATEST---------------------------------------------------
        // Creating buttons:
        JButton b_latest_back = make_button("Back", 'B');

        // Create Scroll Pane for the JTable
        scroll_pane_latest.setPreferredSize(new Dimension (450, 130));
        scroll_pane_latest.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        scroll_pane_latest.getViewport().add(table_latest);

        // Attach Listeners
        b_latest_back.addActionListener(e -> replace_lower_panel(panel_home));

        // Assemble panel
        panel_display_latest.setLayout(new BoxLayout(panel_display_latest, BoxLayout.Y_AXIS));
        panel_display_latest.add(scroll_pane_latest);
        panel_display_latest.add(b_latest_back);
        panel_display_latest.add(Box.createVerticalStrut(3));
    }

    /**
     * Constructs a menu option with mnemonic
     * @param option_name (String) The text that will display on the menu bar.
     * @param mnemonic (char) A character shortcut used to activate the menu option.
     * @return (JMenu) The newly constructed menu option component
     */
    private static JMenu make_menu_option(String option_name, char mnemonic) {
        JMenu option = new JMenu(option_name);
        option.setMnemonic(mnemonic);
        return option;
    }

    /**
     * Constructs a basic menu item with mnemonic
     * @param item_name (String) The text that will display on the menu dropdown.
     * @param mnemonic (char) A character shortcut used to activate the menu item.
     * @return (JMenuItem) The newly constructed menu item component
     */
    private static JMenuItem make_menu_item(String item_name, char mnemonic) {
        JMenuItem item = new JMenuItem(item_name);
        item.setMnemonic(mnemonic);
        return item;
    }

    /**
     * Construct a basic button with mnemonic
     * @param text (String) The text that will display on the button.
     * @param mnemonic (char) A character shortcut used to press the button.
     * @return (JButton) The newly constructed button component
     */
    private static JButton make_button(String text, char mnemonic) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMnemonic(mnemonic);
        return button;
    }

    /**
     * Given some exception, display the error message to the user in a pop-up dialog.
     * @param e (Exception) The exception whose warning message will be displayed.
     */
    private static void show_error(Exception e) {
        JOptionPane.showMessageDialog(frame,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Replaces whatever panel is on the bottom with the component provided, and refreshes the frame.
     * @param panel_to_add (JPanel) The component to display on the bottom of the frame.
     */
    private static void replace_lower_panel(JPanel panel_to_add) {
        frame.getContentPane().remove(panel_home);
        frame.getContentPane().remove(panel_add_new);
        frame.getContentPane().remove(panel_display_latest);
        frame.getContentPane().add(BorderLayout.PAGE_END, panel_to_add);
        SwingUtilities.updateComponentTreeUI(frame); // Refresh to show changes
    }

    /**
     * Creates the Remove Entry popup. Removes all purchases from the database of the given date.
     */
    private static void remove_entry() {
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        UtilDateModel model = new UtilDateModel();

            // ~~~~~~~~~~ Construct Components ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // JDatePicker
        JDatePanelImpl date_panel_impl = new JDatePanelImpl(model, p);
        JDatePickerImpl date_picker_impl = new JDatePickerImpl(date_panel_impl, new date_label_formatter());
        date_picker_impl.setAlignmentX(Component.CENTER_ALIGNMENT);
        date_picker_impl.setBorder(BorderFactory.createEmptyBorder(10,50,10,50));
        // JLabel
        JLabel l_date = new JLabel("Enter a date to clear all entries from:");
        l_date.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Submit and Back Button
        JButton b_back = make_button("Back", 'b');
        JButton b_submit = make_button("Submit", 's');

            // ~~~~~~~~~~ Construct Panels ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // FlowPanel of: JButton, JButton
        JPanel buttons_flow_panel = new JPanel();
        buttons_flow_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttons_flow_panel.add(b_submit);
        buttons_flow_panel.add(Box.createHorizontalStrut(40));
        buttons_flow_panel.add(b_back);
        // Panel combining all components
        JPanel panel_combined = new JPanel();
        panel_combined.setLayout(new BoxLayout(panel_combined, BoxLayout.Y_AXIS));
        panel_combined.add(Box.createVerticalStrut(10));
        panel_combined.add(l_date);
        panel_combined.add(date_picker_impl);
        panel_combined.add(buttons_flow_panel);

            // ~~~~~~~~~~ Create Dialog ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        JDialog date_dialog = new JDialog(frame, "Remove Entry");
        date_dialog.getContentPane().add(panel_combined);
        date_dialog.setSize(300, 150);
        date_dialog.setResizable(false);
        date_dialog.setLocationRelativeTo(frame); // Have dialog display centered on application
        date_dialog.setVisible(true);

            // ~~~~~~~~~~ Button Listeners ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        b_back.addActionListener(e -> {
            date_dialog.dispose();
            date_dialog.setVisible(false);
        });
        b_submit.addActionListener(e -> {
            String date_input = date_picker_impl.getJFormattedTextField().getText();

            // If no date was selected
            if (date_input.equals("")) {
                show_error(new empty_input_exception("No date selected."));

            } else {
                try {
                    // Check validity of date
                    add.confirm_date_format(date_input);

                    // Clear the date from the database.
                    db.clear_date(date_input);
                    summary.setText(summarize_db(simple));
                    populate_comboboxes();
                    date_dialog.dispose();
                    date_dialog.setVisible(false);

                } catch (Exception except) {
                    show_error(except);
                }
            }
        });
    }

    /**
     * Creates the Reset Database pop-up. Clears and re-initializes the database if requested.
     */
    private static void reset_database() {
        int choice = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to reset the database?\nAll entered information will be lost.",
                "Reset Database",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            db.clear_table(); // Delete database entries and drop table
            db.initialize(); // Set up new, empty database
            summary.setText(summarize_db(simple)); // Refresh summary text
            populate_comboboxes();
        }
    }

    /**
     * Exports the database to a csv file for backup.
     */
    private static void export_csv() {
        String path_out = get.db_to_csv();
        JOptionPane.showMessageDialog(frame,
                String.format("<html><body><p style='width: 250px;'>Exported the database to %s.", path_out),
                "Export to .csv",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates the import from .csv popup. Uses a file-finder to isolate a path to some .csv the user wishes to import.
     */
    private static void import_csv() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY); // Pick a file, not a directory
        fc.setAcceptAllFileFilterUsed(false); // Get rid of 'All Files' option
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated value files (*.csv)", "csv"));
        fc.setCurrentDirectory(new File(System.getProperty("user.home"))); // Start search from ~/Home/
        int result = fc.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) { // Confirm a file was picked
            File selected_file = fc.getSelectedFile();
            String file_path = selected_file.getAbsolutePath();

            int confirm = JOptionPane.showConfirmDialog(frame, // Make sure the user knows this will overwrite data.
                    "<html><body><p style='width: 250px;'>Are you sure you want to import new data? This " +
                            "will completely replace the existing database, and all current data will be lost.",
                    "Import .csv",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    add.from_csv(file_path);
                } catch (file_format_exception e) { // File is obviously not in the right format, nothing has been added.
                    show_error(e);
                } catch (future_date_exception | date_format_exception | invalid_cost_exception | empty_input_exception e) {
                    show_error(e);
                    db.clear_table(); // db must be cleared since some data (but not all) may have been entered
                    db.initialize();
                } finally {
                    summary.setText(summarize_db(simple));
                    populate_comboboxes();
                    SwingUtilities.updateComponentTreeUI(frame);
                }
            }
        }
    }

    /**
     * Creates a popup with some basic usage information.
     */
    private static void get_general_help() {
        JOptionPane.showMessageDialog(frame,
                "<html><body><p style='width: 250px;'>The <i>Shared Spending Manager</i> tracks how much " +
                        "money has been spent between a number of individuals. It will display how much each person " +
                        "has spent, who has spent the least, and a weighted average of spending per day.</p>\n\n" +
                        "<html><body><p style='width: 250px;'>Add a new record to the database with " +
                        "\"Add New Purchase\". If a mistake has been made, you can clear the entries for a " +
                        "specified day with <code>Edit->Remove Entry</code>.</p>",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a popup with some information on categories.
     */
    private static void get_category_help() {
        JOptionPane.showMessageDialog(frame,
                "<html><body><p style='width: 250px;'>Each purchase can optionally be categorized with a " +
                        "given label. Having multiple categories for purchases will result in separate categorical " +
                        "summaries; only comparing spending within the same category. A summary will be generated for " +
                        "each category, as well as separated averages.</p>\n\n<html><body><p style='width: 250px;'>" +
                        "Regardless of whether or not categories are used, a summary is always generated for " +
                        "totals across all purchases in the database.</p>",
                "Categories Help",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a popup with some information on exporting and importing the database.
     */
    private static void get_export_help() {
        JOptionPane.showMessageDialog(frame,
                "<html><body><p style='width: 250px;'>The database can be backed up by exporting it to a " +
                        ".csv file. Use <code>File->Export to .csv</code> to generate a .csv file from the database." +
                        "</p>\n\n<html><body><p style='width: 250px;'>A database export can be imported to the " +
                        "program with <code>File->Import from .csv</code>. This will overwrite the contents of the " +
                        "database, but restore it to when it was last exported.</p>",
                "Exporting & Importing Help",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Fills Name-Selection/Category Selection ComboBoxes options with all names/categories that
     * currently exist in the database
     */
    private static void populate_comboboxes() {
        // Populate Names Dropdown Box
        ArrayList<String> names = to_titlecase(get.names()); // Get all names in the db
        input_name.removeAllItems(); // Remove preexisting options in the drop-down menu
        for (String name : names) { input_name.addItem(name); } // Fill with names from list
        input_name.setSelectedItem(null);

        // Populate Categories Dropdown Box
        ArrayList<String> categories = to_titlecase(get.categories()); // Get all names in the db
        input_category.removeAllItems(); // Remove preexisting options in the drop-down menu
        for (String category : categories) { // Fill with categories from list
            if (!category.equals("Uncategorized")) { input_category.addItem(category); }
        }
        input_category.setSelectedItem(null);
    }
}
