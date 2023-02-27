package ankel.worlshare.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;

import com.google.common.hash.Hashing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public interface World extends Comparable<World> {
	static final ResourceLocation ICON_MISSING = 
			new ResourceLocation("textures/misc/unknown_server.png");
	
	public enum Status {
		UP_TO_DATE("Up to date"), 
		NEEDS_DOWNLOAD("Old local version"),
		NEEDS_UPLOAD("Old DropBox version"),
		DISCONNECTED("Unable to connect"),
		NOT_DOWNLOADED("No local version");
		private String value;
		private Status(String value) {
			this.value = value;
		}	
		public String getValue() {
			return value;
		}
	}
	
	@Nullable
	default ResourceLocation loadServerIcon(File iconFile) {
		System.out.println("loading icon");
		String s = this.getWorldName();
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
				Minecraft.getInstance().getTextureManager().register(iconLocation, dynamictexture);
				return iconLocation;
			} catch (FileNotFoundException filenotfoundexception) {
				LogManager.getLogger().warn(filenotfoundexception.getMessage());
			} catch (Exception e) {
				LogManager.getLogger().warn("Failed to load icon from pack {}", s, e);
			}
		} else {
			Minecraft.getInstance().getTextureManager().release(iconLocation);
		}
    
		return ICON_MISSING;
	}
	
	String getWorldName();
	
	String getLastPerson();
	
	Date getLastModified();
	
	ResourceLocation getServerIcon();
	
	String getStatus();
	
	void setStatus(Status status);
	
}
