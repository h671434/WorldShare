package ankel.worlshare.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationResult;
import com.dropbox.core.v2.sharing.FolderAction;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;

import ankel.worlshare.dropbox.WorldDownloader;
import ankel.worlshare.dropbox.WorldSharing;
import ankel.worlshare.dropbox.WorldUploader;
import ankel.worlshare.world.DbxWorld;
import ankel.worlshare.world.LocalWorld;
import ankel.worlshare.world.World;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.world.storage.WorldSummary;

public class DbxController {
	private final static Logger LOGGER = LogManager.getLogger();
	private final static String DBX_FOLDER = "/WorldShare";
	private final Minecraft minecraft;
	private final DbxClientV2 client;
	private final List<World> localworlds;
	private final List<World> dbxworlds;
	private final Runnable onListChange;
	
	public DbxController(Minecraft minecraft, DbxClientV2 client, Runnable onListChange) {
		this.minecraft = minecraft;
		this.client = client;
		this.dbxworlds = new ArrayList<>();
		this.localworlds = new ArrayList<>();
		this.onListChange = onListChange;
	}
	
	public List<World> getLocalWorlds() {
		try {
			List<WorldSummary> worldsummaries = minecraft.getLevelSource().getLevelList();
			this.localworlds.clear();
			worldsummaries.forEach((summary) -> {
				this.localworlds.add(new LocalWorld(summary));
			});
			Collections.sort(this.localworlds);
		} catch (AnvilConverterException e) {
		}
		return this.localworlds;
	}
	
	public List<World> getDbxWorlds() {
		try {
			if(client.files().searchV2("WorldShare").getMatches().size() < 1)
				client.files().createFolderV2(DBX_FOLDER);
			
			updateShared();
			
			List<Metadata> dbxentries = client.files().listFolder(DBX_FOLDER).getEntries();
			this.dbxworlds.clear();
			dbxentries.forEach((metadata) -> {
				this.dbxworlds.add(new DbxWorld(client, metadata.getName()));
			});
			Collections.sort(this.dbxworlds);
		} catch (DbxException e) {
			System.err.println(e.getMessage());
		}
		return this.dbxworlds;
	}
	
	private void updateShared() throws DbxException {
		List<SharedFolderMetadata> mountable = client.sharing().listMountableFolders().getEntries();
		mountable.forEach((metadata) -> {
			if(metadata.getPathLower() == null) {
				try {
					LOGGER.info("Mounting folder " + metadata.getName());
					SharedFolderMetadata newdata = client.sharing().mountFolder(metadata.getSharedFolderId());
					LOGGER.info("Noving folder " + newdata.getName() + " to " +  DBX_FOLDER + "/" + newdata.getName());
					client.files().moveV2Builder("/" + newdata.getName(), DBX_FOLDER + "/" + newdata.getName())
						.withAllowSharedFolder(true)
						.start();
				} catch (DbxException e) {
					LOGGER.error(e.getMessage());
				}
			}

		});
	}
	
	public void uploadWorld(LocalWorld world) {
		File file = new File(minecraft.getLevelSource().getBaseDir().toString(), world.getWorldName());
		WorldUploader uploader = new WorldUploader(client, file);
		new Thread(uploader).start();
		
		IAsyncReloader reloader = uploader.progress;
		this.minecraft.setOverlay(new TransferProgressGui(this.minecraft, reloader, (throwable) -> {
			onListChange.run();
			minecraft.setOverlay(null);
			LOGGER.info("DONE");
		}));
	}
	
	public void downloadWorld(DbxWorld world) {
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

}
