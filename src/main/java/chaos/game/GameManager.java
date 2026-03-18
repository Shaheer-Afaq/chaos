package chaos.game;


import chaos.systems.PlayerSystem;
import chaos.util.TaskScheduler;
import chaos.util.TaskScheduler.ScheduledTask;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;

import java.util.*;

import static chaos.game.GameConfig.ARENA_MAX;
import static chaos.game.GameConfig.ARENA_MIN;
import static chaos.systems.GroundDecay.resetArena;
import static chaos.systems.PlayerSystem.populateLists;
import static chaos.util.HelperMethods.*;

public class GameManager {

    public enum GameState {
        STARTING,
        WAITING,
        RUNNING,
        ENDING
    }

    private static MinecraftServer Server;
    private static ServerWorld World;
    public static MinecraftServer getServer(){return Server;}
    public static ServerWorld getWorld(){return World;}

    public static GameState state;

    public static Set<UUID> players = new HashSet<>();
    public static Set<UUID> activePlayers = new HashSet<>();

    public static final Map<UUID, PlayerData> playerData = new HashMap<>();
    public static PlayerData getData(ServerPlayerEntity player) {
        return playerData.get(player.getUuid());
    }

    private static ScheduledTask weaponTask;

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            state = GameState.WAITING;
            Server = server;
            World = server.getOverworld();
            resetArena();
            populateLists();
            clearAllEntities();

            server.setDifficulty(Difficulty.HARD, true);
            assert World != null;
            World.getGameRules().setValue(GameRules.ADVANCE_TIME, false, server);
            World.getGameRules().setValue(GameRules.DO_IMMEDIATE_RESPAWN, true, server);
            World.getGameRules().setValue(GameRules.DO_MOB_SPAWNING, false, server);
            World.getGameRules().setValue(GameRules.DO_MOB_GRIEFING, false, server);
            World.getGameRules().setValue(GameRules.KEEP_INVENTORY, true, server);
            World.getGameRules().setValue(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
        });
        ServerTickEvents.START_SERVER_TICK.register(GameManager::tick);
    }

    public static void tick(MinecraftServer server) {
        if (state == GameState.RUNNING) {
            PlayerSystem.tick();

            if (activePlayers.size() <= 1){
                endGame(server);
            }
            var active = "";
            for (ServerPlayerEntity player : activePlayers) {
                if (player.getY() < 70){
                    player.damage(World, World.getDamageSources().outOfWorld(), 9999);
                }

                PlayerData playerData = getData(player);
                List<String> messages = playerData.messages;

                playerData.message = Text.literal(messages.getFirst()).formatted(Formatting.YELLOW).formatted(Formatting.BOLD)
                                    .append(Text.literal(messages.get(1)).formatted(Formatting.GREEN).formatted(Formatting.BOLD))
                                    .append(Text.literal(messages.get(2)).formatted(Formatting.BLUE).formatted(Formatting.BOLD));

                player.sendMessage(playerData.message, true);
                active = active.concat(String.valueOf(player.getName().getString())).concat(", ");
            }
            System.out.println("Active Players: ".concat(active));
        }


    }

    public static void startGame() {
        state = GameState.RUNNING;
        clearAllEntities();
        resetArena();
        PlayerSystem.start();

        for (ServerPlayerEntity player : players) {
            toArena(player);
            sendTitle(player, "GO!", Formatting.RED);
        }
    }

    public static void endGame(MinecraftServer server) {
        state = GameState.ENDING;
        PlayerSystem.stop();
        clearAllEntities();
        if (!activePlayers.isEmpty()){
            ServerPlayerEntity winner = activePlayers.iterator().next();
            sendTitle(winner, "You Won!", Formatting.GREEN);
            TaskScheduler.schedule((x)->{
                toLobby(winner);
                state = GameState.WAITING;
            }, 2 * 20, 1, false, null);
        }else{
            state = GameState.WAITING;
        }
    }

    public static void toArena(ServerPlayerEntity player) {
//        Server.execute(()-> {
            activePlayers.add(player);
            playerData.put(player.getUuid(), new PlayerData());
            player.heal(20);
            player.getInventory().clear();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 15 * 60 * 20, 10, true, false));
            player.teleport(
                    GameConfig.ARENA_POS.getX() + 0.5,
                    GameConfig.ARENA_POS.getY(),
                    GameConfig.ARENA_POS.getZ() + 0.5,
                    false
            );
//        });
    }

    public static void toLobby(ServerPlayerEntity player) {
        activePlayers.remove(player);
        players.add(player);
        playerData.remove(player.getUuid());
        player.heal(20);
        player.clearStatusEffects();
        player.teleport(
                GameConfig.LOBBY_POS.getX() + 0.5,
                GameConfig.LOBBY_POS.getY(),
                GameConfig.LOBBY_POS.getZ() + 0.5,
                false
        );
        player.getInventory().clear();
        player.changeGameMode(GameMode.SURVIVAL);
    }


}
