package ankel.worlshare.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.gui.WorldList.Entry;
import ankel.worlshare.world.DbxWorld;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionList;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShareScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ITextComponent EMAIL_LABEL = new TranslationTextComponent("Enter new members e-mail to share");
	private final DbxScreen lastScreen; 
	private final DbxController dbxController;
	private final DbxWorld world;
	private TextFieldWidget emailInput;
	
	public ShareScreen(DbxScreen lastScreen, DbxController dbxController, DbxWorld dbxWorld) {
		super(new StringTextComponent("Share"));
		this.lastScreen = lastScreen;
		this.dbxController = dbxController;
		this.world = dbxWorld;
	}
	
	public void tick() {
		this.emailInput.tick();
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}
	
	public void init() {
		this.emailInput = new TextFieldWidget(this.font, this.width / 2 - 125, 38 + 50, 250, 20,
				this.emailInput, new StringTextComponent("Enter email"));
		this.children.add(emailInput);
		this.addButton(new Button(this.width / 2 - 100, 74 + 50, 200, 20,
				new StringTextComponent("Add member"), (e) -> {
					LOGGER.info("Adding member " + emailInput.getValue() + " to DropBox folder");
					dbxController.shareWorld(this.world, emailInput.getValue());
					emailInput.setValue("");
					
		}));
		this.addButton(new Button(this.width / 2 - 100, 100 + 50, 200, 20, 
				DialogTexts.GUI_DONE, (p_214327_1_) -> {
			this.minecraft.setScreen(this.lastScreen);
      	}));
	}
	
	@Override
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
	      this.renderBackground(p_230430_1_);
	      drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 15, 16777215);
	      drawCenteredString(p_230430_1_, this.font, EMAIL_LABEL, this.width / 2, 30, 10526880);
	      this.emailInput.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	      super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	   }

	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
		return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_) ? true : this.emailInput.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}
	
}
