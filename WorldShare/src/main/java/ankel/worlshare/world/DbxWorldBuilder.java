package ankel.worlshare.world;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import net.minecraft.world.storage.WorldSummary;

public class DbxWorldBuilder {
	private static final String ROOT_FOLDER = "/WorldShare/";
	private static final String LEVEL_FILE = "/level.dat";
	
	private DbxClientV2 client;
	private Metadata dbxFolder;
	private List<WorldSummary> localWorldSummaries;
	
	public DbxWorldBuilder(DbxClientV2 client, Metadata dbxFolder,
			List<WorldSummary> localWorldSummaries) {
		this.client = client;
		this.dbxFolder = dbxFolder;
		this.localWorldSummaries = localWorldSummaries;
	}
	
	public DbxWorld build() {
		FileMetadata fileData = null;
		
		try {
			Metadata metadata = client.files().getMetadata(
					ROOT_FOLDER + dbxFolder.getName() + LEVEL_FILE);
			
			if(metadata instanceof FileMetadata) {
				fileData = (FileMetadata) metadata;
			}
		} catch (DbxException e) { 
		}
		
		String name = dbxFolder.getName();
		Date lastModified = getLastModified(fileData);
		String lastPerson = getLastPerson(fileData);
		LocalWorld localWorld = getLocalWorld(name);
		List<String> members = getMembers(fileData);
		World.Status status = getStatus(lastModified, localWorld);
		
		return new DbxWorld(client, name, lastModified, lastPerson, 
				localWorld, members, status);
	}
	
	private Date getLastModified(FileMetadata fileData) {
		if (fileData == null) 
			return new Date();
		
		return fileData.getClientModified();
	}
	
	private String getLastPerson(FileMetadata fileData) {
		return "Unknown"; //TODO
	}
	
	private LocalWorld getLocalWorld(String dbxName) {
		LocalWorld localWorld = null;
		Iterator<WorldSummary> iterator = localWorldSummaries.iterator();
		
		while(localWorld == null && iterator.hasNext()) {
			WorldSummary summary = iterator.next();
			
			if(summary.getLevelName().equals(dbxName)) {
				localWorld = new LocalWorld(summary);
			}
		}
		
		return localWorld;
	}
	
	private List<String> getMembers(FileMetadata fileData) {
		return Collections.emptyList(); // TODO
	}
	
	private World.Status getStatus(Date dbxLastModified, LocalWorld localWorld) {
		if(localWorld == null) {
			return World.Status.DISCONNECTED;
		}
		
		Date localLastModified = localWorld.getLastModified();
		
		if(localLastModified.equals(dbxLastModified)) {
			return World.Status.UP_TO_DATE;
		} else if(localLastModified.compareTo(dbxLastModified) < 0) {
			return World.Status.NEEDS_DOWNLOAD;
		} else if(localLastModified.compareTo(dbxLastModified) > 0) {
			return World.Status.NEEDS_UPLOAD;
		}
		
		return World.Status.DISCONNECTED;
	}
}
