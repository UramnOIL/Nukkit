package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSeedsWheat
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockTallGrass @JvmOverloads constructor(meta: Int = 1) : BlockFlowable(meta) {
	override val id: Int
		get() = BlockID.Companion.TALL_GRASS

	override val name: String
		get() {
			val names = arrayOf(
					"Grass",
					"Grass",
					"Fern",
					"Fern"
			)
			return names[this.damage and 0x03]
		}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun canBeReplaced(): Boolean {
		return true
	}

	override val burnChance: Int
		get() = 60

	override val burnAbility: Int
		get() = 100

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id == BlockID.Companion.GRASS || down.id == BlockID.Companion.DIRT || down.id == BlockID.Companion.PODZOL) {
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

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0f) {
			val up = this.up()
			if (up.id == BlockID.Companion.AIR) {
				val meta: Int
				meta = when (this.damage) {
					0, 1 -> BlockDoublePlant.Companion.TALL_GRASS
					2, 3 -> BlockDoublePlant.Companion.LARGE_FERN
					else -> -1
				}
				if (meta != -1) {
					if (player != null && player.gamemode and 0x01 == 0) {
						item.count--
					}
					level.addParticle(BoneMealParticle(this))
					level.setBlock(this, Block.Companion.get(BlockID.Companion.DOUBLE_PLANT, meta), true, false)
					level.setBlock(up, Block.Companion.get(BlockID.Companion.DOUBLE_PLANT, meta xor BlockDoublePlant.Companion.TOP_HALF_BITMASK), true)
				}
			}
			return true
		}
		return false
	}

	override fun getDrops(item: Item): Array<Item?> {
		val dropSeeds = ThreadLocalRandom.current().nextInt(10) == 0
		if (item.isShears) {
			//todo enchantment
			return if (dropSeeds) {
				arrayOf(
						ItemSeedsWheat(),
						Item.get(Item.TALL_GRASS, this.damage, 1)
				)
			} else {
				arrayOf(
						Item.get(Item.TALL_GRASS, this.damage, 1)
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

	override val toolType: Int
		get() = ItemTool.TYPE_SHEARS

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR
}