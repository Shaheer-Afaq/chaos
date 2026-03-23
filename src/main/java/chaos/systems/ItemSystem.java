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
        ItemSystemTick = TaskScheduler.schedule(ItemSystem::ItemSystemTick, 120, -1, true, null);
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
        else if (roll < 0.8){
            giveItem(ItemType.Utility);
        }
    }

    public static void giveItem(ItemType itemType){
        List<ItemStack> list = items.get(itemType);
        TaskScheduler.schedule((x)->{
            if (x!=3){
                for (UUID uuid: activePlayers) {
                    if (state != GameState.RUNNING){break;}
                    getPlayer(uuid).sendMessage(Text.literal("Random " + itemType.toString() + " in " + (3 - x)).formatted(Formatting.GREEN).formatted(Formatting.BOLD), false);
                }
            }
        }, 20, 4, true, ()->{
            for (UUID uuid: activePlayers){
                if (state != GameState.RUNNING){break;}
                ServerPlayerEntity player = getPlayer(uuid);
                ItemStack randomItem = list.get(player.getRandom().nextInt(list.size())).copy();
                player.sendMessage(Text.literal("---------------------------").formatted(Formatting.GOLD));
                player.sendMessage(Text.literal("You got ").formatted(Formatting.BOLD).formatted(Formatting.GOLD).append(randomItem.getName()).append(" !"));
                player.sendMessage(Text.literal("---------------------------").formatted(Formatting.GOLD));

                if (!player.getInventory().insertStack(randomItem)) { player.dropItem(randomItem, false);}

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
                .enchant(Enchantments.SHARPNESS, 20)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.DIAMOND_AXE, 1)
                .name("Fast Axe", Formatting.GOLD)
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                        .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(Identifier.of("chaos", "attackspeed"),
                                -2.4, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                        .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(Identifier.of("chaos", "attackdamage"),
                                8.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .build())
                .build()
        );

        weapons.add(new ItemBuilder(Items.IRON_HOE, 1)
                .name("Yeet Stick", Formatting.GREEN)
                .enchant(Enchantments.KNOCKBACK, 8)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.DIAMOND_SWORD, 1)
                .name("Long Sword", Formatting.AQUA)
                .enchant(Enchantments.SHARPNESS, 1)
                .component(DataComponentTypes.ATTACK_RANGE, new AttackRangeComponent(0.1f, 8.0f, 0.1f, 12.0f, 0.0f, 1.0f))
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.NETHERITE_SWORD, 1)
                .name("Fiery Wrath", Formatting.DARK_RED)
                .enchant(Enchantments.FIRE_ASPECT, 8)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.MACE, 1)
                .name("Bouncy Hammer", Formatting.LIGHT_PURPLE)
                .enchant(Enchantments.WIND_BURST, 10)
                .enchant(Enchantments.DENSITY, 2)
                .maxDura(15)
                .build());

        weapons.add(new ItemBuilder(Items.MACE, 1)
                .name("Skull Crusher", Formatting.WHITE)
                .enchant(Enchantments.DENSITY, 10)
                .maxDura(15)
                .build());

        weapons.add(new ItemBuilder(Items.MACE, 1)
                .name("Armor Shredder", Formatting.DARK_BLUE)
                .enchant(Enchantments.BREACH, 5)
                .maxDura(15)
                .build());

        weapons.add(new ItemBuilder(Items.NETHERITE_SPEAR, 1)
                .name("Lunger", Formatting.BLUE)
                .enchant(Enchantments.LUNGE, 8)
                .maxDura(30)
                .build());

        weapons.add(new ItemBuilder(Items.BOW, 1)
                .name("God Bow", Formatting.RED)
                .enchant(Enchantments.PUNCH, 3)
                .enchant(Enchantments.FLAME, 1)
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
                .component(DataComponentTypes.FIREWORKS, new FireworksComponent(3, List.of(explosion, explosion, explosion, explosion, explosion)))
                .build();
        weapons.add(new ItemBuilder(Items.CROSSBOW, 1)
                .name("Grenade Launcher", Formatting.DARK_AQUA)
                .maxDura(1)
                .component(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(List.of(rocket, rocket, rocket)))
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
                .build()
        );
        consumables.add(new ItemBuilder(Items.SPLASH_POTION, 1)
                .name("Wings", Formatting.AQUA)
                .stackable(8)
                .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0xb7b7b7),
                            List.of(new StatusEffectInstance(StatusEffects.LEVITATION, 10 * 20, 1), new StatusEffectInstance(StatusEffects.LEVITATION, 10 * 20, 1)),
                        Optional.empty()))
                .build()
        );
        consumables.add(new ItemBuilder(Items.POTION, 1)
                .name("Warrior's drink", Formatting.YELLOW)
                .stackable(8)
                .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0xc44100),
                            List.of(
                                new StatusEffectInstance(StatusEffects.STRENGTH, 25 * 20, 1),
                                new StatusEffectInstance(StatusEffects.SPEED, 30 * 20, 0),
                                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 30 * 20, 0)
                            ), Optional.empty()))
                .build()
        );
        consumables.add(new ItemBuilder(Items.SPLASH_POTION, 1)
                .name("Turtle soup", Formatting.DARK_GREEN)
                .stackable(8)
                .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0x0a9600),
                            List.of(
                                new StatusEffectInstance(StatusEffects.RESISTANCE, 10 * 20, 2)
                            ), Optional.empty()))
                .build()
        );
        consumables.add(new ItemBuilder(Items.SPLASH_POTION, 1)
                .name("Tears of Ghast", Formatting.LIGHT_PURPLE)
                .stackable(8)
                .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0xff4787),
                            List.of(
                                new StatusEffectInstance(StatusEffects.REGENERATION, 5 * 20, 0)
                            ), Optional.empty()))
                .build()
        );
    }
    public static void addUtilities(){
        List<ItemStack> utilities = items.get(ItemType.Utility);

        utilities.add(new ItemBuilder(Items.SHIELD, 1).name("Shield", Formatting.YELLOW).maxDura(25).build());
        utilities.add(new ItemBuilder(Items.ARROW, 8).name("Arrow", Formatting.BLUE).build());
        utilities.add(new ItemBuilder(Items.COBWEB, 8).name("Spider Web", Formatting.DARK_GRAY).build());
        utilities.add(new ItemBuilder(Items.HORSE_SPAWN_EGG, 1)
                .name("Warhorse", Formatting.BLACK)
                .entityData(EntityType.HORSE, tags -> {
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
                .name("Bomb", Formatting.BLACK)
                .build());
    }
    public static void addArmor(){
        List<ItemStack> armors = items.get(ItemType.Armor);

        armors.add(new ItemBuilder(Items.IRON_BOOTS, 1)
                .name("Roller Skates",  Formatting.DARK_GRAY)
                .desc("Doubles movement speed", Formatting.BLUE)
                .enchant(Enchantments.PROTECTION, 3)
                .attribute(EntityAttributes.MOVEMENT_SPEED, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.FEET)
                .maxDura(15)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_HELMET, 1)
                .name("God Helmet",  Formatting.RED)
                .enchant(Enchantments.PROTECTION, 5)
                .attribute(EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.HEAD)
                .maxDura(20)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_CHESTPLATE, 1)
                .name("God Chestplate",  Formatting.RED)
                .enchant(Enchantments.PROTECTION, 5)
                .attribute(EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.CHEST)
                .maxDura(20)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_CHESTPLATE, 1)
                .name("God Chestplate",  Formatting.RED)
                .enchant(Enchantments.PROTECTION, 5)
                .attribute(EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.CHEST)
                .maxDura(20)
                .build());

        armors.add(new ItemBuilder(Items.NETHERITE_CHESTPLATE, 1)
                .name("God Chestplate",  Formatting.RED)
                .enchant(Enchantments.PROTECTION, 5)
                .attribute(EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, AttributeModifierSlot.CHEST)
                .maxDura(20)
                .build());


    }

}
