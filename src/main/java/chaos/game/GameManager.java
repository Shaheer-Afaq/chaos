package chaos.game;


import chaos.systems.GroundDecay;
import chaos.systems.PlayerSystem;
import chaos.util.TaskScheduler;
import chaos.util.TaskScheduler.ScheduledTask;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.rule.GameRules;

import java.util.*;

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
            TaskScheduler.schedule((x)-> Server.openToLan(GameMode.SURVIVAL, true, 0), 1, 1, false, null);
            setRules();
            resetArena();
            populateLists();
            clearAllEntities();
        });
        ServerTickEvents.START_SERVER_TICK.register(GameManager::tick);
    }

    public static void tick(MinecraftServer server) {
        if (state == GameState.RUNNING) {
            PlayerSystem.tick();

            if (activePlayers.size() <= 1){
                endGame();
            }
            var active = "";
            for (UUID uuid : activePlayers) {
                ServerPlayerEntity player = getPlayer(uuid);
                if (player.getY() < 70){
                    player.damage(World, World.getDamageSources().outOfWorld(), 9999);
                }
                PlayerData playerData = getData(player);

                playerData.messages.set(1, " Health: ".concat(String.format("%.2f", player.getHealth()*5)).concat("%"));
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
//        GroundDecay.start();

        for (UUID uuid : players) {
            ServerPlayerEntity player = getPlayer(uuid);
            toArena(player);
        }
    }

    public static void endGame() {
        state = GameState.ENDING;
        PlayerSystem.stop();
        GroundDecay.stop();
        resetArena();
        clearAllEntities();
        if (!activePlayers.isEmpty()){
            ServerPlayerEntity winner = getPlayer(activePlayers.iterator().next());
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
        activePlayers.add(player.getUuid());
        playerData.put(player.getUuid(), new PlayerData());

        sendTitle(player, "GO!", Formatting.RED);
        player.heal(20);
        player.getInventory().clear();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 15 * 60 * 20, 10, true, false));
        player.teleport(
                GameConfig.ARENA_POS.getX() + 0.5,
                GameConfig.ARENA_POS.getY(),
                GameConfig.ARENA_POS.getZ() + 0.5,
                false
        );
    }

    public static void toLobby(ServerPlayerEntity player) {
        activePlayers.remove(player.getUuid());
        playerData.remove(player.getUuid());
        player.sendMessage(Text.literal(""), true);
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
