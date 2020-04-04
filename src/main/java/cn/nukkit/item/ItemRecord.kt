package cn.nukkit.item

/**
 * @author CreeperFace
 */
abstract class ItemRecord(id: Int, meta: Int?, count: Int) : Item(id, meta, count, "Music Disc") {
	override val maxStackSize: Int
		get() = 1

	abstract val soundId: String
}