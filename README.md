# Shared Spending Manager

I created this application to help me keep track of how much everyone has spent, in a shared spending situation. I track how much my partner and I spend on groceries so we know whose turn it is to pay. This started out as a Python program run entirely in the console, but was used so frequently, I had to learn how to make a GUI for it. The program was ported to Java, and I made a lovely Windows app that does all I need it to do.

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
