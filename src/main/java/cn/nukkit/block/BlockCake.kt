package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemCake
import cn.nukkit.item.food.Food
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockCake @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val name: String
		get() = "Cake Block"

	override val id: Int
		get() = BlockID.Companion.CAKE_BLOCK

	override fun canBeActivated(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override fun getMinX(): Double {
		return x + (1 + damage * 2) / 16
	}

	override fun getMinY(): Double {
		return y
	}

	override fun getMinZ(): Double {
		return z + 0.0625
	}

	override fun getMaxX(): Double {
		return x - 0.0625 + 1
	}

	override fun getMaxY(): Double {
		return y + 0.5
	}

	override fun getMaxZ(): Double {
		return z - 0.0625 + 1
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (down().id != BlockID.Companion.AIR) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (down().id == BlockID.Companion.AIR) {
				getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override fun toItem(): Item? {
		return ItemCake()
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null && player.foodData!!.level < player.foodData!!.maxLevel) {
			if (damage <= 0x06) setDamage(damage + 1)
			if (damage >= 0x06) {
				getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true)
			} else {
				Food.getByRelative(this).eatenBy(player)
				getLevel().setBlock(this, this, true)
			}
			return true
		}
		return false
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override val comparatorInputOverride: Int
		get() = (7 - this.damage) * 2

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}
}