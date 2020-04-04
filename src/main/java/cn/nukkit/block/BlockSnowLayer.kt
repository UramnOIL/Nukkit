package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockFadeEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSnowball
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/6 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockSnowLayer @JvmOverloads constructor(override var damage: Int = 0) : BlockFallable() {

	override val name: String
		get() = "Snow Layer"

	override val id: Int
		get() = BlockID.Companion.SNOW_LAYER

	override val hardness: Double
		get() = 0.1

	override val resistance: Double
		get() = 0.5

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override fun canBeReplaced(): Boolean {
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down!!.isSolid) {
			getLevel().setBlock(block, this, true)
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		super.onUpdate(type)
		if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (getLevel().getBlockLightAt(x.toInt(), y.toInt(), z.toInt()) >= 10) {
				val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.AIR))
				level.server.pluginManager.callEvent(event)
				if (!event.isCancelled) {
					level.setBlock(this, event.newState, true)
				}
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun toItem(): Item? {
		return ItemSnowball()
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isShovel && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.SNOW_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val isTransparent: Boolean
		get() = true

	override fun canBeFlowedInto(): Boolean {
		return true
	}

	override fun canPassThrough(): Boolean {
		return true
	}

	override val isSolid: Boolean
		get() = false

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return null
	}

}