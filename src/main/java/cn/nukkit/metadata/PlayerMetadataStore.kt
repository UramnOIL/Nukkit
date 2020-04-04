package cn.nukkit.metadata

import cn.nukkit.IPlayer
import kotlin.jvm.Throws

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PlayerMetadataStore : MetadataStore() {
	@Override
	protected override fun disambiguate(player: Metadatable?, metadataKey: String?): String? {
		if (player !is IPlayer) {
			throw IllegalArgumentException("Argument must be an IPlayer instance")
		}
		return ((player as IPlayer?).name.toString() + ":" + metadataKey).toLowerCase()
	}
}