package ankel.worlshare.gui;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.v2.DbxClientV2;
import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.gui.WorldList.Entry;
import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.LocalWorld;
import ankel.worlshare.world.World;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DbxScreen extends Screen {
	private static final ITextComponent DRAG_AND_DROP = (new TranslationTextComponent("Drag and drop worlds to upload or download from DropBox")).withStyle(TextFormatting.GRAY);
	private final DbxController dbxController;
	private WorldList dbxlist;
	private WorldList locallist;
	private Button shareButton;
	private Button deleteButton;
	private List<IReorderingProcessor> toolTip;
	
	public DbxScreen(WorldSelectionScreen lastScreen, DbxClientV2 dbxclient) {
		super(new TranslationTextComponent("DropBox"));
		this.dbxController = new DbxController(lastScreen.getMinecraft(), dbxclient, () -> reload());
	}
	
	protected void init() {
	    this.locallist = new WorldList(this, dbxController, this.minecraft,
	    		200, this.height, new TranslationTextComponent("Local"), this.locallist);
	    this.locallist.setLeftPos(this.width / 2 - 4 - 200);
	    this.dbxlist = new WorldList(this, dbxController, this.minecraft, 200, 
	    		this.height, new TranslationTextComponent("DropBox"), this.dbxlist);
	    this.dbxlist.setLeftPos(this.width / 2 + 4);
	    this.children.add(this.locallist);
	    this.children.add(this.dbxlist);
		
		this.shareButton = this.addButton(new Button(
				this.width / 2 - 154, this.height - 44, 150, 20, 
				new TranslationTextComponent("Share"),
				e -> {this.minecraft.setScreen(new ShareScreen(this, dbxController, 
							(DbxWorld) dbxlist.getSelected().getWorld()));
		}));
		this.addButton(new Button(this.width / 2 + 4, this.height - 44, 150, 20, 
				DialogTexts.GUI_DONE, (p_214327_1_) -> {
					this.onClose();
		}));

		this.reload();
	}
	
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderDirtBackground(0);
		this.toolTip = null;
		this.locallist.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		this.dbxlist.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 8, 16777215);
		drawCenteredString(p_230430_1_, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
		super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		if (this.toolTip != null) {
			this.renderTooltip(p_230430_1_, this.toolTip, p_230430_2_, p_230430_3_);
		}
	}	
	
	private void reload() {
		this.populateLists();
	    this.updateButtonStatus();   
	}
	
	private void populateLists() {
		this.updateList(this.dbxlist, this.dbxController.getDbxWorlds());
		this.updateList(this.locallist, this.dbxController.getLocalWorlds());
	}
	
	private void updateList(WorldList list, List<World> worlds) {
		list.children().clear();
		worlds.forEach(world -> {
			list.children().add(new WorldList.Entry(list, world));
		});
	}
	
	public void updateButtonStatus() {
		this.shareButton.active = dbxlist.getSelected() != null;
	}
	
	public void onClose() {
		this.minecraft.setScreen(new WorldSelectionScreen(new MainMenuScreen()));
	}
	
	public void setSelected(World world) {
		dbxlist.setSelected(null);
		locallist.setSelected(null);
		for(Entry entry : dbxlist.children()) {
			if (entry.getWorld().equals(world)) {
				dbxlist.setSelected(entry);
			}
		}
		for(Entry entry : locallist.children()) {
			if (entry.getWorld().equals(world)) {
				locallist.setSelected(entry);
			}
		}
		updateButtonStatus();
	}
	
	public void setToolTip(List<IReorderingProcessor> p_239026_1_) {
		this.toolTip = p_239026_1_;
	}
	
}
