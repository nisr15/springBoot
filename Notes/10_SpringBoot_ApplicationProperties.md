# Topic
Spring Boot Core Fundamentals: Using `application.properties`, `@Value`, `@ConfigurationProperties`, and Application Runners

# Overview
This lecture continues a Spring Framework series focused on strengthening the core fundamentals of Spring Boot. It covers two major topics: how to externalize configuration values using the `application.properties` file instead of hardcoding them in Java classes, and how to run custom logic automatically when a Spring Boot application starts, without manually interacting with the IoC container. The instructor builds a `PaymentGateway` class with fields like `type` and `retryCount`, and demonstrates three ways of injecting values into it: hardcoding, using the `@Value` annotation for individual fields, and using `@ConfigurationProperties` for mapping multiple properties at once through a dedicated properties class (`PaymentProperties`). The video also explains default values, canonical (kebab-case) naming rules for property keys, and the difference between manually fetching beans from the `ApplicationContext` versus using the `ApplicationRunner` and `CommandLineRunner` interfaces to auto-execute code on startup. The session ends with a preview of the next video, which will move into building a Spring Boot web application.

# Detailed Notes

## 1. Recap and Motivation
- Previous lectures covered Spring Core concepts (dependency injection, IoC container) and how much simpler these become with Spring Boot.
- Directly interacting with the IoC container (fetching beans manually via `ApplicationContext`) is **not good practice** in Spring Boot, since Spring Boot is designed to auto-configure things behind the scenes using annotations.
- Goal of this lecture: learn how to (1) use `application.properties` for configuration and (2) run code automatically at startup without manual bean fetching.

## 2. Project Setup
- A basic Spring Boot project was generated using Spring Initializr with: Java, Maven, version 4.1.0, packaging as Jar, Java 21, and only the Spring Boot Starter and Spring Boot Starter Test dependencies.
- The default `application.properties` file (found in the `resources` folder) only contains `spring.application.name=demo` by default.

## 3. Creating a Sample Class (`PaymentGateway`)
- A new class `PaymentGateway` was created with two private fields:
  - `String type` — which payment gateway (e.g., Razorpay, Paytm)
  - `int retryCount` — how many times to retry a payment
- Getters and setters were generated for both fields.
- Marked with `@Component` so Spring can scan and manage it as a bean.

## 4. Why Externalize Configuration?
- Initially, values were set directly in Java code (hardcoded), which required recompiling the application every time a value (e.g., switching from Paytm to Razorpay) needed to change.
- **Key idea:** Externalizing configuration into a non-Java file lets values be changed without touching or recompiling the Java code.
- `application.properties` is a **configuration file** — a non-Java file containing key-value pairs (like a map), automatically loaded by Spring Boot whenever the project is compiled/run.
- Other types of configuration sources also exist:
  - `application.yml` (YAML alternative, also offered by Spring Initializr)
  - Environment variables
  - Command-line arguments
  - System properties
- Important distinction: the "configuration file" (`application.properties`) is different from a "configuration class" (a Java class annotated with `@Configuration` where `@Bean` methods are defined).

## 5. Using `@Value` to Inject Individual Properties
- Properties were added to `application.properties`:
  ```
  payment-gateway.type=Paytm
  payment-gateway.retry-count=5
  ```
- The `@Value` annotation reads individual properties:
  ```java
  @Value("${payment-gateway.type}")
  private String type;

  @Value("${payment-gateway.retry-count}")
  private int retryCount;
  ```
- `@Value` can be used on fields, constructor parameters, or setters — same as `@Autowired` for dependencies, but for primitive/simple values.
- Common syntax mistake: using curly braces `{}` instead of parentheses `()` in the annotation causes a syntax error.
- If a referenced property key doesn't exist in `application.properties`, Spring throws a **BeanCreationException** (unsatisfied dependency / autowiring failure), because it cannot resolve the value.
- **Default values** can be provided using a colon syntax:
  ```java
  @Value("${payment-gateway.type:Razorpay}")
  ```
  If the property is missing, the default (`Razorpay`) is used; if the property exists, the actual value overrides the default.

## 6. Using `@ConfigurationProperties` for Bulk Property Mapping
- Problem with `@Value`: when a class has many properties, annotating each field individually is repetitive, confusing, and error-prone.
- **Solution:** `@ConfigurationProperties`, an annotation placed on a class to automatically map all its fields from a properties file, without needing `@Value` on every field.
- A new class `PaymentProperties` was created with four fields: `type`, `retryCount`, `isEnabled`, `timeout`.
- Annotations used:
  ```java
  @Component
  @ConfigurationProperties(prefix = "payment-property")
  public class PaymentProperties {
      private String type;
      private int retryCount;
      private boolean isEnabled;
      private int timeout;
      // getters and setters
  }
  ```
- Corresponding properties file entries:
  ```
  payment-property.type=Paytm
  payment-property.retry-count=5
  payment-property.enabled=true
  payment-property.timeout=3000
  ```
- **Important naming rule (canonical form):** Property keys must be written in kebab-case (hyphen-separated, lowercase) — not camelCase. Example: `retry-count`, not `retryCount`.
- For a boolean field named `isEnabled`, the convention maps it to the property key `enabled` (the `is` prefix is dropped by convention), and the getter/setter naming follows this convention automatically.
- Spring Boot automatically converts kebab-case property keys (e.g., `retry-count`) into camelCase Java field names (e.g., `retryCount`) during mapping.

## 7. Recommended Design Pattern: Separate Properties Class from Business Class
- Best practice: don't fetch values directly from the properties class inside the class that uses them for business logic.
- Instead:
  - `PaymentProperties` (annotated with `@ConfigurationProperties`) is solely responsible for reading values from `application.properties`.
  - `PaymentGateway` (the business logic class) receives `PaymentProperties` via **constructor injection** and exposes its own getters that internally delegate to `PaymentProperties`.
  ```java
  @Component
  public class PaymentGateway {
      private final PaymentProperties paymentProperties;

      public PaymentGateway(PaymentProperties paymentProperties) {
          this.paymentProperties = paymentProperties;
      }

      public String getType() { return paymentProperties.getType(); }
      public int getRetryCount() { return paymentProperties.getRetryCount(); }
      public boolean isEnabled() { return paymentProperties.isEnabled(); }
      public int getTimeout() { return paymentProperties.getTimeout(); }
  }
  ```
- This is a standard, reusable pattern: a dedicated configuration/properties class reads external values, and business logic classes consume those values through dependency injection rather than reading the properties file directly.
- Result when printed: `Paytm 5 true 3000`.

## 8. Running Code Automatically at Startup: `ApplicationRunner` and `CommandLineRunner`
- Previously, the code manually fetched the `ApplicationContext` and called `context.getBean(PaymentGateway.class)` to get the bean and call methods — this is discouraged as a direct IoC container interaction.
- **Goal:** Have a method run automatically the moment the Spring Boot application starts, without manually managing the context or fetching beans.
- **`ApplicationRunner` interface:**
  - Implemented by a class (e.g., `DemoRunner`), annotated with `@Component`.
  - Requires overriding the `run(ApplicationArguments args)` method.
  - `ApplicationArguments` represents command-line arguments passed to the application; has methods like `getOptionNames()`, `containsOption()`, etc. (not covered in depth as they are rarely used in this course).
  - The dependent bean (e.g., `PaymentGateway`) is injected via constructor injection, and its method (e.g., `print()`) is called inside `run()`.
  - Example:
    ```java
    @Component
    public class DemoRunner implements ApplicationRunner {
        private final PaymentGateway paymentGateway;

        public DemoRunner(PaymentGateway paymentGateway) {
            this.paymentGateway = paymentGateway;
        }

        @Override
        public void run(ApplicationArguments args) {
            paymentGateway.print();
        }
    }
    ```
  - Once this is set up, simply running the application (`SpringApplication.run(...)`) automatically triggers `run()`, with no manual bean-fetching or context handling needed in the main class.
- **`CommandLineRunner` interface:**
  - A similar interface offering the same `run()` behavior, but the method signature takes varargs: `run(String... args)`.
  - `String... args` (varargs) allows passing zero, one, or multiple string arguments.
  - Behaves identically to `ApplicationRunner` in terms of when it executes (automatically on startup); the only difference is the type of arguments received (`ApplicationArguments` vs. `String...`).
- Both interfaces achieve the same end goal: automatically executing logic on application startup without manual IoC container interaction.
- The `print()` method inside `PaymentGateway` simply prints all four property values (`type`, `retryCount`, `isEnabled`, `timeout`).

## 9. Running via Terminal (Maven Command)
- Instead of clicking the IDE's "run" (green) button, the application can be run via terminal using:
  ```
  mvn spring-boot:run
  ```
- This produces the same output (`Paytm 5 true 3000`) as running from the IDE.
- Requires Maven to be installed.

# Important Concepts
- **`application.properties`**: A non-Java configuration file (key = value format) automatically loaded by Spring Boot; used to externalize values so code doesn't need to be recompiled when configuration changes.
- **Configuration file vs. Configuration class**: A configuration *file* (like `application.properties`) is non-Java and holds key-value settings; a configuration *class* is a Java class annotated with `@Configuration` where beans are defined using `@Bean`.
- **`@Value`**: Annotation used to inject a single property value (from `application.properties`, YAML, or environment variables) into a field, constructor parameter, or setter.
- **Default values with `@Value`**: Specified using a colon, e.g. `${key:defaultValue}` — used when the property might not exist.
- **`@ConfigurationProperties`**: Annotation placed on a class to bulk-map multiple properties (sharing a common prefix) into that class's fields, avoiding repetitive use of `@Value`.
- **Canonical form / kebab-case**: Property keys in `application.properties` must use lowercase hyphen-separated words (e.g., `retry-count`), not camelCase; Spring Boot automatically converts this to camelCase for Java field mapping.
- **Constructor injection pattern**: A best-practice pattern where a properties-holding class (`@ConfigurationProperties`) is injected into a business logic class via its constructor, rather than the business class reading properties directly.
- **`ApplicationRunner`**: A functional interface with a `run(ApplicationArguments args)` method; implementing classes auto-execute their `run()` logic when the Spring Boot application starts.
- **`CommandLineRunner`**: Similar to `ApplicationRunner` but its `run(String... args)` method takes variable string arguments (command-line style) instead of an `ApplicationArguments` object.
- **`ApplicationArguments`**: An object representing command-line arguments passed to a Spring Boot application (used with `ApplicationRunner`).
- **Varargs (`String... args`)**: Java syntax allowing a method to accept zero, one, or multiple arguments of the same type.

# Step-by-Step Process

### Setting up property injection via `@Value`
1. Add key-value pairs to `application.properties` (e.g., `payment-gateway.type=Paytm`).
2. Annotate the corresponding Java field with `@Value("${key.name}")`.
3. Optionally provide a default using `${key.name:defaultValue}`.
4. Run the application — the field will automatically be populated from the properties file.

### Setting up bulk property mapping via `@ConfigurationProperties`
1. Create a dedicated properties class (e.g., `PaymentProperties`).
2. Annotate it with `@Component` and `@ConfigurationProperties(prefix = "your-prefix")`.
3. Define fields matching the property names (in camelCase in Java).
4. In `application.properties`, write matching keys using kebab-case with the same prefix.
5. Generate getters and setters for all fields.
6. Inject this properties class into the business logic class via constructor injection.
7. Expose getters in the business class that delegate to the properties class.

### Setting up an application runner
1. Create a new class (e.g., `DemoRunner`) and annotate it with `@Component`.
2. Implement `ApplicationRunner` (or `CommandLineRunner`).
3. Override the `run()` method.
4. Inject any required beans (e.g., `PaymentGateway`) via constructor injection.
5. Call the desired method(s) inside `run()`.
6. Run the application — the logic executes automatically at startup, with no manual `ApplicationContext` handling required.

# Tips and Best Practices
- Avoid directly interacting with the IoC container (`ApplicationContext.getBean(...)`) in application code — let Spring Boot manage this automatically.
- Prefer `@ConfigurationProperties` over multiple `@Value` annotations when a class has many related configuration fields — it's cleaner and less error-prone.
- Follow the standard pattern of separating a dedicated properties/configuration class from the business logic class that consumes those values, injecting the properties class via the constructor.
- Always use kebab-case (canonical form) for keys in `application.properties`.
- Use `ApplicationRunner` or `CommandLineRunner` to execute startup logic automatically instead of manually fetching beans in the main method.

# Mistakes to Avoid
- Using curly braces `{}` instead of parentheses `()` when writing `@Value("${...}")` — this is a syntax error.
- Writing property keys in camelCase inside `application.properties` (e.g., `retryCount`) instead of kebab-case (`retry-count`) — this causes a "prefix must be in canonical form" error with `@ConfigurationProperties`.
- Referencing a property key in `@Value` or `@ConfigurationProperties` that doesn't exist in `application.properties` without a default value — this leads to a `BeanCreationException` due to unsatisfied dependency/autowiring failure.
- Fetching values directly from the properties class inside the business logic class instead of following the recommended constructor-injection separation pattern.
- Manually interacting with the `ApplicationContext` to fetch beans in application code, instead of relying on Spring Boot's automatic bean management and runner interfaces.

# Important Facts
- Spring Initializr project setup used in the demo: Java, Maven, version 4.1.0, Java 21, packaging as Jar.
- Default content of `application.properties`: `spring.application.name=demo`.
- Example property values used: `payment-gateway.type=Paytm`, `payment-gateway.retry-count=5`, `payment-property.enabled=true`, `payment-property.timeout=3000`.
- Final printed output after all configuration steps: `Paytm 5 true 3000`.
- Terminal command to run a Spring Boot app without the IDE run button: `mvn spring-boot:run`.
- Two configuration file formats offered by Spring Initializr: `.properties` and `.yml`.
- Other external configuration sources mentioned: environment variables, command-line arguments, system properties.

# FAQs

**Q1: What is the difference between `application.properties` and a `@Configuration` class?**
A: `application.properties` is a non-Java file holding key-value configuration pairs, automatically loaded by Spring Boot. A `@Configuration` class is a Java class where beans are defined using `@Bean` methods — it is not the same as a properties file.

**Q2: When should I use `@Value` versus `@ConfigurationProperties`?**
A: Use `@Value` for injecting a small number of individual property values. Use `@ConfigurationProperties` when a class has many related properties, since it maps them all at once using a shared prefix, avoiding repetitive annotations.

**Q3: Why do property keys need to be in kebab-case in `application.properties`?**
A: Spring Boot requires the "canonical form" (lowercase, hyphen-separated) for property keys; it then automatically converts them to camelCase to match Java field naming conventions.

**Q4: What happens if a property referenced by `@Value` or `@ConfigurationProperties` doesn't exist in the properties file?**
A: Spring throws a `BeanCreationException` due to unsatisfied dependency/autowiring failure, unless a default value is provided (for `@Value`, using `${key:default}` syntax).

**Q5: Why inject `PaymentProperties` into `PaymentGateway` instead of reading properties directly in `PaymentGateway`?**
A: This follows a standard, reusable design pattern where a dedicated properties class handles reading external configuration, and business logic classes consume those values via dependency injection — keeping responsibilities separated.

**Q6: What is the difference between `ApplicationRunner` and `CommandLineRunner`?**
A: Both auto-execute their `run()` method when the application starts. `ApplicationRunner`'s `run()` takes an `ApplicationArguments` object, while `CommandLineRunner`'s `run()` takes variable string arguments (`String... args`). Their behavior is otherwise identical.

**Q7: Do I need to manually fetch beans from the `ApplicationContext` when using `ApplicationRunner`?**
A: No — that is the whole point of using `ApplicationRunner`/`CommandLineRunner`. Once implemented, Spring Boot automatically calls the `run()` method at startup, and dependencies can be injected via the constructor.

**Q8: Can I run a Spring Boot application without using the IDE's run button?**
A: Yes, using the terminal command `mvn spring-boot:run` (requires Maven installed).

# Final Summary
- This lecture continues building Spring Boot's core foundation, focusing on `application.properties` and application startup runners.
- Directly interacting with the IoC container to fetch beans manually is discouraged in idiomatic Spring Boot code.
- `application.properties` is a non-Java configuration file used to externalize values so code doesn't need recompilation when settings change.
- The `@Value` annotation injects a single property value into a field, constructor, or setter, and supports default values via `${key:default}` syntax.
- `@ConfigurationProperties` maps multiple related properties at once into a class, using a shared prefix, avoiding repetitive `@Value` usage.
- Property keys in `application.properties` must follow canonical (kebab-case) form; Spring Boot auto-converts this to camelCase for Java fields.
- Best practice: separate a dedicated properties class (annotated with `@ConfigurationProperties`) from the business logic class, injecting the former into the latter via the constructor.
- `ApplicationRunner` and `CommandLineRunner` are interfaces that let a method run automatically when the Spring Boot application starts, removing the need for manual context/bean handling.
- The only functional difference between the two runner interfaces is their `run()` method's argument type: `ApplicationArguments` vs. `String... args`.
- Applications can also be run via the terminal using `mvn spring-boot:run` instead of an IDE's run button.
- The next video in the series will move on to building a Spring Boot web application with a CRUD implementation.