package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.generator

/**
 * Created on 2015/11/22 by CreeperFace.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockDaylightDetectorInverted : BlockDaylightDetector() {
	override val id: Int
		get() = BlockID.Companion.DAYLIGHT_DETECTOR_INVERTED

	override val name: String
		get() = "Daylight Detector Inverted"

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.DAYLIGHT_DETECTOR), 0)
	}

	override fun invertDetect(): Boolean {
		return true
	}
}