package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Position
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created on 2016/1/5 by xtypr.
 * Package cn.nukkit.block in project nukkit .
 * The name NetherPortalBlock comes from minecraft wiki.
 */
class BlockNetherPortal @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(0), Faceable {
	override val name: String
		get() = "Nether Portal Block"

	override val id: Int
		get() = BlockID.Companion.NETHER_PORTAL

	override fun canPassThrough(): Boolean {
		return true
	}

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override val hardness: Double
		get() = (-1).toDouble()

	override val lightLevel: Int
		get() = 11

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
	}

	override fun onBreak(item: Item): Boolean {
		var result = super.onBreak(item)
		for (face in BlockFace.values()) {
			val b = this.getSide(face)
			if (b != null) {
				if (b is BlockNetherPortal) {
					result = result and b.onBreak(item)
				}
			}
		}
		return result
	}

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override fun canBePushed(): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return this
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}

	companion object {
		@kotlin.jvm.JvmStatic
		fun spawnPortal(pos: Position) {
			val lvl = pos.level
			val x = pos.floorX
			var y = pos.floorY
			var z = pos.floorZ
			for (xx in -1..3) {
				for (yy in 1..3) {
					for (zz in -1..2) {
						lvl.setBlockAt(x + xx, y + yy, z + zz, BlockID.Companion.AIR)
					}
				}
			}
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.OBSIDIAN)
			z += 1
			lvl.setBlockAt(x, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 3, y, z, BlockID.Companion.OBSIDIAN)
			z += 1
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.OBSIDIAN)
			z -= 1
			y += 1
			lvl.setBlockAt(x, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.NETHER_PORTAL)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.NETHER_PORTAL)
			lvl.setBlockAt(x + 3, y, z, BlockID.Companion.OBSIDIAN)
			y += 1
			lvl.setBlockAt(x, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.NETHER_PORTAL)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.NETHER_PORTAL)
			lvl.setBlockAt(x + 3, y, z, BlockID.Companion.OBSIDIAN)
			y += 1
			lvl.setBlockAt(x, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.NETHER_PORTAL)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.NETHER_PORTAL)
			lvl.setBlockAt(x + 3, y, z, BlockID.Companion.OBSIDIAN)
			y += 1
			lvl.setBlockAt(x, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 1, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 2, y, z, BlockID.Companion.OBSIDIAN)
			lvl.setBlockAt(x + 3, y, z, BlockID.Companion.OBSIDIAN)
		}
	}
}