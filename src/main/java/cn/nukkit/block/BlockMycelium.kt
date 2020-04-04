package cn.nukkit.block

import cn.nukkit.Server
import cn.nukkit.event.block.BlockSpreadEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 03.01.2016.
 */
class BlockMycelium : BlockSolid() {
	override val name: String
		get() = "Mycelium"

	override val id: Int
		get() = BlockID.Companion.MYCELIUM

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override val hardness: Double
		get() = 0.6

	override val resistance: Double
		get() = 2.5

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				ItemBlock(Block.Companion.get(BlockID.Companion.DIRT))
		)
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_RANDOM) {
			//TODO: light levels
			val random = NukkitRandom()
			x = random.nextRange(x.toInt() - 1, x.toInt() + 1).toDouble()
			y = random.nextRange(y.toInt() - 1, y.toInt() + 1).toDouble()
			z = random.nextRange(z.toInt() - 1, z.toInt() + 1).toDouble()
			val block = getLevel().getBlock(Vector3(x, y, z))
			if (block.id == BlockID.Companion.DIRT && block.damage == 0) {
				if (block.up().isTransparent) {
					val ev = BlockSpreadEvent(block, this, Block.Companion.get(BlockID.Companion.MYCELIUM))
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(block, ev.newState)
					}
				}
			}
		}
		return 0
	}

	override val color: BlockColor
		get() = BlockColor.PURPLE_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}