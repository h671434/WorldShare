package ankel.worlshare.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class WorldShareConfigs {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	
	public static final ForgeConfigSpec.BooleanValue AUTO_UPLOAD;
	public static final ForgeConfigSpec.BooleanValue AUTO_DOWNLOAD;
	
	static {
		BUILDER.push("Configs for WorldShare");
		
		// DEFINED CONFIGS
		AUTO_UPLOAD = BUILDER
				.comment("Upload local save to DropBox automaticly after leaving world. "
						+ "WARNING: Will overwrite the current world on DropBox!")
				.define("Auto upload", false);
		
		AUTO_DOWNLOAD = BUILDER
				.comment("Download latest save from DropBox automaticly when opening world. "
						+ "WARNING: Will overwrite the current world on your PC!")
				.define("Auto download", false);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
