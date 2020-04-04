package cn.nukkit.block

import cn.nukkit.Server
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSeedsMelon
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace.Plane
import cn.nukkit.math.NukkitRandom

/**
 * Created by Pub4Game on 15.01.2016.
 */
class BlockStemMelon @JvmOverloads constructor(meta: Int = 0) : BlockCrops(meta) {
	override val id: Int
		get() = BlockID.Companion.MELON_STEM

	override val name: String
		get() = "Melon Stem"

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().id != BlockID.Companion.FARMLAND) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			val random = NukkitRandom()
			if (random.nextRange(1, 2) == 1) {
				if (this.damage < 0x07) {
					val block = clone()
					block.setDamage(block.damage + 1)
					val ev = BlockGrowEvent(this, block)
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(this, ev.newState, true)
					}
					return Level.BLOCK_UPDATE_RANDOM
				} else {
					for (face in Plane.HORIZONTAL) {
						val b = this.getSide(face!!)
						if (b.id == BlockID.Companion.MELON_BLOCK) {
							return Level.BLOCK_UPDATE_RANDOM
						}
					}
					val side = this.getSide(Plane.HORIZONTAL.random(random))
					val d = side!!.down()
					if (side.id == BlockID.Companion.AIR && (d.id == BlockID.Companion.FARMLAND || d.id == BlockID.Companion.GRASS || d.id == BlockID.Companion.DIRT)) {
						val ev = BlockGrowEvent(side, Block.Companion.get(BlockID.Companion.MELON_BLOCK))
						Server.instance!!.pluginManager.callEvent(ev)
						if (!ev.isCancelled) {
							getLevel().setBlock(side, ev.newState, true)
						}
					}
				}
			}
			return Level.BLOCK_UPDATE_RANDOM
		}
		return 0
	}

	override fun toItem(): Item? {
		return ItemSeedsMelon()
	}

	override fun getDrops(item: Item): Array<Item?> {
		val random = NukkitRandom()
		return arrayOf(
				ItemSeedsMelon(0, random.nextRange(0, 3))
		)
	}
}