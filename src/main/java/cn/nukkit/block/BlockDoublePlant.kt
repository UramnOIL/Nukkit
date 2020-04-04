package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSeedsWheat
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

/**
 * Created on 2015/11/23 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockDoublePlant @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val id: Int
		get() = BlockID.Companion.DOUBLE_PLANT

	override fun canBeReplaced(): Boolean {
		return this.damage == TALL_GRASS || this.damage == LARGE_FERN
	}

	override val name: String
		get() = NAMES[if (this.damage > 5) 0 else this.damage]

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.damage and TOP_HALF_BITMASK == TOP_HALF_BITMASK) {
				// Top
				if (this.down().id != BlockID.Companion.DOUBLE_PLANT) {
					getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), false, true)
					return Level.BLOCK_UPDATE_NORMAL
				}
			} else {
				// Bottom
				if (this.down().isTransparent || this.up().id != BlockID.Companion.DOUBLE_PLANT) {
					getLevel().useBreakOn(this)
					return Level.BLOCK_UPDATE_NORMAL
				}
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = down()
		val up = up()
		if (up.id == BlockID.Companion.AIR && (down.id == BlockID.Companion.GRASS || down.id == BlockID.Companion.DIRT)) {
			getLevel().setBlock(block, this, true, false) // If we update the bottom half, it will drop the item because there isn't a flower block above
			getLevel().setBlock(up, Block.Companion.get(BlockID.Companion.DOUBLE_PLANT, damage xor TOP_HALF_BITMASK), true, true)
			return true
		}
		return false
	}

	override fun onBreak(item: Item): Boolean {
		val down = down()
		if (this.damage and TOP_HALF_BITMASK == TOP_HALF_BITMASK) { // Top half
			getLevel().useBreakOn(down)
		} else {
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		}
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		if (this.damage and TOP_HALF_BITMASK != TOP_HALF_BITMASK) {
			when (this.damage and 0x07) {
				TALL_GRASS, LARGE_FERN -> {
					val dropSeeds = ThreadLocalRandom.current().nextInt(10) == 0
					if (item.isShears) {
						//todo enchantment
						return if (dropSeeds) {
							arrayOf(
									ItemSeedsWheat(0, 1),
									toItem()
							)
						} else {
							arrayOf(
									toItem()
							)
						}
					}
					return if (dropSeeds) {
						arrayOf(
								ItemSeedsWheat()
						)
					} else {
						arrayOfNulls(0)
					}
				}
			}
			return arrayOf(toItem())
		}
		return arrayOfNulls(0)
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0f) { //Bone meal
			when (this.damage and 0x07) {
				SUNFLOWER, LILAC, ROSE_BUSH, PEONY -> {
					if (player != null && player.gamemode and 0x01 == 0) {
						item.count--
					}
					level.addParticle(BoneMealParticle(this))
					level.dropItem(this, toItem())
				}
			}
			return true
		}
		return false
	}

	companion object {
		const val SUNFLOWER = 0
		const val LILAC = 1
		const val TALL_GRASS = 2
		const val LARGE_FERN = 3
		const val ROSE_BUSH = 4
		const val PEONY = 5
		const val TOP_HALF_BITMASK = 0x8
		private val NAMES = arrayOf(
				"Sunflower",
				"Lilac",
				"Double Tallgrass",
				"Large Fern",
				"Rose Bush",
				"Peony"
		)
	}
}