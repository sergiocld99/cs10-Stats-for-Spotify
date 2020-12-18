# cs10-Stats-for-Spotify
Personal Chart History - Java Desktop - This application reads your Top Tracks and generates stats for each artist

# Ranking Algorithm
1 - After you grant permissions for read your Top Tracks and Current Playback, this application downloads three rankings from Spotify: "Short Term (Last 4 Weeks)", "Medium Term (Last 6 Months)" and "Long Term" (All Time)

2 - The three rankings are combined in one single list that stores the most relevant information of the tracks

3 - A unique code is generated to identify the ranking. The code is a sum of track popularities (the final number is between 0 and 15000)

4 - This "actual" code is compared with other 2 codes stored in your filesystem. Those codes are called "compare" and "last".

-- If the "actual" code is different to "last" code, then the "compare" code is replaced with "last", and "last" code is replaced with "actual".

5 - The ranking with "compare" code is loaded from filesystem. If "compare" equals zero, then that ranking doesn't exist.

6 - Each song in the actual ranking is compared with the previous ranking. There are 4 possible status: "NEW", "UP", "DOWN" and "NOTHING".

-- If a song has the status "NEW", then the corresponding song file is read to know if it's really a "New Song on Chart" or a "Re-Entry".

7 - The actual ranking is saved in filesystem only if the "actual" code was different to "last" code in Step 4

8 - The actual ranking is showed on a table, that includes status, album cover, rank, name, artists and popularity of each song.

# Ranking Files
* Folder: "ranking"
* Name: the ranking code
* Header: a date (year, month and day)
* Column 1: rank (positive number)
* Column 2: track ID (a String)

# Library Files
These files are saved in the folder "library".
Here, each artist has their folder. An artist folder contains "Song Files".

# Song Files
* Name: track ID
* Header: track name
* Column 1: rank (positive number)
* Column 2: popularity (positive number, 0-100)
* Column 3: ranking code (positive number, 0-15000)

![alt text](https://imgur.com/a/Jf5RZAh)
