# Topic
Understanding Apache Maven in depth — what it is, JAR files, project structure, the POM file, dependency management, repositories, and the Maven build lifecycle (part of a Java Spring Framework series).

# Overview
This lecture, part of a Java Spring Framework series, explains Apache Maven, a project management tool widely used in Java development. It begins by explaining what a JAR (Java ARchive) file is and why sharing raw `.class` files or entire folders is impractical, motivating the need to package code into JARs. It then distinguishes libraries (non-runnable code, used as dependencies) from applications (runnable code with a main method, packaged as executable JARs). The video demonstrates creating a Maven project in IntelliJ IDEA, exploring its standard folder structure (`src/main/java`, `src/main/resources`, `src/test/java`), and comparing it with a Spring Boot project generated via Spring Initializr. It then dives deeply into the `pom.xml` (Project Object Model) file — its schema, GAV coordinates (Group ID, Artifact ID, Version), properties, dependencies, and plugins — and explains transitive dependencies, the Super POM/Parent POM inheritance model, and the Effective POM. It covers how Maven fetches dependencies from Maven Central and caches them locally in the `.m2` repository folder, and how remote/company-specific repositories can be configured. Finally, it explains the Maven build lifecycle (Clean, Default, Site) with all its phases (validate, compile, test, package, verify, install, deploy) and introduces Maven Archetypes as ready-made project templates.

# Detailed Notes

## What is a JAR file?
- When writing Java programs, each `.java` file is compiled into a `.class` file (bytecode) that any JVM can run.
- Real-world projects have hundreds of Java classes, so manually compiling and sharing individual `.class` files (e.g., by zipping a folder) is impractical — folder structures can get mismatched, files can be missed, resources like images or properties files can be left out.
- **JAR (Java ARchive)** solves this: it's essentially a zip-like package that bundles multiple `.class` files, resources (images, properties files), and folders/packages into a single shareable file.
- Two main advantages of JAR files:
  1. **To share your own Java code easily** with others.
  2. **To use external/third-party libraries** in your own application (e.g., a MySQL connector JAR to connect to a database).

## Library vs Application
- **Library**: Code that is not runnable on its own — no main function to execute directly. It contains classes/packages meant to be used by another application. When used inside an application, it becomes a **dependency**.
- **Application**: Runnable code containing a main function; can be executed as a whole program.
- When you package a **library** into a JAR, it does NOT include its external/third-party dependencies inside it — only your own code. Information about what further dependencies are needed is stored in a special file (the POM file, if using Maven).
- When you package an **application** (e.g., a Spring Boot app) into a JAR, the result is an **executable JAR** — it is independently runnable and automatically includes its dependencies, so nothing needs to be downloaded separately.
- The classpath is the mechanism the JVM uses to locate classes at runtime — first checking your own code, then the JAR files, to resolve which class/method to call.

## Introducing Maven
- Before Maven, developers manually downloaded JAR files (e.g., from a database vendor's website) one at a time. This approach has major problems:
  - **Manual effort**: Downloading each dependency by hand doesn't scale to hundreds of dependencies.
  - **Version mismatches**: e.g., Spring 7 requires Spring Boot 4 — mismatched versions can break compatibility, and developers must track this manually.
  - **Transitive dependencies**: A dependency may itself depend on other dependencies, all of which must also be tracked and downloaded manually.
  - Sharing your own library also becomes hard, since you'd need to manually list all required dependencies (and their exact versions) in a text file for others to download.
- **Maven** solves all of this: you simply declare which dependencies you need, and Maven downloads them (including transitive dependencies) automatically.

### Definition
Maven is a **project management tool**. It performs four main functions:
1. Maintains a **standard folder structure** for the project (so teams stay consistent regardless of which IDE they use).
2. Helps **compile** your Java code (with one click/command).
3. **Packages** your code into a JAR file.
4. **Downloads dependencies** (including transitive ones) automatically.

## Setting up a Maven Project (in IntelliJ IDEA)
- Maven can be downloaded from the Apache Maven website and installed with a classpath setup, but most IDEs (like IntelliJ) come with Maven pre-installed.
- When creating a new project in IntelliJ, you choose a **build system**: IntelliJ's built-in system, Maven, or Gradle. Using a standard build system like Maven ensures consistency across team members regardless of which IDE (IntelliJ, Eclipse, NetBeans) they use.
- During project creation, you specify:
  - **Group ID** (e.g., `org.example`)
  - **Artifact ID** (e.g., `maven-demo`)
  - Java version

## Maven Folder Structure
- Top-level project folder (e.g., `maven-demo`) contains:
  - **External Libraries** — shows downloaded JAR dependencies (initially just the JDK).
  - **src** — source folder.
  - **pom.xml** — the most important file for Maven (Project Object Model).
- Inside `src`, there are two folders:
  - **main** — contains your actual application code.
    - **java** — your `.java` source files, organized in a folder structure matching your Group ID + Artifact ID (dots in the Group ID represent nested folders, e.g., `org.example` → `org/example`).
    - **resources** — non-Java files: static resources, images, properties files, templates, etc.
  - **test** — contains unit test code (using libraries like JUnit, Mockito), mirroring the same folder structure as `main`.
- A Spring Boot project (created via Spring Initializer) follows the same structure, but additionally includes a **target** folder (created after building) and pre-configured resource folders (`static`, `templates`) plus an `application.properties` file.

## Compiling Code & the Target Folder
- Clicking **Compile** (via Maven's lifecycle panel or `mvn compile`) compiles all `.java` files into `.class` files.
- Compiled output is stored in a **target** folder, mirroring the same package folder structure as the source.
- Deleting the target folder and recompiling regenerates it — Maven always places compiled code in this same standard structure.
- Even when running code directly (e.g., clicking the Run button in IntelliJ), IntelliJ internally still uses Maven's build system to compile and stores output in the same `target` folder structure.

## Creating a JAR
- Clicking **Package** in Maven's lifecycle triggers building of the JAR file.
- The JAR is placed inside the `target` folder.
- The JAR's name follows the pattern: `<artifactId>-<version>.jar` (e.g., `maven-demo-1.0-SNAPSHOT.jar`).
- This naming pattern (Artifact ID + Version) is fixed and important — it's also why third-party dependency JARs are sometimes called "artifacts."

## Understanding pom.xml
- **POM** = **Project Object Model**. It is the most important file for Maven — it stores complete information about the project: name, version, dependencies, plugins, etc.
- Key elements of `pom.xml`:
  - **`<project>`** — root tag; every POM starts with this.
  - **Schema declaration lines** — define the rules/allowed tags for the POM (adding an invalid/unknown tag triggers an "invalid context" warning).
  - **`<modelVersion>`** — the version of the POM model itself (e.g., `4.0.0`), not Maven's own version.
  - **GAV coordinates** — the three most essential tags, which together uniquely identify a project globally:
    - **Group ID**: should be globally unique — commonly derived from a reversed domain name (e.g., domain `coderarmy.in` → `in.coderarmy`). If you don't own a domain, a placeholder like `org.example` is fine for practice.
    - **Artifact ID**: the name of your project (e.g., `maven-demo`).
    - **Version**: e.g., `1.0-SNAPSHOT`. "SNAPSHOT" is a convention indicating the project is still a work in progress (may contain bugs); once complete and tested, the SNAPSHOT suffix is typically removed.
  - **`<properties>`** — key-value pairs (e.g., compiler source/target version, source encoding). You can add custom properties (e.g., `author.name`) and reference them elsewhere in the POM using `${property.name}` syntax — useful, for example, to append a custom string to the version/JAR name.
  - **`<packaging>`** — specifies output type: `jar` (default) or `war` (Web Archive).
  - **`<dependencies>`** — where all external JAR dependencies are declared, each requiring a Group ID, Artifact ID, and Version (Scope is optional; defaults to `compile`, meaning needed at compile time — can also be `test`, etc.).

## Downloading Dependencies through Maven
- To find dependency coordinates without memorizing them, use the **Maven Repository** website (mvnrepository.com):
  - Search for the library (e.g., "MySQL Connector").
  - Choose a version — generally avoid the very latest (higher bug risk, less tested, fewer users) but also avoid very old versions; a reasonably recent, widely-used version is a good choice.
  - Copy the Maven-format dependency snippet (Group ID, Artifact ID, Version) and paste it into your `pom.xml`.
- After pasting, you must **sync/reload** Maven (via the reload icon or the Maven panel) for the dependency to actually download — pasting the XML alone does not trigger a download.
- Maven automatically resolves **transitive dependencies** — e.g., adding the MySQL connector also pulled in a Google Protobuf dependency because MySQL connector depends on it internally. This hierarchy is visible in the Maven tool window under Dependencies.
- Adding a dependency like Hibernate similarly pulls in many further transitive dependencies automatically — manual downloading would require tracking all of these individually.

## Parent POM & Super POM
- POM files support **inheritance**. At the very top of this hierarchy is the **Super POM** (analogous to Java's `Object` class — the parent of all classes).
- If a POM doesn't declare a `<parent>` tag, its default parent is the Super POM.
- The Super POM contains many default configurations (repositories, plugin management, build settings, resources, etc.) that aren't shown in a simple project's POM, keeping the POM itself lightweight.
- You can view a project's fully resolved configuration via **"Show Effective POM"** in IntelliJ — this shows the Effective POM, i.e., what Maven will actually use (your POM's declarations plus everything inherited from parent POMs).
- A Spring Boot project's POM typically has an explicit `<parent>` tag (e.g., pointing to `org.springframework.boot`), which itself has its own parent, eventually tracing back to the Super POM — forming multi-level inheritance, similar to Java class inheritance, allowing configurations to be overridden at different levels.
- You can override inherited tags (like `<repositories>`) by explicitly redeclaring them in your own POM.

## Central & Local Repository
Maven uses (at least) two types of repositories:
1. **Maven Central** — the default remote repository containing thousands of JAR files, declared in the Effective POM with its URL.
2. **Local Repository** — a hidden folder Maven creates on your machine called **`.m2`** (on Mac: at the root/user level; on Windows: inside the user folder), containing a `repository` subfolder.
   - When resolving a dependency, Maven first checks the **local repository** (`.m2/repository`). If found there, it's used directly (fast).
   - If not found locally, Maven downloads it from **Maven Central**, then **caches** it in `.m2/repository` for future use.
   - This is why the first build is slower (nothing cached yet) but subsequent builds are much faster.
   - Your own project's JAR (created via Package/Install) is also stored in this local cache, following the same `groupId/artifactId/version` folder structure, with filename `<artifactId>-<version>.jar`.
3. **Other Remote Repositories** — companies often maintain their own private remote repository (in addition to or instead of Maven Central) for:
   - Storing private/internal libraries not meant to be public.
   - Security — using a vetted, "tried and tested" internal repository rather than trusting arbitrary packages on the open Maven Central.
   - You can override the default repository by declaring a `<repositories>` tag with a custom `<id>` and `<url>` in your POM.

**Overall dependency resolution flow**: Maven reads `pom.xml` → checks local repository (`.m2`) first → if not found, fetches from Maven Central (or a configured custom remote repository) → caches the downloaded JAR locally.

## Maven Lifecycle
Maven lifecycle is divided into three parts:
1. **Clean lifecycle** — has a single phase, **clean**, which deletes old build artifacts (the `target` folder) — independent of the default lifecycle.
2. **Default lifecycle** — the most important; contains multiple sequential phases:
   - **validate** — checks the project structure is correct (POM exists, is readable, all required details are present).
   - **compile** — compiles `.java` files into `.class` files (stored in `target/classes`).
   - **test** — runs your unit tests (e.g., JUnit, Mockito test cases).
   - **package** — builds the JAR file (stored in `target`); requires the code to have passed compilation and tests first.
   - **verify** — runs checks like integration testing or code quality analysis (via plugins configured in the `<plugins>` tag of the POM).
   - **install** — installs the packaged JAR into your **local repository** (`.m2`).
   - **deploy** — uploads (deploys) the JAR/artifact from your local repository to a **remote repository** (often a company-wide one), so other team members can use it. (Note: this is distinct from pushing code to Git/GitHub for production deployment — Maven's "deploy" phase is specifically about publishing the built artifact to a repository.)
   - **Important rule**: Running any phase automatically runs all preceding phases in order (e.g., running `install` runs validate → compile → test → package → verify → install).
3. **Site lifecycle** — has a single phase, **site**, used for generating documentation and reports (rarely needed in typical workflows).

### Running lifecycle phases
- Can be triggered via IntelliJ's Maven panel (clicking a phase) or via terminal commands: `mvn compile`, `mvn install`, `mvn deploy`, etc.
- Maven must be installed for terminal use (e.g., via Homebrew on Mac: `brew install maven`; via Chocolatey on Windows) or by manually setting up Maven's path from the Maven Repository website.
- You can chain commands from different lifecycles, e.g.:
  - `mvn clean compile` — cleans previous artifacts, then compiles.
  - `mvn clean install` — a very commonly used command: cleans old artifacts, then runs validate → compile → test → package → (verify, if configured) → install (placing the built JAR into the local `.m2` repository).

## Maven Archetypes
- **Archetypes** are ready-made project templates that let you skip writing boilerplate code from scratch.
- Available when creating a new Maven project (e.g., "Maven Quickstart" archetype, which comes with a pre-written app class and test class, plus a JUnit dependency).
- Additional catalogs (like Maven Central) offer more archetypes, e.g., a "Spring Boot Starter" archetype that generates a project similar to what Spring Initializer produces, complete with starter dependencies and sample classes.

# Important Concepts
- **JAR (Java ARchive)**: A packaged bundle of compiled `.class` files, resources, and folders — used for sharing code and libraries.
- **Library vs Application**: A library is non-runnable code used as a dependency; an application is runnable code with a main method.
- **Executable JAR**: A JAR built from an application; runs independently and bundles its dependencies.
- **Dependency**: A library used inside another project/application.
- **Transitive dependency**: A dependency required by another dependency (resolved automatically by Maven).
- **POM (Project Object Model)**: The `pom.xml` file describing a project's configuration, dependencies, and plugins.
- **GAV coordinates**: Group ID, Artifact ID, Version — together uniquely identify a project/artifact.
- **SNAPSHOT**: A version suffix convention indicating a project still under active development.
- **Super POM**: The root/default parent of all POMs, analogous to Java's `Object` class.
- **Effective POM**: The fully resolved POM Maven actually uses at build time (your POM plus everything inherited from parent POMs).
- **Local repository (`.m2`)**: A local, hidden cache folder where Maven stores downloaded and self-built JARs.
- **Maven Central**: Maven's default public remote repository containing a vast collection of JARs.
- **Remote repository (company-wide)**: A private repository organizations may use instead of, or alongside, Maven Central, for internal libraries and security reasons.
- **Maven Lifecycle**: The ordered sequence of build phases (validate, compile, test, package, verify, install, deploy) that Maven executes to build a project.
- **Archetype**: A pre-built project template that scaffolds a starting project structure and code.

# Step-by-Step Process
### Setting up a new Maven project in IntelliJ and adding a dependency
1. Create a new project in IntelliJ IDEA and choose **Maven** as the build system.
2. Specify the Group ID, Artifact ID, and Java version (optionally check "Add sample code").
3. Explore the generated folder structure: `src/main/java`, `src/main/resources`, `src/test/java`, and the `pom.xml` file.
4. To add a dependency: search for it on the Maven Repository website (mvnrepository.com), choose a suitable version, and copy the Maven-format snippet.
5. Paste the snippet into the `<dependencies>` section of `pom.xml`.
6. Click the sync/reload icon (or use the Maven panel) to trigger the actual download of the dependency (and its transitive dependencies) into the project and local `.m2` cache.
7. To compile: click **Compile** in the Maven lifecycle panel (or run `mvn compile`).
8. To create a JAR: click **Package** (or run `mvn package`); the JAR appears in the `target` folder.
9. To install the JAR into the local repository: click **Install** (or run `mvn install`), or run the common combined command `mvn clean install` to clean, rebuild, and install in one step.

# Tips and Best Practices
- Avoid using the absolute latest version of a dependency — it may be poorly tested and carry a higher risk of bugs; instead, pick a reasonably recent version with a healthy number of existing users.
- Use a reversed domain name convention for your Group ID (e.g., `in.coderarmy`) to ensure global uniqueness; if you don't own a domain, a placeholder like `org.example` is acceptable for practice projects.
- Keep your own `pom.xml` minimal by relying on inherited configuration from parent POMs (Super POM / framework-specific parent POMs) rather than repeating everything.
- Use a standard build tool (like Maven) across a team so that folder structures and build processes remain consistent, regardless of which IDE each team member uses.
- Use company-wide/private remote repositories for internal libraries or for security reasons (to avoid depending directly on potentially vulnerable public packages).
- Remember that running any later-lifecycle phase (e.g., `install`) automatically runs all earlier phases, so you don't need to run them individually.

# Mistakes to Avoid
- Don't manually download and manage JAR files and their versions — this is error-prone and doesn't scale, especially with transitive dependencies.
- Don't assume pasting a dependency snippet into `pom.xml` immediately downloads it — you must sync/reload the project for the download to actually happen.
- Don't confuse Maven's **deploy** phase with pushing code to Git/GitHub — deploy refers to publishing a built artifact/JAR to a remote repository, not merging code for production.
- Don't confuse the **Super POM** (parent of all POMs) with the **Effective POM** (the final resolved POM actually used for the build) — they are related but distinct concepts.
- Don't add arbitrary custom XML tags to a POM without following its schema — invalid tags will cause an "invalid context" error.

# Important Facts
- POM model version used in the example: `4.0.0`.
- Default dependency scope: `compile`.
- JAR naming convention: `<artifactId>-<version>.jar`.
- Local repository location: hidden `.m2` folder (at root level on Mac; inside the user folder on Windows), containing a `repository` subfolder.
- Maven lifecycle has three parts: **Clean**, **Default**, and **Site**.
- Default lifecycle phases, in order: validate → compile → test → package → verify → install → deploy.
- Common terminal commands: `mvn compile`, `mvn install`, `mvn deploy`, `mvn clean compile`, `mvn clean install`.
- Packaging types: `jar` (default) or `war`.

# FAQs
**Q: What is the difference between a JAR file and a ZIP file?**
A: A JAR is conceptually similar to a ZIP file — it packages multiple compiled `.class` files, resources, and folders — but it's specifically designed for Java code and is directly usable by the JVM/Java tooling.

**Q: Why doesn't a library's JAR include its third-party dependencies?**
A: Because a library JAR only contains its own compiled code; information about which further dependencies it needs is recorded in a special file (the POM, if using Maven) so that whoever uses the library can resolve those dependencies separately.

**Q: What happens if I delete my local `.m2` repository?**
A: All previously cached dependencies are lost, so the next build must re-download everything from Maven Central (or configured remote repositories) and re-cache it locally, making that build slower.

**Q: What is the difference between the Super POM and the Effective POM?**
A: The Super POM is the ultimate parent of all POMs (like Java's `Object` class). The Effective POM is what Maven actually uses at build time — combining your own POM's declarations with everything inherited from parent POMs (which may ultimately include the Super POM).

**Q: Why should I avoid always picking the latest dependency version?**
A: The latest version may have more bugs, less testing, and fewer users verifying it works well, increasing the risk of instability in your project.

**Q: What's the difference between the "install" and "deploy" phases?**
A: "Install" places the built JAR into your **local** repository (`.m2`), making it available on your own machine. "Deploy" uploads it further to a **remote** (often company-wide) repository so other people/teams can access it.

**Q: Do I need to run `validate`, `compile`, and `test` separately before running `package`?**
A: No — running any later phase in the default lifecycle automatically runs all the preceding phases first.

**Q: What is a Maven Archetype?**
A: A pre-built project template (e.g., Quickstart, Spring Boot Starter) that generates a starting project structure and sample code so you don't have to write everything from scratch.

# Final Summary
- Maven is a **project management tool** that maintains folder structure, compiles code, packages JARs, and downloads dependencies automatically.
- A **JAR** packages compiled classes and resources for easy sharing; **libraries** (non-runnable) differ from **applications** (runnable, produce executable JARs).
- Manually managing dependencies is impractical due to manual effort, version mismatches, and transitive dependencies — Maven automates all of this.
- The **pom.xml** file is central to Maven, containing the project's Group ID, Artifact ID, Version (GAV), properties, dependencies, and plugins.
- **GAV coordinates** uniquely identify every project/artifact globally.
- POMs follow an **inheritance model**, rooted at the **Super POM**; the **Effective POM** is what Maven actually uses after combining inherited configuration.
- Maven checks the **local repository (`.m2`)** first before fetching from **Maven Central** or a configured custom remote repository, caching downloads locally for faster future builds.
- Companies may use **private remote repositories** for internal libraries and added security.
- The **Maven Lifecycle** consists of Clean, Default, and Site lifecycles; the Default lifecycle's phases run in strict order: validate → compile → test → package → verify → install → deploy.
- **Archetypes** provide ready-made project templates to speed up project setup.