package ankel.worlshare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ankel.worlshare.config.WorldShareConfigs;
import ankel.worlshare.gui.ConfigScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WorldShare.MODID)
public class WorldShare {
	public static final String MODID = "worldshare";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public WorldShare() {
		
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();	
		
		// Register setup methods for modloading
        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::processIMC);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, 
        		WorldShareConfigs.SPEC, "worldshare.toml");
        ModLoadingContext.get().registerExtensionPoint(
        		ExtensionPoint.CONFIGGUIFACTORY,
        		() -> (mc, screen) -> new ConfigScreen(screen));
        
        // Register ourselves for other server and game events
        MinecraftForge.EVENT_BUS.register(this);  
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	}
	
	private void clientSetup(final FMLClientSetupEvent event) {
	}
	
	private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("worldshare", "helloworld", 
        		() -> { LOGGER.info("Hello world from the WorldShare"); 
        		return "Hello world";});
	}
	
	private void processIMC(final InterModProcessEvent event) {
	}
	
}
