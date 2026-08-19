# Topic
Implementing Soft Delete vs Hard Delete and Using Request Parameters in a Spring Boot + JPA CRUD Application

# Overview
This lecture, part of a Spring Framework series, builds on a previously created Spring Boot Student CRUD project to introduce two real-world backend concepts: soft delete and request parameters. The instructor first explains how a normal ("hard") delete works and why permanently removing records from a database is risky in enterprise applications — it destroys historical data needed for logs, audits, and account recovery. The alternative, soft delete, is introduced: instead of physically removing a row, an extra boolean column (`isDeleted`) is added to mark a record as deleted without erasing it. The video walks through adding this field to the Student entity, updating every CRUD operation (create, get, get all, update) so deleted records are excluded, and building a new "soft delete" endpoint using the HTTP PATCH method with Spring Data JPA's method-name query generation (e.g., `findByIdAndDeletedFalse`). Hard delete is deliberately left unaffected by the deleted flag, so it can still remove any record regardless of its soft-delete status. The instructor also fixes a duplicate-ID/auto-increment issue using `@GeneratedValue`, and finally demonstrates request parameters (`@RequestParam`) as an alternative to path variables for passing values like `id` in API calls, explaining when to prefer one over the other.

# Detailed Notes

## Section 1: How Normal (Hard) Delete Works
- In a typical CRUD setup, deleting a record means calling a "delete by ID" operation, and JPA internally writes a query that removes the row entirely from the database.
- **Problem:** Once removed, the data is gone. If the application later needs historical records — e.g., all users who ever signed up — there is no way to retrieve deleted ones.
- In enterprise applications, this is considered risky because:
  - Logs and audit trails often need to be preserved.
  - Old accounts sometimes need to be recovered.
- **Key term:** This type of deletion — directly erasing a row from the database — is called **Hard Delete**.

## Section 2: Introducing Soft Delete
- **Definition:** Soft delete means not removing the row from the database at all. Instead, an extra field marks the row as deleted.
- A boolean column, e.g. `isDeleted`, is added to the entity:
  - `false` (0) = record is active.
  - `true` (1) = record is treated as deleted, even though it still physically exists in the database.
- Once a record is marked as deleted:
  - A "get by ID" call for that record should return "not found," even though the row is technically present.
  - No update operation should be allowed on it.
  - It should not appear in "get all" results.
- **Why this is useful:** The data is preserved for future needs — recovering accounts, generating full historical reports, locking accounts, auditing, etc. — while behaving as "deleted" from the application's perspective.
- Hard delete is still occasionally used, but only when the data is genuinely no longer needed. In real-world/enterprise projects, soft delete is generally preferred because storage space is rarely a real constraint — data retention is more valuable than saving space.

## Section 3: Adding the `deleted` Field to the Entity
- The instructor opens the existing `Student` entity class, which already has fields: `id`, `name`, `age`, `email`, `rollNumber`, `subject`.
- A new field is added: `private boolean deleted;`
- Getter and setter methods are generated for this field.
- After restarting the application, this field appears in the database table as a `bit` type column (SQL's representation of boolean), defaulting appropriately.

## Section 4: Impact on the Create Operation
- Originally, "create" simply saved whatever student object was received directly into the database.
- **Change needed:** Before saving, explicitly set `studentRecord.setDeleted(false)` — every new record must default to "not deleted."
- **Important security/design point:** The `deleted` field should never be trusted from the client request body. Even if a client sends `deleted: true` in the create request, the server should ignore it and force it to `false` internally. This prevents a client from marking a record as deleted at creation time — that decision must remain fully controlled by server-side logic (only through the dedicated soft-delete endpoint).
- Demonstrated via Postman: even when `deleted: true` was included in a create request body, the saved record's `deleted` value was `false`, confirming server-side control.

## Section 5: Designing the Soft Delete Endpoint
- A new endpoint is created for soft deletion, separate from the existing hard-delete endpoint.
- **Choice of HTTP method:** Although this is conceptually a "delete," the instructor explains that soft delete is really an **update** operation (changing `isDeleted` from 0 to 1 on an existing record), not an actual removal.
  - Standard CRUD methods used so far: POST (create), GET (read), PUT (update), DELETE (hard delete).
  - For soft delete, **PATCH** is chosen, because PATCH is the correct HTTP method for updating a specific field of an existing record, rather than replacing or removing the whole thing.
- **Endpoint design:**
  - Method: `@PatchMapping`
  - URL: `/api/students/delete-soft` (or `/delete-softly`), with `/{id}` as a path variable.
- **Controller method:** `deleteStudentSoftly`, taking the student ID from the path variable, calling a matching service-layer method, and returning a response like "Student deleted" or a "not found" message if the student doesn't exist.

## Section 6: Implementing Soft Delete in the Service Layer
- New method: `public boolean deleteStudentSoftly(long id)`
- Logic:
  1. First, check the current database table structure — initially there is no `deleted` column shown until the table is dropped and recreated (since adding a Java field alone doesn't auto-alter an existing SQL table with data already in it).
  2. The table is deleted and the application restarted so the `deleted` column is generated in the schema (shown as `bit` type in the DB).

## Section 7: Spring Data JPA's "Magic" Method-Name Query Generation
- Spring Data JPA can automatically generate queries just from a method name — no manual SQL required, as long as the method is properly declared in the Repository interface.
- **Syntax pattern:** Always start with `findBy`, then attach the field name(s) and condition(s), e.g.:
  - `findByIdAndDeletedFalse` → generates: `SELECT * FROM student WHERE id = ? AND deleted = false`
  - `findAllByDeletedFalse` (not `findAllAndDeletedFalse` — you cannot attach `And` directly after `findAll`; it must follow the `findBy` pattern) → generates: `SELECT * FROM student WHERE deleted = false`
- The IDE/editor even auto-suggests valid clauses like `IsFalse`, `IsAfter`, `IsBefore`, `Between`, `Containing`, `Empty`, etc., showing how flexible the naming convention is.
- These custom methods only need to be **declared** in the Repository interface (with correct return type and parameters) — Spring JPA provides the implementation automatically at runtime.
- Example declarations added to `StudentRepository`:
  - `Optional<Student> findByIdAndDeletedFalse(Long id);`
  - `List<Student> findAllByDeletedFalse();` (originally attempted incorrectly as `findAllAndDeletedFalse`, which caused a bean creation error — fixed by renaming to the correct `findBy` pattern).

## Section 8: Updating Existing CRUD Methods to Respect Soft Delete
- **Get by ID:** Changed from `findById` to `findByIdAndDeletedFalse`, so soft-deleted records are excluded from retrieval.
- **Get All:** Changed from `findAll` to `findAllByDeletedFalse`, so soft-deleted records are excluded from listings.
- **Update:** Changed the lookup to `findByIdAndDeletedFalse` as well, and before saving, the code explicitly forces `studentToSave.setDeleted(false)` again — ensuring no update request can accidentally (or intentionally) mark a record as deleted through the update endpoint. Only the dedicated soft-delete endpoint can set `deleted = true`.
- **Exists by ID (used internally):** Could similarly be changed to `existsByIdAndDeletedFalse` if desired, though the instructor chooses not to apply this to the hard-delete flow (see Section 9).

## Section 9: Hard Delete Behavior — Intentionally Unaffected by Soft Delete
- The instructor explains a design decision: hard delete should work regardless of whether a record is already soft-deleted or not.
- Reasoning: hard delete is a rare, deliberate action. When a developer chooses to hard-delete a record, they genuinely want it gone — whether or not it was previously soft-deleted shouldn't matter.
- Therefore, `existsById` (a standard, unmodified method) is kept as-is for the hard-delete flow, rather than restricting it with `AndDeletedFalse`.
- This is presented as a design choice — an alternative implementation (restricting hard delete to only non-soft-deleted records via `existsByIdAndDeletedFalse`) is possible but not used here.

## Section 10: Full Soft Delete Method Implementation
- Step-by-step logic of `deleteStudentSoftly`:
  1. Fetch the record using `studentRepository.findByIdAndDeletedFalse(id)`, returning an `Optional<Student>`.
  2. If the result is empty (record doesn't exist or is already soft-deleted), return `false`.
  3. If it exists, retrieve the actual `Student` object via `.get()`.
  4. Set `studentToSave.setDeleted(true)`.
  5. Save it back using `studentRepository.save(studentToSave)`.
  6. Return `true`.
- This was tested in Postman: soft-deleting a record (e.g., "Rohit," ID 2) updated its `deleted` value to `1` in the database. Attempting to GET that record afterward returned `404 Not Found`, while GET ALL only returned the non-deleted record ("Aditya"). GET by ID for the non-deleted record still worked correctly.
- Hard delete was also tested on a record that was already soft-deleted, and it successfully removed it permanently — confirming hard delete works independent of soft-delete status.

## Section 11: Auto-Incrementing Primary Key (`@GeneratedValue`)
- **Problem identified:** Initially, the client (Postman) was manually specifying the `id` value in create requests. If a client accidentally sent an ID that already existed, JPA's `save()` method would **update** the existing record instead of creating a new one — since `save()` checks for existing keys and updates them if found. This was demonstrated: sending a new record with an already-used ID silently overwrote existing data ("Rohit" became "Rohan").
- **Fix:** Add `@GeneratedValue(strategy = GenerationType.IDENTITY)` above the `id` field in the entity, so the ID auto-increments and doesn't need to be provided by the client.
- **Caveat:** This annotation only takes effect if the underlying SQL table is recreated (it's a DDL-level change), so the table was dropped and the application restarted. Afterward, the table's `id` column showed `AUTO_INCREMENT` in its schema, and new records could be posted without specifying an ID — the ID incremented automatically (1, 2, 3, ...) and no longer caused accidental overwrites.

# Important Concepts
- **Hard Delete:** Physically and permanently removing a row from the database. Fast to implement, but destroys all history — generally avoided in enterprise apps except when data is truly no longer needed.
- **Soft Delete:** Marking a row as deleted (via a boolean flag like `isDeleted`) without removing it from the database. Preserves data for audits, recovery, and reporting, while making it invisible to normal application operations (read, update, list).
- **CRUD Participation Rule for Soft-Deleted Records:** Once a record's `deleted` flag is `true`, it should not participate in any Create, Read, Update, or (soft) Delete operation — it's treated as if it doesn't exist, except by whatever mechanism might restore/unlock it.
- **PATCH vs PUT vs DELETE:** PATCH is used for partial updates to an existing resource (like flipping one field, e.g., `isDeleted`), whereas PUT is used for fuller updates and DELETE for full removal. Since soft delete only changes one field on an existing record, PATCH is the semantically correct HTTP method.
- **Spring Data JPA Method-Name Query Derivation:** By naming a repository method following the `findBy<Field><Condition>` (or `findAllBy...`) convention, Spring JPA automatically generates and implements the corresponding SQL query at runtime — no manual query-writing needed for many common cases.
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`:** A JPA annotation that tells the database to auto-increment the primary key, preventing client-supplied IDs from accidentally overwriting existing records via `save()`.
- **Request Parameters (`@RequestParam`) vs Path Variables (`@PathVariable`):** Both are ways to pass values (like an ID) into an API request. Path variables embed the value directly into the URL path (e.g., `/get/1`); request parameters pass values as key-value pairs after a `?` in the URL (e.g., `/get?id=1&name=Aditya`). Request parameters are generally preferred when multiple fields need to be passed, since chaining several path variables becomes harder to read and fetch correctly.

# Step-by-Step Process

### A. Adding Soft Delete to an Existing CRUD Project
1. Add a `boolean deleted` field to the entity class, and generate its getter/setter.
2. In the **create** method, explicitly set `deleted = false` on the incoming object before saving (ignore any client-supplied value for this field).
3. In the **update** method, similarly force `deleted = false` before saving, so updates can never accidentally mark something deleted.
4. Change the **get by ID** repository call from `findById` to a custom `findByIdAndDeletedFalse`.
5. Change the **get all** repository call from `findAll` to a custom `findAllByDeletedFalse`.
6. Declare these new custom methods in the Repository interface, using the `findBy...` naming convention so Spring JPA auto-generates their implementation.
7. Leave the **hard delete** method (`existsById` + actual delete) unchanged, so it can remove any record regardless of soft-delete status.
8. Create a new **soft delete** endpoint using `@PatchMapping`, taking the record's ID.
9. Implement the soft-delete service method: fetch the non-deleted record by ID; if not found, return false; otherwise set `deleted = true` and save; return true.
10. Test each operation (create, get, get all, update, soft delete, hard delete) via Postman, verifying the database and query logs to confirm the correct behavior.

### B. Preventing Accidental Overwrites via Auto-Incrementing IDs
1. Identify the risk: manually assigned IDs can cause `save()` to silently update an existing record instead of creating a new one.
2. Add `@GeneratedValue(strategy = GenerationType.IDENTITY)` to the `id` field in the entity.
3. Drop the existing SQL table (since this is a schema-level/DDL change).
4. Restart the application so the table is recreated with an auto-incrementing ID column.
5. Create new records without specifying an ID — confirm the ID increments automatically and no data gets overwritten.

### C. Switching from Path Variables to Request Parameters
1. In the controller method (get, update, delete, soft-delete), remove `/{id}` from the URL path and remove `@PathVariable`.
2. Replace it with `@RequestParam` on the same method parameter.
3. Ensure the parameter name sent in the request (e.g., `id`) exactly matches the variable name expected by `@RequestParam`.
4. Call the endpoint using a query string format, e.g., `/get?id=1`, instead of `/get/1`.
5. Confirm that extra unrelated parameters (e.g., `name=Aditya`) can be included in the query string without being read, since only the declared `@RequestParam` fields are picked up.

# Tips and Best Practices
- Prefer soft delete over hard delete in enterprise-level applications, since storage space is rarely a real constraint and data retention is usually more valuable.
- Never trust a `deleted` (or similarly sensitive) field coming from the client in create/update request bodies — always control such flags server-side.
- Use PATCH for partial field updates (like toggling a status flag) rather than reusing the DELETE method for logical/soft deletions.
- Let Spring Data JPA generate queries from method names for simple, well-defined lookups instead of manually writing repository implementations — it reduces boilerplate significantly.
- Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for primary keys so IDs are always unique and auto-assigned, avoiding accidental overwrites of existing data.
- Prefer request parameters over multiple chained path variables when an endpoint may need to accept multiple optional or named fields.
- Practice by adding further validations (e.g., restricting `rollNumber` to a specific range) to strengthen understanding of business logic in the service layer.

# Mistakes to Avoid
- Don't forget to explicitly set `deleted = false` on create and update — without this safeguard, a client could manipulate the field and mark records as deleted unintentionally.
- Don't use `findAllAnd...` as a method name — the `findBy` prefix must come first; `findAll` cannot be directly chained with `And<Condition>`.
- Don't allow clients to supply their own primary key IDs without an auto-increment strategy — this can cause `save()` to silently overwrite existing records instead of creating new ones.
- Don't forget that adding a new field to an entity class does not automatically alter an already-created SQL table with existing data — the table must be dropped and recreated (or migrated) for schema changes like `AUTO_INCREMENT` to take effect.
- Don't mix up the field name sent as a request parameter and the name expected in `@RequestParam` — they must match exactly, or the value won't be received.

# Important Facts
- CRUD stands for Create, Read, Update, Delete — the four core database operations.
- HTTP methods used in this project: POST (create), GET (read), PUT (update), DELETE (hard delete), and PATCH (soft delete / partial update).
- The `deleted` boolean field is stored as a `bit` type column in SQL, which is how SQL represents boolean values.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` maps to SQL's `AUTO_INCREMENT` behavior for primary keys.
- Dropping and recreating a table is necessary for DDL (Data Definition Language) changes, such as adding `AUTO_INCREMENT`, to take effect on an already-existing table.

# FAQs

**Q: What's the main difference between soft delete and hard delete?**
A: Hard delete permanently removes a row from the database. Soft delete keeps the row but marks it with a flag (e.g., `deleted = true`) so it's treated as deleted by the application without actually losing the data.

**Q: Why is soft delete preferred in enterprise applications?**
A: Because storage space is rarely a real limitation, and preserving historical data is valuable for audits, logs, and the ability to recover or reinstate old records.

**Q: Can a client set the `deleted` field to `true` when creating a new record?**
A: No. Even if the client sends `deleted: true` in the request body, the server-side code explicitly overrides it to `false` on create (and on update) so only the dedicated soft-delete endpoint can mark something as deleted.

**Q: Why is PATCH used for the soft-delete endpoint instead of DELETE?**
A: Because soft delete doesn't remove data — it only updates one specific field (`isDeleted`) on an existing record, which is exactly what PATCH is designed for.

**Q: Does hard delete still work on a record that has already been soft-deleted?**
A: Yes. The hard-delete method uses `existsById` without checking the `deleted` flag, so it can remove any record regardless of its soft-delete status.

**Q: How does Spring Data JPA generate queries like `findByIdAndDeletedFalse` without writing any SQL?**
A: By following a specific method-naming convention (starting with `findBy`, followed by field names and conditions), Spring JPA parses the method name and automatically builds and implements the corresponding SQL query at runtime.

**Q: Why did the instructor add `@GeneratedValue(strategy = GenerationType.IDENTITY)`?**
A: To prevent a bug where manually specifying an ID that already existed would cause `save()` to update the existing record instead of creating a new one. Auto-incrementing IDs avoid this collision.

**Q: What's the difference between a path variable and a request parameter?**
A: A path variable is embedded directly in the URL path (e.g., `/get/1`), while a request parameter is passed as a key-value pair in the query string (e.g., `/get?id=1`). Request parameters are preferred when multiple fields need to be passed.

**Q: Does the `find by id` still work after switching to request parameters?**
A: Only if the parameter name matches what's expected — after switching, calling with the old path-variable style (e.g., `/1`) returns `404 Not Found`, since the endpoint now expects `?id=1` instead.

# Final Summary
- Hard delete permanently removes data from the database, which is risky for enterprise applications that need historical records, logs, or account recovery.
- Soft delete introduces a boolean `deleted` field that marks records as deleted without physically removing them, and is the generally preferred approach in real-world projects.
- All core CRUD operations (create, get, get all, update) were modified to respect the `deleted` flag, using custom Spring Data JPA methods like `findByIdAndDeletedFalse` and `findAllByDeletedFalse`.
- The `deleted` field is never trusted from client input on create/update — it's explicitly controlled server-side to prevent misuse.
- A new soft-delete endpoint was built using the PATCH HTTP method, since soft delete is conceptually a partial update, not a true deletion.
- Hard delete was deliberately left able to delete any record, soft-deleted or not, since it's a rare, intentional operation.
- Spring Data JPA can auto-generate query implementations purely from correctly named repository methods (`findBy...` convention), removing the need to write manual SQL for many cases.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` was added to auto-increment the primary key and prevent accidental overwrites of existing records when clients don't provide unique IDs.
- Schema-level changes (like enabling auto-increment) require dropping and recreating the table, since they are DDL operations.
- Request parameters (`@RequestParam`) were introduced as an alternative to path variables, useful for passing multiple named fields via the URL query string (e.g., `?id=1`).
- The instructor tested every change live via Postman and by inspecting the database and query logs to confirm correct query generation and behavior.
- The next lecture will cover Servlet technology and Spring MVC internals to build a deeper understanding of how Spring Boot works under the hood.