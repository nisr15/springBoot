# Topic
Building your first Spring Boot "Hello World" application and understanding the client-server flow, IP addresses, ports, and Spring Initializr.

# Overview
This lecture is a hands-on introduction to Spring Boot, aimed at showing how quickly a working web application can be built even before learning any Spring syntax. The instructor first explains the client-server architecture conceptually: a browser (client) sends a request to a server, which in this case will be a Spring Boot application running locally. He explains how DNS resolves domain names to IP addresses, how port numbers let a single machine run multiple applications simultaneously, and how a reverse proxy maps standard ports (80/443) to an application's actual running port (like 8080). He then walks through generating a project skeleton using Spring Initializr (choosing Maven, Java, a Spring Boot version, packaging as JAR, and adding the Spring Web dependency), opening it in IntelliJ IDEA, and running the default application to see the embedded Tomcat server start. Finally, he creates a simple REST controller class with a `@RestController` annotation and a method annotated with `@GetMapping("hello")` that returns the string "Hello World," demonstrating that hitting `localhost:8080/hello` in a browser displays it. The video closes by acknowledging that many internal mechanisms (how annotations work, how the mapping happens) remain unexplained, motivating the need to study the full Spring framework in future lectures.

# Detailed Notes

### 1. Client-Server Flow Recap
- A **client** (here, the browser) sends a request to a **server** (the Spring Boot application).
- Since both the client and server run on the same machine in this demo, the "host" being hit is actually the local computer itself.
- **Key takeaway:** Localhost setups let you test client-server communication without a real network.

### 2. IP Addresses and Localhost
- Every device has a unique **IP address**, which can be checked by searching "What is my IP" on Google.
- If a client and server are on the same machine, the IP address becomes the fixed address **127.0.0.1**, commonly replaced with the word **localhost**.
- **Example:** Instead of typing an IP address, developers type `localhost:8080/hello`.

### 3. DNS and Real-World Requests
- When a client wants to reach a domain like `coderarmy.in`, it doesn't directly know the IP address of that domain.
- The client first queries a **DNS (Domain Name System)** server, which resolves the domain name into the corresponding IP address.
- Once the IP is obtained, the client uses it to actually call the server.

### 4. Port Numbers
- A single IP address can host **multiple applications** (e.g., Chrome, WhatsApp, Spotify, a Spring Boot app), each listening on a different **port number**.
- The operating system uses the port number to figure out which application a particular incoming request should go to.
- **Standard ports:**
  - HTTP traffic → port **80**
  - HTTPS traffic → port **443**
- Applications like Spring Boot often run on custom ports (e.g., 8080) by default, which can be changed.

### 5. Reverse Proxy
- Even though HTTPS traffic is sent to port 443, an application server might actually be running on port 8080.
- A **reverse proxy** sits between the client and the server and maps incoming requests from the standard port (443) to the port the application is actually running on (8080).
- **Key takeaway:** The reverse proxy allows external requests using standard ports to reach internal applications running on non-standard ports.

### 6. Requirements Before Building
- An **IDE** is needed (the instructor uses IntelliJ IDEA, though Eclipse, NetBeans, etc. work the same way).
- **Java** must be installed on the machine (any recent version is fine).

### 7. Spring Initializr
- **Spring Initializr** (start.spring.io) is a website that generates a ready-made Spring Boot project skeleton, so developers don't need to manually:
  - Set up the folder structure
  - Configure dependencies
  - Configure the Spring Boot and Java versions
  - Set up the package structure
- **Dependency:** An external library that your code relies on to avoid rewriting existing functionality (e.g., a MySQL connector library to connect to a MySQL database).
- **Options selected in the demo:**
  - **Project tool:** Maven (a project management tool for Java projects; Gradle Groovy/Kotlin are alternatives)
  - **Language:** Java
  - **Spring Boot version:** 4.0.6 (avoiding "SNAPSHOT" versions which are unfinished/work-in-progress, and "RC" i.e. Release Candidate versions which are not yet officially released)
  - **Packaging:** JAR (Java Archive) — modern Spring Boot apps use JAR rather than WAR (Web Archive, used in older Java web apps)
  - **Configuration format:** Properties (YAML is the alternative)
  - **Java version:** 21 (a balance between too new and too outdated)
  - **Dependency added:** Spring Web — enables building web and RESTful applications using Spring MVC, and comes with **Apache Tomcat** as the default embedded server/container

### 8. Opening and Running the Generated Project
- After downloading and unzipping the project, it's opened in IntelliJ IDEA.
- The folder structure includes `src > main > java` and `src > main > resources`, along with a generated `DemoApplication.java` file containing a `main` method annotated with a Spring Boot annotation, and a call to a `.run()` method.
- Running this default application (without any custom code) starts an embedded Tomcat server, visible in the console logs (e.g., "Tomcat started on port 8080").
- At this stage, visiting `localhost:8080/hello` doesn't return anything meaningful yet — it shows a "Whitelabel Error Page" because no endpoint has been mapped.

### 9. Writing the First Endpoint
- A new Java class named **HelloController** is created inside the existing package.
- The `@RestController` annotation is added above the class — it tells Spring that this class is a **controller**, acting as a gateway for defining API endpoints.
- A simple method `hello()` is written that returns the String `"Hello World"`.
- To make this method callable via the browser (rather than manually from Java code), two annotations are added:
  - `@GetMapping("hello")` — maps the method to a GET request at the `/hello` endpoint.
- After adding these annotations and rerunning the application, visiting `localhost:8080/hello` in the browser displays **"Hello World"**.
- The text can also be wrapped in HTML tags (e.g., `<h1>Hello World</h1>`) to change how it's rendered in the browser.

### 10. Adding a Second Endpoint
- A second method (e.g., `greetBy()`) can be created and mapped with `@GetMapping("bye")`, returning the string `"Bye"`.
- **Key takeaway:** The method name does not need to match the endpoint name — the mapping is controlled entirely by the `@GetMapping` annotation.
- Multiple endpoints can coexist in the same controller class, each returning its own response.

### 11. Changing the Port Number
- Inside `src/main/resources/application.properties`, the property `server.port` can be set to a custom value (e.g., changing it from 8080 to 9090).
- After this change, the application must be rerun, and the Tomcat server will start on the new port instead. The browser must then be pointed to the new port (e.g., `localhost:9090/hello`) to see the response.

# Important Concepts
- **Client-Server Architecture:** A client sends requests; a server processes them and sends back responses.
- **IP Address:** A unique identifier for every device on a network.
- **Localhost (127.0.0.1):** A fixed IP address meaning "this same computer," used when client and server are on the same machine.
- **DNS:** Resolves human-readable domain names into IP addresses.
- **Port Number:** A unique identifier used alongside an IP address to route incoming data to the correct application on a device.
- **Reverse Proxy:** Maps requests coming in on a standard port (like 443) to the actual port an application is running on (like 8080).
- **Dependency:** An external library your code relies on instead of writing that functionality from scratch.
- **Spring Initializr:** A website that generates a pre-configured Spring Boot project skeleton (folder structure, dependencies, versions) so developers can focus directly on business logic.
- **JAR vs WAR:** JAR (Java Archive) is used by modern Spring Boot apps; WAR (Web Archive) was used by older Java web applications.
- **Embedded Server (Tomcat):** A servlet container bundled directly into the Spring Boot application, removing the need to separately install and configure a server.
- **@RestController:** An annotation marking a class as a controller — a gateway where API endpoints are defined.
- **@GetMapping:** An annotation that maps a specific URL endpoint to a Java method for handling GET requests.
- **Business Logic:** The actual functional code of an application, as opposed to setup/configuration code.

# Step-by-Step Process
1. Ensure an IDE (e.g., IntelliJ IDEA) and Java are installed on your computer.
2. Visit the Spring Initializr website (start.spring.io).
3. Select project settings: Maven as the project tool, Java as the language, a stable Spring Boot version (avoiding SNAPSHOT/RC versions), JAR packaging, Properties configuration format, and a Java version (e.g., 21).
4. Add the **Spring Web** dependency to enable building web/RESTful applications with an embedded Tomcat server.
5. Click **Generate** to download the project as a ZIP file.
6. Unzip the downloaded file and open the project folder in your IDE.
7. Run the default generated application and confirm in the console logs that Tomcat has started (e.g., "Tomcat started on port 8080").
8. Create a new Java class (e.g., `HelloController`) inside the existing package.
9. Annotate the class with `@RestController`.
10. Write a method that returns a String (e.g., `"Hello World"`).
11. Annotate the method with `@GetMapping("hello")` to map it to the `/hello` endpoint.
12. Rerun the application and visit `localhost:8080/hello` in the browser to see the output.
13. (Optional) Add more endpoints by creating additional methods with their own `@GetMapping` annotations.
14. (Optional) Change the server port by editing `server.port` in `application.properties`.

# Tips and Best Practices
- When choosing a Spring Boot version in Spring Initializr, avoid **SNAPSHOT** versions (unfinished, work-in-progress, more likely to have bugs) and be cautious with **RC (Release Candidate)** versions (almost final but not officially released) — prefer the latest **stable** version.
- Choose a Java version that is neither too old nor bleeding-edge (the instructor picked Java 21 as a balanced choice).
- Use JAR packaging for modern Spring Boot web applications rather than WAR.
- Don't worry about understanding every annotation or folder immediately — focus first on getting a basic app running, and deeper understanding will come as you study the full Spring framework.

# Mistakes to Avoid
- Don't manually call controller methods from the `main` method — this defeats the purpose of using annotations like `@RestController` and `@GetMapping`, which let Spring handle the routing automatically based on incoming HTTP requests.
- Don't assume the method name must match the endpoint path — they are independent; the endpoint is defined solely by the `@GetMapping` annotation's value.
- Don't forget to rerun the application after making code or configuration changes (e.g., changing the port or adding new endpoints) — changes won't take effect until the app is restarted.
- Don't confuse the port shown as "Tomcat started on port X" with an error — if you see a "Whitelabel Error Page," it typically just means the specific endpoint you tried hasn't been mapped yet.

# Important Facts
- Default Spring Boot Initializr options used in the demo: Maven, Java, Spring Boot 4.0.6, JAR packaging, Properties configuration, Java 21, Spring Web dependency.
- HTTP traffic conventionally uses port **80**; HTTPS traffic conventionally uses port **443**.
- The embedded server used by Spring Web (via Spring Initializr) is **Apache Tomcat**.
- Localhost / loopback address: **127.0.0.1**.
- The application's default port in the demo was **8080**, later changed to **9090** via `application.properties`.

# FAQs

**Q1: Why do I see a "Whitelabel Error Page" when I visit localhost:8080/hello right after generating the project?**
A: Because no endpoint has been mapped yet in the code — this is Spring Boot's default fallback error page for unmapped requests.

**Q2: Do I need to manually install Tomcat?**
A: No. Tomcat comes embedded automatically when you include the Spring Web dependency via Spring Initializr.

**Q3: Why do I need a port number if I already have an IP address?**
A: Because a single device can run multiple applications at once; the port number tells the operating system which specific application should receive the incoming data.

**Q4: What's the difference between JAR and WAR packaging?**
A: JAR (Java Archive) is used by modern Spring Boot applications, while WAR (Web Archive) was used in older-style Java web applications.

**Q5: Does the method name have to match the endpoint name in `@GetMapping`?**
A: No — the endpoint path is defined entirely by the value passed to `@GetMapping`, regardless of what the method itself is named.

**Q6: How do I change which port my Spring Boot application runs on?**
A: Edit the `server.port` property in the `application.properties` file located under `src/main/resources`.

**Q7: What does the `@RestController` annotation do?**
A: It marks a class as a controller, meaning it acts as a gateway where API endpoints are defined and mapped to methods.

**Q8: Why does the instructor say we should still learn the full Spring framework instead of just Spring Boot?**
A: Because Spring Boot's simplicity relies on underlying Spring concepts (like what a controller is, how mappings work internally); without that foundation, debugging issues or understanding what's happening internally becomes very difficult.

# Final Summary
- The lecture demonstrates building a complete "Hello World" Spring Boot web application from scratch.
- Client-server architecture was explained using the browser (client) and a locally running Spring Boot app (server).
- Localhost (127.0.0.1) represents a request sent to your own machine rather than a network destination.
- DNS resolves domain names into IP addresses so clients can locate servers.
- Port numbers allow a single IP address/machine to run multiple applications simultaneously.
- Standard ports: HTTP uses port 80, HTTPS uses port 443; a reverse proxy maps these to an application's actual running port.
- Spring Initializr auto-generates a fully configured project skeleton (folder structure, dependencies, versions), removing manual setup work.
- Key options chosen: Maven, Java, a stable Spring Boot version, JAR packaging, Properties config, Java 21, and the Spring Web dependency (which bundles Apache Tomcat).
- A REST endpoint was created using `@RestController` and `@GetMapping("hello")`, returning "Hello World" to the browser.
- Multiple endpoints can be added by defining additional annotated methods.
- The server's port can be changed via the `server.port` property in `application.properties`.
- The lecture concludes by noting that many internal mechanisms remain unexplained, motivating further study of the full Spring framework.