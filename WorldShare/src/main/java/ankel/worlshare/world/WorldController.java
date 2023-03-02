package ankel.worlshare.world;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;

import ankel.worlshare.dropbox.WorldDownloader;
import ankel.worlshare.dropbox.WorldSharing;
import ankel.worlshare.dropbox.WorldUploader;
import ankel.worlshare.gui.TransferProgressGui;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.world.storage.WorldSummary;

public class WorldController {
	private final static Logger LOGGER = LogManager.getLogger();
	private final static String DBX_FOLDER = "/WorldShare";
	private final Minecraft minecraft;
	private final DbxClientV2 client;
	private final List<DbxWorld> dbxworlds;
	private final Runnable onListChange;
	
	public WorldController(Minecraft minecraft, DbxClientV2 client, Runnable onListChange) {
		this.minecraft = minecraft;
		this.client = client;
		this.dbxworlds = new ArrayList<>();
		this.onListChange = onListChange;
		createDbxFolderIfAbsent();
	}
	
	private void createDbxFolderIfAbsent() {
		try {
			if(client.files().searchV2("WorldShare").getMatches().size() < 1) {
				client.files().createFolderV2(DBX_FOLDER);
			}
		} catch (DbxException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	public void uploadWorld(LocalWorld world) {
		LOGGER.info("Uploading...");
		File file = new File(minecraft.getLevelSource().getBaseDir().toString(), world.getWorldName());
		WorldUploader uploader = new WorldUploader(client, file, world.getLastModified());
		new Thread(uploader).start();
		
		IAsyncReloader reloader = uploader.progress;
		this.minecraft.setOverlay(new TransferProgressGui(this.minecraft, reloader, (throwable) -> {
			onListChange.run();
			minecraft.setOverlay(null);
			LOGGER.info("DONE");
		}));
	}
	
	public void downloadWorld(DbxWorld world) {
		LOGGER.info("Downloading...");
		String localdir = minecraft.getLevelSource().getBaseDir().toString();
		WorldDownloader downloader = new WorldDownloader(client, DBX_FOLDER + "/" + world.getWorldName(), localdir);
		new Thread(downloader).start();
		
		IAsyncReloader reloader = downloader.progress;
		this.minecraft.setOverlay(new TransferProgressGui(this.minecraft, reloader, (throwable) -> {
			onListChange.run();
			minecraft.setOverlay(null);
			LOGGER.info("DONE");
		}));
	}

	public void shareWorld(DbxWorld world, String string) {
		List<String> emails = new ArrayList<>();
		if(string != null)
			emails.add(string);
		WorldSharing sharer = new WorldSharing(client, DBX_FOLDER + "/" + world.getWorldName(), emails);
		new Thread(sharer).start();
	}
	
	public List<DbxWorld> getDbxWorlds() {
		try {
			updateShared();

			List<Metadata> dbxEntries = client.files().listFolder(DBX_FOLDER).getEntries();
			List<WorldSummary> worldSummaries = minecraft.getLevelSource().getLevelList();
			
			this.dbxworlds.clear();
			dbxEntries.forEach((metadata) -> {
				this.dbxworlds.add(new DbxWorldAutoBuilder(client, metadata, worldSummaries).autoBuild());
			});
			
			Collections.sort(this.dbxworlds);
		} catch (DbxException | AnvilConverterException e) {
			LOGGER.error(e.getMessage());
		}
		return this.dbxworlds;
	}
	
	private void updateShared() throws DbxException {
		List<SharedFolderMetadata> mountable = client.sharing().listMountableFolders().getEntries();
		mountable.forEach((metadata) -> {
			if(metadata.getPathLower() == null) {
				try {
					LOGGER.debug("Mounting folder " + metadata.getName());
					SharedFolderMetadata newdata = client.sharing().mountFolder(metadata.getSharedFolderId());
					LOGGER.debug("Noving folder " + newdata.getName() + " to " +  DBX_FOLDER + "/" + newdata.getName());
					client.files().moveV2Builder("/" + newdata.getName(), DBX_FOLDER + "/" + newdata.getName())
						.withAllowSharedFolder(true)
						.start();
				} catch (DbxException e) {
					LOGGER.error(e.getMessage());
				}
			}
		});
	}
	

}
