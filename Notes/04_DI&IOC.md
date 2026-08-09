# Topic
Understanding Dependency Injection (DI) and Inversion of Control (IoC) in plain Java, as a foundation before learning the Spring Framework.

# Overview
This video is part of a Spring Framework series and focuses on the "why" behind Spring Core concepts — Dependency Injection and Inversion of Control — before actually using Spring. The instructor builds a simple example with an `OrderService` that needs to send a notification after an order is placed, using an `EmailService`. Initially, `OrderService` directly creates its own `EmailService` object, which creates tight coupling — a major design problem in Java code. The video walks through introducing an interface (`NotificationService`) to loosen this coupling, then shows how Dependency Injection (getting a dependency from outside instead of creating it yourself) solves the remaining tight coupling caused by object creation inside the dependent class. Constructor injection and setter injection are demonstrated with working code, and the benefits — flexibility to swap implementations, adherence to the Open/Closed and Single Responsibility principles, and easier unit testing — are explained. The video then covers Inversion of Control as the underlying principle, with Dependency Injection as the technique used to achieve it, and closes by previewing the Spring IoC Container's future role in creating, managing, and connecting objects (referred to as "beans") — work currently being done manually via the `main` method.

# Detailed Notes

## Setting Up the Problem
- A new Maven project ("Core Demo") is created using JDK 23, without adding any Spring dependencies — the point is to first understand the underlying Java design problem.
- A simple `OrderService` class is created with a `placeOrder()` method that prints "Order Placed."
- An `EmailService` class is created with a `sendNotification()` method that prints "Email Notification Sent."
- Inside `OrderService`, an `EmailService` object is created directly (`new EmailService()`), and its `sendNotification()` method is called from within `placeOrder()`.
- Running the code confirms both messages print correctly, but this design has a fundamental problem: **tight coupling**.

## Tight Coupling Problem
- `OrderService` is said to be "dependent" on `EmailService` — without an `EmailService` object, `placeOrder()` cannot do its job.
- This is conceptually similar to a Maven dependency (relying on a third-party library), but here it refers to one class relying on another.
- **Key issue**: `OrderService` is *creating its own dependency* — this is the actual problem, not the dependency itself.
- Analogy: If someone says "I need to go from Delhi to Chandigarh" but insists on a specific bus, specific driver, and specific company, they've become "tightly coupled" to unnecessary specifics. They only cared about reaching Chandigarh — the rest shouldn't matter.
- Two concrete violations from Java/SOLID design principles:
  - **Single Responsibility Principle (SRP)**: `OrderService` should only handle order-related logic, but it's also acting like a factory, creating notification objects.
  - **Open/Closed Principle (OCP)**: Every time the notification type needs to change (e.g., email → SMS), the `OrderService` class itself has to be modified.

## Introducing Interfaces for Loose Coupling
- A `NotificationService` interface is created with a single method, `sendNotification()`.
- Three classes — `EmailService`, `SMSService`, and `PopUpNotificationService` — are created, each implementing this interface with their own version of `sendNotification()`.
- Classes are organized into a `notification` package for cleanliness.
- `OrderService` is updated to use the interface type (`NotificationService`) as the reference variable type instead of a concrete class.
- However, the object itself is still being created *concretely* inside `OrderService` (e.g., `new EmailService()`), so **tight coupling still exists** — using an interface as the variable type alone does not solve the core problem.

## Dependency Injection (Constructor-Based)
- The real fix: `OrderService` should not create its dependency at all — it should simply *ask* for it.
- A constructor is added to `OrderService` that accepts a `NotificationService` object as a parameter and assigns it to an instance variable.
- Now, the `main` method creates the notification object (e.g., `new EmailService()`) and passes it into the `OrderService` constructor.
- Switching notification types (Email → SMS → PopUp) now only requires a one-line change in `main` — `OrderService` itself never needs to be touched.
- This satisfies both:
  - **SRP** — `OrderService` now only handles order logic; it delegates notification-sending rather than creating objects.
  - **OCP** — no need to modify `OrderService` when introducing new notification types.
- This pattern — receiving a dependency from outside instead of creating it — is called **Dependency Injection**.

## Benefits of Dependency Injection
- **Independence**: `OrderService` no longer needs to know which concrete notification type it's using.
- **Better unit testing**: Since the dependency is now injected rather than hardcoded, a "fake" implementation (e.g., `FakeEmailService`, which prints "Dummy Email Sent" instead of sending a real email) can be passed in during testing — avoiding real side effects like sending actual emails while testing `placeOrder()`.

## Types of Dependency Injection
- **Constructor Injection**: Dependency is passed through the class constructor (demonstrated above).
- **Setter Injection**: An empty (no-argument) constructor is added, and a `setNotification()` setter method is used instead to assign the dependency after object creation. Demonstrated by creating `OrderService` with an empty constructor and then calling `.setNotification(new EmailService())`.
- **Field Injection**: Mentioned but not demonstrated here — noted as not possible in plain Java without a framework like Spring; it will be covered later once Spring is introduced.
- Famous guiding principle: **"A class should ask what it needs and not build everything itself."**
- Important clarification: Dependency Injection is *not* a Spring-specific concept — it can exist independently in plain Java. Spring simply automates it.

## Inversion of Control (IoC)
- IoC is closely related to DI but conceptually distinct.
- **Before DI**: Control flowed from `OrderService` → `EmailService` (OrderService was responsible for creating EmailService).
- **After DI**: Control flow is inverted — `main` creates both `EmailService` and `OrderService`, then provides (injects) the `EmailService` object into `OrderService`. Control now flows from `main` → `OrderService`.
- This flip in the direction of control — from being handled *inside* the dependent class to being handled *outside* it — is called **Inversion of Control**.

## Relationship Between IoC and DI
- **IoC** is an idea/principle: the general goal of not letting a class control its own dependency creation.
- **DI** is the approach/technique used to actually achieve IoC.
- Relationship: IoC is the principle; Dependency Injection is the method used to implement that principle.

## Role of the Spring Framework (Preview)
- Currently, the `main` method is responsible for creating objects, managing them, and wiring dependencies together — this becomes very complex as the number of services grows (in a real project, there could be hundreds or thousands of classes).
- Spring solves this scaling problem through its **IoC Container**.
- The Spring IoC Container will take over all the work currently done manually in `main`:
  - **Creating objects**
  — **Managing objects** (their lifecycle — creation, destruction)
  - **Connecting/wiring objects together**
- In Spring terminology, objects managed by the IoC container are called **"beans."**
  - Every bean is an object, but not every object is necessarily a bean — only objects managed by the IoC container are beans.
- Once Spring is introduced (in a future video), the `new` keyword will rarely be used directly in code, since the Spring IoC Container will handle object creation and wiring automatically.

# Important Concepts
- **Tight Coupling**: When a class is overly dependent on the specific implementation details of another class, making changes difficult and requiring modification of the dependent class itself.
- **Loose Coupling**: When a class depends only on an abstraction (interface) and receives its concrete dependency from an external source, making the design flexible and easy to extend.
- **Dependency**: When one class/service relies on another to complete its functionality (having dependencies is normal and expected in real projects).
- **Dependency Injection (DI)**: The practice of providing a class with the dependencies it needs from an external source, rather than having the class create those dependencies itself.
- **Constructor Injection**: Passing a dependency into a class via its constructor.
- **Setter Injection**: Passing a dependency into a class via a setter method after object creation.
- **Field Injection**: Injecting a dependency directly into a field (only possible with a framework like Spring, not in plain Java).
- **Inversion of Control (IoC)**: The principle that control over creating and managing dependencies should be inverted — moved from inside a dependent class to an external controller.
- **IoC Container**: A framework component (like the one in Spring) responsible for creating, managing, and wiring together objects (beans) automatically.
- **Bean**: In Spring terminology, an object that is created and managed by the Spring IoC Container.
- **Single Responsibility Principle (SRP)**: A class should have only one reason to change — i.e., one responsibility.
- **Open/Closed Principle (OCP)**: Classes should be open for extension but closed for modification.

# Step-by-Step Process
1. Identify a class (e.g., `OrderService`) that depends on another service (e.g., a notification service) to complete part of its task.
2. Create an interface (e.g., `NotificationService`) representing the abstract capability needed (e.g., `sendNotification()`).
3. Create concrete implementations of that interface (e.g., `EmailService`, `SMSService`, `PopUpNotificationService`).
4. Change the dependent class to reference the interface type instead of a concrete class — but do not instantiate the concrete class inside the dependent class.
5. Add a constructor (or setter) to the dependent class that accepts the interface type as a parameter and assigns it to an instance variable.
6. In the calling code (e.g., `main`), create the desired concrete implementation and pass it into the dependent class's constructor or setter.
7. To swap implementations, change only the object creation in the calling code — no changes needed inside the dependent class.
8. For testing, create a fake/mock implementation of the interface and inject that instead of the real one.

# Tips and Best Practices
- Always code to interfaces rather than concrete classes wherever possible.
- Organize related classes into packages (e.g., a `notification` package) for cleanliness as a project grows.
- Keep a class's responsibility focused — a business logic class (like `OrderService`) should not be responsible for creating its own dependencies.
- When writing unit tests, use fake/dummy implementations of dependencies to avoid triggering real side effects (like actually sending emails).
- Remember the guiding principle: "A class should ask for what it needs, not build everything itself."

# Mistakes to Avoid
- Don't create a dependency's object directly inside the class that depends on it — this causes tight coupling even if the variable type is declared as an interface.
- Don't assume that using an interface as a variable type alone solves tight coupling — the object creation itself must also happen outside the dependent class.
- Don't let a single class (like `OrderService`) take on multiple responsibilities, such as both handling orders and creating notification objects — this violates the Single Responsibility Principle.
- Don't confuse Dependency Injection and Inversion of Control as being the exact same thing — DI is the technique used to achieve the IoC principle.

# Important Facts
- Dependency Injection and Inversion of Control are **not Spring-specific concepts** — they can be implemented in plain Java without any framework.
- Spring automates Dependency Injection and Inversion of Control through its IoC Container.
- Three types of Dependency Injection are mentioned: Constructor Injection, Setter Injection, and Field Injection (the last being possible only within a framework like Spring).
- In Spring, objects managed by the IoC Container are called "beans."

# FAQs

**Q1: What is the core problem with tight coupling in Java code?**
A: A class becomes tightly coupled when it depends on and directly creates its own dependency, making it hard to change implementations without modifying the dependent class itself.

**Q2: Is Dependency Injection a Spring-only concept?**
A: No. DI can be implemented in plain Java without using the Spring Framework at all — Spring simply automates and manages it for you.

**Q3: What's the difference between Dependency Injection and Inversion of Control?**
A: IoC is the underlying principle — inverting the responsibility for creating/managing dependencies. DI is the specific technique/approach used to achieve that principle.

**Q4: What are the types of Dependency Injection discussed?**
A: Constructor Injection (passing the dependency via the constructor) and Setter Injection (passing it via a setter method) are demonstrated. Field Injection is mentioned but requires a framework like Spring.

**Q5: How does Dependency Injection help with unit testing?**
A: Since dependencies are provided externally rather than created internally, you can inject a fake/mock implementation during testing to avoid real side effects (like sending an actual email).

**Q6: What is a "bean" in Spring terminology?**
A: A bean is an object that is created and managed by the Spring IoC Container. Every bean is an object, but not every object is necessarily a bean.

**Q7: What will the Spring IoC Container do that the `main` method currently does manually?**
A: It will create objects, manage their lifecycle, and connect (wire) them together automatically — replacing the manual wiring currently done in `main`.

**Q8: Which two SOLID principles does tight coupling in the initial design violate?**
A: The Single Responsibility Principle and the Open/Closed Principle.

# Final Summary
- The video explores Dependency Injection (DI) and Inversion of Control (IoC) in plain Java, before introducing the Spring Framework itself.
- An `OrderService` initially creates its own `EmailService` object directly, causing tight coupling.
- Tight coupling violates the Single Responsibility Principle and Open/Closed Principle.
- Introducing an interface (`NotificationService`) alone doesn't fix tight coupling if the concrete object is still created inside the dependent class.
- The real fix is Dependency Injection: providing the dependency from outside (e.g., via `main`) instead of creating it internally.
- Constructor Injection and Setter Injection are the two DI techniques demonstrated with working code examples.
- Field Injection is mentioned as a third type, but requires a framework like Spring to implement.
- DI makes code more flexible (easy to swap implementations) and easier to unit test using fake/mock objects.
- Inversion of Control is the principle behind DI — control over dependency creation is "inverted" from inside a class to an external source.
- IoC is the idea; DI is the technique used to implement that idea.
- Both DI and IoC are independent of Spring and can be used in plain Java.
- Spring's IoC Container will eventually automate object creation, management, and wiring — work currently done manually in the `main` method.
- Objects managed by the Spring IoC Container are called "beans."