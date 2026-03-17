package chaos.game;


import chaos.systems.PlayerSystem;
import chaos.util.TaskScheduler;
import chaos.util.TaskScheduler.ScheduledTask;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.*;

import static chaos.systems.PlayerSystem.populateLists;
import static chaos.util.HelperMethods.*;

public class GameManager {

    public enum GameState {
        WAITING,
        RUNNING,
        ENDING
    }

    public static MinecraftServer Server;
    public static GameState state = GameState.WAITING;

    public static Set<ServerPlayerEntity> players = new HashSet<>();
    public static Set<ServerPlayerEntity> activePlayers = new HashSet<>();

    public static final Map<UUID, PlayerData> playerData = new HashMap<>();
    public static PlayerData getData(ServerPlayerEntity player) {
        return playerData.get(player.getUuid());
    }

    private static ScheduledTask weaponTask;

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(GameManager::tick);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Server = server;
            populateLists();
        });
    }

    public static void tick(MinecraftServer server) {
        if (state == GameState.RUNNING) {
            PlayerSystem.tick();

            if (activePlayers.size() == 0){
                endGame(server);
            }
            for (ServerPlayerEntity player : activePlayers) {
                PlayerData playerData = getData(player);
                List<String> messages = playerData.messages;

                playerData.message = Text.literal(messages.getFirst()).formatted(Formatting.YELLOW).formatted(Formatting.BOLD)
                                    .append(Text.literal(messages.get(1)).formatted(Formatting.GREEN).formatted(Formatting.BOLD))
                                    .append(Text.literal(messages.get(2)).formatted(Formatting.BLUE).formatted(Formatting.BOLD));

                player.sendMessage(playerData.message, true);
            }
        }
    }

    public static void startGame(MinecraftServer server) {
        state = GameState.RUNNING;
        PlayerSystem.start(server);

        for (ServerPlayerEntity player : players) {
            toArena(player);
            sendTitle(player, "GO!", Formatting.RED);
        }
    }

    public static void endGame(MinecraftServer server) {
        state = GameState.ENDING;
        PlayerSystem.stop();
        ServerPlayerEntity winner = activePlayers.iterator().next();

        sendTitle(winner, "You Won!", Formatting.GREEN);

        TaskScheduler.schedule((x)->{
            toLobby(winner);
            state = GameState.WAITING;
        }, 2 * 20, 1, false, null);
    }

    public static void toLobby(ServerPlayerEntity player) {
        playerData.remove(player.getUuid());
        activePlayers.remove(player);
        player.heal(20);
        player.getInventory().clear();
        player.teleport(
                GameConfig.LOBBY_POS.getX() + 0.5,
                GameConfig.LOBBY_POS.getY(),
                GameConfig.LOBBY_POS.getZ() + 0.5,
                false
        );
        player.changeGameMode(GameMode.SURVIVAL);
    }

    public static void toArena(ServerPlayerEntity player) {
        activePlayers.add(player);
        playerData.put(player.getUuid(), new PlayerData());
        player.heal(20);
        player.getInventory().clear();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 15*60, 10, true, false));
        player.teleport(
                GameConfig.ARENA_POS.getX() + 0.5,
                GameConfig.ARENA_POS.getY(),
                GameConfig.ARENA_POS.getZ() + 0.5,
                false
        );
    }

}
