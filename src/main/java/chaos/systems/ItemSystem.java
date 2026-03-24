package chaos.systems;

import chaos.util.TaskScheduler;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPatterns;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;


import java.util.*;

import static chaos.game.GameManager.*;
import static chaos.util.HelperMethods.getPlayer;
import static chaos.util.HelperMethods.sendSound;

public class ItemSystem {
    private static TaskScheduler.ScheduledTask ItemSystemTick;

    public enum ItemType{Weapon, Consumable, Utility, Armor}
    public static final Map<ItemType, List<ItemStack>> items = new HashMap<>();
    static {
        for (ItemType type : ItemType.values()) {
            items.put(type, new ArrayList<>());
        }
    }

    public static void start(){
        ItemSystemTick = TaskScheduler.schedule(ItemSystem::ItemSystemTick, 100, -1, true, null);
    }
    public static void stop(){ TaskScheduler.remove(ItemSystemTick);}

    public static void ItemSystemTick(int currentRun){
        double roll = Math.random();

        if (roll < 0.2) {
            giveItem(ItemType.Armor);
        }
        else if (roll < 0.4) {
            giveItem(ItemType.Weapon);
        }
        else if (roll < 0.6) {
            giveItem(ItemType.Consumable);
        }
        else if (roll < 0.9){
            giveItem(ItemType.Utility);
        }
    }

    public static void giveItem(ItemType itemType) {
        List<ItemStack> shuffledList = new ArrayList<>(items.get(itemType));
        Collections.shuffle(shuffledList);

        TaskScheduler.schedule((x) -> {
            if (x != 3) {
                for (UUID uuid : activePlayers) {
                    if (state != GameState.RUNNING) break;
                    ServerPlayerEntity player = getPlayer(uuid);
                    if (player != null) {
                        player.sendMessage(Text.literal("Random " + itemType.toString() + " in " + (3 - x))
                                .formatted(Formatting.GREEN, Formatting.BOLD), false);
                    }
                }
            }
        }, 20, 4, true, () -> {

            for (UUID uuid : activePlayers) {
                if (state != GameState.RUNNING) break;
                ServerPlayerEntity player = getPlayer(uuid);
                if (player == null) continue;

                ItemStack randomItem = shuffledList.get(new Random().nextInt(shuffledList.size())).copy();

                player.sendMessage(Text.literal("---------------------------").formatted(Formatting.GOLD));
                player.sendMessage(Text.literal("You got ").formatted(Formatting.BOLD, Formatting.GOLD)
                        .append(randomItem.getName()).append(" !"));
                player.sendMessage(Text.literal("---------------------------").formatted(Formatting.GOLD));

                if (!player.getInventory().insertStack(randomItem)) {
                    player.dropItem(randomItem, false);
                }

                sendSound(player, SoundEvents.BLOCK_TRIAL_SPAWNER_EJECT_ITEM);
            }
        });
    }

    public static void populateLists(){
        addWeapons();
        addConsumables();
        addUtilities();
        addArmor();
    }

    public static void addWeapons(){
        List<ItemStack> weapons = items.get(ItemType.Weapon);
        weapons.add(new ItemBuilder(Items.IRON_SWORD, 1)
                .name("Katana", Formatting.GRAY)
                .withEnchant(Enchantments.SHARPNESS, 20)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.DIAMOND_AXE, 1)
                .name("Fast Axe", Formatting.GOLD)
                .desc("Swing axe with the speed of sword", Formatting.BLUE)
                .maxDura(30)
                .withComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                        .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(Identifier.of("chaos", "attackspeed"),
                                -2.4, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                        .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(Identifier.of("chaos", "attackdamage"),
                                8.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .build())
                .build()
        );

        weapons.add(new ItemBuilder(Items.IRON_HOE, 1)
                .name("Yeet Stick", Formatting.GREEN)
                .withEnchant(Enchantments.KNOCKBACK, 5)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.DIAMOND_SWORD, 1)
                .name("Long Sword", Formatting.AQUA)
                .withAttribute(EntityAttributes.ENTITY_INTERACTION_RANGE, 2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.MAINHAND)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.NETHERITE_SWORD, 1)
                .name("Fiery Wrath", Formatting.DARK_RED)
                .withEnchant(Enchantments.FIRE_ASPECT, 8)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.MACE, 1)
                .name("Bouncy Hammer", Formatting.LIGHT_PURPLE)
                .withEnchant(Enchantments.WIND_BURST, 10)
                .withEnchant(Enchantments.DENSITY, 2)
                .maxDura(15)
                .build());

        weapons.add(new ItemBuilder(Items.MACE, 1)
                .name("Skull Crusher", Formatting.WHITE)
                .withEnchant(Enchantments.DENSITY, 10)
                .maxDura(15)
                .build());

        weapons.add(new ItemBuilder(Items.MACE, 1)
                .name("Armor Shredder", Formatting.DARK_BLUE)
                .withEnchant(Enchantments.BREACH, 5)
                .maxDura(15)
                .build());

        weapons.add(new ItemBuilder(Items.NETHERITE_SPEAR, 1)
                .name("Lunger", Formatting.BLUE)
                .withEnchant(Enchantments.LUNGE, 8)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.BOW, 1)
                .name("God Bow", Formatting.RED)
                .withEnchant(Enchantments.PUNCH, 5)
                .withEnchant(Enchantments.FLAME, 1)
                .maxDura(15)
                .build());

        FireworkExplosionComponent explosion = new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.BURST,
                IntList.of(0x545454),
                IntList.of(0x424242),
                true,
                false
        );
        ItemStack rocket = new ItemBuilder(Items.FIREWORK_ROCKET, 1)
                .withComponent(DataComponentTypes.FIREWORKS, new FireworksComponent(3, List.of(explosion, explosion, explosion, explosion, explosion)))
                .build();
        weapons.add(new ItemBuilder(Items.CROSSBOW, 1)
                .name("Grenade Launcher", Formatting.DARK_AQUA)
                .maxDura(1)
                .withComponent(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(List.of(rocket, rocket, rocket)))
                .build());
    }
    public static void addConsumables(){
        List<ItemStack> consumables = items.get(ItemType.Consumable);
        consumables.add(new ItemBuilder(Items.WIND_CHARGE, 4)
                .name("Breeze Balls", Formatting.AQUA)
                .build()
        );
        consumables.add(new ItemBuilder(Items.ENDER_PEARL, 4)
                .name("Ender Pearl", Formatting.GREEN)
                        .setStackSize(64)
                .build()
        );
        consumables.add(new ItemBuilder(Items.SPLASH_POTION, 1)
                .name("Wings", Formatting.AQUA)
                .setStackSize(16)
                .withComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0xb7b7b7),
                            List.of(new StatusEffectInstance(StatusEffects.LEVITATION, 10 * 20, 1), new StatusEffectInstance(StatusEffects.SPEED, 10 * 20, 3)),
                        Optional.empty()))
                .build()
        );
        consumables.add(new ItemBuilder(Items.POTION, 1)
                .name("Warrior's drink", Formatting.YELLOW)
                .setStackSize(16)
                .withComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0xc44100),
                            List.of(
                                new StatusEffectInstance(StatusEffects.STRENGTH, 25 * 20, 1),
                                new StatusEffectInstance(StatusEffects.SPEED, 30 * 20, 0),
                                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 30 * 20, 0)
                            ), Optional.empty()))
                .build()
        );
        consumables.add(new ItemBuilder(Items.SPLASH_POTION, 1)
                .name("Turtle soup", Formatting.DARK_GREEN)
                .setStackSize(16)
                .withComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0x0a9600),
                            List.of(
                                new StatusEffectInstance(StatusEffects.RESISTANCE, 10 * 20, 2)
                            ), Optional.empty()))
                .build()
        );
        consumables.add(new ItemBuilder(Items.SPLASH_POTION, 1)
                .name("Tears of Ghast", Formatting.LIGHT_PURPLE)
                .setStackSize(16)
                .withComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0xff4787),
                            List.of(
                                new StatusEffectInstance(StatusEffects.REGENERATION, 5 * 20, 0)
                            ), Optional.empty()))
                .build()
        );
    }
    public static void addUtilities(){
        List<ItemStack> utilities = items.get(ItemType.Utility);
//
        utilities.add(new ItemBuilder(Items.SHIELD, 1).name("Shield", Formatting.YELLOW).maxDura(25).build());
        utilities.add(new ItemBuilder(Items.ARROW, 8).name("Arrow", Formatting.BLUE).build());
        utilities.add(new ItemBuilder(Items.COBWEB, 8).name("Spider Web", Formatting.WHITE).build());
        utilities.add(new ItemBuilder(Items.WATER_BUCKET, 1).name("Bucket of Water", Formatting.AQUA).build());
        utilities.add(new ItemBuilder(Items.COBBLESTONE, 16).name("Cobblestone", Formatting.YELLOW).build());
        utilities.add(new ItemBuilder(Items.HORSE_SPAWN_EGG, 1)
                .name("Warhorse", Formatting.GOLD)
                .withEntityData(EntityType.HORSE, tags -> {
                    tags.putByte("Tame", (byte) 1);

                    NbtCompound equipment = new NbtCompound();

                    NbtCompound saddle = new NbtCompound();
                    saddle.putString("id", "minecraft:saddle");
                    saddle.putInt("count", 1);
                    equipment.put("saddle", saddle);

                    NbtCompound armor = new NbtCompound();
                    armor.putString("id", "minecraft:netherite_horse_armor");
                    armor.putInt("count", 1);
                    equipment.put("body", armor);

                    tags.put("equipment", equipment);

                    NbtList attributes = new NbtList();

                    NbtCompound maxHealth = new NbtCompound();
                    maxHealth.putString("id", "minecraft:max_health");
                    maxHealth.putDouble("base", 40.0);
                    attributes.add(maxHealth);

                    NbtCompound speed = new NbtCompound();
                    speed.putString("id", "minecraft:movement_speed");
                    speed.putDouble("base", 0.5);
                    attributes.add(speed);

                    NbtCompound jump_strength = new NbtCompound();
                    jump_strength.putString("id", "minecraft:jump_strength");
                    jump_strength.putDouble("base", 1);
                    attributes.add(jump_strength);

                    tags.put("attributes", attributes);

                    tags.putFloat("Health", 40.0f);
                })
                .build());
        utilities.add(new ItemBuilder(Items.CREEPER_SPAWN_EGG, 1)
                .name("Suicide Bomber", Formatting.DARK_RED)
                        .withEntityData(EntityType.CREEPER, nbt -> {
                            nbt.putBoolean("powered", true);
                            nbt.putBoolean("ignited", true);
                            nbt.putFloat("fuse", 100.0f);
                        })
                .build());

        utilities.add(new ItemBuilder(Items.TNT, 4)
                .name("Auto-ignite Bomb", Formatting.RED)
                .build());
    }
    public static void addArmor(){
        List<ItemStack> armors = items.get(ItemType.Armor);

        armors.add(new ItemBuilder(Items.IRON_BOOTS, 1)
                .name("Roller Skates",  Formatting.DARK_GRAY)
                .desc("Doubles movement speed", Formatting.BLUE)
                .withEnchant(Enchantments.PROTECTION, 3)
                .withAttribute(EntityAttributes.MOVEMENT_SPEED, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.FEET)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.NETHERITE)
                .maxDura(100)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_HELMET, 1)
                .name("God Helmet",  Formatting.RED)
                .withEnchant(Enchantments.PROTECTION, 4)
                .maxDura(80)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.GOLD)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_CHESTPLATE, 1)
                .name("God Chestplate",  Formatting.RED)
                .withEnchant(Enchantments.PROTECTION, 4)
                .maxDura(80)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.DIAMOND)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_CHESTPLATE, 1)
                .name("Tank Chestplate", Formatting.BLUE)
                .withEnchant(Enchantments.PROTECTION, 5)
                .withAttribute(EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.CHEST)
                .withAttribute(EntityAttributes.MOVEMENT_SPEED, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.CHEST)
                .maxDura(100)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.REDSTONE)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_LEGGINGS, 1)
                .name("God Leggings",  Formatting.RED)
                .withEnchant(Enchantments.PROTECTION, 4)
                .maxDura(80)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.DIAMOND)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_BOOTS, 1)
                .name("God Boots",  Formatting.RED)
                .withEnchant(Enchantments.PROTECTION, 4)
                .withEnchant(Enchantments.FEATHER_FALLING, 4)
                .withEnchant(Enchantments.DEPTH_STRIDER, 3)
                .maxDura(80)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.DIAMOND)
                .build());

        armors.add(new ItemBuilder(Items.IRON_LEGGINGS, 1)
                .name("Grasshopper Legs", Formatting.DARK_PURPLE)
                .withEnchant(Enchantments.PROTECTION, 3)
                        .withAttribute(EntityAttributes.JUMP_STRENGTH, 2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.LEGS)
                .maxDura(80)
                .withTrim(ArmorTrimPatterns.SILENCE, ArmorTrimMaterials.EMERALD)
                .build());


    }

}
