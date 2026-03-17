package chaos.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HelperMethods {
    public static void sendTitle(ServerPlayerEntity player, String message, Formatting color) {
        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(message).formatted(color)));
    }
    public static void sendMessage(ServerPlayerEntity player, Text message) {
        player.sendMessage(message, true);
    }
    public static void clearAllItems(MinecraftServer server) {
        for (Entity entity : server.getOverworld().iterateEntities()) {
            if (entity instanceof ItemEntity) {
                entity.discard();
            }
        }
    }

    public static void applyEnchantment(ItemStack item, Registry registry, RegistryKey enchantment, int level) {
        item.addEnchantment(registry.getOrThrow(enchantment), level);
    }

    public static void setCustomName(ItemStack item, String name) {
        item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
    }
}
