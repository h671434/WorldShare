package ankel.worlshare.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.world.World;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;

public class WorldPanel extends ScrollPanel {
	private static final int PADDING = 6;
	private static final int TITLE_HEIGHT = 9;
	private static final int ICON_LENGTH = 48;
	
	private DbxScreen screen;
	private World world;
	private List<String> members = new ArrayList<>();

	public WorldPanel(DbxScreen screen, int width, int height, int top, int left) {
		super(screen.getMinecraft(), width, height, top, left);
		this.screen = screen;
	}

	@Override
	protected int getContentHeight() {
		int worldsHeight = TITLE_HEIGHT + ICON_LENGTH + (PADDING * 2);
		int membersHeight = TITLE_HEIGHT + (PADDING * 2) 
				+ (members.size() * (screen.getFontRenderer().lineHeight + PADDING));
		return worldsHeight + membersHeight;
	}
	
	@Override
	protected int getScrollAmount() {
		return screen.getFontRenderer().lineHeight * 3;
	}

	@Override
	protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, 
			Tessellator tess, int mouseX, int mouseY) {
	
		
	}

}
