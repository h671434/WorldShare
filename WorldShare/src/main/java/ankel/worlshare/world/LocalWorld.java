package ankel.worlshare.world;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import net.minecraft.world.storage.WorldSummary;

public class LocalWorld implements World {
	private final WorldSummary summary;
	
	public LocalWorld(WorldSummary summary) {
		this.summary = summary;
	}

	@Override
	public String getWorldName() {
		return this.summary.getLevelName();
	}

	@Override
	public File getWorldIcon() {
		return summary.getIcon();
	}

	@Override
	public Date getLastModified() {
		return new Date(summary.getLastPlayed());
	}

	@Override
	public int hashCode() {
		return Objects.hash(summary.getLevelId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof World))
			return false;
		World other = (World) obj;
		return this.getWorldName().trim().equalsIgnoreCase(other.getWorldName().trim());
	}

	@Override
	public int compareTo(World o) {
		return this.getWorldName().compareTo(o.getWorldName());
	}
}
