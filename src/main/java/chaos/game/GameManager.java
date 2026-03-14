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

    public static GameState state = GameState.WAITING;
    public static Set<ServerPlayerEntity> activePlayers = new HashSet<>();
    public static Set<ServerPlayerEntity> players = new HashSet<>();

    public static void init() {

    }

//    public static void addActivePlayer(ServerPlayerEntity player) {
//        players.add(player);
//    }
//    public static void removeActivePlayer(ServerPlayerEntity player) {
//        players.remove(player);
//    }

    public static void startGame() {
        state = GameState.RUNNING;
        activePlayers.clear();
        for (ServerPlayerEntity player : players) {
            activePlayers.add(player);
            toArena(player);
        }
    }

    public static void endGame() {
        for (ServerPlayerEntity player : activePlayers) {
            toLobby(player);
        }
    }

    public static void toLobby(ServerPlayerEntity player) {
        if (activePlayers.isEmpty()) {
            state = GameState.WAITING;
        }
        player.changeGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.teleport(
                GameConfig.LOBBY_POS.getX() + 0.5,
                GameConfig.LOBBY_POS.getY(),
                GameConfig.LOBBY_POS.getZ() + 0.5,
                false
        );
    }

    public static void toArena(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.teleport(
                GameConfig.ARENA_POS.getX() + 0.5,
                GameConfig.ARENA_POS.getY(),
                GameConfig.ARENA_POS.getZ() + 0.5,
                false
        );
    }
}
