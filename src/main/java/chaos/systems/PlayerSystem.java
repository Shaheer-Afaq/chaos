package chaos.systems;

import chaos.util.TaskScheduler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


import java.util.*;

import static chaos.game.GameManager.*;

public class PlayerSystem {
    private static TaskScheduler.ScheduledTask weaponTask;
    public static final List<ItemStack> Weapons = new ArrayList<>();
    public static final List<ItemStack> Armor = new ArrayList<>();
    public static final List<ItemStack> Utilities = new ArrayList<>();
    public static final List<ItemStack> Misc = new ArrayList<>();
    public static final List<ItemStack> Abilities = new ArrayList<>();

    public static void populateLists(){
        var registry = getServer().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        Weapons.add(new ItemBuilder(Items.NETHERITE_SWORD, registry)
                .name("Sharp Sword", Formatting.DARK_RED)
                .enchant(Enchantments.SHARPNESS, 10)
                .unbreakable()
                .build());

        Weapons.add(new ItemBuilder(Items.DIAMOND_HOE, registry)
                .name("Knockback", Formatting.GREEN)
                .enchant(Enchantments.KNOCKBACK, 10)
                .unbreakable()
                .build());

        Weapons.add(new ItemBuilder(Items.DIAMOND_SWORD, registry)
                .name("Long Sword", Formatting.AQUA)
                .enchant(Enchantments.SHARPNESS, 2)
                .component(DataComponentTypes.ATTACK_RANGE, new AttackRangeComponent(0.1f, 8.0f, 0.1f, 12.0f, 0.0f, 1.0f))
                .unbreakable()
                .build());

        Weapons.add(new ItemBuilder(Items.MACE, registry)
                .name("Bouncy Hammer", Formatting.LIGHT_PURPLE)
                .enchant(Enchantments.WIND_BURST, 10)
                .enchant(Enchantments.DENSITY, 2)
                .unbreakable()
                .build());

        Weapons.add(new ItemBuilder(Items.MACE, registry)
                .name("Skull Crusher", Formatting.WHITE)
                .enchant(Enchantments.DENSITY, 10)
                .unbreakable()
                .build());

        Weapons.add(new ItemBuilder(Items.NETHERITE_SPEAR, registry)
                .name("Silver Skewer", Formatting.DARK_AQUA)
                .enchant(Enchantments.LUNGE, 10)
                .unbreakable()
                .build());

        Misc.add(new ItemBuilder(Items.HORSE_SPAWN_EGG, registry)
                .name("Warhorse", Formatting.GOLD)
                .entityData(EntityType.HORSE, tags -> {
                    tags.putByte("Tame", (byte) 1);

                    NbtCompound equipment = new NbtCompound();

                    NbtCompound saddle = new NbtCompound();
                    saddle.putString("id", "minecraft:saddle");
                    saddle.putInt("count", 1);
                    equipment.put("saddle", saddle);

                    NbtCompound armor = new NbtCompound();
                    armor.putString("id", "minecraft:diamond_horse_armor");
                    armor.putInt("count", 1);
                    equipment.put("body", armor);

                    tags.put("equipment", equipment);

                    NbtList attributes = new NbtList();

                    NbtCompound maxHealth = new NbtCompound();
                    maxHealth.putString("id", "minecraft:generic.max_health");
                    maxHealth.putDouble("base", 40.0);
                    attributes.add(maxHealth);

                    NbtCompound speed = new NbtCompound();
                    speed.putString("id", "minecraft:generic.movement_speed");
                    speed.putDouble("base", 0.5);
                    attributes.add(speed);

                    tags.put("attributes", attributes);

                    tags.putFloat("Health", 40.0f);
                })
                .build());
    }

    public static void start(){
        weaponTask = TaskScheduler.schedule(PlayerSystem::giveWeapon, 3*20, -1, true, null);
    }
    public static void stop(){TaskScheduler.remove(weaponTask);}

    public static void giveWeapon(int currentRun){
        for (ServerPlayerEntity player : activePlayers) {
//            System.out.println(player.getName().getString().concat("Changed weapon"));

//            ItemStack randomWeapon = Weapons.get(player.getRandom().nextInt(Weapons.size())).copy();
////            ItemStack randomMisc = Misc.get(player.getRandom().nextInt(Misc.size())).copy();
//            PlayerInventory inv = player.getInventory();
//
//            int slot = inv.getEmptySlot();
//            if (slot == -1) {
//                getWorld().spawnEntity(new ItemEntity(getWorld(), player.getX(), player.getY(), player.getZ(), randomWeapon));
//            } else {
//                inv.setStack(slot, randomWeapon);
//            }

            getServer().execute(()-> player.getInventory().setStack(0, Weapons.getFirst()));
//            inv.setStack(inv.getEmptySlot(), randomMisc);

//            player.sendMessage(Text.literal("You got ".concat(Objects.requireNonNull(randomWeapon.getCustomName()).getString().concat(" !"))).formatted(Formatting.GOLD));
//            getData(player).messages.set(0, "Weapon: ".concat(randomWeapon.getCustomName().getString()));
        }
    }

    public static void tick(){
        if (weaponTask !=null && weaponTask.ticksLeft <= 5*20 && weaponTask.ticksLeft % 20 == 0){
            for (ServerPlayerEntity player : activePlayers) {
                player.sendMessage(Text.literal("Random weapon in: ".concat(String.valueOf(weaponTask.ticksLeft/20))).formatted(Formatting.GREEN));
            }
        }
    }
}
