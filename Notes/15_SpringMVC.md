# Topic
Spring MVC Internal Architecture — how DispatcherServlet, HandlerMapping, and embedded Tomcat work together, followed by a hands-on Spring MVC (non-Boot) CRUD demo including a JSP-based view example.

# Overview
This video explains the internal architecture of Spring MVC, the actual web framework module inside the Spring ecosystem, and shows that Spring Boot is nothing more than an auto-configuration layer built on top of Spring MVC. It walks through how a client request travels from Tomcat (the servlet container) to a single central servlet called the DispatcherServlet, which internally uses components like HandlerMapping to figure out which controller method should handle a given endpoint. The video contrasts old-style servlet applications (one servlet per resource: UserServlet, OrderServlet, PaymentServlet) with the Spring MVC approach (one DispatcherServlet routing to many controllers). It also explains how request bodies, path variables, and query parameters get mapped into Java objects using libraries like Jackson. In the practical portion, the instructor builds a Spring MVC application from scratch without Spring Boot — manually adding dependencies (Spring MVC, embedded Tomcat, Jackson), writing a `Main.java` with a lot of boilerplate to start Tomcat, register the IoC container, and wire up the DispatcherServlet, and building a simple in-memory Student CRUD REST API. Finally, it demonstrates building a traditional MVC web app that returns a JSP view instead of JSON, explaining the original meaning of "MVC" (Model-View-Controller) and why JSP is largely legacy technology today. The core takeaway: Spring Boot automates all the manual configuration shown here, which is why the ecosystem moved away from raw Spring MVC.

# Detailed Notes

## What is Spring MVC?
- Spring MVC is the **actual, official web framework** of the Spring ecosystem — internally called "Spring Web MVC," commonly shortened to "Spring MVC."
- It provides the annotations and classes used to build web applications: `@Controller`, `@RestController`, `@GetMapping`, `@PostMapping`, `@RequestMapping`, etc. These come from **Spring MVC**, not Spring Boot.
- Two kinds of web applications can be built with it:
  - REST APIs (returning JSON)
  - Traditional web apps (returning HTML pages or JSP pages)
- **Key takeaway:** Spring Boot is not a separate framework — it is an auto-configuration layer sitting on top of Spring MVC. Understanding Spring MVC's internals means understanding Spring Boot's internals too.

## From Servlets to Spring MVC
- Before Spring MVC, applications used raw servlets: one servlet class per resource (e.g., `UserServlet`, `OrderServlet`, `PaymentServlet`), all registered with Tomcat (a servlet container).
- Tomcat's job: listen on a port, receive incoming HTTP requests, parse them (endpoint, method, parameters, path variables), and build two objects — `HttpServletRequest` and `HttpServletResponse` — then hand them to the matching servlet based on a URL mapping it maintains.
- **Problem with this approach:** every servlet repeats the same boilerplate work — checking request parameters, converting JSON to Java objects, building responses, converting Java objects back to JSON, exception handling. This duplicated logic isn't really "business logic."

## The Spring MVC Solution: DispatcherServlet
- Spring MVC's idea: instead of many servlets, Tomcat talks to **one single servlet** — the **DispatcherServlet**.
- The DispatcherServlet centralizes all the repetitive work (parameter checking, JSON↔Java conversion, response building, exception handling) that used to be duplicated across servlets.
- Individual resource servlets are replaced by **Controllers** (`UserController`, `OrderController`, `PaymentController`, etc.) — plain classes with mapped methods, not servlets themselves.
- Flow: Client → Tomcat → DispatcherServlet → (via HandlerMapping) → correct Controller method → response flows back the same path.

## HandlerMapping
- HandlerMapping builds a **mapping table**: which endpoint + HTTP method maps to which controller class and method.
- How the table is built:
  1. The Spring **IoC container** first scans and creates all beans (`@Component`, `@Service`, `@Repository`, `@Controller`/`@RestController` beans, etc.).
  2. HandlerMapping then scans those controller beans for annotations like `@GetMapping`, `@PostMapping`, etc., and builds the endpoint-to-method mapping table.
- Once the table exists, the DispatcherServlet uses it to invoke the correct controller method for each incoming request (internally it also uses a component called the "Handler Method Argument Resolver," but this level of detail isn't essential to remember).

## How Different Types of Client Data Get Mapped
A client can send data to the server in three ways:
1. **Request body** (used in POST/PUT/PATCH requests) — usually JSON.
2. **Path variable** — e.g., `/user/1`.
3. **Query parameter** — e.g., `/users?id=1`.

Mapping responsibilities:
- **Request body → Java object:** handled by the DispatcherServlet using a JSON-conversion library such as the **Jackson library**, which maps JSON fields to matching fields in a Java class (e.g., `id`, `name`).
- **Path variables:** resolved by HandlerMapping (via its internal "Handler Method Argument Resolver"). It matches the URL pattern (e.g., `/users/{id}`) against the incoming request and extracts the variable.
- **Query parameters:** handled directly by Tomcat itself — Tomcat stores query parameters inside the `HttpServletRequest` object, retrievable via methods like `getQueryParameter`.

## Full Request Flow (End to End)
1. Client sends an HTTP request to Tomcat (e.g., on port 8080).
2. Tomcat builds `HttpServletRequest` and `HttpServletResponse` objects and forwards them to the DispatcherServlet (the only servlet Tomcat talks to).
3. DispatcherServlet asks HandlerMapping which controller/method corresponds to the requested endpoint.
4. HandlerMapping returns the correct controller + method reference.
5. DispatcherServlet invokes that method (Controller → Service → Repository → back up).
6. The controller method's response is placed into the `HttpServletResponse` object (methods like `doGet`/`doPost` are `void` — they mutate the response object by reference).
7. Tomcat reads the completed `HttpServletResponse` and returns it to the client.
8. If the response is an object, the DispatcherServlet converts it to JSON before Tomcat sends it back.

## Building a Spring MVC App Manually (Without Spring Boot)
To replicate what Spring Boot gives for free, several dependencies and a large amount of boilerplate code are needed.

**Required Maven dependencies:**
- `spring-webmvc` (Spring MVC itself — provides `@Controller`, `@RestController`, `@GetMapping`, etc.)
- Embedded Tomcat (`tomcat-embed-core`) — so Tomcat runs inside the application rather than as an external server requiring a deployed WAR file.
- `jackson-databind` — for JSON ↔ Java object conversion.

(Contrast: in Spring Boot, a single `spring-boot-starter-web` dependency pulls all of this in automatically.)

**Application layers built (Student CRUD example):**
- **Entity package:** `Student` class (POJO) with `id`, `name`, `email` fields and generated getters/setters. (No JPA annotations used — this is a plain POJO.)
- **Repository package:** `StudentRepository` — a `@Repository`-annotated class using an in-memory `HashMap<Long, Student>` (`studentDb`) to mimic a database, with `save()`, `findById()`, and `findAll()` methods.
- **Service package:** `StudentService` — a `@Service`-annotated class that depends on `StudentRepository` via constructor injection (no `@Autowired` needed on constructors), exposing `createStudent()`, `getStudent()`, and `getAllStudents()`.
- **Controller package:** `StudentController` — annotated `@RestController` and `@RequestMapping("/students")`, exposing:
  - `POST /students` (empty path) → `@RequestBody Student` → calls service `createStudent()`, returns `ResponseEntity.ok(...)`.
  - `GET /students/{id}` → `@PathVariable Long id` → calls `getStudent()`; returns `ResponseEntity.notFound().build()` if null, otherwise `ResponseEntity.ok(...)`.
  - `GET /students` (empty path) → calls `getAllStudents()`, returns the list (empty or not) with `200 OK`.

**Configuration class (`WebConfig`):**
- Annotated with `@Configuration` and `@ComponentScan(basePackages = "in.strikes")` (or whatever the base package is) so all `@Component`/`@Service`/`@Repository`/`@Controller` classes get scanned and registered as beans.
- Annotated with `@EnableWebMvc` — this activates Spring MVC-specific annotations like `@RequestBody`, `@GetMapping`, `@PostMapping`, etc. Without it, these annotations don't work.

**`Main.java` boilerplate required (all done automatically by Spring Boot):**
1. Create a `Tomcat` object: `new Tomcat()`.
2. Set the port: `tomcat.setPort(8080)`.
3. Get a connector: `tomcat.getConnector()` — initializes the listener for that port.
4. Define a `contextPath` (left empty here since not deploying a named app) and a `baseDir` (a temporary/working document folder — e.g., a `webapp` folder under `src/main`, resolved via `new File(...).getAbsolutePath()`).
5. Call `tomcat.addContext(contextPath, baseDir)` to create a `Context` object. (Context = the concept of a deployed "web application" that Tomcat needs to know about, whether embedded or external.)
6. Start the Spring IoC container: `AnnotationConfigWebApplicationContext` (the web-specific IoC container implementation, as opposed to `AnnotationConfigApplicationContext` used in plain Spring Core), then `springContext.register(WebConfig.class)`.
7. Create the `DispatcherServlet` object, passing it the Spring context (so it can access HandlerMapping/all registered beans).
8. Register the servlet with Tomcat: `Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet)`.
9. Add a servlet mapping: map `"/"` (all requests) to the DispatcherServlet via `context.addServletMappingDecoded("/", "dispatcherServlet")`.
10. Start Tomcat: `tomcat.start()`.
11. Keep the server alive: `tomcat.getServer().await()`.

**Notes on this manual setup:**
- None of this boilerplate needs to be memorized — it's meant to build intuition for what Spring Boot automates.
- A common pitfall: using `Long` for an `id` path variable requires explicitly specifying the path-variable name in `@PathVariable("id")`, because Spring appends an `L` suffix internally when parsing longs, which can break automatic name matching.
- Testing via Postman: `POST /students` (with JSON body) and `GET /students/{id}` both worked correctly once the app was configured, confirming the manual setup mirrors what a Spring Boot app does automatically.

## `@Controller` vs `@RestController`
- Switching `@RestController` to `@Controller` still works, but every method that should return raw data (not a view name) must be separately annotated with `@ResponseBody`.
- `@RestController` = `@Controller` + `@ResponseBody` combined — that's the only difference.
- This matters because Spring MVC isn't only for REST APIs — it can also return **views** (HTML or JSP pages) instead of JSON.

## MVC's Original Meaning: Model-View-Controller
- **Controller:** the layer handling HTTP requests and defining endpoints (already covered above).
- **View:** an HTML page or JSP (Java Servlet Pages) page shown to the user on the front end. JSP is largely legacy/primitive technology today — modern front ends use React, Vue, plain JavaScript, etc. — but JSP still appears in legacy codebases.
- **Model:** an object used to pass data from the controller to the view, enabling dynamic HTML generation (e.g., `model.addAttribute("message", "Enter your name")`).

## JSP Demo (Traditional Web App, Not REST)
Building a small "Hello, [Name]" web app that returns HTML/JSP instead of JSON:

**Extra dependency needed:** `tomcat-embed-jasper` (the JSP engine for embedded Tomcat) — replacing the Jackson dependency, since no JSON is returned here.

**Project structure:**
- `src/main/webapp/WEB-INF/views/home.jsp` — mirrors the classic external-Tomcat structure (`META-INF` + `WEB-INF`), needed because Tomcat (embedded or external) always expects this layout for a deployed web app.
- `home.jsp` contains HTML with embedded dynamic expressions like `${message}`, referencing the model attribute set in the controller.
- A basic `style.css` was added under `webapp/assets/` for simple styling.

**Controller (`HelloController`, `@Controller` not `@RestController`):**
- `GET /` (`showHomePage()`): adds a model attribute `message = "Enter your name"`, returns the string `"home"` — this string is **not** literal text returned to the client; it's the logical **view name** Spring MVC resolves to an actual JSP file (because `@Controller` — not `@RestController` — is used).
- `POST /greet` (`greetUser()`): reads the submitted `name` as a `@RequestParameter` (data submitted via an HTML form arrives as a query/request parameter, similar to how Postman sends a request body for JSON APIs), adds a model attribute `message = "Hello " + name`, returns `"home"` again to redisplay the same JSP with the updated message.

**Additional `WebConfig` requirements for JSP support:**
- Implement `WebMvcConfigurer` and override a resource-handling method so static assets (like `style.css`) are served correctly (needed because `@EnableWebMvc` otherwise interferes with static resource serving). This is legacy boilerplate — safe to copy without memorizing.
- Register a `ViewResolver` bean: configure `setPrefix("/WEB-INF/views/")` and `setSuffix(".jsp")` so that a returned view name like `"home"` resolves to the actual file `/WEB-INF/views/home.jsp`.

**Additional `Main.java` requirements for JSP:**
- Use `addWebapp(...)` instead of `addContext(...)` when creating the Tomcat context.
- Add a servlet container initializer for JSP support: `context.addServletContainerInitializer(new JasperInitializer(), Set.of())`, tied to the `tomcat-embed-jasper` dependency.
- Add an extra line so the DispatcherServlet doesn't swallow all `/` requests in a way that blocks JSP pages from being served (by default, the DispatcherServlet claims all `/` mappings, which conflicts with JSP page handling without this adjustment).

**Result when tested in a browser:**
- Visiting `localhost:8080/` shows a form asking "Enter your name."
- Submitting a name (e.g., "Aditya") triggers a `POST /greet` call; the same page re-renders showing "Hello Aditya."

# Important Concepts
- **DispatcherServlet:** The single, central servlet that Tomcat interacts with in a Spring MVC app. It replaces the need for many separate resource-specific servlets and handles JSON↔Java conversion, response building, and exception handling centrally.
- **HandlerMapping:** The component that builds and holds the mapping table connecting each endpoint (and HTTP method) to a specific controller class and method, based on scanning `@GetMapping`/`@PostMapping`/etc. annotations on controller beans.
- **IoC Container (Spring context):** Manages all application beans (`@Component`, `@Service`, `@Repository`, `@Controller`). HandlerMapping relies on the IoC container having already created all beans before it can build its mapping table.
- **Embedded vs. External Tomcat:** External Tomcat runs as a separate server where a WAR file is deployed; embedded Tomcat runs inside the application itself, and the app can simply be packaged as a JAR file and started directly.
- **Context / Context Path:** Represents a single deployed web application within Tomcat. Even one embedded, non-deployed app still needs a context path (can be empty) and a base directory (a physical folder) for Tomcat's internal requirements.
- **`@EnableWebMvc`:** Activates Spring MVC-specific behavior (mapping annotations, `@RequestBody`, etc.). Required in manual (non-Boot) setups; Spring Boot enables this automatically via its own auto-configuration annotation.
- **`@ComponentScan`:** Tells the IoC container which packages to scan for annotated beans (controllers, services, repositories).
- **Jackson library:** Converts JSON request bodies into Java objects and vice versa. Used implicitly by DispatcherServlet.
- **ViewResolver:** Resolves a controller's returned logical view name (a `String`, e.g. `"home"`) into an actual file path (e.g., `/WEB-INF/views/home.jsp`) using a configured prefix and suffix.
- **Model:** An object that carries data from a controller method to a view, enabling dynamic content in JSP/HTML pages.
- **Spring Boot's real role:** Spring Boot = Spring MVC + auto-configuration. It provides `spring-boot-starter-web` (bundling embedded Tomcat, Spring MVC, and JSON support automatically), and its `@SpringBootApplication`-family annotation internally uses `@EnableAutoConfiguration`, eliminating almost all of the manual Tomcat/DispatcherServlet/IoC wiring shown in this video.

# Step-by-Step Process

**Building a Spring MVC REST CRUD app manually (Student example):**
1. Create a new empty Maven project.
2. Add dependencies: Spring MVC (`spring-webmvc`), embedded Tomcat (`tomcat-embed-core`), and Jackson (`jackson-databind`).
3. Create an `entity` package with a plain `Student` POJO class (`id`, `name`, `email` + getters/setters).
4. Create a `repository` package with a `@Repository`-annotated `StudentRepository` class backed by an in-memory `HashMap`, exposing `save()`, `findById()`, and `findAll()`.
5. Create a `service` package with a `@Service`-annotated `StudentService` class that depends on `StudentRepository` (constructor injection), exposing `createStudent()`, `getStudent()`, and `getAllStudents()`.
6. Create a `controller` package with a `@RestController` + `@RequestMapping("/students")` class exposing `POST`, `GET /{id}`, and `GET` (all) endpoints.
7. Create a `config` package with a `WebConfig` class annotated `@Configuration`, `@ComponentScan(basePackages = "...")`, and `@EnableWebMvc`.
8. In `Main.java`: create and configure a `Tomcat` object (port, connector, context path, base directory, context).
9. Start the Spring IoC container (`AnnotationConfigWebApplicationContext`) and register `WebConfig`.
10. Create a `DispatcherServlet`, passing it the Spring context; register it with Tomcat and map it to `/`.
11. Start Tomcat (`tomcat.start()`) and keep it running (`tomcat.getServer().await()`).
12. Test endpoints via Postman (`POST /students`, `GET /students/{id}`, `GET /students`).

**Adding JSP-based view support (extension of the above):**
1. Add the `tomcat-embed-jasper` dependency (JSP engine).
2. Create `src/main/webapp/WEB-INF/views/home.jsp` with HTML plus dynamic `${message}` expressions.
3. (Optional) Add static assets like `style.css` under `webapp/assets/`.
4. Create a `@Controller` (not `@RestController`) with a `GET /` method that adds a model attribute and returns the logical view name `"home"`, and a `POST /greet` method that reads a `@RequestParameter` name, updates the model attribute, and returns `"home"` again.
5. In `WebConfig`: implement `WebMvcConfigurer` (override the resource-handling method for static assets) and register a `ViewResolver` bean with prefix `/WEB-INF/views/` and suffix `.jsp`.
6. In `Main.java`: use `addWebapp(...)` instead of `addContext(...)`, add a `JasperInitializer` via `addServletContainerInitializer`, and adjust the DispatcherServlet mapping so JSP requests aren't swallowed.
7. Run the app and test in a browser at `localhost:8080/`.

# Tips and Best Practices
- You don't need to memorize the Spring MVC boilerplate (Tomcat setup, DispatcherServlet wiring) — the goal is to understand *what* is happening conceptually, since Spring Boot automates all of it.
- When using `Long` as a path variable type, explicitly name the path variable in `@PathVariable("id")` to avoid mismatches caused by Spring's internal `L` suffix handling for longs.
- Package names should always start with lowercase letters (a general Java convention mentioned while creating the `repository` package).
- Use constructor injection for dependencies (e.g., service depending on repository) — Spring auto-wires constructor parameters without needing an explicit `@Autowired` annotation.
- When testing manually built apps, use Postman to verify each CRUD endpoint (POST to create, GET by path variable, GET all) before moving on.
- `@RestController` and `@Controller` + `@ResponseBody` are functionally equivalent — use whichever is clearer for your use case, but understand that `@RestController` is simply a convenience combination.

# Mistakes to Avoid
- Don't try to use `ApplicationContext` for a web application — this will fail; you need `AnnotationConfigWebApplicationContext`, the web-specific IoC container implementation.
- Don't forget `@EnableWebMvc` in a manually configured `WebConfig` — without it, mapping annotations like `@GetMapping`/`@PostMapping`/`@RequestBody` won't be activated.
- Don't forget that the DispatcherServlet, by default, captures all `/` requests — when serving JSP pages too, this can prevent proper routing unless explicitly adjusted.
- Don't forget the `ViewResolver` bean when returning views — without a prefix/suffix configuration, a returned view name like `"home"` won't resolve to the correct JSP file.
- Ensure the string returned from a `@Controller` method matches the actual view file name exactly (a mismatch was shown as a bug during the demo, where the file was named `hello.jsp` but the code returned `"home"` — the file had to be renamed to `home.jsp` to match).
- Don't confuse `contextPath` (identifies which deployed application you're referring to) with `baseDir`/document base (the physical folder path) — Tomcat needs both, even for an embedded, non-deployed app.

# Important Facts
- Spring MVC's internal module name is "Spring Web MVC."
- MVC stands for **Model-View-Controller**.
- JSP stands for **Java Servlet Pages**.
- Default port used throughout the demo: **8080**.
- Sample dependency versions mentioned: Spring MVC/Spring Web `7.0.7`; `tomcat-embed-core` and `tomcat-embed-jasper` `11.0.22`.
- Three dependencies needed for a basic Spring MVC REST setup: `spring-webmvc`, `tomcat-embed-core`, `jackson-databind`.
- A fourth dependency, `tomcat-embed-jasper`, is needed specifically for JSP/view support (replacing the need for Jackson in that scenario).
- `@RestController` = `@Controller` + `@ResponseBody`.

# FAQs

**Q: Is Spring Boot a completely different framework from Spring MVC?**
A: No. Spring Boot is an auto-configuration layer built on top of Spring MVC (which itself is built on Spring Core). Learning Spring MVC's internals is effectively learning Spring Boot's internals.

**Q: What is the DispatcherServlet, in simple terms?**
A: It's the single central servlet that Tomcat forwards all HTTP requests to in a Spring MVC application, instead of Tomcat interacting with many separate resource-specific servlets.

**Q: How does the DispatcherServlet know which controller method to call?**
A: It relies on HandlerMapping, which scans all controller beans (already created by the IoC container) for mapping annotations (`@GetMapping`, `@PostMapping`, etc.) and builds a lookup table of endpoint → controller method.

**Q: How are query parameters different from path variables and request bodies in terms of who handles them?**
A: Query parameters are handled directly by Tomcat itself; path variables are resolved by HandlerMapping; request bodies (JSON) are converted to Java objects by the DispatcherServlet using the Jackson library.

**Q: Why does a Spring Boot app need far less code than the manual Spring MVC app shown here?**
A: Spring Boot's starter dependencies (like `spring-boot-starter-web`) auto-configure embedded Tomcat, register the DispatcherServlet, enable `@EnableWebMvc`-equivalent behavior, and set up component scanning — all of which had to be written manually in this demo.

**Q: What's the difference between `@Controller` and `@RestController`?**
A: `@RestController` automatically applies `@ResponseBody` to all its methods; `@Controller` requires `@ResponseBody` to be added manually per method if you want raw data (like JSON) returned instead of a view name.

**Q: Is JSP still commonly used today?**
A: No — it's considered a legacy/primitive technology. Modern applications use front-end frameworks like React or Vue instead, though JSP may still be found in older legacy codebases.

**Q: What does the string returned from a `@Controller` method (like `"home"`) actually mean?**
A: When using `@Controller` (not `@RestController`), the returned string is treated as a logical view name, which the `ViewResolver` converts into an actual file path (e.g., prefix + name + suffix = `/WEB-INF/views/home.jsp`) rather than being sent to the client as literal text.

**Q: Why does embedded Tomcat still need a `webapp`/`WEB-INF` folder structure?**
A: Because Tomcat — whether embedded or external — expects a context (representing a deployed web application) to have an associated base directory and, for JSP/static content, the conventional `WEB-INF` layout, even if nothing is being formally "deployed" in the traditional external-server sense.

# Final Summary
- Spring MVC is Spring's official web framework module (internally "Spring Web MVC"); Spring Boot is simply an auto-configuration layer on top of it.
- In raw servlet-based apps, Tomcat interacts with many separate servlets, each duplicating parameter parsing, JSON conversion, and response-building logic.
- Spring MVC centralizes this into one DispatcherServlet, with individual Controllers replacing per-resource servlets.
- HandlerMapping builds the endpoint-to-method mapping table by scanning controller beans (created by the IoC container) for mapping annotations.
- Request bodies are converted via Jackson (by DispatcherServlet); path variables via HandlerMapping; query parameters directly by Tomcat.
- Building a Spring MVC app without Spring Boot requires manually adding dependencies (Spring MVC, embedded Tomcat, Jackson) and writing substantial boilerplate in `Main.java` to configure Tomcat, the IoC container, and the DispatcherServlet.
- A demo Student CRUD REST API was built with entity, repository, service, and controller layers, using an in-memory `HashMap` instead of a real database.
- `@RestController` is equivalent to `@Controller` + `@ResponseBody` on every method.
- MVC originally stands for Model-View-Controller, where View = HTML/JSP page and Model = data passed from controller to view.
- A JSP-based traditional web app demo showed returning view names instead of JSON, requiring an additional `ViewResolver` bean, `tomcat-embed-jasper` dependency, and adjusted DispatcherServlet mapping.
- JSP is largely legacy technology today, replaced by modern front-end frameworks, but understanding it clarifies MVC's original design intent.
- The overall purpose of this deep dive was to understand the underlying technology (servlets, Spring MVC internals) so that Spring Boot's automation makes sense — future application development in the series will move to Spring Boot.