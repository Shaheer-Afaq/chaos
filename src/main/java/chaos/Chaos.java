package chaos;

import chaos.game.GameManager;
import chaos.util.TaskScheduler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chaos implements ModInitializer {
	public static final String MOD_ID = "chaos";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TaskScheduler.init();
		Events.register();
		GameManager.init();
	}
}