package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

/**
 * Created on 2015/11/23 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockFlower @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val id: Int
		get() = BlockID.Companion.FLOWER

	override val name: String
		get() {
			val names = arrayOf(
					"Poppy",
					"Blue Orchid",
					"Allium",
					"Azure Bluet",
					"Red Tulip",
					"Orange Tulip",
					"White Tulip",
					"Pink Tulip",
					"Oxeye Daisy",
					"Unknown",
					"Unknown",
					"Unknown",
					"Unknown",
					"Unknown",
					"Unknown",
					"Unknown"
			)
			return names[this.damage and 0x0f]
		}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id == BlockID.Companion.GRASS || down.id == BlockID.Companion.DIRT || down.id == BlockID.Companion.FARMLAND || down.id == BlockID.Companion.PODZOL) {
			getLevel().setBlock(block, this, true)
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().isTransparent) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0f) { //Bone meal
			if (player != null && player.gamemode and 0x01 == 0) {
				item.count--
			}
			level.addParticle(BoneMealParticle(this))
			for (i in 0..7) {
				val vec: Vector3 = this.add(
						ThreadLocalRandom.current().nextInt(-3, 4).toDouble(),
						ThreadLocalRandom.current().nextInt(-1, 2).toDouble(),
						ThreadLocalRandom.current().nextInt(-3, 4).toDouble())
				if (level.getBlock(vec).id == BlockID.Companion.AIR && level.getBlock(vec.down()).id == BlockID.Companion.GRASS && vec.getY() >= 0 && vec.getY() < 256) {
					if (ThreadLocalRandom.current().nextInt(10) == 0) {
						level.setBlock(vec, uncommonFlower, true)
					} else {
						level.setBlock(vec, Block.Companion.get(id), true)
					}
				}
			}
			return true
		}
		return false
	}

	protected open val uncommonFlower: Block
		protected get() = Block.Companion.get(BlockID.Companion.DANDELION)

	companion object {
		const val TYPE_POPPY = 0
		const val TYPE_BLUE_ORCHID = 1
		const val TYPE_ALLIUM = 2
		const val TYPE_AZURE_BLUET = 3
		const val TYPE_RED_TULIP = 4
		const val TYPE_ORANGE_TULIP = 5
		const val TYPE_WHITE_TULIP = 6
		const val TYPE_PINK_TULIP = 7
		const val TYPE_OXEYE_DAISY = 8
	}
}