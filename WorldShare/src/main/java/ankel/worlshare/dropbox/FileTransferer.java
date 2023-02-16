package ankel.worlshare.dropbox;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.v2.files.FileMetadata;

import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.Unit;

public interface FileTransferer extends Runnable {
	
	public boolean isFinished();
	
	public FileMetadata[] getResult() throws Exception;
	
	public final class Progress implements IAsyncReloader {
		private static final Logger LOGGER = LogManager.getLogger();
		public long sizeInBytes;
		public long progressInBytes;
		private final FileTransferer parent;
		
		public Progress(FileTransferer parent) {
			this.sizeInBytes = 0;
			this.progressInBytes = 0;
			this.parent = parent;
		}

		public void onProgress() {
			LOGGER.info(String.format("%,.2f", getProgress() * 100) + "% " 
					+ progressInBytes + " out of " + sizeInBytes + " bytes");
		}
		
		public double getProgress() {
			return ((double) this.progressInBytes / this.sizeInBytes);
		}
		
		public void onCompletion(Runnable runnable) {
			runnable.run();
		}

		@Override
		public CompletableFuture<Unit> done() {
			 return null;
		}

		@Override
		public float getActualProgress() {
			return (float) getProgress();
		}

		@Override
		public boolean isApplying() {
			return !isDone();
		}

		@Override
		public boolean isDone() {
			return parent.isFinished();
		}

		@Override
		public void checkExceptions() {			
		}
	}
}
