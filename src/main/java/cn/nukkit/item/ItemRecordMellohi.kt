package cn.nukkit.item

/**
 * @author CreeperFace
 */
class ItemRecordMellohi @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemRecord(ItemID.Companion.RECORD_MELLOHI, meta, count) {
	override val soundId: String
		get() = "record.mellohi"
}