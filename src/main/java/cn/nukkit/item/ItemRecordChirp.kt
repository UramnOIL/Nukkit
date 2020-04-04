package cn.nukkit.item

/**
 * @author CreeperFace
 */
class ItemRecordChirp @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemRecord(ItemID.Companion.RECORD_CHIRP, meta, count) {
	override val soundId: String
		get() = "record.chirp"
}