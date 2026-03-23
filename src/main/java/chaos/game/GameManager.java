package chaos.game;


import chaos.systems.DisasterSystem;
import chaos.systems.GroundDecay;
import chaos.systems.ItemSystem;
import chaos.util.HelperMethods;
import chaos.util.TaskScheduler;
import chaos.util.TaskScheduler.ScheduledTask;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;

import java.util.*;

import static chaos.game.GameConfig.*;
import static chaos.systems.ItemSystem.populateLists;
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

    private static ScheduledTask timeLimitTask;

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            state = GameState.WAITING;
            Server = server;
            World = server.getOverworld();
            TaskScheduler.schedule((x)-> Server.openToLan(GameMode.SURVIVAL, true, 25565), 20, 1, false, null);
            setRules();
            populateLists();
            resetArena();
            Server.execute(HelperMethods::clearAllEntities);
        });
        ServerWorldEvents.LOAD.register((server, world) -> {
        });
        ServerTickEvents.START_SERVER_TICK.register(GameManager::tick);
    }

    public static void tick(MinecraftServer server) {
        if (state == GameState.RUNNING) {
            if (activePlayers.size() <= 1){
                endGame();
            }
            for (UUID uuid : activePlayers) {
                ServerPlayerEntity player = getPlayer(uuid);
                if (player.getY() < VOID_Y){
                    player.damage(World, World.getDamageSources().outOfWorld(), 9999);
                }
                PlayerData playerData = getData(player);

                playerData.messages.set(1, " Health: " + String.format("%.2f", player.getHealth()*5) + "%");
                List<String> messages = playerData.messages;

                playerData.message = Text.literal(messages.getFirst()).formatted(Formatting.YELLOW).formatted(Formatting.BOLD)
                                    .append(Text.literal(messages.get(1)).formatted(Formatting.GREEN).formatted(Formatting.BOLD))
                                    .append(Text.literal(messages.get(2)).formatted(Formatting.BLUE).formatted(Formatting.BOLD));

                player.sendMessage(playerData.message, true);
            }
        }

    }

    public static void startGame() {
        state = GameState.RUNNING;
        resetArena();
        clearAllEntities();
        ItemSystem.start();
        DisasterSystem.start();
        GroundDecay.start();
        int i = 0;
        for (UUID uuid : players) {
            double angle = 2 * Math.PI * i / players.size();
            ServerPlayerEntity player = getPlayer(uuid);
            toArena(player, new BlockPos(
                    (int) (ARENA_POS.getX() + SPAWN_RADIUS * Math.cos(angle)),
                    ARENA_POS.getY(),
                    (int) (ARENA_POS.getZ() + SPAWN_RADIUS * Math.sin(angle))
            ));
            sendSound(player, SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT);
            i++;
        }
        timeLimitTask = TaskScheduler.schedule((x)->{
            endGame();
        }, MAX_TIME, 1, false,  null);
    }

    public static void endGame() {
        state = GameState.ENDING;
        TaskScheduler.remove(timeLimitTask);
        ItemSystem.stop();
        GroundDecay.stop();
        DisasterSystem.stop();
        clearAllEntities();
        if (!activePlayers.isEmpty()) {
            UUID winnerUUID = activePlayers.iterator().next();
            if (activePlayers.size() > 1) {
                float highest = getPlayer(winnerUUID).getHealth();
                for (UUID uuid : new ArrayList<>(activePlayers)) {
                    if (getPlayer(uuid).getHealth() > highest) {
                        winnerUUID = uuid;
                        highest = getPlayer(uuid).getHealth();
                    }
                    toLobby(getPlayer(uuid));
                    getPlayer(uuid).sendMessage(Text.literal("Times Up!").formatted(Formatting.RED).formatted(Formatting.BOLD), true);
                    getPlayer(uuid).sendMessage(Text.literal("=========").formatted(Formatting.RED).formatted(Formatting.BOLD));
                    getPlayer(uuid).sendMessage(Text.literal("Times Up!").formatted(Formatting.RED).formatted(Formatting.BOLD));
                    getPlayer(uuid).sendMessage(Text.literal("=========").formatted(Formatting.RED).formatted(Formatting.BOLD));
                }
            } else {toLobby(getPlayer(winnerUUID));}

            ServerPlayerEntity winner = getPlayer(winnerUUID);
            sendTitle((winner), "You Won!", Formatting.GREEN);
            for (UUID uuid : players) {
                getPlayer(uuid).sendMessage(Text.literal("==========================").formatted(Formatting.GREEN).formatted(Formatting.BOLD));
                getPlayer(uuid).sendMessage(Text.literal(winner.getName().getString()).formatted(Formatting.GOLD).formatted(Formatting.BOLD)
                                .append(Text.literal(" won the game!").formatted(Formatting.AQUA)));
                getPlayer(uuid).sendMessage(Text.literal("==========================").formatted(Formatting.GREEN).formatted(Formatting.BOLD));
            }
            TaskScheduler.schedule((x) -> {
            }, 2 * 20, 1, false, null);
        }
        state = GameState.WAITING;

    }

    public static void toArena(ServerPlayerEntity player, Vec3i pos) {
        activePlayers.add(player.getUuid());
        playerData.put(player.getUuid(), new PlayerData());

        sendTitle(player, "GO!", Formatting.RED);
        player.changeGameMode(GameMode.CREATIVE);
        player.heal(20);
        player.getInventory().clear();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 15 * 60 * 20, 10, true, false));

        player.equipStack(EquipmentSlot.HEAD,  new ItemStack(Items.LEATHER_HELMET));
        player.equipStack(EquipmentSlot.CHEST,  new ItemStack(Items.LEATHER_CHESTPLATE));
        player.equipStack(EquipmentSlot.LEGS,  new ItemStack(Items.LEATHER_LEGGINGS));
        player.equipStack(EquipmentSlot.FEET,  new ItemStack(Items.LEATHER_BOOTS));
        player.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));



        player.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, false);
    }

    public static void toLobby(ServerPlayerEntity player) {
        activePlayers.remove(player.getUuid());
        playerData.remove(player.getUuid());
        player.sendMessage(Text.literal(""), true);
        player.heal(20);
        player.clearStatusEffects();
        player.setFireTicks(0);
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
