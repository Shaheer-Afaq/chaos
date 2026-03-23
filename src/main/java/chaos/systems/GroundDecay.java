package chaos.systems;

import chaos.util.TaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static chaos.game.GameConfig.DECAY_MAX;
import static chaos.game.GameConfig.DECAY_MIN;
import static chaos.game.GameConfig.MAX_TIME;
import static chaos.game.GameManager.getWorld;
import static chaos.util.HelperMethods.map;

public class GroundDecay {
    private static TaskScheduler.ScheduledTask decayTask;
    private static TaskScheduler.ScheduledTask delayTask;

    public static void start(){
        stop();
        delayTask = TaskScheduler.schedule((x)->{

        decayTask = TaskScheduler.schedule(GroundDecay::decay, 8*20, 30, false, null);
        }, MAX_TIME - 300*20, 1, false, null);
    }
    public static void stop(){
        TaskScheduler.remove(delayTask);
        TaskScheduler.remove(decayTask);
    }

    public static void decay(int currentRun) {
        int index = map(currentRun, 1, 31, DECAY_MAX, DECAY_MIN);
        BlockState air = Blocks.AIR.getDefaultState();

        for (int i = -index; i <= index; i++) {
            for (int y = 74; y <= 76; y++) {
                BlockPos[] positions = {
                        new BlockPos(index, y, i),
                        new BlockPos(-index, y, i),
                        new BlockPos(i, y, index),
                        new BlockPos(-i, y, -index)
                };

                for (BlockPos pos : positions) {
                    if (!getWorld().getBlockState(pos).isAir()) {
                        getWorld().setBlockState(pos, air, 2 | 16);
                    }
                }
            }
        }
    }

}
