# Topic
Building a Complete CRUD REST API in Spring Boot Using Spring Data JPA (with MySQL)

# Overview
This lecture continues a Spring Boot series in which the instructor builds a "CRUD" (Create, Read, Update, Delete) application for managing student records. In the previous session, the Controller → Service → Repository flow was set up conceptually, without actually saving anything to a database. In this session, the instructor connects the application to a real MySQL database and implements all four CRUD operations end-to-end. The core idea taught is that Spring Data JPA removes the need to write manual SQL queries: by turning the Repository into an interface that extends `JpaRepository`, the developer automatically gets methods like `save()`, `findAll()`, `findById()`, `deleteById()`, and `existsById()`, whose actual implementation is provided by Spring/Hibernate at runtime. The lecture walks through configuring the database connection in `application.properties`, letting Hibernate auto-generate the database table from the entity class, and then building out Create, Read (single and all), Update, and Delete endpoints in the Controller and Service layers, testing each one with Postman and verifying the results in a database client (DBeaver). It ends with a preview of "soft delete," a technique to be covered in the next lecture.

# Detailed Notes

## 1. Recap and Cleanup
- In the prior lecture, the Controller, Entity, Repository, and Service layers were scaffolded, and one endpoint (`/create`) was demonstrated conceptually without real database storage.
- The instructor removes leftover print statements and dummy `return null` placeholders used earlier for explanation purposes, since real database logic will now replace them.

## 2. Why Use Spring Data JPA Instead of Manual Queries
- The Repository layer's job is to interact with the database (store and retrieve values).
- Instead of manually writing SQL, Spring JPA (Jakarta Persistence API — note: "Jakarta" replaced "Java" in newer versions of the framework/imports) provides ready-made methods:
  - `save()` — inserts a new record (internally runs an `INSERT INTO` query) or updates an existing one.
  - `findAll()` — retrieves all records (internally a `SELECT * FROM table`).
  - `findById()` — retrieves one record by primary key (internally uses a `WHERE` clause).
  - `deleteById()` — deletes a record by primary key.
  - `existsById()` — returns a boolean indicating whether a record with the given ID exists.
- There is no separate "update" method — `save()` handles both insert and update. Hibernate decides internally (based on the primary key) whether to run an INSERT or an UPDATE query; this mechanism will be covered in more detail in a future lecture.

## 3. Converting the Repository into an Interface
- The Repository class is converted from a `class` to an `interface` that extends `JpaRepository<Student, Long>`:
  - The first generic parameter (`Student`) tells Spring which entity this repository manages.
  - The second parameter (`Long`) is the data type of that entity's primary key.
- **Why an interface instead of a class?** If it were a class, every method (e.g., a `save()` method) would need a manual implementation, requiring hand-written SQL. By declaring it as an interface extending `JpaRepository`, all of JPA's built-in methods become available automatically — no implementation code required.
- **Interface hierarchy:** `JpaRepository` extends `ListCrudRepository`, which extends `CrudRepository`. Methods like `save()` originate in `CrudRepository` and flow down through the chain into the custom `StudentRepository`.
- Because it's an interface, calling `studentRepository.save(...)` from the Service layer works immediately — no explicit `save` declaration is needed in the custom repository.

## 4. How the "Magic" Works (No Manual Implementation Needed)
- Normally, implementing an interface requires overriding every method (using `@Override`).
- Here, none of the methods are overridden manually — Spring Data JPA provides the implementation automatically at runtime.
- The `@Repository` annotation can optionally be added to mark a class as a repository (useful for framework/developer clarity), but it is not required on an interface — interfaces don't get instantiated into objects, so no Spring bean is created directly from them, and thus no annotation is strictly necessary.
- `@Repository` internally uses `@Component`, similar to `@Service` and `@Controller`.
- Custom repository implementations (writing a class that implements the interface and manually overrides methods) become necessary later, when complex custom queries (e.g., joins) are needed that don't have a built-in JPA method.

## 5. Connecting to MySQL
- Before coding, a database connection is set up in DBeaver (or similar tool):
  - Choose MySQL, connect via host (`localhost`), leave the database name blank initially (it will be created manually), and provide the username (`root`) and password.
  - Test the connection before finishing setup.
- MySQL runs on port `3306` by default (compare: the Spring Boot app runs on port `8080`).
- A new database is created manually via SQL:
  ```sql
  CREATE DATABASE student_crud_db;
  ```
- Tables are **not** created manually — Hibernate/JPA will auto-generate them based on the entity class.

## 6. Application Configuration (`application.properties`)
Key properties configured to connect Spring Boot to MySQL:
- `spring.datasource.url` — JDBC URL pointing to `localhost:3306` and the database name (`student_crud_db`).
- `spring.datasource.username` — e.g., `root`.
- `spring.datasource.password` — the MySQL password set during installation.
- `spring.jpa.hibernate.ddl-auto` — set to `update`. This tells Hibernate it is allowed to automatically create/update tables based on the entity classes. Other possible values (`create`, `none`, etc.) exist and will be discussed later when Hibernate is covered in depth.
- `spring.jpa.show-sql` — set to `true` to display the SQL queries JPA generates in the console.
- `spring.jpa.properties.hibernate.format_sql` — set to `true` to pretty-print those SQL queries.
- The `@SpringBootApplication` class no longer needs the earlier `exclude` (which had been used to prevent auto-configuration of the datasource before the connection was properly configured).

## 7. Table Auto-Creation
- On starting the application, Hibernate reads the `Student` entity class (fields like `id`, `name`, `age`, `email`, `rollNumber`, `subject`) and automatically creates a matching `student` table, using the class's `@Id`-annotated field as the primary key and other fields as columns.
- With `show-sql` and `format_sql` enabled, the generated `CREATE TABLE` statement can be seen in the logs.

## 8. Create Operation (`POST /api/students/create`)
- Flow: Controller → `studentService.createStudent()` → `studentRepository.save()`.
- The `save()` method (inherited from `CrudRepository`) accepts an entity object and persists it.
- Testing in Postman: sending a POST request with a JSON body (name, email, age, roll number, subject) returns HTTP `201 Created` along with the saved object (including its generated ID) in the response body — because Jackson automatically converts the Java object to JSON.
- Logs show that `save()` first runs a `SELECT` query (to check whether the record's ID already exists) and then, if not found, runs an `INSERT INTO` query.
- Multiple records can be created by sending multiple POST requests with different data.

## 9. Read One Record (`GET /api/students/get/{id}`)
- Endpoint uses `@GetMapping` with a path variable: `/get/{id}`.
- The ID is received via `@PathVariable Long id`.
- Two ways to pass values in a GET request are discussed:
  - **Path variable** — part of the URL itself, e.g. `/get/1`.
  - **Query parameter** — appended as `?id=1&name=Aditya`, set via the "Params" tab in Postman.
- Service method `getStudent(Long id)` calls `studentRepository.findById(id)`.
- `findById()` returns an `Optional<Student>` (not a plain `Student`), because the record may or may not exist — this avoids a `NullPointerException` (raw `null` handling).
- Logic:
  - If `Optional.isPresent()` → return `studentResponse.get()` (extracts the `Student` from the `Optional`).
  - If not present → initially the instructor tries returning `null`, but this results in an incorrect `200 OK` response even when nothing is found.
  - **Fix:** explicitly check if the response is `null` and return `ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)`, or more cleanly, `ResponseEntity.notFound().build()`.
- A common bug encountered: forgetting to wrap `{id}` in curly braces in the `@GetMapping` path causes a `404 Not Found`, since the literal path doesn't match any real request URL.
- Testing: valid IDs (1, 2) return the correct student data; an ID that doesn't exist (3) correctly returns `404 Not Found`.

## 10. Read All Records (`GET /api/students/getAll`)
- No path variable is needed since all records are requested.
- Service method `getAllStudents()` calls `studentRepository.findAll()`, which returns a `List<Student>`.
- Controller checks `studentList.isEmpty()`:
  - If empty → return not found.
  - If not empty → return the full list.
- Tested successfully in Postman, returning a JSON array of all student records.

## 11. Update Operation (`PUT /api/students/update/{id}`)
- Uses `@PutMapping("/update/{id}")`, since PUT is the conventional HTTP method for updates.
- The ID is received as a `@PathVariable Long id`.
- Since PUT requests also carry a request body, the updated field values are received via `@RequestBody Student studentRequest`.
- Service method `updateStudent(Long id, Student studentRequest)`:
  1. Calls `findById(id)` to fetch the existing record as an `Optional<Student>`.
  2. Checks if the `Optional` is empty; if so, returns `null` (later refined by checking `isPresent()`/`isEmpty()` directly in the controller is noted as a "cleaner" alternative practice, though not implemented here for simplicity).
  3. If present, extracts the existing student via `.get()` into a variable (e.g., `studentToSave`).
  4. Manually copies each updated field from `studentRequest` into `studentToSave` using setters (`setName`, `setEmail`, `setAge`, `setRollNumber`, `setSubject`), reading each value with the corresponding getter from `studentRequest`.
  5. The `id` field is deliberately **not** overwritten — it always remains the original ID, regardless of what is sent in the request body. This is called out as good coding practice.
  6. Calls `studentRepository.save(studentToSave)` — since the entity already has an existing primary key, JPA runs an `UPDATE` query instead of an `INSERT`.
- Tested in Postman: updating a student's age and roll number via `PUT /update/2` correctly updates the database record (verified in DBeaver and in the SQL logs showing an `UPDATE` statement).

## 12. Delete Operation (`DELETE /api/students/delete/{id}`)
- Uses `@DeleteMapping("/delete/{id}")` with `@PathVariable Long id`.
- Service method `deleteStudent(Long id)` returns a `boolean`:
  1. First checks `studentRepository.existsById(id)`.
  2. If the record does not exist → returns `false`.
  3. If it exists → calls `studentRepository.deleteById(id)` (which returns nothing/`void`) and then manually returns `true`.
- Controller logic:
  - If `isDeleted` is `false` → return `ResponseEntity.notFound().build()`.
  - If `true` → return `ResponseEntity.ok("Record deleted")` (a descriptive string is used instead of returning a plain boolean).
- Tested in Postman: deleting a non-existent ID (3) correctly returns `404 Not Found`; deleting an existing ID (2) returns a success message and the record is confirmed removed from the database.

# Important Concepts
- **JPA (Jakarta Persistence API):** A specification providing built-in methods (`save`, `findAll`, `findById`, `deleteById`, `existsById`, etc.) that let developers interact with a database without writing raw SQL.
- **Hibernate:** The underlying implementation that JPA uses internally to actually execute database operations; will be studied in more depth in future lectures.
- **Repository as an Interface:** Making the repository extend `JpaRepository<Entity, IdType>` (rather than writing it as a class) automatically provides CRUD methods without manual implementation.
- **`Optional<T>`:** A wrapper type used by methods like `findById()` to safely represent a value that may or may not exist, avoiding null pointer errors.
- **Path Variable vs Query Parameter:** Two different ways to pass values in a URL — as part of the URL path (`/get/{id}`) versus as key-value pairs after a `?` (`?id=1`).
- **`ddl-auto=update`:** A Hibernate setting that allows automatic creation/updating of database tables based on entity class structure.
- **Soft Delete (introduced conceptually, to be covered next lecture):** Instead of physically removing a record from the database, an `isDeleted` flag is set to `true` (or 1). The record still physically exists but is treated as deleted by the application logic (excluded from fetches/updates). Useful for scenarios like auditing, tracking active users, or recovering data later.

# Step-by-Step Process
1. **Clean up placeholder code** — remove old print statements and dummy `return null` logic from the Controller, Service, and Repository.
2. **Convert the Repository from a class to an interface** that extends `JpaRepository<Student, Long>`.
3. **Set up a MySQL connection** in a database client (e.g., DBeaver) using host `localhost`, port `3306`, and your root credentials.
4. **Create the database manually** using `CREATE DATABASE student_crud_db;` (do not create tables manually).
5. **Configure `application.properties`** with the datasource URL, username, password, `hibernate.ddl-auto=update`, and (optionally) `show-sql=true` / `format_sql=true`.
6. **Start the application** and verify that Hibernate auto-creates the `student` table matching the entity class fields.
7. **Implement the Create endpoint** (`POST /create`) by calling `studentRepository.save()` from the Service layer.
8. **Implement the Read One endpoint** (`GET /get/{id}`) using `@PathVariable` and `findById()`, handling the `Optional` result and returning `404` if not found.
9. **Implement the Read All endpoint** (`GET /getAll`) using `findAll()` and checking if the returned list is empty.
10. **Implement the Update endpoint** (`PUT /update/{id}`) by fetching the existing record, copying over updated fields from the request body, and calling `save()` again.
11. **Implement the Delete endpoint** (`DELETE /delete/{id}`) by checking `existsById()` first, then calling `deleteById()` if the record exists.
12. **Test every endpoint in Postman** and confirm changes by refreshing the database table in DBeaver.

# Tips and Best Practices
- Enable `spring.jpa.show-sql` and `hibernate.format_sql` during development to see and verify exactly what SQL queries JPA/Hibernate is generating — useful for debugging and learning.
- Never hardcode IDs in endpoint logic (e.g., always fetching record `1`) — use path variables so the ID is dynamic per request.
- When updating a record, avoid overwriting the primary key (`id`) with whatever the client sends — keep the original ID intact.
- Prefer returning a proper `ResponseEntity.notFound().build()` over manually setting status and returning `null` in the body — it's cleaner.
- Returning a descriptive string (e.g., `"Record deleted"`) is more informative to API consumers than returning a raw boolean.
- Use a database GUI tool (like DBeaver) to visually verify that operations (insert/update/delete) are actually reflected in the database, rather than trusting the API response alone.
- In production, never use a simple/weak database password (the instructor notes his simple password is only acceptable because it's a local development setup).

# Mistakes to Avoid
- Forgetting to wrap a path variable in curly braces (e.g., writing `/get/id` instead of `/get/{id}`) — this causes the endpoint to not match the request URL, resulting in `404 Not Found`.
- Returning `null` directly instead of a proper `404` response when a record isn't found — this can cause the API to incorrectly return `200 OK` with an empty body.
- Confusing which HTTP method to use: `GET` for reading, `POST` for creating, `PUT` for updating, `DELETE` for deleting.
- Trying to receive a `findById()` result directly into a `Student` variable instead of an `Optional<Student>` — this causes a compile error, since `findById()` returns `Optional<Student>`.
- Manually writing SQL `CREATE TABLE` statements when Hibernate can auto-generate the schema via `ddl-auto=update`.
- Overwriting the record's ID during an update operation based on client-supplied data.

# Important Facts
- Default MySQL port: `3306`.
- Default Spring Boot application port: `8080`.
- JPA stands for **Jakarta Persistence API** (previously referred to using "Java" in older versions/imports).
- `JpaRepository` extends `ListCrudRepository`, which extends `CrudRepository` — this is where methods like `save()`, `findAll()`, `deleteById()`, and `existsById()` originate.
- `@Repository` internally relies on `@Component` (same underlying mechanism as `@Service` and `@Controller`).
- Successful record creation returns HTTP status `201 Created`; successful reads/updates typically return `200 OK`; missing records return `404 Not Found`.

# FAQs

**Q: Why convert the Repository from a class to an interface?**
A: So that Spring Data JPA can automatically provide implementations for standard database operations (save, find, delete, etc.) at runtime, without the developer writing any SQL or method bodies manually.

**Q: Do I need to write SQL queries for basic CRUD operations?**
A: No. Methods like `save()`, `findAll()`, `findById()`, `deleteById()`, and `existsById()` are provided out of the box by extending `JpaRepository`.

**Q: What's the difference between a path variable and a query parameter?**
A: A path variable is embedded directly in the URL path (e.g., `/get/1`), while a query parameter is appended after a `?` (e.g., `/get?id=1`). Both are ways to pass values in a GET request.

**Q: Why does `findById()` return an `Optional<Student>` instead of a `Student`?**
A: Because the requested record might not exist. `Optional` is a safe way to represent a value that may or may not be present, avoiding null pointer exceptions.

**Q: How does Spring Data JPA decide whether `save()` should insert or update a record?**
A: It checks the primary key. If a record with that ID doesn't exist, it runs an `INSERT` query; if it does exist, it runs an `UPDATE` query. The exact internal mechanism will be covered in a future lecture on Hibernate.

**Q: Why does the table get created automatically when the app starts?**
A: Because `spring.jpa.hibernate.ddl-auto` is set to `update`, which tells Hibernate to generate/update the database schema based on the entity class definitions.

**Q: What is a "soft delete," and why is it preferred over permanently deleting records?**
A: A soft delete marks a record as deleted (e.g., via an `isDeleted` flag) instead of physically removing it from the database. This preserves data for auditing, analytics, or recovery purposes while hiding it from normal application operations.

**Q: Why shouldn't the ID field be updated during an update operation?**
A: Because the ID uniquely identifies the record; overwriting it based on client input is considered bad practice and could corrupt data integrity.

# Final Summary
- The lecture continues a Spring Boot CRUD project, moving from theoretical flow (from the last lecture) to an actual working database-connected application.
- The Repository was converted from a class into an interface extending `JpaRepository<Student, Long>`, unlocking built-in methods like `save()`, `findAll()`, `findById()`, `deleteById()`, and `existsById()` without writing any SQL.
- A MySQL database (`student_crud_db`) was created manually, but tables were left to be auto-generated by Hibernate based on the `Student` entity class.
- Key configuration properties (`datasource.url`, `username`, `password`, `ddl-auto=update`, `show-sql`, `format_sql`) were set in `application.properties` to connect the app to MySQL and visualize generated SQL.
- The Create endpoint (`POST /create`) was implemented using `save()`, tested via Postman, and verified in the database.
- The Read One endpoint (`GET /get/{id}`) used `findById()` and `Optional`, with proper handling for missing records via `404 Not Found`.
- The Read All endpoint (`GET /getAll`) used `findAll()`, returning a list of all students.
- The Update endpoint (`PUT /update/{id}`) fetched the existing record, applied field-by-field updates from the request body (excluding the ID), and called `save()` again to trigger an update query.
- The Delete endpoint (`DELETE /delete/{id}`) used `existsById()` to check existence before calling `deleteById()`, returning appropriate success/`404` responses.
- All endpoints were successfully tested end-to-end using Postman and verified visually in DBeaver, including inspecting the generated SQL queries in the application logs.
- The lecture closes with a preview of "soft delete" as the topic for the next session — a technique for marking records as deleted rather than physically removing them from the database.