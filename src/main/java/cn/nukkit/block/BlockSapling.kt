package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockSapling @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val id: Int
		get() = BlockID.Companion.SAPLING

	override val name: String
		get() {
			val names = arrayOf(
					"Oak Sapling",
					"Spruce Sapling",
					"Birch Sapling",
					"Jungle Sapling",
					"Acacia Sapling",
					"Dark Oak Sapling",
					"",
					""
			)
			return names[this.damage and 0x07]
		}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id == BlockID.Companion.GRASS || down.id == BlockID.Companion.DIRT || down.id == BlockID.Companion.FARMLAND || down.id == BlockID.Companion.PODZOL) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0F) { //BoneMeal
			if (player != null && player.gamemode and 0x01 == 0) {
				item.count--
			}
			level.addParticle(BoneMealParticle(this))
			if (ThreadLocalRandom.current().nextFloat() >= 0.45) {
				return true
			}
			this.grow()
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
		} else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
			if (ThreadLocalRandom.current().nextInt(1, 8) == 1) {
				if (this.damage and 0x08 == 0x08) {
					this.grow()
				} else {
					this.setDamage(this.damage or 0x08)
					getLevel().setBlock(this, this, true)
					return Level.BLOCK_UPDATE_RANDOM
				}
			} else {
				return Level.BLOCK_UPDATE_RANDOM
			}
		}
		return Level.BLOCK_UPDATE_NORMAL
	}

	private fun grow() {
		var generator: BasicGenerator? = null
		var bigTree = false
		var x = 0
		var z = 0
		when (this.damage and 0x07) {
			JUNGLE -> {
				loop@ while (x >= -1) {
					while (z >= -1) {
						if (findSaplings(x, z, JUNGLE)) {
							generator = ObjectJungleBigTree(10, 20, Block.Companion.get(BlockID.Companion.WOOD, BlockWood.Companion.JUNGLE), Block.Companion.get(BlockID.Companion.LEAVES, BlockLeaves.Companion.JUNGLE))
							bigTree = true
							break@loop
						}
						--z
					}
					--x
				}
				if (!bigTree) {
					x = 0
					z = 0
					generator = NewJungleTree(4, 7)
				}
			}
			ACACIA -> generator = ObjectSavannaTree()
			DARK_OAK -> {
				loop@ while (x >= -1) {
					while (z >= -1) {
						if (findSaplings(x, z, DARK_OAK)) {
							generator = ObjectDarkOakTree()
							bigTree = true
							break@loop
						}
						--z
					}
					--x
				}
				if (!bigTree) {
					return
				}
			}
			else -> {
				ObjectTree.growTree(level, this.floorX, this.floorY, this.floorZ, NukkitRandom(), this.damage and 0x07)
				return
			}
		}
		if (bigTree) {
			level.setBlock(this.add(x.toDouble(), 0.0, z.toDouble()), Block.Companion.get(BlockID.Companion.AIR), true, false)
			level.setBlock(this.add(x + 1.toDouble(), 0.0, z.toDouble()), Block.Companion.get(BlockID.Companion.AIR), true, false)
			level.setBlock(this.add(x.toDouble(), 0.0, z + 1.toDouble()), Block.Companion.get(BlockID.Companion.AIR), true, false)
			level.setBlock(this.add(x + 1.toDouble(), 0.0, z + 1.toDouble()), Block.Companion.get(BlockID.Companion.AIR), true, false)
		} else {
			level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, false)
		}
		if (!generator.generate(level, NukkitRandom(), this.add(x.toDouble(), 0.0, z.toDouble()))) {
			if (bigTree) {
				level.setBlock(this.add(x.toDouble(), 0.0, z.toDouble()), this, true, false)
				level.setBlock(this.add(x + 1.toDouble(), 0.0, z.toDouble()), this, true, false)
				level.setBlock(this.add(x.toDouble(), 0.0, z + 1.toDouble()), this, true, false)
				level.setBlock(this.add(x + 1.toDouble(), 0.0, z + 1.toDouble()), this, true, false)
			} else {
				level.setBlock(this, this, true, false)
			}
		}
	}

	private fun findSaplings(x: Int, z: Int, type: Int): Boolean {
		return isSameType(this.add(x.toDouble(), 0.0, z.toDouble()), type) && isSameType(this.add(x + 1.toDouble(), 0.0, z.toDouble()), type) && isSameType(this.add(x.toDouble(), 0.0, z + 1.toDouble()), type) && isSameType(this.add(x + 1.toDouble(), 0.0, z + 1.toDouble()), type)
	}

	fun isSameType(pos: Vector3?, type: Int): Boolean {
		val block = level.getBlock(pos)
		return block.id == id && block.damage and 0x07 == type and 0x07
	}

	override fun toItem(): Item? {
		return Item.get(BlockID.Companion.SAPLING, this.damage and 0x7)
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	companion object {
		const val OAK = 0
		const val SPRUCE = 1
		const val BIRCH = 2

		/**
		 * placeholder
		 */
		const val BIRCH_TALL = 8 or BIRCH
		const val JUNGLE = 3
		const val ACACIA = 4
		const val DARK_OAK = 5
	}
}