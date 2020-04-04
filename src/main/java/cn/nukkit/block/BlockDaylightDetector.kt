package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * Created on 2015/11/22 by CreeperFace.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockDaylightDetector : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.DAYLIGHT_DETECTOR

	override val name: String
		get() = "Daylight Detector"

	override val hardness: Double
		get() = 0.2

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	//This function is a suggestion that can be renamed or deleted
	protected open fun invertDetect(): Boolean {
		return false
	} //todo redstone
}