# Topic
Understanding Java Servlets and Tomcat from the ground up, and building a simple CRUD web application using raw Servlets (Coder Army Spring Framework series).

# Overview
This lecture explains why understanding Java Servlets is essential for truly understanding Spring/Spring Boot internally, since the entire Spring MVC architecture is built on top of Servlet technology. The video starts with client-server architecture basics, then explores why plain Java (using `java.net`) is insufficient for building scalable web applications — it can listen on a port but cannot understand the HTTP request/response format, forcing the developer to manually parse everything (method, endpoint, headers, body, query parameters) and manually handle multi-threading. This manual burden is solved by introducing a **Servlet Container** (Tomcat), which handles all server-related plumbing so developers can focus purely on business logic. The instructor draws a direct parallel between Tomcat managing Servlets and Spring's IoC container managing Beans. The bulk of the lecture is a hands-on walkthrough of setting up an external Tomcat server, creating a Maven project with WAR packaging, adding the Jakarta Servlet dependency, and building a simple in-memory (HashMap-based) CRUD application for a `User` entity using a single `UserServlet` class that overrides `doGet`, `doPost`, `doPut`, and `doDelete`. It covers manually extracting request parameters, manually building JSON responses as strings, deploying WAR files to Tomcat's webapps folder, and testing with Postman. The lecture ends by explaining the Servlet lifecycle (constructor → init → service → doGet/doPost/etc. → destroy) and introduces the concept of a single "Dispatcher Servlet" pattern — foreshadowing how Spring MVC's `DispatcherServlet` evolved from this exact idea.

# Detailed Notes

## Client-Server Architecture Recap
- A client sends an **HTTP request** to a server; the server replies with an **HTTP response**.
- A client can be a frontend app, a mobile app, or a tool like Postman.
- A server is where backend code is deployed. In this lecture, the server is a **Tomcat server**, and Servlets will be deployed inside it.

## Why Not Just Use Core Java (`java.net`)?
- Core Java's `java.net` package (e.g., `ServerSocket`) can listen on a port (e.g., `localhost:8080`) and respond to incoming connections.
- **Problem:** `java.net` only sees incoming data as a raw stream of bytes — it has no understanding of HTTP concepts like method (GET/POST/PUT/PATCH/DELETE), endpoint, headers, body, or query parameters.
- To build anything usable, a developer would have to **manually**:
  1. Read the raw HTTP text.
  2. Parse out the URL, headers, query parameters, body, and method.
  3. Manually map the parsed endpoint/method to the correct Java method (e.g., `if (endpoint.equals("/hello") && method.equals("GET")) callHelloMethod();`).
  4. Manually build the HTTP response (status code, headers, body).
  5. Manually implement multi-threading to handle multiple simultaneous clients (otherwise one request blocks all others).
- This is a huge amount of non-business-logic "plumbing" work — roughly 13 distinct manual responsibilities were listed, and none of them involve actual business logic.

## Enter the Servlet Container
- Imagine a **container** that takes over all of this server-side plumbing (parsing requests, building responses, threading) so the server's job shrinks down to just routing.
- This container is called a **Servlet Container**. The most famous implementation is **Tomcat**.
- Because Tomcat now handles everything server-related, it is often just called "the server."

### What Tomcat Actually Does
1. Listens on a port (default **8080**).
2. When a request arrives, Tomcat parses it and builds two Java objects:
   - An **`HttpServletRequest`** object — populated with method, URL, host, headers, body, etc.
   - An empty **`HttpServletResponse`** object — to be filled in by the Servlet.
3. Tomcat determines which Servlet should handle the request (based on the endpoint mapping) and calls the relevant method (`doGet`, `doPost`, `doPut`, `doDelete`) on that Servlet, passing in the request and response objects.
4. The Servlet fills in the response object; because Java objects are passed by reference, whatever the Servlet writes into the response object is reflected in Tomcat's original object.
5. Tomcat sends that filled `HttpServletResponse` back to the client.

### What Is a Servlet?
- A **Servlet** is just a special Java class — not "special" in a magical sense, just a plain class that Tomcat manages and calls into.
- Multiple Servlets can exist; each is mapped to specific endpoint(s).
- Tomcat maintains the mapping of "which endpoint → which Servlet" (via annotations or an XML file).

### Parallel to Spring's IoC Container
| Spring Framework | Servlet World |
|---|---|
| IoC Container | Tomcat |
| Beans | Servlets |

Both are containers that manage the lifecycle of certain Java objects — Spring manages Beans, Tomcat manages Servlets. This is presented as the conceptual origin of Spring's IoC/Bean design.

## External Tomcat vs Embedded Tomcat
- **Embedded Tomcat** (used in Spring Boot via the `spring-boot-starter-web` dependency): Tomcat runs inside the application itself; you just run the app.
- **External Tomcat** (the older/manual way, used in this lecture):
  1. Download Tomcat separately (from the official Apache Tomcat site) and unzip it.
  2. Package your Java web app as a **WAR file** (Web Application Archive) — unlike a JAR, a WAR contains compiled classes, config files, Servlet classes, and optionally HTML/CSS/JS.
  3. Deploy (copy) the WAR file into Tomcat's `webapps` folder.
  4. Start Tomcat, which unpacks and runs the deployed application.
  5. Restart Tomcat every time the code changes, since it's not hot-reloaded automatically.

## Setting Up the Project
- Created a new Maven project (no Spring dependencies at all).
- In `pom.xml`:
  - Set `<packaging>war</packaging>` (default is `jar`, but Servlets require a WAR).
  - Added the **Jakarta Servlet API** dependency (found via Maven Repository, version noted as widely used and roughly 2–3 years old at the time).
  - Set the dependency's `<scope>` to **`provided`** — meaning it's needed only to *compile* the code (since it provides classes like `HttpServletRequest`/`HttpServletResponse`), but should **not** be bundled into the WAR file, because Tomcat already has its own copy of these Servlet library classes. Including it anyway would cause dependency resolution conflicts.
  - Added a `<build><finalName>crud-app</finalName></build>` tag so the generated WAR file has a clean name (`crud-app.war`) instead of the default `artifactId-version` (e.g., `demo-0.0.1-SNAPSHOT.war`).

## Building the CRUD Application

### Design Decisions
- No real database — data is stored in an **in-memory `HashMap<Integer, User>`** (key = user ID) to keep the example simple and avoid JDBC boilerplate.
- Structure mirrors Spring Boot conceptually: a **Servlet** layer (like a Controller) and a **Service** layer. No Repository layer since there's no real database.
- `UserServlet` directly instantiates `UserService` (`new UserService()`) since there's no dependency injection / auto-wiring outside of Spring — this creates **tight coupling**, explicitly called out as one of the drawbacks of raw Servlets.

### `User` Model
A plain POJO class (`in.strikers.model.User`) with four private fields: `id` (Integer), `name` (String), `email` (String), `mobile` (String) — plus generated getters/setters and an all-args constructor. No annotations needed since Spring isn't involved.

### `UserServlet`
- Annotated with `@WebServlet("/users")` — this tells Tomcat that **all** requests to `/users` (regardless of HTTP method) should route to this class.
- Extends `HttpServlet` (an abstract class), overriding:
  - `doPost` — Create a user.
  - `doGet` — Read one user (by `id` query parameter) or all users (if no `id` given).
  - `doPut` — Update a user (left as an assignment for viewers).
  - `doDelete` — Delete a user (left as an assignment for viewers).
- **Important Servlet design rule:** endpoint mapping in Servlets is always **class-specific**, not method-specific — one Servlet class handles all HTTP verbs for one endpoint, distinguished internally via `doGet`/`doPost`/etc.

### Reading Request Data
- Since there's no framework like Jackson to auto-convert JSON to Java objects, the lecturer avoided sending a JSON body altogether and instead sent all fields as **query/request parameters** (simpler to extract).
- Extraction pattern: `request.getParameter("id")` returns a `String`; since `id` is stored as `Integer` in the model, it must be manually converted using `Integer.parseInt(...)`.
- Basic validation example: if `id`, `name`, `email`, or `mobile` is `null`, return an error response (400) instead of proceeding.

### Building the Response Manually
- Since `doGet`/`doPost`/etc. all return `void`, the response must be written directly into the `HttpServletResponse` object:
  - `response.setStatus(200)` (or `400`, `404`, etc.)
  - `response.setContentType("application/json")`
  - `response.getWriter().write(jsonString)` — where `jsonString` is manually built by concatenating field values into a JSON-formatted string (with proper escaping of embedded quotes, and a `throws IOException` on the method signature).
- For a **single user**, JSON was built with simple string concatenation.
- For a **list of users**, a `StringBuilder` was used with a loop: append `[`, then for each user append its JSON (via a helper method `userToJson`), append a comma after every user except the last (checked via a standard indexed `for` loop, not a for-each, since a for-each doesn't give an easy "is this the last element" check), then finally append `]`.
- A `usersToJson(List<User>)` helper method wraps this looping/comma logic and calls the single-user `userToJson` method internally.

### Service Layer (`UserService`)
- Holds `private Map<Integer, User> userDb = new HashMap<>();` initialized in the constructor.
- Methods built: `createUser(User)` (puts into the map, returns the same user), `getAllUsers()` (returns `userDb.values()` — needed to be manually converted into a `List` via a simple loop since a naive cast to `List` throws a `ClassCastException`), and `getUserById(Integer id)` (returns `userDb.getOrDefault(id, null)`).

## Deploying and Testing
1. Run `mvn package` (or the "package" lifecycle step in the IDE) to generate the WAR file in the `target` folder.
2. Copy the WAR file into Tomcat's `webapps` folder — Tomcat automatically unpacks it into a matching folder structure (with compiled classes for the model, service, and servlet).
3. Restart the Tomcat server (via `catalina.sh run`, or `shutdown.sh` to stop it) for the new deployment to take effect.
4. Test using **Postman**:
   - POST to create a user (fields sent as query parameters: `id`, `name`, `email`, `mobile`).
   - GET with an `id` parameter to fetch one user; GET with no parameter to fetch all users.
   - Encountered and fixed two bugs live: an incorrect direct cast of `Collection` to `List` (fixed with a manual loop), and a missing `return` statement in the "get all users" branch causing a 500 error.

## Application Context Path
- An external Tomcat can host **multiple applications** simultaneously inside its `webapps` folder (e.g., `crud-app`, `order-app`, `teachers-app`), each identified by a unique context path.
- The final URL structure is: `http://localhost:8080/<context-path>/<servlet-endpoint>` — e.g., `http://localhost:8080/crud-app/users`.
- Within a single application, there can be multiple Servlets, each with its own endpoint mapping (e.g., `/users`, `/students`), determined by the `@WebServlet` annotation (or the older XML-based mapping approach).

## The `provided` Scope Explained
- Marking the Jakarta Servlet dependency as `scope=provided` means: use it to **compile** the code, but **do not include** it in the packaged WAR file.
- Reason: Tomcat itself already ships with its own Servlet API classes (`HttpServletRequest`, `HttpServletResponse`, etc.) since it's responsible for creating and managing Servlet objects. Bundling a duplicate copy into the WAR could cause dependency resolution conflicts (Tomcat wouldn't know whether to use its own classes or the ones bundled in the WAR).

## Servlet Lifecycle
Tomcat manages Servlet objects similarly to how Spring's IoC container manages Beans — it's responsible for creation and destruction (though not dependency injection/auto-wiring).

The lifecycle has three key callback methods:

| Method | Spring Boot Equivalent | When It's Called |
|---|---|---|
| `init()` | `@PostConstruct` | Once, when the Servlet is first created (lazily — only when first needed, not eagerly at startup) |
| `service()` | The normal request-handling call | On every incoming request; internally routes to `doGet`/`doPost`/`doPut`/`doDelete` based on the HTTP method |
| `destroy()` | `@PreDestroy` | Once, right before the Servlet/application is shut down |

- Under the hood, when a request arrives, Tomcat does roughly:
  1. `new HelloServlet()` → constructor runs.
  2. Calls `init()` (if overridden).
  3. On each request, calls `service()`.
  4. `service()` (defined in the parent `HttpServlet` class) internally checks the HTTP method and calls `doGet`/`doPost`/`doPut`/`doDelete` accordingly — this is why developers override those `do*` methods rather than `service()` directly.
  5. On shutdown, `destroy()` is called.
- Demonstrated live with a separate minimal `HelloServlet` (`@WebServlet("/hello")`) that prints log messages at each lifecycle stage (constructor, init, destroy) and returns plain text "hello". Confirmed via Tomcat's terminal logs (not visible in the IDE, since the code runs inside Tomcat, not inside IntelliJ).

## From Servlets to Spring MVC (Preview)
- As an application grows, having every individual Servlet duplicate the same logic (reading requests, writing responses, validation, routing) becomes repetitive and messy.
- The solution: introduce a single **Central Servlet** that all requests pass through first. This central Servlet handles cross-cutting concerns (logging, validation, request/response parsing) and then **dispatches** the request to the appropriate lightweight Servlet/handler.
- This is exactly the idea behind Spring MVC's **`DispatcherServlet`** — Spring didn't discard the Servlet concept; it centralized it, and replaced the small individual Servlets with **Controllers** (Beans).
- Spring Boot, in turn, is just an auto-configuration layer on top of Spring MVC — Spring MVC is what actually builds the web application, and internally, it still uses Servlets.

# Important Concepts
- **Client-Server Architecture:** A client sends HTTP requests; a server sends HTTP responses back.
- **`java.net` package:** Core Java's low-level networking package; can listen on a port but does not understand HTTP semantics.
- **Servlet Container:** A runtime (like Tomcat) that manages the full lifecycle of Servlets and handles all HTTP parsing/response-building plumbing.
- **Servlet:** A special Java class managed by a Servlet Container, mapped to one or more URL endpoints, used to handle incoming HTTP requests.
- **`HttpServletRequest` / `HttpServletResponse`:** Java objects built by Tomcat representing the incoming request (populated) and the outgoing response (initially empty, to be filled by the Servlet).
- **WAR file (Web Application Archive):** A package format (like a JAR) used to deploy web applications to an external Tomcat server; contains compiled classes, config files, and Servlet classes.
- **`provided` scope (Maven):** Marks a dependency as needed only for compilation, not for inclusion in the final packaged artifact, because the runtime environment (Tomcat) already supplies it.
- **Servlet Lifecycle (`init` → `service` → `destroy`):** The three callback stages of a Servlet's life, conceptually parallel to Spring's `@PostConstruct`, normal method execution, and `@PreDestroy`.
- **Context Path:** The unique path segment identifying a specific application deployed within Tomcat (e.g., `/crud-app`), distinguishing it from other applications hosted on the same Tomcat instance.
- **DispatcherServlet (preview):** Spring MVC's single centralized Servlet that all requests pass through before being routed to the appropriate Controller — conceptually the "Central Servlet" idea described in this lecture.

# Step-by-Step Process

## Setting Up External Tomcat and the Project
1. **Download Apache Tomcat** from the official website — choose the ZIP (Windows) or TAR.GZ (Mac/Linux) under the "Core" section, and unzip it into a folder.
2. **(Mac only) Grant execute permission** to the Tomcat startup script via terminal (`chmod`-style permission command, provided in notes).
3. **Start Tomcat** by running `./bin/catalina.sh run` from inside the Tomcat folder; confirm it started by visiting `http://localhost:8080` in a browser (should show the Apache Tomcat default page).
4. **Create a new Maven project** in IntelliJ (no Spring dependencies).
5. **Edit `pom.xml`:** set packaging to `war`, add the Jakarta Servlet API dependency with `scope=provided`, and add a `<finalName>` under `<build>` for a clean WAR filename.
6. **Reload Maven** to pull in the new dependency.

## Building the CRUD Servlet Application
1. Create a `model` package with a plain `User` POJO (fields: `id`, `name`, `email`, `mobile`; generate getters/setters and a constructor).
2. Create a `servlet` package with a `UserServlet` class extending `HttpServlet`, annotated `@WebServlet("/users")`.
3. Create a `service` package with a `UserService` class holding an in-memory `Map<Integer, User>` and methods for create/read operations.
4. In `UserServlet`, instantiate `UserService` directly (`new UserService()`).
5. Implement `doPost` to read parameters (`id`, `name`, `email`, `mobile`) via `request.getParameter(...)`, validate for nulls, build a `User` object, call `userService.createUser(...)`, and manually write a JSON success/error response.
6. Implement `doGet` to check for an `id` parameter — if present, fetch and return a single user (with a helper `userToJson` method); if absent, fetch and return all users (with a helper `usersToJson` method using a `StringBuilder` loop).
7. Leave `doPut` and `doDelete` as an exercise, following the same parameter-reading and service-delegation pattern.

## Deploying and Iterating
1. Run Maven's **package** lifecycle step to generate the WAR file in the `target` folder.
2. Copy the WAR file into Tomcat's `webapps` folder (delete any old version first).
3. Restart the Tomcat server from the terminal to pick up the new deployment.
4. Test endpoints in **Postman**, remembering to prefix the context path (e.g., `/crud-app/users`).
5. Repeat steps 1–4 after every code change, since there's no hot reload.

# Tips and Best Practices
- Keep the example simple by using an in-memory `HashMap` instead of a real database (avoids JDBC boilerplate) when the goal is to learn Servlet fundamentals rather than persistence.
- Mark framework-provided dependencies (like the Servlet API when deploying to an external Tomcat) with `provided` scope to avoid packaging conflicts.
- Use a `finalName` in the Maven build config to control the generated WAR file's name and keep the resulting URL path clean and predictable.
- Structure code into separate Servlet (controller-like) and Service layers even without Spring, to keep some separation of concerns.
- Prefer request/query parameters over a raw JSON body when there's no library (like Jackson) available to auto-parse JSON into objects — it significantly simplifies manual extraction.
- Always include a `return` statement after handling a response case in a `doGet`/`doPost` method to avoid falling through to unintended code (source of a live bug in the video).

# Mistakes to Avoid
- Don't try to directly cast a `Collection` (from `map.values()`) to a `List` — this throws a `ClassCastException`; convert manually via a loop (or Java Streams) instead.
- Don't forget `return` statements in branching request-handling logic — a missing `return` can cause the code to fall through to unrelated logic, leading to unexpected errors (a live 500 Internal Server Error in the video was traced to this).
- Don't bundle the Servlet API dependency into the deployed WAR file (i.e., don't leave its scope as default/`compile`) — this creates a conflict with the Servlet classes Tomcat already provides.
- Don't forget to include the application's context path (e.g., `/crud-app`) before the endpoint when testing — omitting it (e.g., calling `/users` instead of `/crud-app/users`) results in a 404.
- Don't expect logs from Servlet lifecycle methods (constructor, `init`, `destroy`) to appear in the IDE console — the code runs inside the external Tomcat process, so logs appear in Tomcat's own terminal/log output.
- Avoid overriding the `service()` method directly — override `doGet`/`doPost`/`doPut`/`doDelete` instead, since `service()` in the parent `HttpServlet` class already contains the logic to dispatch to the correct `do*` method based on the HTTP verb.

# Important Facts
- Default Tomcat port: **8080** (configurable in `server.xml` under the `<Connector>` tag).
- WAR = **Web Application Archive**; used for deploying Java web apps to an external Servlet container, as opposed to JAR files used for typical Spring Boot apps.
- The Jakarta Servlet API version referenced in the video was noted as being about 2–3 years old at the time and the most widely used version among users.
- A Servlet is created **lazily** by Tomcat by default (only on first request needing it), not eagerly at startup — though this behavior can be changed via configuration.
- `HttpServlet` is an **abstract class** and cannot be instantiated directly; custom Servlets must extend it and override specific `do*` methods.
- Escape sequences (`\"`) are required when embedding double quotes inside a Java string that is itself delimited by double quotes — relevant when manually constructing JSON strings.

# FAQs

**Q: Why learn Servlets if Spring Boot already provides an embedded Tomcat?**
A: Understanding Servlets reveals what Spring/Spring Boot is doing internally, since the whole Spring MVC architecture (and its `DispatcherServlet`) is built on top of Servlet technology.

**Q: What's the difference between embedded and external Tomcat?**
A: Embedded Tomcat (used by Spring Boot via `spring-boot-starter-web`) runs inside the application itself. External Tomcat is a separately downloaded and run server, to which you deploy a packaged WAR file.

**Q: Why does a Servlet's endpoint mapping apply to the whole class instead of individual methods?**
A: Because Servlets, by design, always use class-specific endpoint mapping — one Servlet class (via `@WebServlet`) handles all HTTP verbs for a given endpoint, distinguished internally through `doGet`, `doPost`, `doPut`, and `doDelete`.

**Q: Why is the Servlet dependency marked as `provided` scope?**
A: Because Tomcat already includes its own copy of the Servlet API classes at runtime; the dependency is only needed to compile the code, not to be bundled into the deployed WAR (to avoid conflicts).

**Q: How does Tomcat know which Servlet to call for a given request?**
A: Through mapping information supplied either via the `@WebServlet` annotation or an older XML-based configuration (`web.xml`), matched against the URL's context path and endpoint.

**Q: What happens if a request field like `id`, `name`, `email`, or `mobile` is missing?**
A: In this implementation, the Servlet checks for `null` values and returns a 400-style error response instead of proceeding to create/update the user.

**Q: What are the three main Servlet lifecycle callback methods?**
A: `init()` (like `@PostConstruct`), `service()` (dispatches to `doGet`/`doPost`/etc. on every request), and `destroy()` (like `@PreDestroy`, called before shutdown).

**Q: Why does raw JSON have to be built manually as a string?**
A: Because there's no Jackson-like library automatically converting Java objects to JSON without Spring; the response body has to be manually written into the `HttpServletResponse` writer as a formatted JSON string.

**Q: What is the "Central Servlet" idea mentioned near the end, and how does it relate to Spring?**
A: It's the idea of routing all incoming requests through one Servlet that handles common concerns (logging, validation, parsing) before dispatching to specific handlers — this is conceptually identical to Spring MVC's `DispatcherServlet`.

# Final Summary
- Understanding Servlets is foundational to understanding how Spring/Spring Boot and Spring MVC work internally.
- Core Java's `java.net` package can listen on a port but cannot parse or understand HTTP requests/responses — everything must be done manually, which is impractical for scalable applications.
- A **Servlet Container** (like Tomcat) solves this by handling all HTTP parsing, response building, and threading, leaving developers to focus on business logic.
- A **Servlet** is simply a special Java class managed by the container (analogous to a Spring Bean managed by the IoC container).
- Tomcat builds `HttpServletRequest` and `HttpServletResponse` objects for each incoming request and calls the mapped Servlet's `doGet`/`doPost`/`doPut`/`doDelete` method.
- The lecture builds a simple CRUD app using a single `UserServlet`, an in-memory `HashMap`-based `UserService`, manual parameter extraction, and manually constructed JSON responses.
- Deploying to an **external Tomcat** requires packaging the app as a **WAR file** and copying it into Tomcat's `webapps` folder, then restarting the server after every change.
- The Servlet API dependency should be scoped as `provided` in Maven since Tomcat supplies these classes at runtime.
- The **Servlet lifecycle** consists of `init()` → `service()` (which internally dispatches to `doGet`/`doPost`/etc.) → `destroy()`, paralleling Spring's `@PostConstruct` and `@PreDestroy`.
- Raw Servlet development is noticeably more complex than Spring Boot: tight coupling between Servlet and Service classes, manual JSON handling, and manual WAR deployment are all called out as pain points.
- The lecture closes by previewing the "Central Servlet" concept — the direct ancestor of Spring MVC's `DispatcherServlet` — setting up the next video in the series.