# Paper 26.1 Monsters Reference Manual
## Comprehensive Catalog of Hostile, Neutral, Boss, and Custom Monsters

This reference manual documents all monsters (hostile, neutral, boss, and custom variants) supported by the Paper 26.1 ecosystem. It provides visual profiles, behavioral characteristics, Bukkit/Paper API hooks, and creative minigame designs.

---

## 1. Undead Monsters

### 1. Zombie
*   **Visual Profile:** A green, decaying humanoid wearing a tattered cyan shirt and blue trousers.
*   **Behavior:** Attacks players, villagers, and iron golems. Can break wooden doors on Hard difficulty.
*   **API Control:** `org.bukkit.entity.Zombie`. Check if it is a baby via `isBaby()` / `setBaby(boolean)`.
*   **Minigame Idea:** **"Door Defender"**: Players must reinforce wooden doors using materials while waves of zombies try to break through.

### 2. Husk
*   **Visual Profile:** A dry, sand-colored zombie wearing ragged, sun-bleached desert robes.
*   **Behavior:** Immune to sunlight. Attacks apply the Hunger effect to targets.
*   **API Control:** `org.bukkit.entity.Husk`.
*   **Minigame Idea:** **"Hunger Arena"**: Players fight Husks in a desert colosseum where no food items drop; hunger management is the primary challenge.

### 3. Drowned
*   **Visual Profile:** A waterlogged zombie, teal-blue in color, wrapped in dripping seaweed with cyan-glowing accents.
*   **Behavior:** Swims fast. Attacks with tridents or fists. Burns in sunlight if not in water.
*   **API Control:** `org.bukkit.entity.Drowned`. Set/get if it is searching for land via `org.bukkit.entity.Drowned#isSearchingForLand()`.
*   **Minigame Idea:** **"Harpoon Defense"**: Players defend a raft from fast-swimming Drowned throwing tridents.

### 4. Zombie Villager
*   **Visual Profile:** A decaying zombie with a villager's long nose, wearing torn professional clothing.
*   **Behavior:** Aggressive. Can be cured using a Splash Potion of Weakness and a Golden Apple.
*   **API Control:** `org.bukkit.entity.ZombieVillager`. Use `getVillagerProfession()` and `setVillagerProfession(Profession)`.
*   **Minigame Idea:** **"Field Hospital"**: A fast-paced clinic where players must trap, weaken, and cure zombie villagers before they break quarantine.

### 5. Skeleton
*   **Visual Profile:** An animated pile of white bones with empty eye sockets, carrying a wooden bow.
*   **Behavior:** Attacks from range. Flees from wolves. Burns in sunlight.
*   **API Control:** `org.bukkit.entity.Skeleton`.
*   **Minigame Idea:** **"Arrow Dodgeball"**: Players navigate an arena full of Skeletons behind glass blocks, utilizing cover to dodge incoming arrows.

### 6. Stray
*   **Visual Profile:** A skeletal figure wearing tattered, grey-blue winter cloaks with glowing icy-blue eyes.
*   **Behavior:** Shoots tipped arrows of Slowness. Burns in sunlight.
*   **API Control:** `org.bukkit.entity.Stray`.
*   **Minigame Idea:** **"Glacial Sprint"**: A parkour course where players are shot at by Strays; getting hit slows them down, causing them to fall into ice water.

### 7. Wither Skeleton
*   **Visual Profile:** A tall, charcoal-black skeleton wielding a stone sword.
*   **Behavior:** Inflicts the Wither effect on hit. Immune to fire and lava.
*   **API Control:** `org.bukkit.entity.WitherSkeleton`.
*   **Minigame Idea:** **"Nether Gauntlet"**: Players run through a narrow obsidian bridge dodging Wither Skeletons while dealing with health decay.

### 8. Phantom
*   **Visual Profile:** A flying, skeletal beast with tattered dark-blue wings and glowing green eyes.
*   **Behavior:** Swoops down to attack players who haven't slept for 3+ in-game days.
*   **API Control:** `org.bukkit.entity.Phantom`. Get/set size via `getSize()` / `setSize(int)`.
*   **Minigame Idea:** **"Night Patrol"**: Players use bows to shoot down oversized Phantoms in a pitch-black sky.

### 9. Zombie Horse
*   **Visual Profile:** A decaying horse with dark-green skin, exposed ribcage, and hollow eyes.
*   **Behavior:** Completely passive in 26.1; does not panic when damaged.
*   **API Control:** `org.bukkit.entity.ZombieHorse`.
*   **Minigame Idea:** **"Apocalypse Joust"**: Players ride Zombie Horses in an arena, utilizing their lack of panic for precision attacks.

### 10. Skeleton Horse
*   **Visual Profile:** A horse skeleton made of white bones with red glowing eyes.
*   **Behavior:** Can swim on the ocean floor without drowning. Can be ridden by skeleton trap riders.
*   **API Control:** `org.bukkit.entity.SkeletonHorse`.
*   **Minigame Idea:** **"Sunken Raceway"**: A race course placed entirely underwater on the seabed where players ride Skeleton Horses.

### 11. Giant
*   **Visual Profile:** An enormous, 12-block-tall zombie.
*   **Behavior:** Unused in vanilla; does not have AI by default.
*   **API Control:** `org.bukkit.entity.Giant`. Toggle AI using `setAI(boolean)`.
*   **Minigame Idea:** **"Attack on Giant"**: Players use grapple hooks (fishing rods) to scale a moving Giant and strike its head block to deal damage.

### 12. Zombified Piglin
*   **Visual Profile:** A pig-man humanoid with half-rotted green bones and tattered clothes, wielding a golden sword.
*   **Behavior:** Neutral. Aggroing one alerts all Zombified Piglins in a large radius.
*   **API Control:** `org.bukkit.entity.PigZombie`.
*   **Minigame Idea:** **"Rage Trigger"**: Players navigate a maze packed with PigZombies; they must avoid hitting them while dealing with other hazards.

### 13. Zoglin
*   **Visual Profile:** A decaying, green-skinned hoglin with hollow eyes.
*   **Behavior:** Hostile to all living things except other Zoglins and Creepers.
*   **API Control:** `org.bukkit.entity.Zoglin`.
*   **Minigame Idea:** **"Zoglin Rampage"**: Players guide a hostile Zoglin through a village of target dummies by using bait, trying to knock down all targets.

---

## 2. Nether Monsters

### 14. Blaze
*   **Visual Profile:** A floating head made of golden rods rotating around a central smoke core.
*   **Behavior:** Hovers and shoots fireballs. Hurt by water.
*   **API Control:** `org.bukkit.entity.Blaze`.
*   **Minigame Idea:** **"Blaze Barrage"**: Players stand on floating platforms and must deflect fireballs back at Blazes using shields.

### 15. Ghast
*   **Visual Profile:** A massive, floating white jellyfish-like cube that shoots explosive fireballs.
*   **Behavior:** Fires explosive projectiles at target lines.
*   **API Control:** `org.bukkit.entity.Ghast`.
*   **Minigame Idea:** **"Dodgeball Sky"**: Players shoot Ghast fireballs at each other using bow hits to redirect the projectile.

### 16. Magma Cube
*   **Visual Profile:** A dark-grey magma block with orange glowing seams that splits into smaller segments when jumping.
*   **Behavior:** Jumps and splits into smaller cubes upon death. Immune to fire and fall damage.
*   **API Control:** `org.bukkit.entity.MagmaCube`. Get/set size with `getSize()`.
*   **Minigame Idea:** **"Magma Split"**: Players must destroy a giant Magma Cube and handle the explosive multiplier of smaller ones.

### 17. Hoglin
*   **Visual Profile:** A large, aggressive tusked beast with a red-brown hide.
*   **Behavior:** Hostile. Attacks knock players high into the air. Scared of warped fungi.
*   **API Control:** `org.bukkit.entity.Hoglin`.
*   **Minigame Idea:** **"Launchpad Bounce"**: Players use Hoglin knockback to reach high platforms and chests.

### 18. Piglin
*   **Visual Profile:** A pig-like humanoid wearing leather clothes, carrying a crossbow or gold sword.
*   **Behavior:** Hostile unless the player wears gold armor. Barters gold items.
*   **API Control:** `org.bukkit.entity.Piglin`.
*   **Minigame Idea:** **"Gold Barter Rush"**: Players must quickly barter gold ingots to get items required to solve a puzzle.

### 19. Piglin Brute
*   **Visual Profile:** A muscular, gold-plated Piglin carrying a golden axe.
*   **Behavior:** Always hostile; ignores gold armor. Immune to bartering.
*   **API Control:** `org.bukkit.entity.PiglinBrute`.
*   **Minigame Idea:** **"Brute Fortress"**: Players sneak through a piglin bastion collecting treasure while dodging hyper-aggressive Brutes.

---

## 3. Illager & Raid Monsters

### 20. Pillager
*   **Visual Profile:** A grey-skinned villager variant carrying a wooden crossbow.
*   **Behavior:** Attacks villages in raids.
*   **API Control:** `org.bukkit.entity.Pillager`.
*   **Minigame Idea:** **"Tower Defense Raid"**: Players build defenses to defend a villager outpost from Pillager waves.

### 21. Vindicator
*   **Visual Profile:** A grey-skinned villager wielding an iron axe.
*   **Behavior:** Sprints at targets to deal massive melee damage.
*   **API Control:** `org.bukkit.entity.Vindicator`. Set if it is aggressive/johnny mode via custom NBT or metadata.
*   **Minigame Idea:** **"Maze Runner"**: Players navigate a narrow maze while being chased by fast-moving Vindicators.

### 22. Evoker
*   **Visual Profile:** A robed Illager that summons Vexes and Evoker Fangs from the ground.
*   **Behavior:** Casts spells. Converts blue sheep to red sheep (easter egg).
*   **API Control:** `org.bukkit.entity.Evoker`.
*   **Minigame Idea:** **"Fang Dodge"**: An arena floor where players must dodge circular patterns of Evoker Fangs.

### 23. Ravager
*   **Visual Profile:** A massive, four-legged armored beast with bull horns.
*   **Behavior:** Rams blocks, breaking leaves and crops. Deals huge knockback.
*   **API Control:** `org.bukkit.entity.Ravager`.
*   **Minigame Idea:** **"Bull Run"**: Players are placed in a destructible arena with a Ravager; they must bait the beast to smash pillars to reveal keys.

### 24. Vex
*   **Visual Profile:** A tiny, flying grey imp-like spirit carrying an iron sword.
*   **Behavior:** Can fly through solid blocks to attack.
*   **API Control:** `org.bukkit.entity.Vex`. Get/set life ticks via `getLifeTicks()`.
*   **Minigame Idea:** **"Spirit Hunt"**: A parkour map where players hunt fast-flying Vexes that phase through walls.

### 25. Illusioner
*   **Visual Profile:** A blue-robed Illager that shoots a bow, blinds targets, and creates copy projections of itself.
*   **Behavior:** Unused in vanilla survival. Blinds the player and shoots arrows.
*   **API Control:** `org.bukkit.entity.Illusioner`.
*   **Minigame Idea:** **"Mirage Duel"**: Players fight an Illusioner and must find the real entity among the moving holograms.

---

## 4. Ocean Monsters

### 26. Guardian
*   **Visual Profile:** A blocky, spiked orange-and-teal fish with a single central eye.
*   **Behavior:** Shoots a laser beam at players and squid. Retractable spikes deal damage when extended.
*   **API Control:** `org.bukkit.entity.Guardian`.
*   **Minigame Idea:** **"Laser Run"**: Swimming through a temple avoiding Guardian lasers using speed rings.

### 27. Elder Guardian
*   **Visual Profile:** A giant, pale-grey Guardian variant.
*   **Behavior:** Inflicts Mining Fatigue III on nearby players. Shoots powerful lasers.
*   **API Control:** `org.bukkit.entity.ElderGuardian`.
*   **Minigame Idea:** **"Mine Fatigue Hunt"**: Players must find and defeat the Elder Guardian inside a maze before their oxygen runs out.

---

## 5. Cave & Arthropod Monsters

### 28. Spider
*   **Visual Profile:** A large, black eight-legged arachnid with red glowing eyes.
*   **Behavior:** Climbs vertical walls. Hostile in low light levels.
*   **API Control:** `org.bukkit.entity.Spider`.
*   **Minigame Idea:** **"Spider Climb"**: Players climb a high tower while Spiders climb after them, threatening to knock them off.

### 29. Cave Spider
*   **Visual Profile:** A smaller, teal-colored spider found in mineshafts.
*   **Behavior:** Inflicts Poison on hit. Climbs walls. Fits through 0.5-block gaps.
*   **API Control:** `org.bukkit.entity.CaveSpider`.
*   **Minigame Idea:** **"Toxic Trench"**: Navigating a narrow tunnel web-field where cave spiders bite and poison players.

### 30. Silverfish
*   **Visual Profile:** A tiny, grey insectoid mob that spawns from infested stone blocks.
*   **Behavior:** Summons other nearby Silverfish when damaged.
*   **API Control:** `org.bukkit.entity.Silverfish`.
*   **Minigame Idea:** **"Minefield Excavation"**: Digging stone blocks to find gold while avoiding triggering a massive Silverfish swarm.

### 31. Endermite
*   **Visual Profile:** A purple, crawling void pest emitting purple portal particles.
*   **Behavior:** Spawns sometimes when throwing Ender Pearls. Attracts Endermen aggro.
*   **API Control:** `org.bukkit.entity.Endermite`.
*   **Minigame Idea:** **"Ender Pearl Tag"**: Throwing pearls to spawn Endermites, using them to guide/lure angry Endermen to opponent bases.

### 32. Shulker
*   **Visual Profile:** A purple shell block that opens to reveal a small yellow head.
*   **Behavior:** Fires heat-seeking Levitation bullets. Clings to walls.
*   **API Control:** `org.bukkit.entity.Shulker`. Get/set shell color via `getColor()`.
*   **Minigame Idea:** **"Levitation Climb"**: Getting hit by Shulker bullets on purpose to float up through a vertical obstacle course.

---

## 6. Eldritch & Boss Monsters

### 33. Ender Dragon
*   **Visual Profile:** A massive black dragon with purple eyes, residing in The End.
*   **Behavior:** Flies, breaks blocks, shoots dragon breath, and heals from End Crystals.
*   **API Control:** `org.bukkit.entity.EnderDragon`.
*   **Minigame Idea:** **"Dragon Wing Flight"**: Flying around the dragon using Elytras, destroying crystals, and landing critical bow shots.

### 34. Wither
*   **Visual Profile:** A three-headed black skeletal boss that floats and shoots explosive skulls.
*   **Behavior:** Attacks all living things. Explodes upon spawning. Inflicts Wither effect.
*   **API Control:** `org.bukkit.entity.Wither`.
*   **Minigame Idea:** **"Wither Siege"**: Co-op boss battle where players defend a central structure from Wither skulls.

### 35. Warden
*   **Visual Profile:** A huge, blind blue-black subterranean beast with rib-like chest plates and glowing antlers.
*   **Behavior:** Blind. Reacts to vibrations (sound, movement). Fires sonic booms.
*   **API Control:** `org.bukkit.entity.Warden`. Get/set anger levels via `Warden#getAnger(Entity)`.
*   **Minigame Idea:** **"Quiet Please"**: Players must sneak through a dark wool-covered corridor to steal items without alerting the Warden.

---

## 7. Trial Chambers & Pale Garden Monsters (1.21 / 1.21.3)

### 36. Breeze (1.21)
*   **Visual Profile:** A wind elemental creature surrounded by blue-purple wind spirals.
*   **Behavior:** Jumps fast. Shoots wind charges that deal knockback and trigger redstone blocks (buttons, levers).
*   **API Control:** `org.bukkit.entity.Breeze`.
*   **Minigame Idea:** **"Wind Charge Football"**: A sports game where players use wind charges or are pushed by a Breeze to direct a ball into a goal.

### 37. Bogged (1.21)
*   **Visual Profile:** A mossy, mushroom-covered skeleton variant found in swamps and trial chambers.
*   **Behavior:** Shoots poisoned arrows. Drops mushrooms when sheared.
*   **API Control:** `org.bukkit.entity.Bogged`.
*   **Minigame Idea:** **"Bog Jump"**: Players parkour across swamp pads while dodging Bogged poison arrows.

### 38. Creaking (1.21.3)
*   **Visual Profile:** A wooden, branch-like monster with glowing orange eyes, spawning in the Pale Garden biome.
*   **Behavior:** Bound to a Creaking Heart block. Only moves when players look away. Invulnerable to direct weapon damage.
*   **API Control:** `org.bukkit.entity.Creaking`.
*   **Minigame Idea:** **"Don't Blink"**: Players navigate a dark forest filled with Creakings; they must walk backwards or keep eyes on them while searching for their hearts.

---

## 8. Neutral & Defensive Monsters

### 39. Iron Golem
*   **Visual Profile:** A huge, white iron giant with a red flower in its hand.
*   **Behavior:** Passive to villagers and players (unless attacked). Throws hostile targets high.
*   **API Control:** `org.bukkit.entity.IronGolem`.
*   **Minigame Idea:** **"Golem Defense"**: Players team up with Iron Golems to hold off waves of zombies.

### 40. Snow Golem
*   **Visual Profile:** A snowman with a pumpkin head.
*   **Behavior:** Throws snowballs at hostile mobs. Leaves a snow trail on the ground.
*   **API Control:** `org.bukkit.entity.Snowman`.
*   **Minigame Idea:** **"Snow Paint"**: Snow Golems color the arena; players must direct them to paint the largest area.

### 41. Creeper
*   **Visual Profile:** A green, armless creature with a sad face.
*   **Behavior:** Sneaks up on players and explodes after a brief hiss.
*   **API Control:** `org.bukkit.entity.Creeper`. Check/set charged state with `isPowered()`.
*   **Minigame Idea:** **"Creep Defuse"**: Players use shears to defuse creepers right before they explode.

### 42. Enderman
*   **Visual Profile:** A tall, purple-eyed dark humanoid that teleports and picks up blocks.
*   **Behavior:** Hostile if looked in the eyes. Teleports away from water and projectiles.
*   **API Control:** `org.bukkit.entity.Enderman`. Get/set carried block with `getCarriedBlock()`.
*   **Minigame Idea:** **"Ender Block Stack"**: Players must block-trade with Endermen to construct a tower.

---

## 9. Exotic & Neutral Animals (Attacking Behaviors)

### 43. Polar Bear
*   **Visual Profile:** A large white bear.
*   **Behavior:** Attacks if a player gets too close to its cub.
*   **API Control:** `org.bukkit.entity.PolarBear`.
*   **Minigame Idea:** **"Bear Clan"**: Players navigate an ice field of polar bears without triggering cub aggression.

### 44. Goat
*   **Visual Profile:** A white, bearded mountain goat.
*   **Behavior:** Rams stationary players or mobs, causing massive knockback.
*   **API Control:** `org.bukkit.entity.Goat`.
*   **Minigame Idea:** **"Goat Launch"**: Using goat ramming vectors to launch players onto high platforms.

### 45. Llama
*   **Visual Profile:** A long-necked woolly pack animal.
*   **Behavior:** Spits at attackers, dealing minor damage.
*   **API Control:** `org.bukkit.entity.Llama`.
*   **Minigame Idea:** **"Spit Battle"**: Riding llamas and using their spits to knock opponents off narrow ledges.

### 46. Bee
*   **Visual Profile:** A fuzzy yellow-black flying insect.
*   **Behavior:** Attacks in swarms if their hive is broken, applying poison and losing their stinger.
*   **API Control:** `org.bukkit.entity.Bee`.
*   **Minigame Idea:** **"Honey Heist"**: Players steal honey from hives while using smoke (campfires) to keep bees pacified.

### 47. Dolphin
*   **Visual Profile:** A grey aquatic mammal.
*   **Behavior:** Attacks in packs if hit. Leads players to treasure.
*   **API Control:** `org.bukkit.entity.Dolphin`.
*   **Minigame Idea:** **"Dolphin Race"**: Speed swimming using Dolphin's Grace to escape a pack of angry dolphins.

### 48. Wolf
*   **Visual Profile:** A grey, canine animal.
*   **Behavior:** Becomes aggressive in packs (red eyes) if attacked. Can be tamed.
*   **API Control:** `org.bukkit.entity.Wolf`.
*   **Minigame Idea:** **"Wolf Pack Hunt"**: Players tame wolves to hunt monsters in a dark forest.

### 49. Panda
*   **Visual Profile:** A large black-and-white bear.
*   **Behavior:** Aggressive variants will attack players if provoked.
*   **API Control:** `org.bukkit.entity.Panda`.
*   **Minigame Idea:** **"Angry Panda Escape"**: Navigating a bamboo maze where aggressive pandas patrol.

---

## 10. Custom & Special Variant Monsters (26.1 Runtime)

### 50. Copper Golem
*   **Visual Profile:** A small, metallic golem made of copper blocks with a lightning rod head.
*   **Behavior:** Attracted to copper buttons. Oxidizes over time until it freezes into a copper statue.
*   **API Control:** Handled via custom entity models or NMS.
*   **Minigame Idea:** **"Button Presser"**: Copper Golems run around pressing random buttons; players must complete tasks linked to the activated buttons.

### 51. Zombie Nautilus
*   **Visual Profile:** A drowned zombie wearing a glowing nautilus shell over its head.
*   **Behavior:** Immune to fire. Can breathe underwater. Drops Nautilus Shells.
*   **API Control:** Handled as a custom variant of `Drowned`.
*   **Minigame Idea:** **"Shell Collector"**: Players hunt Zombie Nautiluses in deep sea trenches.

### 52. Parched
*   **Visual Profile:** A dry, dark-orange variant of the Husk spawning in dry biomes.
*   **Behavior:** Moves fast. Deals fire damage on hit.
*   **API Control:** Handled via Husk custom variants.
*   **Minigame Idea:** **"Parched Desert"**: Surviving waves of fire-inflicting Parched Husks in a collapsing arena.
