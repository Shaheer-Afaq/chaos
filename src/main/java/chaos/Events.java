package chaos;

import chaos.game.GameConfig;
import chaos.game.GameManager;
import chaos.util.TaskScheduler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;

import java.util.Objects;

import static chaos.util.HelperMethods.sendMessage;
import static chaos.util.HelperMethods.sendTitle;
import static chaos.game.GameManager.*;


public class Events {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getWorld(World.OVERWORLD);
            server.setDifficulty(Difficulty.HARD, true);

            assert world != null;
            world.getGameRules().setValue(GameRules.ADVANCE_TIME, false, server);
            world.getGameRules().setValue(GameRules.DO_IMMEDIATE_RESPAWN, true, server);
            world.getGameRules().setValue(GameRules.DO_MOB_SPAWNING, false, server);
            world.getGameRules().setValue(GameRules.DO_MOB_GRIEFING, false, server);
            world.getGameRules().setValue(GameRules.KEEP_INVENTORY, true, server);
            world.getGameRules().setValue(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
        });

        ServerPlayerEvents.JOIN.register((player) -> {
            players.add(player);
            toLobby(player);
            sendTitle(player, "Welcome to Chaos!", Formatting.GOLD);
//            Objects.requireNonNull(player.getEntityWorld().getServer()).execute(()->{
//                toLobby(player);
//            });
        });

        ServerPlayerEvents.LEAVE.register(player -> {
            players.remove(player);
            activePlayers.remove(player);
            playerData.remove(player.getUuid());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldplayer, newplayer, alive) -> {
            activePlayers.remove(oldplayer);
            toLobby(newplayer);
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount)->{
            if (entity instanceof ServerPlayerEntity player && !source.isOf(DamageTypes.GENERIC_KILL)){
                return activePlayers.contains(player);
            }
            return true;
        });

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient()){
                return ActionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            if (pos.equals(GameConfig.START_BUTTON) && world.getBlockState(pos).getBlock() == Blocks.STONE_BUTTON && !world.getBlockState(pos).get(Properties.POWERED)){
                if (state != GameState.WAITING) {
                    sendMessage((ServerPlayerEntity) player, Text.literal("Game is already running").formatted(Formatting.RED));
//                } else if (players.size() < MIN_PLAYERS) {
                } else if (false) {
                    sendMessage((ServerPlayerEntity) player, Text.literal("Not enough players!").formatted(Formatting.RED));
                } else {
                    TaskScheduler.schedule((int x) -> {
                        for (ServerPlayerEntity p: activePlayers){
                            sendTitle(p, "String.valueOf(x)", Formatting.YELLOW);
                        }
                    }, 20, 5, true, GameManager::startGame);
                }
            }
            return ActionResult.PASS;
        }));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
//            System.out.println(String.valueOf(activePlayers.size()).concat(String.valueOf(players.size())));
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, blockPos, state, entity)->{
            return Objects.equals(player.getGameMode(), GameMode.CREATIVE);
        });
    }
}
