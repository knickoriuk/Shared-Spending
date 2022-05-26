# Shared Spending Manager

An application for tracking how much has been spent by a group of individuals. It determines which person has paid the least, as well as some statistics on the total spending. The program allows for storage of a name, date, and amount into a SQLite database.

This started out as a Python program run entirely in the console, but was used so frequently, I learned how to make a GUI for it. The program was ported to Java, and it became a deskop application that does all I need it to do.

### Features

- Functions with as many individuals as needed, as long as they each are uniquely named
- For each purchase to be tracked, the user inputs the buyer's name, date of purchase, and amount spent
- Tracks the current total spent by each individual
- Determines which person has spent the least, and by how much
- Provides a total of how much has been spent since the beginning of tracked data
- Provides a weighted average of money spent between all individuals per day
    - It is weighted such that entries from the last 365 days have more influence on the average, and purchases have less influence on the average the longer ago they happened.
- Can display the most recent 10 entries in the database, useful to check if a purchase has already been added
- Allows for exporting the database as a .csv for further analysis or safekeeping if needed
- Allows for importing from .csv
- Allows for deleting entries by date, and deleting all entries

### The GUI

![Shared Spending Manager](/Images/preview_shared_spending.png?raw=true)
