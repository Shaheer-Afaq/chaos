package chaos.systems;

import chaos.game.GameManager;
import chaos.mixin.CreeperAccessor;
import chaos.util.TaskScheduler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.component.CustomDataPredicate;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

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
            for (UUID uuid: activePlayers) {
                if (x==4){
                    sendSound(getPlayer(uuid), SoundEvents.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE);
                }else{
                    if (state != GameManager.GameState.RUNNING){break;}
                    getPlayer(uuid).sendMessage(Text.literal(name + " in " + (4 - x)).formatted(Formatting.RED).formatted(Formatting.BOLD), false);
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
}
