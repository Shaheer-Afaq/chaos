package chaos.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static chaos.game.GameManager.getWorld;

public class HelperMethods {
    public static void sendTitle(ServerPlayerEntity player, String message, Formatting color) {
        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(message).formatted(color)));
    }
    public static void sendMessage(ServerPlayerEntity player, Text message) {
        player.sendMessage(message, true);
    }
    public static void clearAllEntities() {
        for (Entity entity : getWorld().iterateEntities()) {
            if (!(entity instanceof PlayerEntity)) {
                assert entity != null;
                entity.discard();
            }
        }
    }

}
