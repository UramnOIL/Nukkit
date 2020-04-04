package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemNetherWart
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * Created by Leonidius20 on 22.03.17.
 */
class BlockNetherWart @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id == BlockID.Companion.SOUL_SAND) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().id != BlockID.Companion.SOUL_SAND) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (Random().nextInt(10) == 1) {
				if (this.damage < 0x03) {
					val block = clone() as BlockNetherWart
					block.setDamage(block.damage + 1)
					val ev = BlockGrowEvent(this, block)
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(this, ev.newState, true, true)
					} else {
						return Level.BLOCK_UPDATE_RANDOM
					}
				}
			} else {
				return Level.BLOCK_UPDATE_RANDOM
			}
		}
		return 0
	}

	override val color: BlockColor
		get() = BlockColor.RED_BLOCK_COLOR

	override val name: String
		get() = "Nether Wart Block"

	override val id: Int
		get() = BlockID.Companion.NETHER_WART_BLOCK

	override fun getDrops(item: Item): Array<Item?> {
		return if (this.damage == 0x03) {
			arrayOf(
					ItemNetherWart(0, 2 + (Math.random() * (4 - 2 + 1)).toInt())
			)
		} else {
			arrayOf(
					ItemNetherWart()
			)
		}
	}

	override fun toItem(): Item? {
		return ItemNetherWart()
	}
}