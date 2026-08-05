# Topic
Spring Framework Bean Lifecycle — the complete journey of a bean from creation to destruction, explained in depth with a hands-on Maven project.

# Overview
This video is part of a Spring Framework series and focuses entirely on the lifecycle of a Spring Bean — a topic considered very important, especially from an interview perspective. The instructor sets up a fresh Maven project with the `spring-context` dependency, creates a configuration class (`AppConfig`) annotated with `@Configuration` and `@ComponentScan`, and builds two simple beans, `OrderService` and `PaymentService`, where `OrderService` depends on `PaymentService` via constructor injection. Using this project as a working example, the video walks step by step through every phase a singleton bean goes through inside the IoC container: container startup, reading configuration, creating bean definitions, instantiating objects, injecting dependencies, invoking Aware interfaces, running initialization callbacks, using the bean, running destruction callbacks, and finally destroying the bean. Along the way it covers the three ways to hook into initialization (`InitializingBean`, init-method, `@PostConstruct`) and destruction (`DisposableBean`, destroy-method, `@PreDestroy`), explains why constructors shouldn't be used for heavy initialization work, and closes with how lazy and prototype-scoped beans deviate from the standard lifecycle, plus a trick for resolving circular dependencies using `@PostConstruct`.

# Detailed Notes

## Setting Up the Project
- A new Maven project called "Bean Lifecycle Demo" is created, and the `spring-context` dependency (version 7.0.7) is added via the Maven repository.
- An `AppConfig` class is created in the same package as `Main`, annotated with `@Configuration` and `@ComponentScan`. Without an explicit package argument, component scanning defaults to the package containing `AppConfig`.
- The IoC container is created using `new AnnotationConfigApplicationContext(AppConfig.class)`. Note that the **metadata** of `AppConfig` (via `.class`) is passed, not an object — Spring uses **Reflection** to read this metadata (constructors, members, methods, annotations) internally.
- Two beans, `OrderService` and `PaymentService`, are created and annotated `@Component`. `OrderService` takes a `PaymentService` object through constructor injection — since there's only one constructor, `@Autowired` is not required.
- `OrderService.placeOrder()` prints "Order Placed" and calls `PaymentService.pay()`, which prints "Payment Done". Retrieving `OrderService` via `context.getBean(OrderService.class)` and calling `placeOrder()` confirms the wiring works end-to-end.

## Bean Lifecycle — Step by Step

### Step 1: IoC Container Starts
- Everything begins with the IoC container starting up. Without this, no bean management is possible.

### Step 2: Read Configuration
- The container reads the configuration class (`AppConfig`) to learn which packages/components to scan.
- Interestingly, `@Configuration` itself internally uses `@Component`, so `AppConfig` is also managed as a bean by the IoC container. Any `@Bean`-annotated methods inside `AppConfig` are also tracked and can be called later.
- Component scanning (via Reflection) discovers all classes annotated `@Component` in the target package — in the example, `OrderService` and `PaymentService`.

### Step 3: Read Bean Definitions
- Before creating actual objects, the container first builds a **Bean Definition** for every discovered bean — metadata such as bean name (default: class name with lowercase first letter, though it can be customized via `@Component("customName")`), class type, scope (default: singleton), laziness (default: false), and dependencies.
- All bean definitions are created **first**, for every bean, before any object instantiation begins. This is because the container needs to know scope/laziness ahead of time — only singleton, non-lazy beans are eagerly instantiated; prototype or lazy beans are deferred until requested.

### Step 4: Instantiate Objects
- The container is **dependency-aware**, not random. If it starts creating a bean that depends on another (e.g., `OrderService` needs `PaymentService`), it will create the dependency first, then the dependent bean.
- If a bean has no dependencies (e.g., `PaymentService` alone), it's created directly.

### Step 5: Dependency Injection
- With **constructor injection**, object creation and dependency injection happen together — the dependency must be resolved before the constructor call completes.
- With **setter or field injection**, the object is created first, and dependencies are injected afterward as a separate step.

## Aware Interfaces
- Aware interfaces are special, rarely-used interfaces that give a bean information about itself or its container. They are functional interfaces (one method each).
- **`BeanNameAware`**: implementing `setBeanName(String name)` lets Spring call back and tell the bean its own bean name. This method is a **callback** — Spring calls it, not the developer. Calling it manually does not change the actual bean name registered in the IoC container; it only affects local variables/printing.
- **`ApplicationContextAware`**: implementing `setApplicationContext(ApplicationContext context)` lets the bean learn which `ApplicationContext` (container) it belongs to.
- **Use case**: Mostly used for niche needs, especially in logging — e.g., tagging every log line with the bean's name or the container it came from, useful when an application uses multiple containers (e.g., mixed XML-based and annotation-based configuration).
- Aware interface callbacks occur **after** dependency injection and **before** initialization callbacks.

## Initialization Callbacks
- These run after dependencies are injected and Aware interfaces are called, but before the bean is used. Purpose: perform setup tasks before business logic runs — e.g., flushing/populating a map, invalidating a cache, or pre-loading an expensive resource.
- **Three ways to receive initialization callbacks:**
  1. **`InitializingBean` interface** — implement it and override `afterPropertiesSet()`. Considered old-fashioned now, mostly replaced by `@PostConstruct`.
  2. **Init-method** — specify a custom method name via `@Bean(initMethod = "start")` when the bean is created through an `@Bean`-annotated factory method in a configuration class (since there's no natural place to implement an interface there).
  3. **`@PostConstruct` annotation** — the most commonly used approach today. Simply annotate any method with `@PostConstruct` and Spring calls it automatically as a callback — no interface implementation needed. Requires the Jakarta Annotations API dependency when using plain `spring-context` (it's pre-bundled in Spring Boot).
- **Why not just use the constructor for this logic?**
  1. At constructor time, dependencies injected via field/setter injection are **not yet available** — only constructor-injected ones are. Post-construct logic runs after all dependencies (regardless of injection style) are resolved.
  2. Keeping constructors lightweight is good practice — expensive/heavy setup (like loading large files) should be deferred to a dedicated initialization method so object creation stays fast and clean.

## Using the Beans
- Once initialization callbacks complete, the bean is fully ready. This is the phase where the application actually calls the bean's business methods (e.g., `getValue()`, `addToCart()`).

## Destruction Callbacks
- Mirror the initialization callbacks — same three mechanisms:
  1. **`DisposableBean` interface** — override `destroy()`.
  2. **Destroy-method** — specify a custom method name via `@Bean(destroyMethod = "stop")`.
  3. **`@PreDestroy` annotation** — the modern, most-used approach, requiring no interface or explicit method wiring.
- Purpose: reverse whatever was done at initialization — clear a map, invalidate a cache, close an expensive resource, etc.
- To simulate destruction in a simple `main()` program (since the JVM would otherwise just exit), the container must be explicitly closed using `ConfigurableApplicationContext` and calling `context.close()`. This triggers the destruction callback before the container actually shuts down.

## Bean Is Destroyed
- After the destruction callback runs, the bean is removed/destroyed and the lifecycle is complete.

## Lazy Singleton Beans
- Marking a singleton bean `@Lazy` changes when instantiation happens: the container still starts, configuration is read, and the bean definition is created — but the object itself is **not instantiated** until the bean is actually requested (via `getBean()` or as a dependency elsewhere). From that point on, the rest of the lifecycle (dependency injection, Aware interfaces, initialization callbacks, usage, destruction) proceeds normally.

## Prototype-Scoped Beans
- Prototype beans are **lazy by default** and cannot be made eager.
- Every time a prototype bean is requested, a **new object** is created.
- The Spring container manages the bean **only up to the point it hands it over** to the caller (after initialization callbacks). Once handed over, Spring stops managing it entirely.
- Consequently, **destruction callbacks (like `@PreDestroy`) never fire for prototype beans**, and calling `context.close()` does **not** destroy prototype bean instances — it only shuts down the container.
- Prototype bean cleanup happens purely via **Java's garbage collection** once no references remain.
- **Why doesn't Spring manage prototype bean destruction?** To avoid memory leaks — if Spring held references to every prototype instance ever created, garbage collection could never reclaim them, eventually filling up heap memory.
- **Practical implication**: If a prototype bean holds an expensive resource, the developer must manually clean it up (e.g., using try-with-resources or a finally block), since Spring won't call any destroy method for it.

## Solving Circular Dependency Using @PostConstruct
- Circular dependency (e.g., class `A` needs `B` in its constructor, and `B` needs `A` in its constructor) causes constructor injection to fail with a "currently in creation" exception.
- **Fix demonstrated**: Break the cycle by removing the constructor-based dependency in one class (e.g., `B`) and replacing it with a setter (`setA(A a)`), while `A` still takes `B` via constructor. Then, inside `A`, use `@PostConstruct` to call `b.setA(this)` after both objects are constructed — manually wiring the circular reference once both beans exist.
- This is explicitly called a "hacky" workaround — the recommended fix is to **refactor the code to avoid circular dependencies altogether**, rather than relying on tricks like `@Lazy` or `@PostConstruct` to patch around them.

# Important Concepts
- **IoC Container**: The core Spring component (accessed via `ApplicationContext`) responsible for creating, managing, and destroying beans, and for injecting their dependencies (autowiring).
- **Reflection API**: A Java mechanism that lets code inspect a class's complete metadata (constructors, fields, methods, annotations) at runtime; Spring uses this to read configuration classes and scan components.
- **Bean Definition**: Metadata about a bean (name, class, scope, laziness, dependencies) created by the container before the actual object is instantiated.
- **Singleton scope**: Default scope; one instance per class definition; eagerly initialized by default.
- **Prototype scope**: A new instance is created every time the bean is requested; always lazy; not fully managed by Spring after handoff.
- **Lazy initialization (`@Lazy`)**: Defers object instantiation until the bean is first requested, rather than creating it immediately at container startup.
- **Aware Interfaces**: Special functional interfaces (e.g., `BeanNameAware`, `ApplicationContextAware`) that let a bean receive information about itself from the container via callback methods.
- **Callback methods**: Methods that the framework calls automatically (not the developer) at a specific lifecycle stage.
- **`@PostConstruct` / `@PreDestroy`**: Annotation-based, modern mechanisms for hooking into the initialization and destruction phases of a bean's lifecycle without implementing any interface.

# Important Facts
- Spring Context dependency version used in the demo: 7.0.7.
- Jakarta Annotation API dependency (version 3.x shown, e.g., "2024" release) is required to use `@PostConstruct`/`@PreDestroy` with plain `spring-context`; it comes pre-bundled with Spring Boot.
- Default bean name = class name with the first letter lowercased (can be overridden via the `@Component` annotation's value).
- Default bean scope = singleton; default initialization = eager, unless marked `@Lazy` or scope is `prototype` (which is always lazy).
- `ApplicationContext` interface does not have a `close()` method; `ConfigurableApplicationContext` (a child interface) does.

# FAQs

**Q1: What is the very first step in a Spring bean's lifecycle?**
A: The IoC container starting up — nothing else can happen until this occurs.

**Q2: Why does the container create all bean definitions before instantiating any objects?**
A: It needs to know each bean's scope and laziness upfront so it only eagerly instantiates singleton, non-lazy beans and skips lazy/prototype beans until they're actually needed.

**Q3: What's the difference in timing between constructor injection and setter/field injection?**
A: With constructor injection, object creation and dependency injection happen simultaneously. With setter/field injection, the object is created first, and dependencies are injected as a separate, later step.

**Q4: Why use `@PostConstruct` instead of putting setup logic in the constructor?**
A: At constructor time, setter/field-injected dependencies aren't available yet, and heavy setup logic in a constructor slows down and complicates object creation. `@PostConstruct` runs after all dependencies are resolved and keeps constructors lightweight.

**Q5: Do prototype beans get destroyed by Spring when `context.close()` is called?**
A: No. Spring stops managing a prototype bean once it hands the object over to the caller, so `@PreDestroy`/destroy callbacks never run for prototype beans — cleanup relies purely on Java's garbage collection.

**Q6: Why doesn't the Spring container manage the full lifecycle of prototype beans?**
A: To avoid memory leaks — if Spring retained references to every prototype instance ever created, garbage collection could never reclaim them, eventually filling up heap memory.

**Q7: What happens to a bean marked `@Lazy` at container startup?**
A: The container still starts, reads configuration, and creates its bean definition, but the actual object is not instantiated until the bean is explicitly requested (e.g., via `getBean()` or as another bean's dependency).

**Q8: Can calling a Bean's Aware-interface callback method manually (e.g., `setBeanName()`) change its actual name in the container?**
A: No. Calling it yourself only affects local behavior/printing; the real bean name registered inside the IoC container remains unchanged.

**Q9: How can `@PostConstruct` help resolve a circular dependency?**
A: By removing the constructor-based mutual dependency (using a setter instead in one class) and then, inside a `@PostConstruct` method, manually wiring the reference once both objects already exist — though this is considered a workaround, not a proper fix.

# Final Summary
- The Spring Bean lifecycle begins when the IoC container starts up.
- The container next reads the configuration class to learn which packages/components to scan.
- All bean definitions (metadata: name, class, scope, laziness, dependencies) are created before any actual object instantiation.
- Objects are instantiated in a dependency-aware order — dependencies are created before the beans that need them.
- With constructor injection, object creation and dependency injection happen together; with setter/field injection, they happen as separate steps.
- Aware interfaces (`BeanNameAware`, `ApplicationContextAware`) let a bean receive callback information about its name or container, mainly useful for logging in niche cases.
- Initialization callbacks (`InitializingBean`, init-method, or `@PostConstruct`) run once dependencies and Aware interfaces are resolved, before the bean is used — ideal for setup tasks like clearing caches or loading resources.
- `@PostConstruct` is the most commonly used initialization mechanism today because it's simple, annotation-based, and requires no interface implementation.
- Once ready, the bean is handed over for actual use (calling its business methods).
- Destruction callbacks (`DisposableBean`, destroy-method, or `@PreDestroy`) run before a singleton bean is destroyed, typically triggered by closing the application context.
- Lazy singleton beans delay instantiation until first use but otherwise follow the same lifecycle.
- Prototype beans are always lazy, get a new instance on every request, and are only partially managed by Spring — Spring stops handling them after handoff, so destruction callbacks never fire and garbage collection handles cleanup, meaning any expensive resources must be manually released by the developer.
- `@PostConstruct` can be used as a workaround to resolve circular dependencies by manually wiring references after both objects are constructed, though refactoring to eliminate circular dependencies is the recommended solution.