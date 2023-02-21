package ankel.worlshare.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.LocalWorld;
import ankel.worlshare.world.World;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldList extends ExtendedList<WorldList.Entry> {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public List<World> list;
	private final DbxScreen screen;
	
	public WorldList(DbxScreen screen, Minecraft mc, int width, 
			int top, int bottom,  @Nullable WorldList list) {
		super(mc, width, screen.height, top, bottom, 36);
		this.screen = screen;
	    this.centerListVertically = false;
	    if(list != null)
	    	this.list = list.list;
	}
	
	protected int getScrollbarPosition() {
		return this.x1 - 6;
	}
	
	public int getRowWidth() {
		return this.width;
	}
	
	protected DbxScreen getScreen() {
		return this.screen;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Entry extends ExtendedList.AbstractListEntry<WorldList.Entry> {
		protected final Minecraft minecraft;
	    protected final DbxScreen screen;
	    private final WorldList parent;
	    private final World world;
	    private final IReorderingProcessor nameDisplayCache;
	    private final IBidiRenderer ownerDisplayCache;
	    
		public Entry(WorldList parent, World world) {
			this.minecraft = Minecraft.getInstance();
			this.screen = parent.getScreen();
			this.parent = parent;
			this.world = world;
			this.nameDisplayCache = cacheName(minecraft, 
					new StringTextComponent(world.getWorldName()));
			this.ownerDisplayCache = cacheDescription(minecraft, 
					new StringTextComponent(world.getStatus()));
		}
		
		private static IReorderingProcessor cacheName(Minecraft minecraft, 
				ITextComponent text) {
			int i = minecraft.font.width(text);
			if( i < 157) {
				ITextProperties textporperties = ITextProperties.composite(
						minecraft.font.substrByWidth(text, 130 - minecraft.font.width("")), 
						ITextProperties.of(""));
				return LanguageMap.getInstance().getVisualOrder(textporperties);
			} 
			return text.getVisualOrderText();
		}
		
	    private static IBidiRenderer cacheDescription(Minecraft minecraft, 
	    		ITextComponent textcomponent) {
	    	return IBidiRenderer.create(minecraft.font, textcomponent, 130, 2);
	    }
	    
	    @SuppressWarnings("deprecation")
		public void render(MatrixStack mStack, int p_230432_2_, 
	    		int y0, int x0, int p_230432_5_, 
	    		int p_230432_6_, int p_230432_7_, int p_230432_8_, 
	    		boolean p_230432_9_, float p_230432_10_) {	        
	    	
	        this.minecraft.getTextureManager().bind(world.getServerIcon());
	        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	        AbstractGui.blit(mStack, x0, y0, 0.0F, 0.0F, 32, 32, 32, 32);
	        
	        IReorderingProcessor ireorderingprocessor = this.nameDisplayCache;
	        IBidiRenderer ibidirenderer = this.ownerDisplayCache;
	         
	        
	        this.minecraft.font.drawShadow(mStack, ireorderingprocessor, 
	        		(float)(x0 + 32 + 2), (float)(y0 + 1), 16777215);
	        ibidirenderer.renderLeftAligned(mStack, x0 + 32 + 2, y0 + 12, 10, 8421504);
	    }
	    
		public boolean mouseClicked(double a, double b, int c) {
			this.parent.getScreen().setSelected(this);
			return false;
		}
		
		public World getWorld() {
			return world;
		}
	}
}
