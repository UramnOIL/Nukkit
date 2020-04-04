package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSign
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * @author Nukkit Project Team
 */
open class BlockSignPost @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.SIGN_POST

	override val hardness: Double
		get() = 1

	override val resistance: Double
		get() = 5

	override val isSolid: Boolean
		get() = false

	override val name: String
		get() = "Sign Post"

	override val boundingBox: AxisAlignedBB?
		get() = null

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (face != BlockFace.DOWN) {
			val nbt = CompoundTag()
					.putString("id", BlockEntity.SIGN)
					.putInt("x", block.x.toInt())
					.putInt("y", block.y.toInt())
					.putInt("z", block.z.toInt())
					.putString("Text1", "")
					.putString("Text2", "")
					.putString("Text3", "")
					.putString("Text4", "")
			if (face == BlockFace.UP) {
				setDamage(Math.floor((player!!.yaw + 180) * 16 / 360 + 0.5).toInt() and 0x0f)
				getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.SIGN_POST, damage), true)
			} else {
				setDamage(face.index)
				getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.WALL_SIGN, damage), true)
			}
			if (player != null) {
				nbt.putString("Creator", player.getUniqueId().toString())
			}
			if (item.hasCustomBlockData()) {
				for (aTag in item.customBlockData.allTags) {
					nbt.put(aTag.name, aTag)
				}
			}
			val sign = BlockEntity.createBlockEntity(BlockEntity.SIGN, getLevel().getChunk(block.x.toInt() shr 4, block.z.toInt() shr 4), nbt) as BlockEntitySign
			return sign != null
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (down().id == BlockID.Companion.AIR) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun toItem(): Item? {
		return ItemSign()
	}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromIndex(this.damage and 0x07)
	}
}