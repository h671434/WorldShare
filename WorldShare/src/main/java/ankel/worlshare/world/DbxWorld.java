package ankel.worlshare.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import ankel.worlshare.world.World.Status;
import net.minecraft.util.ResourceLocation;

public class DbxWorld implements World {
	private static final String ROOT_FOLDER = "/WorldShare/";
	private static final String ICON_FILE = "/icon.png";
	private static final String LEVEL_FILE = "/level.dat";
	private final DbxClientV2 client;
	private final String worldname;
	private ResourceLocation icon;
	private Status status;
	
	public DbxWorld(DbxClientV2 client, String worldname) {
		this.client = client;
		this.worldname = worldname;
		icon = null;
		status = Status.UP_TO_DATE;
	}	


	public LocalWorld getLocalWorld() {
		return null;
	}
	
	public List<String> getMembers() {
		return Collections.emptyList();
	}
	
	@Override
	public ResourceLocation getServerIcon() {
		if(icon == null) {
			File localPath = new File("/saves/" + worldname + ICON_FILE).getAbsoluteFile();
			new File(localPath.getParent()).mkdir();
			try(OutputStream out = new FileOutputStream(localPath)) {
				client.files().getThumbnail(ROOT_FOLDER + worldname + ICON_FILE).download(out);
			} catch (DbxException | IOException e) {
			}
			icon = loadServerIcon(localPath);
		}
		
		return icon;	
	}
	
	@Override
	public String getWorldName() {
		return worldname;
	}

	@Override
	public String getLastPerson() {
		return "";
	}
	
	@Override
	public Date getLastModified() {
		try {
			Metadata data = client.files().getMetadata(ROOT_FOLDER + worldname + LEVEL_FILE);
			if (data instanceof FileMetadata) {
				return ((FileMetadata) data).getClientModified();
			}
		} catch (DbxException e) {
			System.out.println(e);
		}
		return new Date(0);
	}
	
	@Override
	public String getStatus() {
		return status.getValue();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(worldname);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof World))
			return false;
		World other = (World) obj;
		return this.getWorldName().trim().equalsIgnoreCase(other.getWorldName().trim());
	}
	
	@Override
	public int compareTo(World o) {
		return this.getWorldName().compareTo(o.getWorldName());
	}

}
