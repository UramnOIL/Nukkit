package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockFlowable protected constructor(meta: Int) : BlockTransparentMeta(meta) {
	override fun canBeFlowedInto(): Boolean {
		return true
	}

	override fun canPassThrough(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 0

	override val resistance: Double
		get() = 0

	override val isSolid: Boolean
		get() = false

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return null
	}
}