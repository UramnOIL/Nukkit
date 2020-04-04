package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace

/**
 * Created by Pub4Game on 26.12.2015.
 */
class BlockWallSign @JvmOverloads constructor(meta: Int = 0) : BlockSignPost(meta) {
	override val id: Int
		get() = BlockID.Companion.WALL_SIGN

	override val name: String
		get() = "Wall Sign"

	override fun onUpdate(type: Int): Int {
		val faces = intArrayOf(
				3,
				2,
				5,
				4)
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.damage >= 2 && this.damage <= 5) {
				if (this.getSide(BlockFace.fromIndex(faces[this.damage - 2])).id == Item.AIR) {
					getLevel().useBreakOn(this)
				}
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}
}