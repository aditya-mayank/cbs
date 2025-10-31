# cbs
Cycle Borrowing System (CBS) is a JavaFX-based smart cycle sharing app with real-time location tracking and MySQL-backed login/borrowing.

## Setup
1. Install MySQL and create DB user with privileges.
2. Run the SQL schema:
   - Open a terminal and execute: `mysql -u root -p < "Cycle Borrowing System/schema.sql"`
3. Configure DB credentials in `Cycle Borrowing System/src/main/resources/db.properties`.
4. Build and run:
   - `mvn -f "Cycle Borrowing System/pom.xml" clean javafx:run`

## Features
- User signup/login (passwords hashed with SHA-256)
- List available cycles
- Borrow a selected cycle
- Basic seed cycles included in `schema.sql`
