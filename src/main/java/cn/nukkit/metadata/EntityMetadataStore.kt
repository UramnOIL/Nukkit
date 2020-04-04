package cn.nukkit.metadata

import cn.nukkit.entity.Entity
import kotlin.jvm.Throws

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityMetadataStore : MetadataStore() {
	@Override
	protected override fun disambiguate(entity: Metadatable?, metadataKey: String?): String? {
		if (entity !is Entity) {
			throw IllegalArgumentException("Argument must be an Entity instance")
		}
		return (entity as Entity?).getId().toString() + ":" + metadataKey
	}
}