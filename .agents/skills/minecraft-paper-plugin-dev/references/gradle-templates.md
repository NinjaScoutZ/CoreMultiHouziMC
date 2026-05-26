# Gradle Templates for Paper Plugins

## Paper 26.1+ — Gradle Kotlin DSL

### `build.gradle.kts`

```kotlin
plugins {
    java
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.jar {
    archiveBaseName.set("MyPlugin")
}

// If you need to shade dependencies:
// plugins { id("com.github.johnrengelman.shadow") version "8.1.1" }
// tasks.shadowJar {
//     relocate("some.library", "com.example.libs.somelibrary")
// }
```

### `settings.gradle.kts`

```kotlin
rootProject.name = "my-plugin"
```

---

## Paper 1.21.11 — Gradle Kotlin DSL

### `build.gradle.kts`

```kotlin
plugins {
    java
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

---

## paperweight-userdev (NMS access)

### 26.1+ — No reobfuscation

```kotlin
plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" // check latest
}

dependencies {
    paperweight.paperDevBundle("26.1.2-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

// NOTE: On 26.1+, reobfuscation is DISABLED.
// Use Mojang-mapped class/method/field names directly.
// The produced JAR runs as-is on Paper 26.1+ servers.
```

### 1.21.11 — With reobfuscation

```kotlin
plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.7" // check latest for 1.21.x
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.reobfJar {
    // This task reobfuscates the JAR for production use
    // The output goes to build/libs/*-reobf.jar
}
```

---

## Maven alternative (for reference)

### `pom.xml` snippet — Paper 26.1+

```xml
<repositories>
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>26.1.2-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <release>25</release>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### `pom.xml` snippet — Paper 1.21.11

```xml
<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.11-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <release>21</release>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Spigot/Bukkit alternative (broad compatibility)

```xml
<repositories>
    <repository>
        <id>spigot-repo</id>
        <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.21.11-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

> ⚠️ For CraftBukkit/NMS with Spigot, you must run **BuildTools** to install artifacts locally.
> Paper's paperweight-userdev is the recommended alternative.
