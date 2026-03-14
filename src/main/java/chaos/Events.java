package chaos;

import chaos.game.GameConfig;
import chaos.game.GameManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.io.Console;
import java.util.Objects;

import static chaos.game.GameManager.state;


public class Events {
    public static void register() {
        ServerPlayerEvents.JOIN.register((player) -> {
            GameManager.players.add(player);
            Objects.requireNonNull(player.getEntityWorld().getServer()).execute(()->{
                GameManager.toLobby(player);
            });
            player.sendMessage(Text.literal("Welcome to CHAOS!"));
        });

        ServerPlayerEvents.LEAVE.register(player -> {
            GameManager.players.remove(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldplayer, newplayer, alive) -> {
            GameManager.activePlayers.remove(oldplayer);
            GameManager.players.add(newplayer);
            GameManager.toLobby(newplayer);
        });

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {

            BlockPos pos = hitResult.getBlockPos();
            if (pos.equals(GameConfig.START_BUTTON) && world.getBlockState(pos).getBlock() == Blocks.STONE_BUTTON){
                if (state == GameManager.GameState.RUNNING) {
                    player.sendMessage(Text.literal("Game is already running!"), false);
                } else {
                    GameManager.startGame();
                }
            }
            return ActionResult.PASS;
        }));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            System.out.println(String.valueOf(GameManager.activePlayers.size()).concat(String.valueOf(GameManager.players.size())));
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, blockPos, state, entity)->{
            return Objects.equals(player.getGameMode(), GameMode.CREATIVE);
        });
    }
}
