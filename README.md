# Student Record System (Java + MySQL + MongoDB)

A browser-based Java application that can save, list, find, update, and delete student records using either:

- **MySQL** through XAMPP (inspect records in phpMyAdmin)
- **MongoDB** (inspect records in MongoDB Compass)

## Prerequisites

- Java 17+ and Maven
- XAMPP with **MySQL** started for the SQL option
- MongoDB Server running locally and MongoDB Compass for the NoSQL option

## Configure and run

1. In XAMPP, start MySQL. Open `http://localhost/phpmyadmin`, select **Import**, and import `database/student_records.sql`.
2. Start the local MongoDB server. In Compass, connect to `mongodb://localhost:27017`; the `student_records` database is created on first save.
3. Adjust `src/main/resources/application.properties` only if your credentials or ports are different.
4. From this project folder run:

```powershell
mvn compile exec:java
```

Open `http://localhost:8080` after starting the command. Select **MySQL** or **MongoDB** in the interface. Adding a student whose ID already exists updates it.

## Data design

MySQL uses a normalized `students` table with `student_id` as its primary key. MongoDB stores equivalent flexible documents in the `student_records.students` collection, with `studentId` used as the application key.

## Troubleshooting

- `Cannot connect to MySQL`: confirm XAMPP MySQL is running and the database import completed. The standard XAMPP root password is blank; otherwise set `mysql.password`.
- MongoDB connection failure: start MongoDB Server (Compass alone is only a viewer) and check `mongodb.uri`.
- Maven is not found: install Apache Maven and reopen the terminal.

