package chaos.systems;

import chaos.util.TaskScheduler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

import static chaos.game.GameConfig.Weapons;
import static chaos.game.GameManager.*;

public class WeaponSystem {
    private static TaskScheduler.ScheduledTask weaponTask;

    public static void start(){
        weaponTask = TaskScheduler.schedule(WeaponSystem::changeWeapon, 10*20, -1, true, null);
    }
    public static void stop(){
        TaskScheduler.remove(weaponTask);
    }

    public static void changeWeapon(int currentRun){
        for (ServerPlayerEntity player : activePlayers) {
            System.out.println(player.getName().getString().concat(" Changed weapon"));

            ItemStack randomWeapon = Weapons.get(player.getRandom().nextInt(Weapons.size())).copy();
            PlayerInventory inv = player.getInventory();

            inv.setStack(inv.getEmptySlot(), randomWeapon);

            player.sendMessage(Text.literal("You got ".concat(Objects.requireNonNull(randomWeapon.getCustomName()).getString().concat(" !"))).formatted(Formatting.GOLD));
            getData(player).messages.set(0, "Weapon: ".concat(randomWeapon.getCustomName().getString()));
        }
    }

    public static void tick(){
        if (weaponTask !=null && weaponTask.ticksLeft <= 5*20 && weaponTask.ticksLeft % 20 == 0){
            for (ServerPlayerEntity player : activePlayers) {
                player.sendMessage(Text.literal("Random weapon in: ".concat(String.valueOf(weaponTask.ticksLeft/20))).formatted(Formatting.GREEN));
            }
        }
    }
}
