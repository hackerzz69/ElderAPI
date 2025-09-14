# Easy Setup Guide for ZenyteAPI with XAMPP and Redis

Follow these simple steps to set up ZenyteAPI on your local machine using XAMPP for Apache and MySQL, along with Redis for caching.

## Step 1: Download and Install XAMPP

Download XAMPP from [this link](https://sourceforge.net/projects/xampp/files/latest/download). Once the download is complete, run the installer and follow the on-screen instructions to install XAMPP on your machine.

## Step 2: Download and Configure Redis for Microsoft

Download Redis for Microsoft from [this link](https://github.com/microsoftarchive/redis/releases/download/win-3.0.504/Redis-x64-3.0.504.msi). After the installation, follow these straightforward steps:

### 2a. Navigate to Redis Installation Directory

Navigate to "C:\Program Files\Redis."

### 2b. Rename Configuration File

Find and rename `redis.windows.conf` to `redis.conf`.

### 2c. Run Redis Server

Execute `redis-server.exe` to start the Redis server.

## Step 3: Open XAMPP and Start Servers

Open the XAMPP control panel and do the following:

### 3a. Start Apache Server

Click the "Start" button next to "Apache" to initiate the Apache server.

### 3b. Start MySQL Server

Click the "Start" button next to "MySQL" to start the MySQL server.

## Step 4: Import Database in MySQL AdminPanel

Open your web browser and go to [http://localhost/phpmyadmin](http://localhost/phpmyadmin). Then, follow these user-friendly steps:

### 4a. Import SQL Files

Navigate to the "SQL Files" directory and import the included `Redis` SQL files using the easy-to-use interface.

## Step 5: Run ZenyteAPI in IntelliJ

Open your IntelliJ IDE and perform these straightforward actions:

### 5a. Run Zenyte API

Run the Zenyte API project in IntelliJ with a simple click.

### 5b. Run Discord Bot

Start the Discord bot associated with the Zenyte API, making sure to follow any prompts or setup instructions.

Congratulations! Your development environment is now set up for ZenyteAPI, and you should be ready to start coding. Double-check that all services and servers are running properly before testing your application.
