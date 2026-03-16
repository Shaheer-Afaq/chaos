package chaos.game;


import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GameConfig {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 20;

    public static final BlockPos LOBBY_POS = new BlockPos(0,107,0);
    public static final BlockPos ARENA_POS = new BlockPos(0,76,0);
    public static final BlockPos START_BUTTON = new BlockPos(4,108,0);

    public static final List<ItemStack> Weapons = new ArrayList<>();
    static {
        ItemStack Sword = new ItemStack(Items.NETHERITE_SWORD);
        Sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sword"));
        Weapons.add(Sword);

        ItemStack KnockbackSword = new ItemStack(Items.IRON_SWORD);
        KnockbackSword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Knockback Sword"));
        Weapons.add(KnockbackSword);

        ItemStack Mace = new ItemStack(Items.MACE);
        Mace.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Skull Crusher"));
        Weapons.add(Mace);

    }
//    public static void initWeapons(DynamicRegistryManager registries) {
//        Registry<Enchantment> enchantmentRegistry = registries.getOptional(RegistryKeys.ENCHANTMENT);
//
//        // --- The Knockback Sword ---
//        ItemStack knockbackSword = new ItemStack(Items.IRON_SWORD);
//        knockbackSword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("The Knockback Sword"));
//
//        // Add Knockback II
//        knockbackSword.addEnchantment(enchantmentRegistry.getEntry(Enchantments.KNOCKBACK).get(), 2);
//        // Add Sharpness V
//        knockbackSword.addEnchantment(enchantmentRegistry.getEntry(Enchantments.SHARPNESS).get(), 5);
//
//        Weapons.add(knockbackSword);
//    }

}