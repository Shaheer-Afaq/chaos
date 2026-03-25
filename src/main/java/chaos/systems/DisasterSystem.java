package chaos.systems;

import chaos.game.GameManager;
import chaos.mixin.CreeperAccessor;
import chaos.util.TaskScheduler;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static chaos.game.GameConfig.*;
import static chaos.game.GameManager.*;
import static chaos.util.HelperMethods.getPlayer;
import static chaos.util.HelperMethods.sendSound;

public class DisasterSystem {
    private static TaskScheduler.ScheduledTask disasterTask;
    private static TaskScheduler.ScheduledTask delayTask;

    static Map<String, Runnable> disasters = new HashMap<>();

    static{
        disasters.put("Baby Apocalypse", DisasterSystem::babyApocalypse);
        disasters.put("Orbital Strike Canon", DisasterSystem::orbitalCanon);
        disasters.put("Explosive Surprise", DisasterSystem::explosiveSurprise);
        disasters.put("Lightning Storm", DisasterSystem::lightningStorm);
        disasters.put("Position Shuffle", DisasterSystem::positionShuffle);
    }


    public static void start(){
        stop();
        delayTask = TaskScheduler.schedule(x->{
            disasterTask = TaskScheduler.schedule(DisasterSystem::disasterSystemTick, 300, -1, true, null);
        }, 30*20, 1, false, null);
    }
    public static void stop(){
        TaskScheduler.remove(delayTask);
        TaskScheduler.remove(disasterTask);
    }

    public static void disasterSystemTick(int currentRun){
        if (Math.random() < 0.5){
            List<String> keys = new ArrayList<>(disasters.keySet());
            Collections.shuffle(keys);
            String name = keys.get(new Random().nextInt(keys.size()));
            countdownDisaster(name, disasters.get(name));
        }
    }
    public static void countdownDisaster(String name, Runnable disaster){
        TaskScheduler.schedule((x) -> {
            for (UUID uuid: activePlayers) {
                if (x==4){
                    sendSound(getPlayer(uuid), SoundEvents.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE);
                }else{
                    if (state != GameManager.GameState.RUNNING){break;}
                    getPlayer(uuid).sendMessage(Text.literal(name + " in " + (4 - x)).formatted(Formatting.RED, Formatting.BOLD), false);
                }
            }
        }, 20, 5, true, disaster);
    }

    private static void babyApocalypse(){
        if (state != GameState.RUNNING){return;}
        for (double x = ARENA_MIN.getX() + 1; x <= ARENA_MAX.getX() - 1; x += 18){
            for (double z = ARENA_MIN.getZ() + 1; z <= ARENA_MAX.getZ() - 1; z += 18) {
                x += (Math.random() * 2) - 1;
                z += (Math.random() * 2) - 1;
                ZombieEntity zombie = EntityType.ZOMBIE.create(getWorld(), SpawnReason.MOB_SUMMONED);
                if (zombie != null) {
                    zombie.refreshPositionAndAngles(x, ARENA_POS.getY(), z, 0, 0);
                    zombie.setBaby(true);
                    zombie.setCustomName(Text.literal("Baby Warrior").formatted(Formatting.DARK_GREEN));
                    zombie.equipStack(EquipmentSlot.HEAD,  new ItemStack(Items.GOLDEN_HELMET));
                    zombie.equipStack(EquipmentSlot.CHEST,  new ItemStack(Items.DIAMOND_CHESTPLATE));
                    zombie.equipStack(EquipmentSlot.LEGS,  new ItemStack(Items.LEATHER_LEGGINGS));
                    zombie.equipStack(EquipmentSlot.FEET,  new ItemStack(Items.IRON_BOOTS));
                    zombie.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        zombie.setEquipmentDropChance(slot, 0.0f);
                    }
                    getWorld().spawnEntity(zombie);
                }
            }
        }
    }
    private static void orbitalCanon() {
        if (state != GameState.RUNNING){return;}
        for (double x = ARENA_MIN.getX() + 1; x <= ARENA_MAX.getX() - 1; x += 5) {
            for (double z = ARENA_MIN.getZ() + 1; z <= ARENA_MAX.getZ() - 1; z += 5) {
                x += (Math.random() * 3) - 1.5;
                z += (Math.random() * 3) - 1.5;
                getWorld().spawnEntity(EntityType.TNT.spawn(getWorld(), new BlockPos((int) x, ARENA_MAX.getY() - 2, (int) z), SpawnReason.TRIGGERED));
            }
        }
    }
    private static void explosiveSurprise(){
        if (state != GameState.RUNNING){return;}
        for (double x = ARENA_MIN.getX() + 2; x <= ARENA_MAX.getX() - 2; x += 15){
            for (double z = ARENA_MIN.getZ() + 2; z <= ARENA_MAX.getZ() - 2; z += 15) {
                x += (Math.random() * 2) - 1;
                z += (Math.random() * 2) - 1;
                CreeperEntity creeper = EntityType.CREEPER.create(getWorld(), SpawnReason.MOB_SUMMONED);
                if (creeper != null) {
                    creeper.refreshPositionAndAngles(x, ARENA_MIN.getY() + 10 , z, 0, 0);
                    var speedAttribute = creeper.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
                    if (speedAttribute != null) {
                        speedAttribute.setBaseValue(0.5);
                    }
                    creeper.getDataTracker().set(CreeperAccessor.getCharged(), true);
                    creeper.setFuseSpeed(10);
                    creeper.setCustomName(Text.literal("Tesla's Pet").formatted(Formatting.DARK_GREEN));
                    getWorld().spawnEntity(creeper);
                }
            }
        }
    }
    private static void lightningStorm(){
        if (state != GameState.RUNNING){return;}
         TaskScheduler.schedule((run)->{
            for (int i = 0; i <= 50; i++) {
                int x = ThreadLocalRandom.current().nextInt(ARENA_MIN.getX() - 1, ARENA_MAX.getX());
                int z = ThreadLocalRandom.current().nextInt(ARENA_MIN.getZ() - 1, ARENA_MAX.getZ());
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(getWorld(), SpawnReason.TRIGGERED);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(x, ARENA_MIN.getY(), z);
                    getWorld().spawnEntity(lightning);
                }

            }
         }, 15, 5, true, null);
    }
    private static void positionShuffle(){
        List<Vec3d> positions = new ArrayList<>();
        List<ServerPlayerEntity> players = new ArrayList<>();
        for (UUID uuid : activePlayers){
            players.add(getPlayer(uuid));
            positions.add(getPlayer(uuid).getEntityPos());
        }

        for (int i = 0; i < players.size(); i++){
            Vec3d newPos = positions.get((i + 1) % positions.size());
            var player = players.get(i);
            player.teleport(getWorld(), newPos.x, newPos.y, newPos.z, Set.of(), player.getYaw(), player.getPitch(), false);
            sendSound(players.get(i), SoundEvents.ENTITY_PLAYER_TELEPORT);
        }
    }
}
