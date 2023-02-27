package ankel.worlshare.world;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSummary;

public class LocalWorld implements World {
	private final WorldSummary summary;
	
	private String name;
	private ResourceLocation icon;
	private Date lastModified;
	private String lastPerson;
	private Status status;
	
	public LocalWorld(WorldSummary summary) {
		this.summary = summary;
		this.name = summary.getLevelName();
		this.icon = null;
		this.lastModified = new Date(summary.getLastPlayed());
		this.lastPerson = "You";
		this.status = Status.DISCONNECTED;
	}

	@Override
	public String getWorldName() {
		return name;
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
		return lastModified;
	}
	
	@Override
	public String getLastPerson() {
		return lastPerson;
	}

	@Override
	public String getStatus() {
		return status.getValue();
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
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
