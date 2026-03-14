package chaos.game;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.*;

public class GameManager {
    public enum GameState {
        WAITING,
        RUNNING,
        ENDING
    }

    private static GameState state = GameState.WAITING;
    private static Set<ServerPlayerEntity> activePlayers = new HashSet<>();
    private static Set<ServerPlayerEntity> players = new HashSet<>();

    public static void init() {

    }

    public static void addPlayer(ServerPlayerEntity player) {
        players.add(player);
    }

    public static void startGame() {
        activePlayers = new HashSet<>(players);
        for (ServerPlayerEntity player : activePlayers) {
            toArena(player);
        }
    }

    public static void endGame() {
        for (ServerPlayerEntity player : activePlayers) {
            toLobby(player);
            player.getInventory().clear();
        }
    }

    public static void toLobby(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.ADVENTURE);
        player.teleport(
                GameConfig.LOBBY_POS.getX() + 0.5,
                GameConfig.LOBBY_POS.getY(),
                GameConfig.LOBBY_POS.getZ() + 0.5,
                false
        );
    }

    public static void toArena(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SURVIVAL);
        player.teleport(
                GameConfig.ARENA_POS.getX() + 0.5,
                GameConfig.ARENA_POS.getY(),
                GameConfig.ARENA_POS.getZ() + 0.5,
                false
        );
    }
}
