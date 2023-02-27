package ankel.worlshare.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import ankel.worlshare.world.World.Status;
import net.minecraft.util.ResourceLocation;

public class DbxWorld implements World {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String ROOT_FOLDER = "/WorldShare/";
	private static final String ICON_FILE = "/icon.png";
	
	private final DbxClientV2 client;
	
	private String name;
	private ResourceLocation icon;
	private Date lastModified;
	private String lastPerson;
	private LocalWorld localWorld;
	private List<String> members;
	
	private Status status;
	
	public DbxWorld(DbxClientV2 client, String name, Date lastModified,
			String lastPerson, LocalWorld localWorld, List<String> members,
			Status status) {
		this.client = client;
		this.name = name;
		this.icon = null;
		this.lastModified = lastModified;
		this.lastPerson = lastPerson;
		this.localWorld = localWorld;
		this.members = members;
		this.status = status;
	}	

	
	@Override
	public ResourceLocation getServerIcon() {
		if(icon == null) {
			File localPath = new File("/saves/" + ROOT_FOLDER + name).getAbsoluteFile();
			new File(localPath.getParent()).mkdirs();
			try(OutputStream out = new FileOutputStream(localPath)) {
				client.files().getThumbnail(ROOT_FOLDER + name + ICON_FILE).download(out);
			} catch (DbxException | IOException e) {
				LOGGER.error(e.getMessage());
			}
			icon = loadServerIcon(new File(localPath + ICON_FILE));
		}
		
		return icon;	
	}
	
	@Override
	public String getWorldName() {
		return name;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String getLastPerson() {
		return lastPerson;
	}
	
	public LocalWorld getLocalWorld() {
		return localWorld;
	}
	
	public List<String> getMembers() {
		return members;
	}
	
	@Override
	public String getStatus() {
		return status.getValue();
	}
	
	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
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
