package ankel.worlshare.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.WorldController;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AddMemberScreen extends Screen {
	private static final ITextComponent ENTER_EMAIL =
			new StringTextComponent("Enter new members email");
	private Screen lastScreen;
	private WorldController controller;
	private DbxWorld world;
	
	private TextFieldWidget memberInput;
	
	public AddMemberScreen(Screen lastScreen, WorldController controller, DbxWorld world) {
		super(new StringTextComponent("Add member"));
		this.lastScreen = lastScreen;
		this.controller = controller;
		this.world = world;
	}

	@Override
	public void init() {
		this.memberInput = new TextFieldWidget(this.font, this.width / 2 - 125, 
				38 + 50, 250, 20, new TranslationTextComponent("Enter token"));
		children.add(memberInput);
	
		this.addButton(new Button(this.width / 2 - 100, 126 + 50, 200, 20,  
				new TranslationTextComponent("Add member"), e -> {
					this.controller.shareWorld(world, memberInput.getValue());
		}));
		
		this.addButton(new Button(this.width / 2 - 100, 100 + 50, 200, 20, 
				DialogTexts.GUI_DONE, e -> {
					this.minecraft.setScreen(lastScreen);
		}));
	}
	
	@Override
	public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
		this.renderDirtBackground(0);
		drawCenteredString(mStack, this.font, this.title, this.width / 2, 15, 16777215);
	    drawCenteredString(mStack, this.font, ENTER_EMAIL, this.width / 2, 30, 10526880);
		this.memberInput.render(mStack, mouseX, mouseY, partialTicks);
		super.render(mStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void tick() {
		this.memberInput.tick();
	}
}
