package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * Created on 2015/12/7 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockFence @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.FENCE

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val name: String
		get() {
			val names = arrayOf(
					"Oak Fence",
					"Spruce Fence",
					"Birch Fence",
					"Jungle Fence",
					"Acacia Fence",
					"Dark Oak Fence",
					"",
					""
			)
			return names[this.damage and 0x07]
		}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		val north = canConnect(this.north())
		val south = canConnect(this.south())
		val west = canConnect(this.west())
		val east = canConnect(this.east())
		val n: Double = if (north) 0 else 0.375
		val s: Double = if (south) 1 else 0.625
		val w: Double = if (west) 0 else 0.375
		val e: Double = if (east) 1 else 0.625
		return SimpleAxisAlignedBB(
				x + w,
				y,
				z + n,
				x + e,
				y + 1.5,
				z + s
		)
	}

	override val burnChance: Int
		get() = 5

	override val burnAbility: Int
		get() = 20

	open fun canConnect(block: Block?): Boolean {
		return block is BlockFence || block is BlockFenceGate || block!!.isSolid && !block.isTransparent
	}

	override val color: BlockColor
		get() = when (this.damage and 0x07) {
			FENCE_OAK -> BlockColor.WOOD_BLOCK_COLOR
			FENCE_SPRUCE -> BlockColor.SPRUCE_BLOCK_COLOR
			FENCE_BIRCH -> BlockColor.SAND_BLOCK_COLOR
			FENCE_JUNGLE -> BlockColor.DIRT_BLOCK_COLOR
			FENCE_ACACIA -> BlockColor.ORANGE_BLOCK_COLOR
			FENCE_DARK_OAK -> BlockColor.BROWN_BLOCK_COLOR
			else -> BlockColor.WOOD_BLOCK_COLOR
		}

	override fun toItem(): Item? {
		return ItemBlock(this, this.damage)
	}

	companion object {
		const val FENCE_OAK = 0
		const val FENCE_SPRUCE = 1
		const val FENCE_BIRCH = 2
		const val FENCE_JUNGLE = 3
		const val FENCE_ACACIA = 4
		const val FENCE_DARK_OAK = 5
	}
}