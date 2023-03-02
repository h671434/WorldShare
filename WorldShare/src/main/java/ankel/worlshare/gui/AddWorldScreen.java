package ankel.worlshare.gui;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.world.WorldController;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionList;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AddWorldScreen extends Screen {
	private final Screen lastScreen;
	private final WorldController controller;
	private TextFieldWidget searchBox;
	private AddWorldList list;
	private Button selectButton;
	
	public AddWorldScreen(Screen lastScreen, WorldController controller) {
		super(new StringTextComponent("Add world to DropBox"));
		this.lastScreen = lastScreen;
		this.controller = controller;
	}
	
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	public void tick() {
		this.searchBox.tick();
	}
	
	@Override
	public void init() {
		 this.searchBox = new TextFieldWidget(this.font, this.width / 2 - 100, 
				 22, 200, 20, this.searchBox, 
				 new TranslationTextComponent("selectWorld.search"));
	     this.searchBox.setResponder((p_214329_1_) -> {
	    	 this.list.refreshList(() -> {
	    		 return p_214329_1_;
	    	 }, false);
	     });
	     this.children.add(searchBox);
	     
	     this.list = new AddWorldList(this, this.width, 
	    		 this.height, 48, this.height - 64, 36, () -> {
	    			 return this.searchBox.getValue();
	    		 }, this.list);
	     children.add(list);
	     
	     this.selectButton = this.addButton(new Button(this.width / 2 - 154,
	    		 this.height - 48, 150, 20, new StringTextComponent("Select"), (p) -> {
	    			 this.list.getSelectedOpt().ifPresent(AddWorldList.Entry::addWorld);
	    		 }));
		
	     this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 
	    		 20, DialogTexts.GUI_CANCEL, (p) -> {
	    			 this.minecraft.setScreen(this.lastScreen);
	    		 }));
	     
	     this.updateButtonStatus(false);
	     this.setInitialFocus(this.searchBox);
	}
	
	public WorldController getController() {
		return controller;
	}
	
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		return super.keyPressed(pKeyCode, pScanCode, pModifiers) 
				? true : this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	public boolean charTyped(char pCodePoint, int pModifiers) {
		return this.searchBox.charTyped(pCodePoint, pModifiers);
	}

	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		this.searchBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 8, 16777215);
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	public void updateButtonStatus(boolean activate) {
	    this.selectButton.active = activate;
	}

	public void removed() {
		if (this.list != null) {
			this.list.children().forEach(AddWorldList.Entry::close);
		}	
	}

}
