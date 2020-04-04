package cn.nukkit.metadata

import cn.nukkit.level.Level
import kotlin.jvm.Throws

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class LevelMetadataStore : MetadataStore() {
	@Override
	protected override fun disambiguate(level: Metadatable?, metadataKey: String?): String? {
		if (level !is Level) {
			throw IllegalArgumentException("Argument must be a Level instance")
		}
		return ((level as Level?).getName().toString() + ":" + metadataKey).toLowerCase()
	}
}