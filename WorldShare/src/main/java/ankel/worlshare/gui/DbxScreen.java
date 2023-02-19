package ankel.worlshare.gui;

import java.util.List;

import com.dropbox.core.v2.DbxClientV2;
import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.gui.WorldList.Entry;
import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.World;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;

@OnlyIn(Dist.CLIENT)
public class DbxScreen extends Screen {
	private static final int PADDING = 6;
	private static final int LIST_WIDTH = 130;
	
	private final DbxController controller;
	private WorldList worldList;
	private WorldList.Entry selected;
	private WorldPanel worldPanel;
	private TextFieldWidget search;
	private List<IReorderingProcessor> toolTip;
	
	public DbxScreen(WorldSelectionScreen lastScreen, DbxClientV2 dbxclient) {
		super(new TranslationTextComponent("DropBox"));
		this.controller = new DbxController(lastScreen.getMinecraft(),
				dbxclient, () -> reload());
	}
	
	@Override
	protected void init() {
		int y = this.height - PADDING - 20;
		this.addButton(new Button(((
				LIST_WIDTH + PADDING + this.width - 200) / 2), 
				y, 200, 20, 
				new TranslationTextComponent("gui.done"), 
				b -> onClose()));
		
		this.addButton(new Button(PADDING, y, LIST_WIDTH, 20,
				new StringTextComponent("Refresh"),
				b -> reload())); 
		
		y -= PADDING + 20;
		this.addButton(new Button(PADDING, y, LIST_WIDTH, 20,
				new StringTextComponent("Add world"),
				b -> System.out.println("TODO"))); 
		
		y -= PADDING;
	    this.worldList = new WorldList(this, minecraft, LIST_WIDTH, 
	    		30 + (PADDING * 2), y, worldList);
	    this.worldList.setLeftPos(PADDING);
	    this.children.add(worldList);

		this.search =  new TextFieldWidget(getFontRenderer(), 
				PADDING + 1, PADDING + 10,  LIST_WIDTH - 2, 14, 
				new StringTextComponent("Search"));
		children.add(search);
		search.setFocus(false);
        search.setCanLoseFocus(true);
		
        this.worldPanel = new WorldPanel(this,
        		this.width - LIST_WIDTH - (PADDING * 3),
        		this.height - 20 - 10 - (PADDING * 2),
        		20 + PADDING,
        		worldList.getRight() + PADDING);
        children.add(worldPanel);
		
        reload();
	}
	
	@Override
	public void tick() {
		search.tick();
		worldList.setSelected(selected);
		
		updateWorldInfo();
	}
	
	private void reload() {
		worldList.children().clear();
		controller.getDbxWorlds().forEach(world -> {
			worldList.children().add(new WorldList.Entry(worldList, world));
		});
		setSelected(null); 
	}
	
	public void updateWorldInfo() {
		
	}
	
	@Override
	public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
		this.renderDirtBackground(0);
		this.toolTip = null;
		this.worldList.render(mStack, mouseX, mouseY, partialTicks);
		this.worldPanel.render(mStack, mouseX, mouseY, partialTicks);
		this.search.render(mStack, mouseX, mouseY, partialTicks);
		drawCenteredString(mStack, this.font, this.title, this.width / 2, 8, 16777215);
		super.render(mStack, mouseX, mouseY, partialTicks);
		if (this.toolTip != null) {
			this.renderTooltip(mStack, this.toolTip, mouseX, mouseY);
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
	
	public void setSelected(WorldList.Entry entry) {
		this.selected = entry == this.selected ? null : entry;
		updateWorldInfo();
	}
	
}
