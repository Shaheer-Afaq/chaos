package chaos.game;


import net.minecraft.util.math.BlockPos;

public class GameConfig {

    public static final int MIN_PLAYERS = 2;

    public static final BlockPos LOBBY_POS = new BlockPos(0,107,0);
    public static final BlockPos START_BUTTON = new BlockPos(4,108,0);
    public static final BlockPos ARENA_POS = new BlockPos(0,77,0);
    public static final BlockPos GROUND_MIN = new BlockPos(-40, 74, -40);
    public static final BlockPos GROUND_MAX = new BlockPos(40, 76, 40);
    public static final BlockPos ARENA_MIN = new BlockPos(-40, 77, -40);
    public static final BlockPos ARENA_MAX = new BlockPos(40, 105, 40);
    public static final int VOID_Y = 70;
    public static final int SPAWN_RADIUS = 10;
    public static final int DECAY_MAX = 40;
    public static final int DECAY_MIN = 10;
    public static final int MAX_TIME = 15 * 60 * 20;

}