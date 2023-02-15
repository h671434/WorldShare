package ankel.worlshare.world;

import java.io.File;
import java.util.Date;

public interface World extends Comparable<World> {

	String getWorldName();
	
	File getWorldIcon();

	Date getLastModified();

}
