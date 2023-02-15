package ankel.worlshare.event;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ankel.worlshare.WorldShare;
import ankel.worlshare.dropbox.AuthorizationException;
import ankel.worlshare.dropbox.Authorizer;
import ankel.worlshare.gui.DbxScreen;
import ankel.worlshare.gui.LoginScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WorldShare.MODID, value = Dist.CLIENT)
public class ScreenEventHandler {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
    public static void addDbxButtonToScreen(final GuiScreenEvent.InitGuiEvent.Post event) {
    	if(event.getGui() instanceof WorldSelectionScreen) {
    		WorldSelectionScreen gui = (WorldSelectionScreen) event.getGui();
    		List<IGuiEventListener> widgetList = (List<IGuiEventListener>) gui.children();
    		for(IGuiEventListener w : widgetList) {
    			if(w instanceof TextFieldWidget) {
    				TextFieldWidget searchBar = (TextFieldWidget) w;
    				searchBar.setX(gui.width / 2 - 140);
    				searchBar.setWidth(196);	
    				searchBar.setHeight(18);	
    			}
    		}
    		
    		event.addWidget(new Button(gui.width / 2 + 66, 21, 74, 20, 
    				new TranslationTextComponent("DropBox"), e -> {
    					Minecraft minecraft = gui.getMinecraft();
    					Authorizer auth = new Authorizer(minecraft.gameDirectory);
    					try {
    						auth.AuthorizeFromFile();
    						minecraft.setScreen(new DbxScreen(gui, auth.getClient()));
    					} catch (AuthorizationException ex) {
    						gui.getMinecraft().setScreen(new LoginScreen(gui));
    						return;
    					}
    		}));
    	}
    }
}
