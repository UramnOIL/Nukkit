package cn.nukkit.item

/**
 * @author CreeperFace
 */
class ItemRecordStrad @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemRecord(ItemID.Companion.RECORD_STRAD, meta, count) {
	override val soundId: String
		get() = "record.strad"
}