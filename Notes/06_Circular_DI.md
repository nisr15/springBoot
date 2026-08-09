# Topic
Spring Framework: Circular Dependency, Bean Scopes (Singleton vs Prototype), and Bean Initialization (Eager vs Lazy)

# Overview
This lecture is part of a Spring Framework series and covers three interlinked topics. First, it explains **circular dependency** — a situation where two classes depend on each other (e.g., an `OrderService` needing a `PaymentService`, and `PaymentService` needing `OrderService` back), which prevents Spring's IoC container from determining a starting point for object creation, resulting in a `BeanCurrentlyInCreationException`. The video demonstrates this problem using constructor injection, shows that it's fundamentally a Java/design problem (not a Spring-specific one, since it can even cause a `StackOverflowError` in plain Java), and shows how field/setter injection can technically resolve it via Java Reflection and a "partial object reference" mechanism — but argues this should be avoided by refactoring code to follow the Single Responsibility Principle rather than solved by hacks. Second, it covers **bean scopes**: singleton (one object per bean definition, eagerly created by default) and prototype (a new object every time it's requested, always lazily created). It clarifies a common misconception — singleton means one object *per bean definition*, not one object per class, since multiple `@Bean` methods can create multiple bean definitions for the same class. Third, it covers **eager vs. lazy initialization**, how `@Lazy` can override the default eager behavior of singleton beans (but not prototype, which is always lazy), why Spring defaults to eager initialization (the "fail-fast" principle), and finally how `@Lazy` can be used as a workaround to resolve circular dependency by injecting a proxy object instead of a real one at startup.

# Detailed Notes

## Section 1: What Is Circular Dependency?
- In Spring, Dependency Injection (DI) means a class needs an object of another class to be constructed — that object is called a "dependency."
- Example: `OrderService` depends on `PaymentService` (needs a `PaymentService` object to be constructed). `PaymentService` in turn depends on `PaymentGateway`.
- Normally, Spring's IoC container is smart enough to resolve a dependency chain: it creates `PaymentGateway` first, then `PaymentService` (injecting the gateway), then `OrderService` (injecting the service).
- **Circular dependency** occurs when Class A depends on Class B, and Class B also depends on Class A. Neither can be created first because each requires the other to exist first.
- Key point: **Circular dependency is not a Spring-specific problem** — it can happen in plain Java too, and it arises from poor code design, not from using the DI framework.

## Section 2: Demonstrating Circular Dependency in Spring (Constructor Injection)
- Setup: A Maven project with the `spring-context` dependency (version 7.0.7, compatible with Spring Boot 4), an `AppConfig` class annotated with `@Configuration` and `@ComponentScan`.
- Important side note: `@Configuration` itself is internally annotated with `@Component`, meaning the configuration class is itself a Spring-managed bean/component.
- Two classes, `OrderService` and `PaymentService`, are annotated with `@Component`. Each expects the other in its constructor (constructor injection), creating a circular dependency.
- Running this produces: `BeanCurrentlyInCreationException` — Spring explicitly reports it may be due to an "unresolvable circular reference."
- The same effect is demonstrated in **plain Java** (no Spring) using two classes `A` and `B` that require each other in their constructors — attempting to manually instantiate them leads to infinite constructor calls and a `StackOverflowError`. This proves the issue is a fundamental object-creation problem, not something introduced by Spring.

## Section 3: Why Constructor Injection Fails but Field/Setter Injection "Works"
- With **constructor injection**, an object cannot be considered "created" until its constructor finishes running — and the constructor requires the other (not-yet-created) object. This creates a deadlock with no valid starting point.
- With **field injection** (`@Autowired` on a field) or **setter injection**, object creation and dependency injection are two separate steps:
  1. Spring creates an *empty* object of `OrderService` (constructor runs without needing dependencies).
  2. Spring creates an *empty* object of `PaymentService`.
  3. Spring then injects `PaymentService`'s reference into `OrderService`, and `OrderService`'s reference into `PaymentService`.
- This works because IoC container uses **Java Reflection** to inject values directly into private fields — it doesn't need a constructor or setter to do this, since it has full class metadata via the bean definition.
- The video shows this working code-wise: fields injected via `@Autowired`, circular dependency resolved, program prints `Payment Done`, `Order Details`, `Order Placed` as expected.

## Section 4: Why You Shouldn't "Solve" Circular Dependency (Even Though You Can)
- Even though field/setter injection resolves the circular reference technically, it is **not good coding practice**.
- From Spring Boot 2.6 onward, circular references are **disallowed by default**. The property `spring.main.allow-circular-references` defaults to `false` in Spring Boot (though plain Spring Core still allows it). Setting it to `true` re-enables the old behavior.
- Other ways exist to resolve circular dependency (e.g., via bean scopes, lifecycle callbacks like `@PostConstruct`) — but the lecturer emphasizes these are all *workarounds*, not real solutions.
- **Root cause**: Circular dependency indicates the classes' responsibilities are too tightly coupled — it's a violation of the **Single Responsibility Principle** (a core SOLID design principle).
- **Correct fix — refactor the code**: In the `OrderService`/`PaymentService` example, the real problem was that `PaymentService.pay()` was calling `OrderService.getOrderDetails()` — i.e., `PaymentService` was doing work that isn't its responsibility. Moving that call into `OrderService` (call `getOrderDetails()` from within `OrderService.placeOrder()` after payment) makes the dependency one-directional (`OrderService → PaymentService` only), eliminating the circular dependency entirely. Output remains the same, but the design is now clean and decoupled.

## Section 5: Bean Scopes — Singleton
- **Singleton** is the **default scope** for every Spring bean.
- Singleton means: **one object is created per bean definition.** If you call `getBean()` multiple times, or if multiple classes depend on the same bean via injection, they all receive the **same object reference** (proved using `==` comparison, which returns `true`).
- **Important distinction from the classic "Singleton Design Pattern"**: Spring's singleton scope is *not* as strict as the GoF Singleton pattern (which guarantees only one object can ever exist for a class). In Spring:
  - You can still manually create additional objects using `new` — Spring simply won't manage them.
  - If you define the same class via **multiple `@Bean` methods** in a config class (e.g., `getOrder()` and `getOrderTwo()` both returning `new OrderService()`), Spring creates **two separate bean definitions**, and therefore **two separate singleton objects** — each individually a singleton per its own definition, but the class as a whole has more than one bean instance in the container.
  - Trying to inject such an ambiguous dependency elsewhere leads to a `NoUniqueBeanDefinitionException` (resolved via `@Primary` or `@Qualifier`).

## Section 6: Bean Scopes — Prototype
- **Prototype** scope means: every time the bean is requested (via `getBean()` or dependency injection), a **brand-new object** is created.
- Must be explicitly declared using `@Scope("prototype")`.
- Demonstrated with two dependent classes `A` and `B`, both depending on `OrderService`: with prototype scope, four separate `OrderService` objects get created (two via direct `getBean()` calls, two via injection into `A` and `B`), and `==` comparisons return `false`.

## Section 7: When to Use Singleton vs Prototype
- **Stateless classes** (no unique per-instance data, e.g., service classes like `OrderService`, manager classes like `PaymentManager`) → best candidates for **Singleton**.
- **Stateful classes** (classes holding unique data per instance, e.g., a `User` class with `name` and `age` fields, where each user needs distinct data) → best candidates for **Prototype**.

## Section 8: Other Scopes (Brief Mention)
- Besides singleton and prototype, Spring (in web applications) also offers:
  - **Request** scope — new object per HTTP request.
  - **Session** scope — new object per user session.
  - **Application** scope — one object for the lifetime of the web application context.
- These are relevant specifically to Spring Web MVC applications and will be covered in more depth later.

## Section 9: Bean Initialization — Eager vs Lazy
- **Eager initialization** (default for singleton beans): the bean object is created as soon as the `ApplicationContext` (IoC container) starts up — even if nothing has requested it yet.
- **Lazy initialization**: the bean object is created only when it is actually requested (e.g., via `getBean()` or when another bean needs it as a dependency). Enabled with `@Lazy` on the class.
- **Key rule**: Only singleton-scoped beans can toggle between eager and lazy (via `@Lazy`). **Prototype-scoped beans are always lazy by default and can never be made eager** — because eager initialization for a prototype makes no sense (a new object is created every time it's requested anyway, so pre-creating one serves no purpose).
- **Global lazy setting**: The property `spring.main.lazy-initialization=true` (in `application.properties`) makes *all* beans lazy by default. Individual beans can be excluded using `@Lazy(false)` on that specific class.

## Section 10: Why Spring Defaults to Eager Initialization
- Reason: **"Fail-fast" principle.** If all beans and their wiring are created at application startup, any configuration or dependency-resolution errors (e.g., multiple bean definitions, unresolved dependencies) surface immediately at startup rather than later in production when a bean is first actually used — which could cause the application to crash unpredictably in production.
- Lazy initialization is still useful for very heavy classes you don't want instantiated immediately at startup, as long as you're confident they won't fail.

## Section 11: Resolving Circular Dependency Using `@Lazy`
- If class A needs B and B needs A (both singleton, both eager), the container still hits `BeanCurrentlyInCreationException`.
- **Fix demonstrated**: Mark one of the two classes (e.g., `PaymentService`) with `@Lazy`. Now:
  1. The container tries to create `OrderService` first (it's eager).
  2. `OrderService`'s constructor needs `PaymentService`, but since `PaymentService` is `@Lazy`, the container **injects a proxy object** instead of the real one.
  3. `OrderService` finishes being created successfully (with the proxy in place of `PaymentService`).
  4. Only when a method on the proxy (e.g., `pay()`) is actually invoked (such as via `placeOrder()`) does Spring create the **real** `PaymentService` object and inject the real dependency, replacing the proxy behavior.
- `@Lazy` can also be applied not on the whole class, but specifically at the injection point/field (similar to how `@Qualifier` is used) — meaning "create this bean eagerly, but don't eagerly wire its lazy dependency."
- This proxy-based mechanism technically resolves circular dependency, but the lecturer reiterates: **this is a workaround, not a fix** — the real solution is refactoring the code so that circular dependency never arises in the first place.

# Important Concepts
- **Dependency Injection (DI)**: A class receiving objects it needs (its dependencies) from an external container (Spring's IoC container) rather than creating them itself.
- **IoC Container**: Spring's mechanism for creating, managing, and wiring beans, responsible for resolving dependency order.
- **Circular Dependency**: Two (or more) classes depending on each other, making it impossible to determine which one should be created first.
- **Bean Definition**: The metadata Spring's IoC container holds about how to construct a bean, created before the actual object.
- **Constructor Injection**: Dependencies are passed in through the constructor; the object cannot exist without them, so circular dependency cannot self-resolve here.
- **Field/Setter Injection**: Object creation and dependency wiring happen in two separate steps, allowing circular references to be resolved using partially-created object references and Java Reflection.
- **Singleton Scope**: One object per bean definition (Spring's default scope); note this differs from the classic Singleton design pattern.
- **Prototype Scope**: A new object is created every time the bean is requested; always lazily initialized.
- **Eager Initialization**: Bean object created immediately at application/context startup (default for singleton beans).
- **Lazy Initialization**: Bean object created only when first requested/needed; default and unchangeable for prototype beans; optional (via `@Lazy`) for singleton beans.
- **Proxy (Design Pattern)**: A stand-in object that behaves like the real object but isn't the actual instance — used by Spring to "fake" a lazy dependency until it's genuinely needed.
- **Fail-Fast Principle**: The design philosophy behind defaulting to eager initialization — surfacing configuration errors immediately at startup rather than later during runtime.
- **Single Responsibility Principle (SRP)**: A SOLID design principle stating a class should have one clear responsibility; circular dependency is usually a symptom of violating this.

# Step-by-Step Process

### How to Reproduce Circular Dependency (Constructor Injection)
1. Create two classes (e.g., `OrderService` and `PaymentService`), both annotated `@Component`.
2. Give each a constructor that requires an instance of the other (constructor injection).
3. Run the application — Spring throws `BeanCurrentlyInCreationException` due to an unresolvable circular reference.

### How to "Resolve" Circular Dependency Using Field/Setter Injection (Not Recommended)
1. Remove the constructors from both classes.
2. Annotate the dependency fields in both classes with `@Autowired` (field injection) or provide setter methods annotated with `@Autowired`.
3. Spring creates empty objects for both classes first, then injects dependencies into each other's fields using reflection.
4. Run — the circular reference now resolves successfully (though this is discouraged as a practice, and Spring Boot 2.6+ disables it by default).

### How to Properly Fix Circular Dependency (Recommended)
1. Identify which class is taking on a responsibility that isn't really its own (e.g., `PaymentService` calling an `OrderService` method).
2. Move that call into the class where it logically belongs (e.g., have `OrderService` call its own `getOrderDetails()` method after invoking `PaymentService.pay()`).
3. Remove the now-unnecessary dependency injection from the class that no longer needs it.
4. Re-run — the dependency graph is now unidirectional (linear), and the circular reference is eliminated with the same output as before.

### How to Resolve Circular Dependency Using `@Lazy` (Workaround, Not Best Practice)
1. Keep constructor injection in place for both classes.
2. Add `@Lazy` to one of the two classes (e.g., `PaymentService`).
3. Spring creates the eager class (`OrderService`) first, injecting a **proxy** in place of the lazy class.
4. Once a method on the proxy is actually called, Spring instantiates the real lazy bean and completes the wiring.

# Tips and Best Practices
- Always aim to design classes so that circular dependencies never arise in the first place — this usually means better separation of responsibilities.
- Use constructor injection where possible for critical dependencies, since Spring won't silently allow circular dependencies to slip through unnoticed (fail-fast behavior).
- Reserve `@Lazy` for genuinely heavy classes you deliberately don't want instantiated at startup — not as a routine fix for design problems.
- Use singleton scope for stateless classes (services, managers) and prototype scope for stateful classes (e.g., classes representing per-user or per-request data).
- When defining beans via `@Bean` methods in a configuration class, be aware that multiple methods returning the same class create multiple separate bean definitions (and hence separate singleton instances) — use `@Primary` or `@Qualifier` to disambiguate when injecting.

# Mistakes to Avoid
- Assuming circular dependency is a "Spring problem" — it's fundamentally a coding/design flaw that can occur in plain Java too.
- Using field or setter injection specifically as a hack to bypass circular dependency errors, instead of fixing the underlying design issue.
- Confusing Spring's singleton *scope* with the classic Singleton *design pattern* — Spring's singleton only guarantees one object per bean definition, not one object per class overall.
- Assuming a class marked `@Scope("prototype")` can ever be made eager — it cannot; prototype scope is always lazy.
- Forgetting that in Spring Boot 2.6+, circular references are disabled by default (`spring.main.allow-circular-references=false`), so old workaround code may simply stop working unless this property is explicitly set to `true`.
- Letting responsibilities blur between classes (e.g., a `PaymentService` handling order-detail logic) — this is what caused the circular dependency in the example.

# Important Facts
- Spring dependency demonstrated: `spring-context` version **7.0.7**, chosen for compatibility with **Spring Boot 4**.
- The exception thrown for circular dependency: **`BeanCurrentlyInCreationException`**, which explicitly hints at "an unresolvable circular reference."
- Spring Boot disables circular references **by default starting from version 2.6**, via the property `spring.main.allow-circular-references` (default: `false`).
- Default bean scope in Spring: **singleton**.
- Default initialization mode: **eager** (for singleton beans only).
- Property to make all beans lazy globally: `spring.main.lazy-initialization` (default: `false`).
- To exclude one bean from a global lazy setting: `@Lazy(false)` on that specific class.
- Ambiguous multiple bean definitions error: `NoUniqueBeanDefinitionException`.
- `@Configuration` is internally annotated with `@Component` — meaning configuration classes are themselves Spring-managed beans.

# FAQs

**Q1: Is circular dependency caused by Spring itself?**
No. It's a general object-creation problem that can occur in plain Java as well; it results from poor code design (specifically, violating the Single Responsibility Principle), not from using Spring's DI.

**Q2: Why does constructor injection fail to resolve circular dependency, but field/setter injection can?**
Constructor injection requires the dependency at the moment of object creation, so there's no valid "starting point." Field/setter injection separates object creation from dependency wiring, allowing Spring to create empty objects first and inject partial references into each other afterward using reflection.

**Q3: Should I use field/setter injection to fix circular dependency in my projects?**
No — while technically possible, it's considered bad practice. It's a "code smell" indicating your classes are too tightly coupled. Refactor to remove the circular dependency instead.

**Q4: Does Spring Boot allow circular dependencies by default?**
No, not since version 2.6. It disables them by default via the `spring.main.allow-circular-references` property, unless you manually set it to `true`.

**Q5: What's the difference between Spring's singleton scope and the classic Singleton design pattern?**
Spring's singleton scope only guarantees one object per **bean definition** — not one object for the entire class. If you create multiple bean definitions for the same class (e.g., via multiple `@Bean` methods), you get multiple singleton instances.

**Q6: Can a prototype-scoped bean ever be eager?**
No. Prototype beans are always lazily initialized, and this cannot be changed, because eager creation would serve no purpose when a new object is generated on every request anyway.

**Q7: Why does Spring default to eager initialization instead of lazy?**
To follow the "fail-fast" principle — any dependency or configuration errors surface immediately at application startup instead of unpredictably later in production.

**Q8: How does `@Lazy` help resolve circular dependency?**
By marking one of the two circularly dependent beans as `@Lazy`, Spring injects a **proxy** object in place of the real dependency during the other bean's creation. The real object is only instantiated when it's actually used (e.g., a method call), breaking the creation deadlock.

**Q9: What's the recommended way to actually eliminate circular dependency (not just make it "work")?**
Refactor responsibilities so dependencies flow in a single direction — identify which class is doing work outside its core responsibility and move that logic to the appropriate class.

**Q10: What determines whether a class should be singleton or prototype scoped?**
Stateless classes (no unique per-instance data, like service/manager classes) should be singleton. Stateful classes (holding unique data per instance, like a `User` class) should be prototype.

# Final Summary
- Circular dependency occurs when two classes depend on each other, preventing Spring's IoC container from determining a valid creation order.
- It is not a Spring-specific issue — it's a general coding/design flaw, demonstrable in plain Java too (leading to a `StackOverflowError`).
- Constructor injection cannot resolve circular dependency because the dependency is required at construction time.
- Field/setter injection *can* technically resolve it, since object creation and dependency wiring are separated, using Java Reflection to inject references into partially-created objects.
- Despite being technically possible, resolving circular dependency via field/setter injection is discouraged; Spring Boot 2.6+ disables it by default.
- The recommended fix is to refactor code so responsibilities are properly separated (following the Single Responsibility Principle), removing the circular reference entirely.
- Bean scopes: **Singleton** (default) creates one object per bean definition; **Prototype** creates a new object every time it's requested.
- Spring's singleton is not identical to the classic Singleton design pattern — multiple bean definitions for the same class can create multiple singleton instances.
- Stateless classes should be singleton-scoped; stateful classes should be prototype-scoped.
- Beans are initialized either **eagerly** (default for singletons, created at startup) or **lazily** (created only when needed; mandatory default for prototypes, optional via `@Lazy` for singletons).
- Spring defaults to eager initialization to follow the "fail-fast" principle, surfacing errors early rather than in production.
- `@Lazy` can also be used as a workaround to resolve circular dependency by injecting a proxy object until the real dependency is genuinely needed — but this remains a workaround, not a true fix.