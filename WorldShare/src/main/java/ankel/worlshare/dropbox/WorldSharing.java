package ankel.worlshare.dropbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.AccessLevel;
import com.dropbox.core.v2.sharing.AddMember;
import com.dropbox.core.v2.sharing.MemberSelector;
import com.dropbox.core.v2.sharing.ShareFolderLaunch;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;

public class WorldSharing implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger();
	private DbxClientV2 client;
	private String dbxfolder;
	private List<String> emails;
	
	public WorldSharing(DbxClientV2 client, String dbxfolder, List<String> emails) {
		this.client = client;
		this.dbxfolder = dbxfolder;
		this.emails = emails;
	}
	
	@Override
	public void run() {
		try {
			client.sharing().addFolderMemberBuilder(getShareId(), getMembersFromMail())
				.withQuiet(true)
				.start();
			LOGGER.info(emails + " has been added");
		} catch (DbxException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	private String getShareId() throws DbxException {
		Metadata data = client.files().getMetadata(dbxfolder);
		if(!(data instanceof FolderMetadata)) {
			throw new IllegalArgumentException("Folder not found");
		}
		
		String folderid = ((FolderMetadata) data).getSharedFolderId();
		if(folderid == null) {
			folderid = shareFolder().getSharedFolderId();
		}
		return folderid;
	}
	
	private SharedFolderMetadata shareFolder() throws DbxException {
		ShareFolderLaunch launch = client.sharing().shareFolder(dbxfolder);
		if(!launch.isAsyncJobId()) {
			return launch.getCompleteValue();
		}
		
		String asyncid = launch.getAsyncJobIdValue();
		while(client.sharing().checkShareJobStatus(asyncid).isInProgress()) {
			try {
				Thread.sleep(10);
				LOGGER.info("Waiting...");
			} catch (InterruptedException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return client.sharing().checkShareJobStatus(asyncid).getCompleteValue();
	}
	
	private List<AddMember> getMembersFromMail() {
		List<AddMember> members = new ArrayList<>();
		emails.forEach(e -> {
			members.add(new AddMember(MemberSelector.email(e), AccessLevel.EDITOR));
		});
		return members;
	}
}
