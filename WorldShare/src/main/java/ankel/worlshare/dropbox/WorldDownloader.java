package ankel.worlshare.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

public class WorldDownloader implements FileTransferer {
	private static final Logger LOGGER = LogManager.getLogger();
	public final Progress progress;
	private final DbxClientV2 client;
	private final String localpath;
	private final List<String> files;
	private FileMetadata[] result;
	
	public WorldDownloader(DbxClientV2 client, String dbxfolder, String localpath) {
		this.progress = new Progress(this);
		this.client = client;
		this.localpath = localpath;
		this.files = new ArrayList<>();
		sortDirectory(dbxfolder);
	}
	
	private void sortDirectory(String folder) {
		try {
			List<Metadata> content = client.files().listFolderBuilder(folder.toLowerCase())
					.withRecursive(false)
					.start()
					.getEntries();
			LOGGER.debug(content);
			content.forEach((metadata) -> {
				if(metadata instanceof FileMetadata) {
					this.files.add(metadata.getPathLower());
					this.progress.sizeInBytes += ((FileMetadata) metadata).getSize();
				}
				if(metadata instanceof FolderMetadata) {
					sortDirectory(metadata.getPathLower());
				}
			});
		} catch (DbxException e) {
			LOGGER.error(e);
		}
	}
	
	@Override
	public void run() {
		downloadFileBatch(files);
	}
	
	private void downloadFileBatch(List<String> files) {
		LOGGER.debug("Downloading files...");
		LOGGER.debug(files);
		int amount = files.size();
		this.result = new FileMetadata[amount];

		for(int i = 0; i < amount; i++) {
			String file = files.get(i);
			String localdirectory = getLocalPath(file);
			
			new File(new File(localdirectory).getParent()).mkdirs();

			try {
				this.result[i] = downloadFile(file, localdirectory);
				this.progress.progressInBytes += result[i].getSize();
				this.progress.onProgress();
			} catch (DbxException | IOException e) {
				LOGGER.error("Error uploading file \"" + file + "\": " + e.getMessage());			
				this.result[i] = new FileMetadata(file, "ERROR", new Date(0), new Date(0), e.getMessage(), 0);
				amount--;
			}
		}
	}
	
	private FileMetadata downloadFile(String dbxfile, String localfile) throws DbxException, IOException {
		try (OutputStream out = new FileOutputStream(localfile)) {
			return client.files().downloadBuilder(dbxfile)
					.download(out);
		}
	}
	
	private String getLocalPath(String dbxfile) {
		return new File(localpath + "/" + dbxfile.replace("worldshare/", "")).getAbsolutePath();
	}

	@Override
	public boolean isFinished() {
		return result != null && result.length > 0 && result[result.length - 1] != null;
	}

	@Override
	public FileMetadata[] getResult() {
		return result;
	}
}
