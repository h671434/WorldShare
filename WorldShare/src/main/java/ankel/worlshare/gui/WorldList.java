package ankel.worlshare.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
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
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	public List<World> list;
	private final ITextComponent title;
	private final DbxScreen screen;
	private final DbxController controller;
	
	public WorldList(DbxScreen screen, DbxController controller, Minecraft p_i241200_1_, int p_i241200_2_, 
			int p_i241200_3_, ITextComponent p_i241200_4_,  @Nullable WorldList list) {
		super(p_i241200_1_, p_i241200_2_, p_i241200_3_, 32, p_i241200_3_ - 55 + 4, 36);
		this.title = p_i241200_4_;
		this.screen = screen;
		this.controller = controller;
	    this.centerListVertically = false;
	    this.setRenderHeader(true, (int)(9.0F * 1.5F));
	    if(list != null)
	    	this.list = list.list;
	}

	protected void renderHeader(MatrixStack p_230448_1_, int p_230448_2_, 
		   int p_230448_3_, Tessellator p_230448_4_) {
	    ITextComponent itextcomponent = (new StringTextComponent("")).append(this.title)
	    		.withStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD);
	    this.minecraft.font.draw(p_230448_1_, itextcomponent, 
	    		(float)(p_230448_2_ + this.width / 2 - this.minecraft.font.width(itextcomponent) / 2),
	    		(float)Math.min(this.y0 + 3, p_230448_3_), 16777215);
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
	
    @Nullable
    private ResourceLocation loadServerIcon(World world) {
		String s = world.getWorldName();
		File iconFile = world.getWorldIcon();
		ResourceLocation iconLocation = new ResourceLocation("minecraft", "worlds/" 
				+ Util.sanitizeName(s, ResourceLocation::validPathChar) 
				+ "/" + Hashing.sha1().hashUnencodedChars(s) 
				+ "/icon");
        boolean flag = iconFile != null && iconFile.isFile();
        if (flag) {
        	try (InputStream inputstream = new FileInputStream(iconFile)) {
        		NativeImage nativeimage = NativeImage.read(inputstream);
        		Validate.validState(nativeimage.getWidth() == 64, "Must be 64 pixels wide");
        		Validate.validState(nativeimage.getHeight() == 64, "Must be 64 pixels high");
        		DynamicTexture dynamictexture = new DynamicTexture(nativeimage);
        		minecraft.getTextureManager().register(iconLocation, dynamictexture);
        		return iconLocation;
        	} catch (FileNotFoundException filenotfoundexception) {
        	} catch (Exception e) {
        		LOGGER.warn("Failed to load icon from pack {}", s, e);
            }
         } else {
            minecraft.getTextureManager().release(iconLocation);
         }
        
        return ICON_MISSING;
     }
	
	@OnlyIn(Dist.CLIENT)
	public static class Entry extends ExtendedList.AbstractListEntry<WorldList.Entry> {
		protected final Minecraft minecraft;
	    protected final DbxScreen screen;
	    private final WorldList parent;
	    private final World world;
	    private final IReorderingProcessor nameDisplayCache;
	    private final IBidiRenderer descriptionDisplayCache;
	    
		public Entry(WorldList parent, World world) {
			this.minecraft = Minecraft.getInstance();
			this.screen = parent.getScreen();
			this.parent = parent;
			this.world = world;
			this.nameDisplayCache = cacheName(minecraft, new StringTextComponent(world.getWorldName()));
			this.descriptionDisplayCache = cacheDescription(minecraft, 
					new StringTextComponent(new SimpleDateFormat().format(world.getLastModified())));
		}
		
		public World getWorld() {
			return world;
		}
		
		private static IReorderingProcessor cacheName(Minecraft minecraft, ITextComponent textcomponent) {
			int i = minecraft.font.width(textcomponent);
			if( i < 157) {
				ITextProperties textporperties = ITextProperties.composite(
						minecraft.font.substrByWidth(textcomponent, 157 - minecraft.font.width("")),
						ITextProperties.of(""));
				return LanguageMap.getInstance().getVisualOrder(textporperties);
			} 
			return textcomponent.getVisualOrderText();
		}
		
	    private static IBidiRenderer cacheDescription(Minecraft minecraft, ITextComponent textcomponent) {
	    	return IBidiRenderer.create(minecraft.font, textcomponent, 157, 2);
	    }
	    
	    
	    public void render(MatrixStack p_230432_1_, int p_230432_2_, 
	    		int p_230432_3_, int p_230432_4_, int p_230432_5_, 
	    		int p_230432_6_, int p_230432_7_, int p_230432_8_, 
	    		boolean p_230432_9_, float p_230432_10_) {	        
	    	
	        this.minecraft.getTextureManager().bind(this.parent.loadServerIcon(world));
	        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	        AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 0.0F, 0.0F, 32, 32, 32, 32);
	        
	        IReorderingProcessor ireorderingprocessor = this.nameDisplayCache;
	        IBidiRenderer ibidirenderer = this.descriptionDisplayCache;
	         
	        if ((this.minecraft.options.touchscreen || p_230432_9_)) {
	        	this.minecraft.getTextureManager().bind(WorldList.ICON_OVERLAY_LOCATION);
	        	AbstractGui.fill(p_230432_1_, p_230432_4_, p_230432_3_, p_230432_4_ + 32, 
	        			p_230432_3_ + 32, -1601138544);
	            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	            int i = p_230432_7_ - p_230432_4_;
	            int j = p_230432_8_ - p_230432_3_;
	            
	            if(world instanceof LocalWorld) {
	            	if (i < 32) {
	            		AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 0.0F, 32.0F, 32, 32, 256, 256);
	                } else {
	                	AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 0.0F, 0.0F, 32, 32, 256, 256);
	                }
	            } else {
	            	if(world instanceof DbxWorld) {
		            	if (i < 16) {
		            		AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 32.0F, 32.0F, 32, 32, 256, 256);
		                } else {
		                	AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 32.0F, 0.0F, 32, 32, 256, 256);
		                }
	            	}
	            }
	         }
	        
	        this.minecraft.font.drawShadow(p_230432_1_, ireorderingprocessor, (float)(p_230432_4_ + 32 + 2), (float)(p_230432_3_ + 1), 16777215);
	        ibidirenderer.renderLeftAligned(p_230432_1_, p_230432_4_ + 32 + 2, p_230432_3_ + 12, 10, 8421504);
	    }
	    
		public boolean mouseClicked(double a, double b, int c) {
			double d0 = a - (double)this.parent.getRowLeft();
			this.parent.getScreen().setSelected(world);
			if(d0 <= 32.0D && d0 >= 0 && world instanceof DbxWorld) {
				this.parent.controller.downloadWorld((DbxWorld)world);
				return true;
			}
			if(d0 < 32D && world instanceof LocalWorld) {
				this.parent.controller.uploadWorld((LocalWorld)world);
				return true;
			}
			
			return false;
		}
	}
}
