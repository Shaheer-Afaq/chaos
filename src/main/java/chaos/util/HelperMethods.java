package chaos.util;

import chaos.game.GameManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.rule.GameRules;

import java.util.List;
import java.util.UUID;

import static chaos.game.GameConfig.ARENA_MAX;
import static chaos.game.GameConfig.ARENA_MIN;
import static chaos.game.GameManager.getServer;
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
    public static ServerPlayerEntity getPlayer(UUID uuid) {
        return getServer().getPlayerManager().getPlayer(uuid);
    }
    public static void resetArena(){
        List<BlockState> palette = List.of(
                Blocks.BROWN_TERRACOTTA.getDefaultState(),
                Blocks.BROWN_TERRACOTTA.getDefaultState(),
                Blocks.BROWN_TERRACOTTA.getDefaultState(),
                Blocks.BROWN_TERRACOTTA.getDefaultState(),
                Blocks.GRAY_TERRACOTTA.getDefaultState()
        );
        for (BlockPos pos : BlockPos.iterate(ARENA_MIN, ARENA_MAX)) {
            BlockState randomState = palette.get(getWorld().random.nextInt(palette.size()));
            getWorld().setBlockState(pos, randomState, 2);
        }
    }
    public static void setRules(){
        getServer().setDifficulty(Difficulty.HARD, true);
        assert getWorld() != null;
        getWorld().getGameRules().setValue(GameRules.ADVANCE_TIME, false,getServer());
        getWorld().getGameRules().setValue(GameRules.DO_IMMEDIATE_RESPAWN, true, getServer());
        getWorld().getGameRules().setValue(GameRules.DO_MOB_SPAWNING, false, getServer());
        getWorld().getGameRules().setValue(GameRules.DO_MOB_GRIEFING, false, getServer());
        getWorld().getGameRules().setValue(GameRules.KEEP_INVENTORY, true, getServer());
        getWorld().getGameRules().setValue(GameRules.NATURAL_HEALTH_REGENERATION, false, getServer());
        getWorld().getGameRules().setValue(GameRules.ANNOUNCE_ADVANCEMENTS, false, getServer());
    }

    public static int map(int value, int inMin, int inMax, int outMin, int outMax) {
        return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
    }


}
