# Topic
Spring Core Fundamentals: Dependency Injection, IoC Container, and Beans (Annotation-Based Configuration)

# Overview
This lecture is part of a Spring Framework series and covers the core fundamentals that the entire framework is built on: Dependency Injection (DI), the Inversion of Control (IoC) Container, and Beans. The instructor starts by recreating manual dependency injection (using plain Java, no Spring) with an `OrderService` and `PaymentService` example, to recap what was covered in the previous lecture. He then introduces the `spring-context` Maven dependency and shows how Spring's IoC Container (called `ApplicationContext`) can take over object creation and wiring automatically. Along the way, he explains Java's Reflection API (since Spring relies heavily on it), the `@Component` annotation for marking classes to be managed by Spring, `@ComponentScan` for telling Spring where to look, and `@Autowired` for wiring dependencies via constructor, setter, or field injection — with constructor injection identified as the most recommended approach. The lecture also covers what happens when `@Component` can't be used (e.g., classes needing constructor arguments, or classes coming from external/third-party JARs) and introduces `@Bean` and `@Configuration` as the alternative way to manually create and register objects with Spring. It closes with handling multiple implementations of an interface using `@Primary` and `@Qualifier`, and a brief comparison of `ApplicationContext` vs the older `BeanFactory` interface.

# Detailed Notes

## Recap: Manual Dependency Injection (without Spring)
- Two classes are created: `OrderService` (with a `placeOrder()` method) and `PaymentService` (with a `pay()` method).
- Initially, `OrderService` creates its own `PaymentService` object internally (hardcoded) — this breaks the Single Responsibility Principle, since `OrderService`'s job should only be handling orders, not creating payment objects.
- Fix: `PaymentService` is passed into `OrderService` via a constructor. The object is created in `main()` and passed in — this is manual Dependency Injection.
- Goal for the rest of the video: let Spring's IoC Container handle this creation and wiring automatically instead of doing it manually in `main()`.

## IoC Container Concept
- The IoC (Inversion of Control) Container is a part of Spring Core that manages all objects.
- It creates objects, injects dependencies into them, and manages their entire lifecycle.
- In Spring, objects managed by the container are called **Beans**.
- Key distinction: every Bean is an object, but not every object is a Bean — only objects that Spring is told to manage become Beans.
- Two ways to configure Spring: **annotation-based** (modern, widely used) and **XML-based** (older/legacy, more complex, will be covered in a later lecture).

## Setting Up the Project
- A new empty Maven project is created (JDK 23).
- To use Spring Core (IoC Container) without Spring Boot or Spring MVC, only the **spring-context** dependency is needed (found via mvnrepository.com). Spring Boot is for auto-configuration; Spring MVC is for web apps — neither is needed for a simple console application.
- Best practice: avoid the absolute latest version (possible vulnerabilities); pick the second-latest stable version.
- Adding the dependency to `pom.xml` requires reloading/re-importing Maven changes to actually download the dependency (and its transitive dependencies).

## Java Reflection API (Background Concept)
- Java has a special class called `Class` that holds the metadata of any class (not to be confused with an object of that class).
- Example: `Class<Student> c1 = Student.class;` — `c1` is not a `Student` object; it stores metadata about the `Student` class: its name, fields, field types, constructors, methods, and annotations.
- Spring relies heavily on Reflection to inspect classes, read their annotations, and know how to create and manage objects — this is how it "understands" what to do with a class.

## Setting Up Spring's IoC Container (`ApplicationContext`)
- `ApplicationContext` is Spring's interface representing the IoC Container.
- Since it's an interface, an implementation is needed. For annotation-based configuration: `AnnotationConfigApplicationContext`.
- Example: `ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);`
- This line starts the Spring container using annotation-based configuration, with rules defined in a configuration class (`AppConfig`).

## Marking Classes as Components
- `@Component` annotation on a class tells Spring: "I want you to manage the objects (beans) of this class."
- Applied to both `OrderService` and `PaymentService`.

## The Configuration Class (`AppConfig`)
- A new class, e.g. `AppConfig`, is created to hold configuration rules (keeps `main()` simple, since `main()` is the program's entry point).
- `@Configuration` annotation marks this as a special configuration class.
- `@ComponentScan(basePackages = "in.codearmy")` tells Spring which package (and all its sub-packages) to scan for classes annotated with `@Component`.
- If no package name is given, Spring defaults to scanning the package where the configuration class itself resides (and its sub-packages) — not packages outside of it.
- If a class isn't annotated with `@Component`, Spring will not create/manage its bean, and trying to fetch it will throw: **"No qualifying bean available."**

## Fetching Beans from the Container
- Instead of `new OrderService()`, use: `OrderService order = context.getBean(OrderService.class);`
- `context.getBean(...)` fetches the object (bean) that the IoC Container already created and manages.
- Calling `order.placeOrder()` works correctly because the container already created and wired the beans behind the scenes.

## Wiring Dependencies with `@Autowired`
- `@Autowired` tells Spring to automatically inject a required dependency.
- Can be applied at three points:
  1. **Constructor Injection** (most recommended) — dependency is injected the moment the object is created.
  2. **Setter Injection** — dependency is injected via a setter method after the object is created.
  3. **Field Injection** — dependency is injected directly into the field; Spring shows a warning that "field injection is not recommended."
- If a class has only **one constructor**, `@Autowired` is optional — Spring will automatically use it. It is mandatory for setter and field injection.

## Why Constructor Injection Is Recommended
1. Dependencies are wired immediately when the object is created (no partially-initialized state).
2. The dependency field can be declared `final` (Java allows final fields to be set inside a constructor) — this prevents anyone from changing the dependency later.
3. **Easier unit testing** — since the dependency is passed via constructor, a class can be tested independently of Spring by passing in a fake/mock dependency manually. With field injection (private field, no setter/constructor), there is no way to assign a value from outside without Spring, and calling a method that uses it results in a `NullPointerException`.

## Internal Steps When Spring Manages Beans
1. Code executes `new AnnotationConfigApplicationContext(AppConfig.class)` → Spring is told to start the container using annotation-based configuration and to read rules from `AppConfig`.
2. Spring starts the ApplicationContext (container).
3. Spring reads `AppConfig`.
4. Spring processes the `@ComponentScan` instruction.
5. Spring finds all classes annotated with `@Component`.
6. **Spring creates Bean Definitions** — before creating actual objects, Spring stores metadata/definitions about each bean (bean name, class, scope, dependencies, etc.) via the `BeanDefinition` interface. This happens because Spring needs full metadata knowledge to correctly manage wiring, not just create objects.
7. Spring starts creating objects — dependency-free classes first (e.g., `PaymentService`), then dependent classes (e.g., `OrderService`, passing in the already-created `PaymentService` bean via its constructor).
8. The application then uses these beans (e.g., `context.getBean(OrderService.class)`).

## Loose Coupling with Interfaces
- To avoid tight coupling between `OrderService` and a specific `PaymentService` implementation, `PaymentService` is converted into an **interface** with a single `pay()` method.
- Multiple implementations are created: `CardPayment` and `UpiPayment`, each implementing `PaymentService` and overriding `pay()`.
- Only implementation classes annotated with `@Component` are picked up by component scanning — the interface itself cannot be instantiated, so annotating the interface has no effect.
- If only one implementation (e.g., `CardPayment`) has `@Component`, Spring injects that one automatically since it "is a" `PaymentService`.

## Handling Multiple Beans of the Same Type: `@Primary` and `@Qualifier`
- If **both** `CardPayment` and `UpiPayment` are annotated with `@Component`, Spring gets confused about which one to inject into `OrderService`, resulting in: **"No qualifying bean of type PaymentService... expected single matching bean but found 2."**
- **`@Primary`**: Placed on one implementation to give it priority when there's ambiguity.
- **`@Qualifier("beanName")`**: Placed on both implementations, and the desired bean name is specified where injection happens (e.g., in the constructor parameter).
  - By default, a bean's name equals the class name in camelCase (e.g., `CardPayment` → `cardPayment`).
  - The bean's default name can be overridden by passing a custom name to `@Qualifier` on the implementation class itself, and then referencing that same custom name at the injection point.
  - `@Qualifier` (like `@Autowired`) can be applied at the constructor, setter, or field, matching whichever type of injection is used.

## When `@Component` Fails
There are cases where `@Component` cannot be used:
1. **Classes needing constructor arguments Spring can't infer** — e.g., a `User` class with `name` and `age` fields and a constructor requiring both. Spring doesn't know what values to supply, so it throws an error if `@Component` is added directly.
2. **Classes from external/third-party libraries** — these come as compiled `.class` files inside JAR files, which are read-only. You cannot add annotations to code you don't own or can't edit (demonstrated using a separately built and installed `CartService` class packaged as a JAR dependency via Maven install).

## `@Bean` and `@Configuration` for Manual Object Creation
- For cases where `@Component` can't be used, Spring provides the **`@Bean`** annotation, applied on a **method** (not a class) inside a `@Configuration` class.
- The method creates and returns the object manually (e.g., `return new User("Aditya", 28);` or `return new CartService();`), and Spring calls this method, stores the returned object in the IoC Container, and manages it from then on.
- This solves both problems: for `User`, the developer supplies the needed constructor values; for `CartService` (external library class), the developer creates the object manually since the class itself can't be annotated.
- `@Bean` methods can also be used for classes that could have used `@Component` (e.g., `OrderService`, `CardPayment`) — just to demonstrate the alternative approach.

## Dependency Injection Inside `@Bean` Methods
- If a `@Bean` method (e.g., for `OrderService`) needs a dependency (e.g., `PaymentService`), that dependency can simply be added as a **parameter** to the `@Bean` method — no `@Autowired` needed, since it functions like constructor injection.
- Spring automatically matches the parameter type to an already-created bean of that type and passes it in.
- If a class doesn't use constructor injection (e.g., only has a setter), and `@Bean` is used, the developer must manually create the object, then manually call the setter to wire the dependency — unless `@Autowired` is added on the setter, in which case Spring handles the setter call automatically even without `@Component`.

## `@Primary` and `@Qualifier` in `@Bean` Configurations
- The same ambiguity problem occurs if multiple `@Bean` methods return the same type (e.g., both `createCardPayment()` and `createUpiPayment()` return `PaymentService`).
- Solved the same way: `@Primary` on one `@Bean` method, or `@Qualifier("methodName")` at the injection point — where the bean's name corresponds to the `@Bean` method's name (not the class name, since these beans are created via methods).
- The `@Qualifier` name can also be customized directly on the `@Bean` method.

## `@Component` + `@Bean` Together
- If a class has **both** `@Component` and a corresponding `@Bean` method, only **one** bean is created — the one created via `@Bean` takes priority. The `@Component` annotation is effectively ignored in this case, because manually-created beans (via `@Bean`) always take precedence over those Spring creates on its own.

## Why Not Put Configuration in `main()`?
- Technically possible (and was done in the previous lecture), but it makes `main()` (the program's entry point) overly complex.
- Keeping configuration in a separate `@Configuration` class keeps `main()` clean and simple.
- The class name (e.g., `AppConfig`) is arbitrary — what matters is the `@Configuration` annotation (so Spring recognizes it) and `@ComponentScan` (to enable scanning). `@Bean`-annotated methods work regardless of whether `@ComponentScan` is present, since they're called directly by Spring.

## `ApplicationContext` vs `BeanFactory`
- `ApplicationContext` is the modern interface used to interact with the IoC Container.
- Historically, `BeanFactory` (also an interface, not a class) was used for this purpose.
- `ApplicationContext` is a **super-interface** that internally extends `BeanFactory` — so everything in `BeanFactory` is available in `ApplicationContext`, and many `BeanFactory` methods are now deprecated/removed.
- `ApplicationContext` extends other interfaces too, including `HierarchicalBeanFactory`, which itself extends `BeanFactory`.

# Important Concepts
- **IoC Container**: The core Spring component that creates, wires, and manages the lifecycle of objects (beans).
- **Bean**: An object that is created and managed by the Spring IoC Container (as opposed to an object created and managed manually by the developer).
- **`ApplicationContext`**: The interface representing Spring's IoC Container; `AnnotationConfigApplicationContext` is its annotation-based implementation.
- **`@Component`**: Marks a class so Spring will scan for it and manage its bean automatically.
- **`@ComponentScan`**: Tells Spring which package (and sub-packages) to scan for `@Component`-annotated classes.
- **`@Configuration`**: Marks a class as holding Spring configuration rules.
- **`@Autowired`**: Tells Spring to automatically inject a dependency, via constructor, setter, or field.
- **`@Bean`**: Applied to a method inside a `@Configuration` class; the method manually creates and returns an object, which Spring then stores and manages as a bean.
- **`@Primary`**: Gives one bean priority when multiple beans of the same type exist.
- **`@Qualifier`**: Explicitly specifies which bean (by name) should be injected when multiple candidates exist.
- **Bean Definition**: Metadata Spring stores about a bean (name, class, scope, dependencies) before actually creating the object; managed via the `BeanDefinition` interface.
- **Reflection API**: A Java API that lets code inspect metadata (fields, methods, constructors, annotations) of a class at runtime; Spring depends heavily on this to manage beans.
- **Transitive Dependencies**: Additional dependencies automatically pulled in because the main dependency you added depends on them.
- **Tight Coupling**: When a class directly depends on a specific concrete implementation rather than an abstraction (interface) — avoided by using interfaces like `PaymentService`.

# Step-by-Step Process

**Setting up Spring IoC Container with Annotation-Based Configuration:**
1. Create a Maven project and add the `spring-context` dependency (search on mvnrepository.com; prefer a stable, slightly older version over the absolute latest).
2. Reload/import Maven changes so the dependency and its transitive dependencies download.
3. Annotate classes that Spring should manage with `@Component`.
4. Create a configuration class (e.g., `AppConfig`) and annotate it with `@Configuration`.
5. Add `@ComponentScan(basePackages = "your.package.name")` to the configuration class (package name is optional if the configuration class is in the same package as the classes to scan).
6. In `main()`, start the container: `ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);`
7. Fetch beans as needed: `SomeClass obj = context.getBean(SomeClass.class);`
8. Use `@Autowired` on constructors, setters, or fields to have Spring inject dependencies automatically (optional for constructors if there is only one).
9. If multiple beans of the same type exist, resolve ambiguity using `@Primary` or `@Qualifier`.
10. For classes that can't be annotated directly (constructor needs specific values, or class comes from an external library), define a `@Bean` method inside the `@Configuration` class that manually creates and returns the object.

# Tips and Best Practices
- When adding a Maven dependency, avoid picking the absolute latest version — prefer the second-latest, well-adopted version to reduce the risk of undiscovered vulnerabilities (though for practice projects, the latest version is fine too).
- Prefer **constructor injection** over setter or field injection wherever possible — it enables immediate wiring, allows `final` fields, and makes unit testing easier.
- Use interfaces (e.g., `PaymentService`) for dependencies that may have multiple implementations, to keep classes loosely coupled.
- Keep configuration logic in a separate `@Configuration` class rather than in `main()`, to keep the entry point simple and readable.
- Use `@Bean` for objects that Spring cannot construct on its own (e.g., needing specific constructor arguments, or coming from external/third-party JARs).

# Mistakes to Avoid
- Forgetting to reload/import Maven after adding a dependency in `pom.xml` — the dependency won't actually be available until you do.
- Forgetting to annotate an implementation class with `@Component` when using an interface-based dependency — this results in "No qualifying bean available" errors.
- Trying to place `@Component` directly on a class whose constructor needs specific values Spring can't determine (e.g., a `User` class with `name`/`age`) — this will cause an error since Spring won't know what values to use.
- Trying to place `@Component` on classes from external, read-only libraries (compiled `.class` files) — not possible; use `@Bean` instead.
- Leaving multiple beans of the same type without `@Primary` or `@Qualifier` — this causes a "found 2 beans" error when Spring tries to autowire.
- Using field injection without `@Autowired` — field injection requires it explicitly (unlike single-constructor injection, where it's optional).

# Important Facts
- Only the `spring-context` dependency is needed for basic Spring Core (IoC Container) functionality — not Spring Boot or Spring MVC.
- If a class has only one constructor, `@Autowired` on it is optional.
- `@Autowired` is mandatory for setter and field injection.
- Default bean names: for `@Component`-based beans, the bean name is the class name in camelCase; for `@Bean`-based beans, the bean name is the method name.
- When both `@Component` and a `@Bean` method exist for the same type, the `@Bean`-created instance takes priority.
- `ApplicationContext` is a super-interface that extends `BeanFactory` (the older interface used for the IoC Container in earlier Spring versions).
- `@ComponentScan` without an explicit package name defaults to scanning the package of the configuration class itself (and its sub-packages).

# FAQs

**Q: Do I need Spring Boot to use Spring's IoC Container?**
A: No. Spring Boot is only for easier auto-configuration. For basic Spring Core functionality (the IoC Container), only the `spring-context` dependency is required.

**Q: What's the difference between an object and a bean in Spring?**
A: Every bean is an object, but not every object is a bean. A bean is specifically an object that Spring's IoC Container creates and manages; objects you create yourself manually are not beans.

**Q: Why does Spring create Bean Definitions before creating actual objects?**
A: Because Spring needs to manage objects (not just create them) — including wiring dependencies between them. Storing metadata (bean definitions) first gives Spring complete knowledge of each class before it starts creating and linking actual objects.

**Q: Which type of dependency injection is recommended, and why?**
A: Constructor injection is most recommended. It wires dependencies immediately at object creation, allows use of `final` fields, and makes unit testing easier since dependencies can be passed in directly without needing Spring.

**Q: What happens if I annotate a class with `@Component` but it needs a constructor argument Spring can't figure out?**
A: Spring will throw an error because it doesn't know what values to supply for the constructor arguments (e.g., a `name` and `age` for a `User` class).

**Q: Can I use `@Component` on a class from an external JAR/library?**
A: No — since it's compiled, read-only bytecode, you cannot add annotations to it. Use a `@Bean` method inside a `@Configuration` class instead to manually create and register the object.

**Q: What happens if two beans of the same type exist and I try to inject that type without specifying which one?**
A: Spring throws an error: "No qualifying bean... expected single matching bean but found 2." You must resolve this using `@Primary` or `@Qualifier`.

**Q: What is the default name of a bean created via `@Bean` versus `@Component`?**
A: For `@Component`, the default bean name is the class name in camelCase. For `@Bean`, the default bean name is the method name.

**Q: If a class has both `@Component` and a `@Bean` method defined for it, which one does Spring use?**
A: The bean created via `@Bean` takes priority; Spring ignores the `@Component` in this case.

# Final Summary
- Spring Core provides an IoC (Inversion of Control) Container that creates, wires, and manages objects called beans, instead of the developer doing this manually.
- Only the `spring-context` Maven dependency is needed for basic IoC Container functionality (no Spring Boot or Spring MVC required).
- `ApplicationContext` is the interface representing the IoC Container; `AnnotationConfigApplicationContext` is used for annotation-based setup.
- `@Component` marks a class for Spring to manage automatically; `@ComponentScan` tells Spring which package to scan for such classes.
- `@Configuration` marks a class holding Spring's configuration rules (kept separate from `main()` to keep the entry point simple).
- Dependencies are wired using `@Autowired`, via constructor (recommended), setter, or field injection; constructor injection is preferred for immediate wiring, `final` field support, and easier unit testing.
- Before creating actual bean objects, Spring first creates "Bean Definitions" — metadata about each bean needed for proper management and wiring.
- Interfaces (e.g., `PaymentService` with `CardPayment` and `UpiPayment` implementations) help achieve loose coupling.
- When multiple beans of the same type exist, `@Primary` or `@Qualifier` resolves the ambiguity for Spring.
- `@Component` can't be used for classes needing specific constructor values Spring can't infer, or for classes from external/third-party libraries; `@Bean` (inside a `@Configuration` class) is the solution — the developer manually creates the object, and Spring manages it from there.
- If both `@Component` and a `@Bean` method exist for the same class, the `@Bean`-created object takes priority.
- `ApplicationContext` is a modern super-interface that extends the older `BeanFactory` interface, which was used to interact with the IoC Container in earlier Spring versions.