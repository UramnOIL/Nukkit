package cn.nukkit.block

import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace

/**
 * Created by PetteriM1
 */
class BlockWallBanner @JvmOverloads constructor(meta: Int = 0) : BlockBanner(meta) {
	override val id: Int
		get() = BlockID.Companion.WALL_BANNER

	override val name: String
		get() = "Wall Banner"

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.damage >= BlockFace.NORTH.index && this.damage <= BlockFace.EAST.index) {
				if (this.getSide(BlockFace.fromIndex(this.damage).opposite).id == BlockID.Companion.AIR) {
					getLevel().useBreakOn(this)
				}
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}
}