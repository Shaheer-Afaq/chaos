package chaos.systems;

import chaos.util.TaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static chaos.game.GameConfig.ARENA_MAX;
import static chaos.game.GameConfig.ARENA_MIN;
import static chaos.game.GameManager.getWorld;

public class GroundDecay {
    private static TaskScheduler.ScheduledTask decayTask;

    public static void start(){
//        decayTask = TaskScheduler.schedule(decayTask, 3*20, -1, true, null);

    }

    public static void tick(){

    }

    public static void resetArena(){
        List<BlockState> palette = List.of(
                Block.getBlockFromItem(Items.BROWN_TERRACOTTA).getDefaultState(),
                Block.getBlockFromItem(Items.BROWN_TERRACOTTA).getDefaultState(),
                Block.getBlockFromItem(Items.BROWN_TERRACOTTA).getDefaultState(),
                Block.getBlockFromItem(Items.BROWN_TERRACOTTA).getDefaultState(),
                Block.getBlockFromItem(Items.GRAY_TERRACOTTA).getDefaultState()
        );
        for (BlockPos pos : BlockPos.iterate(ARENA_MIN, ARENA_MAX)) {
            BlockState randomState = palette.get(getWorld().random.nextInt(palette.size()));
            getWorld().setBlockState(pos, randomState, 2);
        }
    }
}
