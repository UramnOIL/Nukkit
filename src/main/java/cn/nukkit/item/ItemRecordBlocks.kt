package cn.nukkit.item

/**
 * @author CreeperFace
 */
class ItemRecordBlocks @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemRecord(ItemID.Companion.RECORD_BLOCKS, meta, count) {
	override val soundId: String
		get() = "record.blocks"
}