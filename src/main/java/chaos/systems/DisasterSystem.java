package chaos.systems;

import chaos.game.GameManager;
import chaos.util.TaskScheduler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static chaos.game.GameConfig.*;
import static chaos.game.GameManager.*;
import static chaos.util.HelperMethods.getPlayer;

public class DisasterSystem {
    private static TaskScheduler.ScheduledTask disasterTask;
    private static TaskScheduler.ScheduledTask delayTask;

//    public static final Map<, List<ItemStack>> items = new HashMap<>();
    static Map<String, Runnable> disasters = new HashMap<>();

    static{
        disasters.put("Baby Apocalypse", DisasterSystem::babyApocalypse);
        disasters.put("Orbital Strike Canon", DisasterSystem::orbitalCanon);
    }


    public static void start(){
        delayTask = TaskScheduler.schedule((x)->{
            disasterTask = TaskScheduler.schedule(DisasterSystem::DisasterSystemTick, 100, -1, true, null);
        }, 40*20, 1, false, null);
    }
    public static void stop(){
        TaskScheduler.remove(delayTask);
        TaskScheduler.remove(disasterTask);
    }

    public static void DisasterSystemTick(int currentRun){
        if (Math.random() < 0.2){
            List<String> keys = new ArrayList<>(disasters.keySet());
            String name = keys.get(new Random().nextInt(keys.size()));
            countdownDisaster(name, disasters.get(name));
        }
    }
    public static void countdownDisaster(String name, Runnable disaster){
        TaskScheduler.schedule((x) -> {
            if (x!=4){
                for (UUID uuid: activePlayers) {
                    if (state != GameManager.GameState.RUNNING){break;}
                    getPlayer(uuid).sendMessage(Text.literal(name + " in " + (4 - x)).formatted(Formatting.RED).formatted(Formatting.BOLD), false);
                }
            }
        }, 20, 5, true, disaster);
    }
    private static void babyApocalypse(){
        if (state != GameState.RUNNING){return;}
        for (double x = ARENA_MIN.getX() + 1; x <= ARENA_MAX.getX() - 1; x += ZOMBIE_SPACING){
            for (double z = ARENA_MIN.getZ() + 1; z <= ARENA_MAX.getZ() - 1; z += ZOMBIE_SPACING) {
                x += (Math.random() * 2) - 1;
                z += (Math.random() * 2) - 1;
                ZombieEntity zombie = EntityType.ZOMBIE.create(getWorld(), SpawnReason.MOB_SUMMONED);
                if (zombie != null) {
                    zombie.refreshPositionAndAngles(x, ARENA_POS.getY(), z, 0, 0);
                    zombie.setBaby(true);
                    zombie.setCustomName(Text.literal("Baby Warrior").formatted(Formatting.DARK_GREEN));
                    zombie.equipStack(EquipmentSlot.HEAD,  new ItemStack(Items.NETHERITE_HELMET));
                    zombie.equipStack(EquipmentSlot.CHEST,  new ItemStack(Items.NETHERITE_CHESTPLATE));
                    zombie.equipStack(EquipmentSlot.LEGS,  new ItemStack(Items.NETHERITE_LEGGINGS));
                    zombie.equipStack(EquipmentSlot.FEET,  new ItemStack(Items.NETHERITE_BOOTS));
                    zombie.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        zombie.setEquipmentDropChance(slot, 0.0f);
                    }
                    getWorld().spawnEntity(zombie);
                }
            }
        }
    }
    private static void orbitalCanon() {
        for (double x = ARENA_MIN.getX() + 1; x <= ARENA_MAX.getX() - 1; x += TNT_SPACING) {
            for (double z = ARENA_MIN.getZ() + 1; z <= ARENA_MAX.getZ() - 1; z += TNT_SPACING) {
                x += (Math.random() * 3) - 1.5;
                z += (Math.random() * 3) - 1.5;
                getWorld().spawnEntity(EntityType.TNT.spawn(getWorld(), new BlockPos((int) x, ARENA_MAX.getY() - 2, (int) z), SpawnReason.TRIGGERED));
            }
        }
    }
}
