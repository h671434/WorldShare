package ankel.worlshare.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderBatchBuilder;
import com.dropbox.core.v2.files.CreateFolderBatchJobStatus;
import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

public class WorldUploader implements FileTransferer {
	private final static Logger LOGGER = LogManager.getLogger();
	public final Progress progress;
	private final DbxClientV2 client;
	private final File localfolder;
	private final List<File> files;
	private final List<File> directories;
	private FileMetadata[] result;
	private Date lastModified;
	
	public WorldUploader(DbxClientV2 client, File folder, Date lastModified) throws IllegalArgumentException {
		if(!folder.isDirectory()) {
			throw new IllegalArgumentException();
		}
		this.progress = new Progress(this);
		this.client = client;
		this.localfolder = folder;
		this.files = new ArrayList<>();
		this.directories = new ArrayList<>();
		this.lastModified = lastModified;
		sortDirectory(localfolder);
	}
	
	private void sortDirectory(File folder) {
		for(File child : folder.listFiles())  {
			if(child.isFile() && !child.getName().contains(".lock")) {
				this.files.add(child);
				this.progress.sizeInBytes += child.length();
			} else if(child.isDirectory()) {
				this.directories.add(child);
				sortDirectory(child);
			} 
		}
	}
	
	@Override
	public void run() {
		createFolders(this.directories);
		uploadFileBatch(this.files);
	}
	
	private void createFolders(List<File> directories) {
		List<String> paths = new ArrayList<>();
		directories.forEach((dir) -> {
			paths.add(getDbxPath(dir));
		});
		LOGGER.info(paths.toString());

		try {
			CreateFolderBatchBuilder builder = this.client.files().createFolderBatchBuilder(paths);
			CreateFolderBatchLaunch launch = builder.start();
			if(launch.isAsyncJobId()) {
				String jobId = launch.getAsyncJobIdValue();
				boolean done = false;
				while(!done) {
					CreateFolderBatchJobStatus status = this.client.files().createFolderBatchCheck(jobId);
					done = status.isComplete();
				}
			}	
		} catch (DbxException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	private void uploadFileBatch(List<File> files) {
		int amount = files.size();
		this.result = new FileMetadata[amount];
	
		for(int i = 0; i < amount; i++) {
			File file = files.get(i);
			String dbxPath = getDbxPath(file);

			try {
				this.result[i] = uploadFile(file, dbxPath);
				this.progress.progressInBytes += result[i].getSize();
				this.progress.onProgress();
			} catch (DbxException | IOException e) {
				LOGGER.error("Error uploading file \"" + file + "\": " + e.getMessage());			
				this.result[i] = new FileMetadata(file.getName(), "ERROR", new Date(0), new Date(0), "__ERROR__", 0);
				amount--;
			} 
		}
	}
	
	private FileMetadata uploadFile(File file, String dbxPath) throws UploadErrorException, DbxException, IOException {
		try (InputStream in = new FileInputStream(file)) {
			return client.files().uploadBuilder(dbxPath)
//					.withPropertyGroups(null)
	        		.withMode(WriteMode.OVERWRITE)
	                .withClientModified(lastModified)
	                .uploadAndFinish(in);
		}
	}
	
	private String getDbxPath(File file) {
		return file.toString().replace(localfolder.getParent(), "/WorldShare").replace("\\", "/");
	}
    
	@Override
	public boolean isFinished() {
		return result != null && result.length > 0 && result[result.length - 1] != null;
	}

	@Override
	public FileMetadata[] getResult() throws NullPointerException {
		if(!isFinished()) {
			throw new NullPointerException("Upload is not complete");
		}
		return result;
	}
}
