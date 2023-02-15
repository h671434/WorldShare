package ankel.worlshare.dropbox;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxPKCEWebAuth;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;

public class Authorizer {
	private final static Logger LOGGER = LogManager.getLogger();
	private final static String APP_KEY = "1idg6h8f8m1g915";
	private final static String JSON_DIRECTORY = "/worldshare.json";
	private File json;
	private DbxAppInfo appInfo;
	private DbxCredential credential;
	private DbxPKCEWebAuth webAuth;
	private DbxClientV2 client;
	
	public Authorizer(File localDirectory) {
		this.json = new File(localDirectory + JSON_DIRECTORY);
		this.appInfo = new DbxAppInfo(APP_KEY);
	}
	
	public boolean isAuthorized() {
		return client != null;
	}
	
	public DbxClientV2 getClient() {
		return client;
	}
	
	public void AuthorizeFromFile() throws AuthorizationException {
		if(!json.exists()) {
			json.getParentFile().mkdir();
			try {
				json.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			this.credential = DbxCredential.Reader.readFromFile(json);
			client = buildClient();
		} catch (Exception e) {
			throw new AuthorizationException("Unable to load file. " + e.getMessage());
		}
	}
	
	public String getAuthUrl() {
		DbxRequestConfig requestConfig = new DbxRequestConfig("worldshare-authorize");
		webAuth = new DbxPKCEWebAuth(requestConfig, appInfo);
		
		DbxWebAuth.Request webAuthRequest =  DbxWebAuth.newRequestBuilder()
	            .withNoRedirect()
	            .withTokenAccessType(TokenAccessType.OFFLINE)
	            .build();
		
		String authorizeUrl = webAuth.authorize(webAuthRequest);
		return authorizeUrl;
	}
	
	public void finishAuthWithToken(String token) throws AuthorizationException {
		String code = token;
		code = code.trim();
		
		try {
			DbxAuthFinish authFinish = webAuth.finishFromCode(code);
			credential = new DbxCredential(authFinish.getAccessToken(), authFinish
		            .getExpiresAt(), authFinish.getRefreshToken(), appInfo.getKey(), appInfo.getSecret());
			client = buildClient();
			writeJson(credential);
		} catch (DbxException e) {
			LOGGER.error(e.getMessage());
		} catch (NullPointerException e) {
			LOGGER.error(e.getMessage());
		}

		if(!isAuthorized()) {
			throw new AuthorizationException("Not authorized");
		}
	}
	
	private DbxClientV2 buildClient() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("WorldShare/saves").build();
		return new DbxClientV2(config, credential);
	}
	
	private void writeJson(DbxCredential credential) {
		try {
			if(!json.exists()) {
				json.getParentFile().mkdirs();
				json.createNewFile();
			}
			DbxCredential.Writer.writeToFile(credential, json);
		} catch (IOException e) {
			LOGGER.error("Unable to save DropBox access token\n Error: " + e);
		}		
	}	
}
