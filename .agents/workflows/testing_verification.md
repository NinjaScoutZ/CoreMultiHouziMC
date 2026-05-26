---
description: How to run, verify, and write automated tests for HouziCore (Shared & Arcade)
---

# Testing & Verification Workflow

HouziCore uses automated JUnit 5 tests, localization guardrails, and MockBukkit to verify code safety and bilingual content alignment before deployment.

---

## 1. Running the Automated Test Suite

Before compiling or building the plugin for deployment, you **MUST** run the unit tests to ensure no regressions are introduced.

### Shared Module
The `Shared` module contains localization guardrail tests. Run them from `Code/Shared/`:
```bash
mvn test
```

### Arcade Module
The `Arcade` module contains kit and minigame runtime contract tests. Run them from `Code/Arcade/`:
```bash
mvn test
```

---

## 2. Localization Guardrails (CRITICAL BILINGUAL RULE)

If you modify or add any player-facing messages, you must update the localization catalogs and corresponding guardrail tests.

### Test Locations:
- Primal Games: [PrimalGamesLocalizationGuardrailsTest](file:///e:/Houzicore/Code/Shared/src/test/java/com/houzicore/shared/core/lang/PrimalGamesLocalizationGuardrailsTest.java)
- Prop Rush: [PropRushLocalizationGuardrailsTest](file:///e:/Houzicore/Code/Shared/src/test/java/com/houzicore/shared/core/lang/PropRushLocalizationGuardrailsTest.java)
- Shared System: [SharedMessageCatalogSmokeTest](file:///e:/Houzicore/Code/Shared/src/test/java/com/houzicore/shared/core/lang/SharedMessageCatalogSmokeTest.java)

### Rules for Localization Changes:
1. **Key Parity**: If you add a key in `messages/en/my_game.yml`, you **MUST** add it in `messages/th/my_game.yml`.
2. **Key Registration**: Add the key name to the `REQUIRED_KEYS` array inside the respective `LocalizationGuardrailsTest` class.
3. **No Legacy Orphans**: If a key is deprecated or replaced, delete it from both YAML files AND check that it is listed under the legacy checks in the test class (or remove it entirely) to prevent test failures.

---

## 3. Writing MockBukkit Unit Tests

When implementing complex game logic, commands, or events, write unit tests using JUnit 5 and the **MockBukkit** framework to verify runtime behavior without booting a real Minecraft server.

### Setup Checklist
1. Verify the module's `pom.xml` includes `mockbukkit` and `junit-jupiter` dependencies.
2. In your test class, use the `@BeforeEach` and `@AfterEach` lifecycle hooks to initialize and tear down the mock environment:

```java
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class MyFeatureTest {
    private ServerMock server;
    private MyPluginMock plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        // Load the plugin mocks
        plugin = MockBukkit.load(MyPluginMock.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
}
```

### Testing Common Behaviors

#### Spawning a Mock Player
```java
PlayerMock player = server.addPlayer("Dansac");
```

#### Dispatching a Command
```java
player.performCommand("mycommand arg1 arg2");
// Assert player received expected messages
player.assertSaid("§aSuccess!");
```

#### Simulating Events
```java
PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, ...);
server.getPluginManager().callEvent(event);
// Assert that the event changed the player state as expected
```

---

## 4. Verification in the Handoff

When completing a task, always report:
- Whether `mvn test` was executed.
- The number of tests run and passed in each module.
- Any manual verification details (such as in-game smoke testing).
