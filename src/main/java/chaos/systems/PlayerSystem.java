package chaos.systems;

import chaos.util.TaskScheduler;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


import java.util.*;

import static chaos.game.GameManager.*;
import static chaos.util.HelperMethods.applyEnchantment;
import static chaos.util.HelperMethods.setCustomName;

public class PlayerSystem {
    private static TaskScheduler.ScheduledTask weaponTask;
    public static final List<ItemStack> Weapons = new ArrayList<>();
    public static final List<ItemStack> Utilities = new ArrayList<>();
    public static final List<ItemStack> Misc = new ArrayList<>();
    public static final List<ItemStack> Abilities = new ArrayList<>();

    public static void populateLists(){
        var registry = Server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        Weapons.add(new ItemBuilder(Items.NETHERITE_SWORD, registry)
                        .name("Sharp Sword", Formatting.BLACK)
                        .enchant(Enchantments.SHARPNESS, 10)
                        .enchant(Enchantments.UNBREAKING, 3)
                        .component(DataComponentTypes.ATTACK_RANGE, new AttackRangeComponent(0.0f, 10.0f, 0.0f, 10.0f, 0.0f, 1.0f))
                        .build());

//        ItemStack Sword = new ItemStack(Items.NETHERITE_SWORD);
//        Sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sword"));
//        applyEnchantment(Sword, registry, Enchantments.SHARPNESS, 10);
//        applyEnchantment(Sword, registry, Enchantments.UNBREAKING, 3);
//        Weapons.add(Sword);
//
//
//        ItemStack KnockbackSword = new ItemStack(Items.IRON_SWORD);
//        setCustomName(KnockbackSword, "Knockback Sword");
//        applyEnchantment(KnockbackSword, registry, Enchantments.KNOCKBACK, 5);
//        applyEnchantment(KnockbackSword, registry, Enchantments.UNBREAKING, 3);
//        Weapons.add(KnockbackSword);
//
//        ItemStack Mace = new ItemStack(Items.MACE);
//        setCustomName(Mace, "Skull Crusher");
//        applyEnchantment(Mace, registry, Enchantments.DENSITY, 5);
//        applyEnchantment(Mace, registry, Enchantments.WIND_BURST, 10);
//        Weapons.add(Mace);
    }

    public static void start(MinecraftServer server){
        weaponTask = TaskScheduler.schedule(PlayerSystem::changeWeapon, 10*20, -1, true, null);
    }
    public static void stop(){
        TaskScheduler.remove(weaponTask);
    }

    public static void changeWeapon(int currentRun){
        for (ServerPlayerEntity player : activePlayers) {
            System.out.println(player.getName().getString().concat(" Changed weapon"));

            ItemStack randomWeapon = Weapons.get(player.getRandom().nextInt(Weapons.size())).copy();
            PlayerInventory inv = player.getInventory();

            inv.setStack(inv.getEmptySlot(), randomWeapon);

            player.sendMessage(Text.literal("You got ".concat(Objects.requireNonNull(randomWeapon.getCustomName()).getString().concat(" !"))).formatted(Formatting.GOLD));
            getData(player).messages.set(0, "Weapon: ".concat(randomWeapon.getCustomName().getString()));
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
