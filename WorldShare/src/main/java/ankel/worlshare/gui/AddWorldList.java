package ankel.worlshare.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import ankel.worlshare.world.LocalWorld;
import ankel.worlshare.world.WorldController;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.ErrorScreen;
import net.minecraft.client.gui.screen.WorldSelectionList;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldSummary;

public class AddWorldList extends ExtendedList<AddWorldList.Entry> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	private static final ResourceLocation ICON_MISSING = 
			new ResourceLocation("textures/misc/unknown_server.png");
   
	private final AddWorldScreen screen;
	@Nullable
	private List<WorldSummary> cachedList;
	
	public AddWorldList(AddWorldScreen screen, int width,
			int height, int top, int bottom, int itemHeight,
			Supplier<String> searchFilter, @Nullable AddWorldList list) {
		super(screen.getMinecraft(), width, height, top, bottom, itemHeight);
		this.screen = screen;
		if(list != null)  {
			this.cachedList = list.cachedList;
		}
		this.refreshList(searchFilter, false);
	}
	
	public void refreshList(Supplier<String> searchFilter, boolean reloadList) {
		this.clearEntries();
		SaveFormat saveformat = this.minecraft.getLevelSource();
		if (this.cachedList == null || reloadList) {
			try {
				this.cachedList = saveformat.getLevelList();
			} catch (AnvilConverterException anvilconverterexception) {
				LOGGER.error("Couldn't load level list", (Throwable)anvilconverterexception);
				this.minecraft.setScreen(new ErrorScreen(
						new TranslationTextComponent("selectWorld.unable_to_load"), 
						new StringTextComponent(anvilconverterexception.getMessage())));
				return;
			}

			Collections.sort(this.cachedList);
		}

		String s = searchFilter.get().toLowerCase(Locale.ROOT);
		for(WorldSummary worldsummary : this.cachedList) {
			if (worldsummary.getLevelName().toLowerCase(Locale.ROOT).contains(s) 
					|| worldsummary.getLevelId().toLowerCase(Locale.ROOT).contains(s)) {
        	this.addEntry(new AddWorldList.Entry(this, worldsummary));
			}
		}
	}
	
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
	}

	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	protected boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	public void setSelected(@Nullable AddWorldList.Entry pEntry) {
		super.setSelected(pEntry);
		if (pEntry != null) {
			WorldSummary worldsummary = pEntry.summary;
			NarratorChatListener.INSTANCE.sayNow((
					new TranslationTextComponent("narrator.select", 
					new TranslationTextComponent("narrator.select.world", 
							worldsummary.getLevelName(), 
							new Date(worldsummary.getLastPlayed()), 
							worldsummary.isHardcore() 
								? new TranslationTextComponent("gameMode.hardcore") 
								: new TranslationTextComponent("gameMode." 
									+ worldsummary.getGameMode().getName()), 
							worldsummary.hasCheats() 
								? new TranslationTextComponent("selectWorld.cheats") 
								: StringTextComponent.EMPTY, 
									worldsummary.getWorldVersionName()))).getString());
		}

		this.screen.updateButtonStatus(pEntry != null && !pEntry.summary.isLocked());
	}

	protected void moveSelection(AbstractList.Ordering pOrdering) {
		this.moveSelection(pOrdering, (p_241652_0_) -> {
			return !p_241652_0_.summary.isLocked();
		});
	}

	public Optional<AddWorldList.Entry> getSelectedOpt() {
		return Optional.ofNullable(this.getSelected());
	}

	public AddWorldScreen getScreen() {
	      return this.screen;
	}

	public final class Entry 
			extends ExtendedList.AbstractListEntry<AddWorldList.Entry>
			implements AutoCloseable {
		private final Minecraft minecraft;
		private final AddWorldScreen screen;
		private final WorldSummary summary;
		private final ResourceLocation iconLocation;
		private File iconFile;
		@Nullable
		private final DynamicTexture icon;
		private long lastClickTime;
		
		public Entry(AddWorldList p_i242066_2_, WorldSummary p_i242066_3_) {
			this.screen = p_i242066_2_.getScreen();
			this.summary = p_i242066_3_;
			this.minecraft = Minecraft.getInstance();
			String s = p_i242066_3_.getLevelId();
			this.iconLocation = new ResourceLocation("minecraft", "worlds/" 
					+ Util.sanitizeName(s, ResourceLocation::validPathChar) 
					+ "/" + Hashing.sha1().hashUnencodedChars(s) + "/icon");
			this.iconFile = p_i242066_3_.getIcon();
			if (!this.iconFile.isFile()) {
				this.iconFile = null;
			}

			this.icon = this.loadServerIcon();
		}
		
		public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
			if (this.summary.isLocked()) {
        	  return true;
			} else {
				AddWorldList.this.setSelected(this);
				this.screen.updateButtonStatus(AddWorldList.this.getSelectedOpt().isPresent());
				if (pMouseX - (double)AddWorldList.this.getRowLeft() <= 32.0D) {
					this.addWorld();
				return true;
				} else if (Util.getMillis() - this.lastClickTime < 250L) {
					this.addWorld();
					return true;
				} else {
					this.lastClickTime = Util.getMillis();
					return false;
				}
			}
       }

		public void render(MatrixStack pMatrixStack, int pIndex, int pTop, 
				int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, 
				boolean pIsMouseOver, float pPartialTicks) {
			String s = this.summary.getLevelName();
			String s1 = this.summary.getLevelId() + " (" 
			+ AddWorldList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
			if (StringUtils.isEmpty(s)) {
				s = I18n.get("selectWorld.world") + " " + (pIndex + 1);
			}
			
			ITextComponent itextcomponent = this.summary.getInfo();
			this.minecraft.font.draw(pMatrixStack, s, 
					(float)(pLeft + 32 + 3), (float)(pTop + 1), 16777215);
			this.minecraft.font.draw(pMatrixStack, s1, 
					(float)(pLeft + 32 + 3), (float)(pTop + 9 + 3), 8421504);
			this.minecraft.font.draw(pMatrixStack, itextcomponent, 
					(float)(pLeft + 32 + 3), (float)(pTop + 9 + 9 + 3), 8421504);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager()
				.bind(this.icon != null ? this.iconLocation : AddWorldList.ICON_MISSING);
			RenderSystem.enableBlend();
			AbstractGui.blit(pMatrixStack, pLeft, pTop, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
		}

		@Nullable
      	private DynamicTexture loadServerIcon() {
			boolean flag = this.iconFile != null && this.iconFile.isFile();
			if (flag) {
				try (InputStream inputstream = new FileInputStream(this.iconFile)) {
					NativeImage nativeimage = NativeImage.read(inputstream);
					Validate.validState(nativeimage.getWidth() == 64, "Must be 64 pixels wide");
         	     Validate.validState(nativeimage.getHeight() == 64, "Must be 64 pixels high");
         	     DynamicTexture dynamictexture = new DynamicTexture(nativeimage);
         	     this.minecraft.getTextureManager().register(this.iconLocation, dynamictexture);
         	     return dynamictexture;
				} catch (Throwable throwable) {
					AddWorldList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), throwable);
					this.iconFile = null;
					return null;
				}
			} else {
				this.minecraft.getTextureManager().release(this.iconLocation);
				return null;
			}
		}
		
		public void addWorld() {
			this.screen.getController().uploadWorld(new LocalWorld(summary));
			this.screen.onClose();
		}

		public void close() {
			if (this.icon != null) {
        	 this.icon.close();
			}
		}
	}
}
