package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.LevelException

/**
 * Created on 2015/12/6 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
abstract class BlockThin protected constructor() : BlockTransparent() {
	override val isSolid: Boolean
		get() = false

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		val offNW = 7.0 / 16.0
		val offSE = 9.0 / 16.0
		val onNW = 0.0
		val onSE = 1.0
		var w = offNW
		var e = offSE
		var n = offNW
		var s = offSE
		try {
			val north = canConnect(this.north())
			val south = canConnect(this.south())
			val west = canConnect(this.west())
			val east = canConnect(this.east())
			w = if (west) onNW else offNW
			e = if (east) onSE else offSE
			n = if (north) onNW else offNW
			s = if (south) onSE else offSE
		} catch (ignore: LevelException) {
			//null sucks
		}
		return SimpleAxisAlignedBB(
				x + w,
				y,
				z + n,
				x + e,
				y + 1,
				z + s
		)
	}

	fun canConnect(block: Block?): Boolean {
		return block!!.isSolid || block.id == this.id || block.id == BlockID.Companion.GLASS_PANE || block.id == BlockID.Companion.GLASS
	}
}