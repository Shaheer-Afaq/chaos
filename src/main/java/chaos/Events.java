package chaos;

import chaos.game.GameConfig;
import chaos.game.GameManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;


public class Events {
    public static void register() {
        ServerPlayerEvents.JOIN.register((player) -> {
            GameManager.addPlayer(player);
            Objects.requireNonNull(player.getEntityWorld().getServer()).execute(()->{
                GameManager.toLobby(player);
            });
            player.sendMessage(Text.literal("Welcome to CHAOS!"));
        });

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            if (pos.equals(GameConfig.START_BUTTON) && world.getBlockState(pos).getBlock() == Blocks.STONE_BUTTON){
                    GameManager.startGame();
            }
            return ActionResult.PASS;
        }));

//        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount)->{
//            if (entity instanceof ServerPlayerEntity player){
//                ServerWorld world = (ServerWorld) player.getEntityWorld();
//                player.damage(world, source, amount*0.1f);
//                return false;
//            }
//            else return true;
//        });
    }
}
