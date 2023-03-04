package ankel.worlshare.gui;

import java.util.List;

import com.dropbox.core.v2.DbxClientV2;
import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.World;
import ankel.worlshare.world.WorldController;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DbxScreen extends Screen {
	private static final int PADDING = 6;
	private static final int LIST_WIDTH = 130;
	
	private final WorldController controller;
	
	private DbxWorldList worldList;
	private DbxWorldList.Entry selected;
	private TextFieldWidget search;
	private DbxWorldPanel worldPanel;
	private Widget downloadButton;
	private Widget uploadButton;
	private Widget addMemberButton;
	private List<IReorderingProcessor> toolTip;
	

	public DbxScreen(WorldSelectionScreen lastScreen, DbxClientV2 dbxclient) {
		super(new TranslationTextComponent(""));
		this.controller = new WorldController(lastScreen.getMinecraft(),
				dbxclient, () -> reload());
	}
	
	@Override
	protected void init() {
		int y = this.height - PADDING - 20;
		this.addButton(new Button((
				(LIST_WIDTH + PADDING + this.width - 200) / 2), 
				y, 200, 20, 
				new TranslationTextComponent("gui.done"), 
				b -> onClose()));
		this.addButton(new Button(PADDING, y, LIST_WIDTH, 20,
				new StringTextComponent("Config"),
				b -> minecraft.setScreen(new ConfigScreen(this)))); 
		y -= PADDING + 20;
		this.addButton(new Button(PADDING, y, LIST_WIDTH, 20,
				new StringTextComponent("Add world"),
				b -> minecraft.setScreen(new AddWorldScreen(this, controller)))); 
		
		y -= PADDING;
	    this.worldList = new DbxWorldList(this, minecraft, LIST_WIDTH, 
	    		30 + (PADDING * 2), y, worldList);
	    this.worldList.setLeftPos(PADDING);
	    children.add(worldList);
	    
		this.search =  new TextFieldWidget(getFontRenderer(), 
				PADDING + 1, PADDING + 16,  LIST_WIDTH - 2, 14, 
				new StringTextComponent("DropBox"));
		search.setFocus(false);
        search.setCanLoseFocus(true);
		children.add(search);

        this.worldPanel = new DbxWorldPanel(this,
        		this.width - LIST_WIDTH - (PADDING * 3),
        		this.height - (PADDING * 4) - 50,
        		26 + (PADDING * 2),
        		worldList.getRight() + PADDING);
        children.add(worldPanel);
        
		y = 12;
		int x = (LIST_WIDTH + PADDING + this.width - 200) / 2;
		this.downloadButton = this.addButton(new Button(x, y, 62, 20,
				new StringTextComponent("Download"),
				b -> controller.downloadWorld(selected.getWorld())));
		downloadButton.active = false;
		x += 62 + PADDING + 1;
		this.uploadButton = this.addButton(new Button(x, y, 62, 20,
				new StringTextComponent("Upload"),
				b -> controller.uploadWorld(selected.getWorld().getLocalWorld())));
		uploadButton.active = false;
		x += 62 + PADDING + 1;
		this.addMemberButton = this.addButton(new Button(x, y, 62, 20,
				new StringTextComponent("Add member"),
				b -> this.minecraft.setScreen(new AddMemberScreen(
						this, controller, selected.getWorld()))));
		addMemberButton.active = false;
		
		reload();
	}
	
	@Override
	public void render(MatrixStack mStack, int mouseX, int mouseY, 
			float partialTicks) {
		this.renderDirtBackground(0);
		this.toolTip = null;
		this.worldList.render(mStack, mouseX, mouseY, partialTicks);
		this.search.render(mStack, mouseX, mouseY, partialTicks);
		ITextComponent searchText = new StringTextComponent("Search");
		drawCenteredString(mStack, this.font, searchText, 
				PADDING + (LIST_WIDTH / 2), PADDING, 16777215);
		this.worldPanel.render(mStack, mouseX, mouseY, partialTicks);
		super.render(mStack, mouseX, mouseY, partialTicks);
		if (this.toolTip != null) {
			this.renderTooltip(mStack, this.toolTip, mouseX, mouseY);
		}
	}	
	
	@Override
	public void tick() {
		search.tick();
		worldList.setSelected(selected);
	}
	
	private void reload() {
		worldList.children().clear();
		controller.getDbxWorlds().forEach(world -> {
			worldList.children().add(new DbxWorldList.Entry(worldList, world));
		});
		setSelected(null); 
	}
	
	public void updateWorldInfo() {
		boolean active = selected != null;
		this.downloadButton.active = active;
		this.uploadButton.active = active;
		this.addMemberButton.active = active;
				
		if(active) {
			this.worldPanel.setWorldInfo((DbxWorld) selected.getWorld());
		} else {
			this.worldPanel.clearInfo();
		}
	}
	
	public void onClose() {
		this.minecraft.setScreen(new WorldSelectionScreen(new MainMenuScreen()));
	}
	
	public void setToolTip(List<IReorderingProcessor> p_239026_1_) {
		this.toolTip = p_239026_1_;
	}
	
	public FontRenderer getFontRenderer() {
		return font;
	}
	
	public void setSelected(DbxWorldList.Entry entry) {
		this.selected = entry == this.selected ? null : entry;
		System.out.println("updated");
		updateWorldInfo();
	}
	
}
