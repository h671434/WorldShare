package ankel.worlshare.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.LocalWorld;
import ankel.worlshare.world.World;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class WorldPanel extends ScrollPanel {	
	private static IReorderingProcessor NO_WORLD_SELECTED =
			new StringTextComponent("No world selected").getVisualOrderText();
	private static IReorderingProcessor DROPBOX =
			new StringTextComponent("Dropbox").getVisualOrderText();
	private static IReorderingProcessor LOCAL =
			new StringTextComponent("Local").getVisualOrderText();
	private static IReorderingProcessor NO_LOCAL =
			new StringTextComponent("No local version").getVisualOrderText();
	private static IReorderingProcessor MEMBERS =
			new StringTextComponent("Members").getVisualOrderText();
	private static DateFormat DATE_FORMAT = new SimpleDateFormat();
	
	private DbxScreen screen;
	private DbxWorld dbxWorld;
	private LocalWorld localWorld;
	
	private List<IReorderingProcessor> membersCache = Collections.emptyList();
	private List<IReorderingProcessor> dbxInfoCache =  Collections.emptyList();
	private List<IReorderingProcessor> localInfoCache =  Collections.emptyList();
	
	private FontRenderer font;
	private int contentHeight = 0;

	public WorldPanel(DbxScreen screen, int width, int height, int top, int left) {
		super(screen.getMinecraft(), width, height, top, left);
		this.screen = screen;
		this.font = screen.getFontRenderer();
	}

	public void setWorldInfo(DbxWorld world) {
		this.dbxWorld = world;
		this.localWorld = world.getLocalWorld();
		this.dbxInfoCache = cacheWorldInfo(dbxWorld);
		if(localWorld != null)
			this.localInfoCache = cacheWorldInfo(localWorld);
		this.membersCache = cacheMembers(world.getMembers());
	}  
	
	private List<IReorderingProcessor> cacheWorldInfo(World world) {
		List<ITextComponent> textComponents = new ArrayList<>();
		textComponents.add(new StringTextComponent(world.getWorldName()));
		textComponents.add(new StringTextComponent(world.getLastPerson()));
		textComponents.add(new StringTextComponent(DATE_FORMAT.format(world.getLastModified())));
		
		List<IReorderingProcessor> cache = new ArrayList<>();
		for(ITextComponent text : textComponents) {
			if(font.width(text) < 157) {
				ITextProperties textporperties = ITextProperties.composite(
						font.substrByWidth(text, 130 - font.width("")), 
						ITextProperties.of(""));
				cache.add(LanguageMap.getInstance().getVisualOrder(textporperties));
			}  else {
				cache.add(text.getVisualOrderText());
			}
		}
		return cache;
	}
	
	private List<IReorderingProcessor> cacheMembers(List<String> members) {
		List<IReorderingProcessor> cache = new ArrayList<>();
		for(String s : members) {
			ITextComponent text = new StringTextComponent(s);
			cache.add(text.getVisualOrderText());
		}
		return cache;
		
	}
	
	
	public void clearInfo() {
		this.dbxWorld = null;
		this.localWorld = null;
		this.dbxInfoCache =  Collections.emptyList();
		this.localInfoCache =  Collections.emptyList();
		this.membersCache = Collections.emptyList();
	}

	@Override
	protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, 
			Tessellator tess, int mouseX, int mouseY) {
		int y = relativeY + 6;
		int middlex = this.left + (this.width / 2);
		int x = middlex - (font.width(NO_WORLD_SELECTED) / 2);
		if(dbxWorld == null) {
			font.drawShadow(mStack, NO_WORLD_SELECTED, 	x, y, 16777215);
			return;
		}
		
		x = middlex - (font.width(DROPBOX) / 2);
		font.drawShadow(mStack, DROPBOX, x, y, 16777215);
		y += 9 + 3;
		drawWorld(dbxInfoCache, dbxWorld.getServerIcon(), mStack, middlex - 65 - 3 - 32, y);
		y += 32 + 9;
		x = middlex - (font.width(LOCAL) / 2);
		font.drawShadow(mStack, LOCAL, x, y, 16777215);
		y += 9 + 3;
		if(localWorld != null ) {		
			drawWorld(localInfoCache, localWorld.getServerIcon(), mStack, middlex - 65 - 3 - 32, y);
			y += 32 + 9;
		} else {
			y += 3;
			x = middlex - (font.width(NO_LOCAL) / 2);
			font.drawShadow(mStack, NO_LOCAL, x, y, 8421504);
			y += 32 + 9;
		}
		x = middlex - (font.width(MEMBERS) / 2);
		font.drawShadow(mStack, MEMBERS, x - 3, y, 16777215);	
		y += 9 + 3;
		drawCenteredString(mStack, screen.getFontRenderer(), "You", middlex, y, 8421504);
		for(IReorderingProcessor text : membersCache) {
			y += 9 + 3;
			x = middlex - (font.width(text) / 2);
			font.drawShadow(mStack, text, x, y, 8421504);
		}
		
		this.contentHeight = y - 9;
	}
	
	@SuppressWarnings("deprecation")
	private void drawWorld(List<IReorderingProcessor> lines, ResourceLocation icon, 
			MatrixStack mStack, int x, int y) {
		this.screen.getMinecraft().getTextureManager().bind(icon);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        GuiUtils.drawInscribedRect(mStack, x, y, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        
        FontRenderer font = screen.getFontRenderer();
        RenderSystem.enableBlend();
        font.drawShadow(mStack, lines.get(0), (float)(x + 32 + 3), (float)(y + 1), 16777215);
        font.drawShadow(mStack, lines.get(1), (float)(x + 32 + 3), (float)(y + 9 + 3), 8421504);
        font.drawShadow(mStack, lines.get(2), (float)(x + 32 + 3), (float)(y + 9 + 9 + 3), 8421504);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
	}
	
	@Override
	protected int getContentHeight() {
		int noScrollHeight = this.bottom - this.top - 8;
		return contentHeight < noScrollHeight ? noScrollHeight : contentHeight;
	}
}
