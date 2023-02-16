package ankel.worlshare.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

public class DbxWorld implements World {
	private static final String ROOT_FOLDER = "/WorldShare/";
	private static final String ICON_FILE = "/icon.png";
	private static final String LEVEL_FILE = "/level.dat";
	private final DbxClientV2 client;
	private final String worldname;
	
	public DbxWorld(DbxClientV2 client, String worldname) {
		this.client = client;
		this.worldname = worldname;
	}	

	@Override
	public String getWorldName() {
		return worldname;
	}

	@Override
	public File getWorldIcon() {
		File localPath = new File("/saves/" + worldname + ICON_FILE).getAbsoluteFile();
		new File(localPath.getParent()).mkdir();
		try(OutputStream out = new FileOutputStream(localPath)) {
			client.files().getThumbnail(ROOT_FOLDER + worldname + ICON_FILE).download(out);
		} catch (DbxException | IOException e) {
		}
		
		return localPath;	
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
