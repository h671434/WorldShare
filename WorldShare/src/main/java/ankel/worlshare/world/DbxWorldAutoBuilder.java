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

public class DbxWorldAutoBuilder {
	private static final String ROOT_FOLDER = "/WorldShare/";
	private static final String LEVEL_FILE = "/level.dat";
	
	private DbxClientV2 client;
	private Metadata dbxFolder;
	private List<WorldSummary> localWorldSummaries;
	
	public DbxWorldAutoBuilder(DbxClientV2 client, Metadata dbxFolder,
			List<WorldSummary> localWorldSummaries) {
		this.client = client;
		this.dbxFolder = dbxFolder;
		this.localWorldSummaries = localWorldSummaries;
	}
	
	public DbxWorld autoBuild() {
		DbxWorldBuilder builder  = new DbxWorldBuilder();
		
		String name = dbxFolder.getName();
		
		Date dbxLastModified = getLastModified(name);
		
		String lastPerson = getLastPerson();
		
		LocalWorld localWorld = getLocalWorld(name);
		
		List<String> members = getMembers();
		
		World.Status status = World.Status.NOT_DOWNLOADED; 
		if(localWorld != null) {
			status = getStatus(dbxLastModified, localWorld.getLastModified());
			localWorld.setStatus(status);
		} 
		
		builder.setClient(client)
				.setName(dbxFolder.getName())
				.setLastModified(dbxLastModified)
				.setLastPerson(lastPerson)
				.setLocalWorld(localWorld)
				.setMembers(members)
				.setStatus(status);
		
		return builder.build();
	}
	
	private Date getLastModified(String name) {
		Date date = new Date();
		
		try {
			Metadata data = client.files().getMetadata(ROOT_FOLDER + name + LEVEL_FILE);
			if (data instanceof FileMetadata) {
				date = ((FileMetadata) data).getClientModified();
			}
		} catch (DbxException e) {
		}
		return date;
	}
	
	private String getLastPerson() {
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
	
	private List<String> getMembers() {
		return Collections.emptyList(); // TODO
	}
	
	private World.Status getStatus(Date dbxLastModified, Date localLastModified) {
		World.Status status = World.Status.DISCONNECTED;
		if(dbxLastModified.equals(new Date(0))) {
			status = World.Status.DISCONNECTED;
		} else if(localLastModified.equals(dbxLastModified)) {
			status = World.Status.UP_TO_DATE;
		} else if(localLastModified.compareTo(dbxLastModified) < 0) {
			status = World.Status.NEEDS_DOWNLOAD;
		} else if(localLastModified.compareTo(dbxLastModified) > 0) {
			status = World.Status.NEEDS_UPLOAD;
		}
		return status;
	}
	
	protected final class DbxWorldBuilder {
		private DbxClientV2 client;
		private String name;
		private Date lastModified;
		private String lastPerson;
		private LocalWorld localWorld;
		private List<String> members;
		private World.Status status;
		
		protected DbxWorldBuilder setClient(DbxClientV2 client) {
			this.client = client;
			return this;
		}
		
		public DbxWorldBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public DbxWorldBuilder setLastModified(Date lastModified) {
			this.lastModified = lastModified;
			return this;
		}
		
		public DbxWorldBuilder setLastPerson(String lastPerson) {
			this.lastPerson = lastPerson;
			return this;
		}
		
		public DbxWorldBuilder setLocalWorld(LocalWorld localWorld) {
			this.localWorld = localWorld;
			return this;
		}
		
		public DbxWorldBuilder setMembers(List<String> members) {
			this.members = members;
			return this;
		}
		
		public DbxWorldBuilder setStatus(World.Status status) {
			this.status = status;
			return this;
		}
		
		public DbxWorld build() {
			return new DbxWorld(client, name, lastModified, lastPerson, 
					localWorld, members, status);
		}
	}
}
