package chaos.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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
}
