package ankel.worlshare.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.config.WorldShareConfigs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class ConfigScreen extends Screen {
	private static final ITextComponent AUTO_UPLOAD_TEXT = 
			new StringTextComponent("Automatic upload");
	private static final ITextComponent AUTO_DOWNLOAD_TEXT = 
			new StringTextComponent("Automatic download");
	
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_TOP_OFFSET = 26;
	
	private final Screen lastScreen;
	private OptionsRowList optionsRowList;
	
	public ConfigScreen(Screen lastScreen) {
		super(new StringTextComponent("WorldShare Config"));
		this.lastScreen = lastScreen;
	}
	
	@Override
	protected void init() {
		this.optionsRowList = new OptionsRowList(
				this.minecraft, this.width, this.height,
				24, this.height - 32, 25);
		
		this.optionsRowList.addBig(new BooleanOption(
				"Auto upload on world close", 
				unused -> WorldShareConfigs.AUTO_UPLOAD.get(), 
				(unused, newValue) -> WorldShareConfigs.AUTO_UPLOAD.set(newValue)
		));
		this.optionsRowList.addBig(new BooleanOption(
				"Auto upload on world close", 
				unused -> WorldShareConfigs.AUTO_DOWNLOAD.get(), 
				(unused, newValue) -> WorldShareConfigs.AUTO_DOWNLOAD.set(newValue)
		));
		
		this.children.add(optionsRowList);
		
        this.addButton(new Button(
                (this.width - BUTTON_WIDTH) / 2,
                this.height - DONE_BUTTON_TOP_OFFSET,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                new TranslationTextComponent("gui.done"),
                button -> this.onClose()
        ));
	}
	
	@Override
	public void render(MatrixStack mStack, int mouseX, int mouseY, 
			float partialTicks) {
		this.renderBackground(mStack);
		drawCenteredString(mStack, this.font, this.title,
				this.width / 2, 8, 16777215);
		this.optionsRowList.render(mStack, mouseX, mouseY, partialTicks);
		super.render(mStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onClose() {
		WorldShareConfigs.SPEC.save();
		this.minecraft.setScreen(lastScreen);
	}

}
