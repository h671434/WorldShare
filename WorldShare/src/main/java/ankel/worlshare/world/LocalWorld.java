package ankel.worlshare.world;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSummary;

public class LocalWorld implements World {
	private final WorldSummary summary;
	private ResourceLocation icon;
	private Status status;
	
	public LocalWorld(WorldSummary summary) {
		this.summary = summary;
		icon = null;
		status = Status.UP_TO_DATE;
	}

	@Override
	public String getWorldName() {
		return this.summary.getLevelName();
	}

	@Override
	public ResourceLocation getServerIcon() {
		if(icon == null) {
			icon = loadServerIcon(summary.getIcon());
		}
		return icon;
	}

	@Override
	public Date getLastModified() {
		return new Date(summary.getLastPlayed());
	}
	
	@Override
	public String getLastPerson() {
		return "";
	}

	public String getStatus() {
		return status.getValue();
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
