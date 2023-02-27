package ankel.worlshare.gui;

import ankel.worlshare.world.WorldController;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class AddWorldScreen extends Screen {
	private Screen lastScreen;
	private WorldController controller;
	private AddWorldList list;
	
	public AddWorldScreen(Screen lastScreen, WorldController controller) {
		super(new StringTextComponent("Add world to DropBox"));
	}
	
	@Override
	public void init() {
		this.list = new AddWorldList(this.minecraft, this.width, this.height, 48, this.height - 64, 36);
		children.add(list);
		
		this.addButton(new Button(this.width / 2 + 82, this.height - 28, 72, 20, DialogTexts.GUI_CANCEL, (p_214327_1_) -> {
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

}
