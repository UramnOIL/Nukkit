package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntitySkull
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSkull
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.BlockColor

/**
 * author: Justin
 */
class BlockSkull @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.SKULL_BLOCK

	override val hardness: Double
		get() = 1

	override val resistance: Double
		get() = 5

	override val isSolid: Boolean
		get() = false

	override val name: String
		get() {
			var itemMeta = 0
			if (level != null) {
				val blockEntity = getLevel().getBlockEntity(this)
				if (blockEntity != null) itemMeta = blockEntity.namedTag.getByte("SkullType")
			}
			return ItemSkull.getItemSkullName(itemMeta)
		}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		when (face) {
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP -> this.setDamage(face.index)
			BlockFace.DOWN -> return false
			else -> return false
		}
		getLevel().setBlock(block, this, true, true)
		val nbt = CompoundTag()
				.putString("id", BlockEntity.SKULL)
				.putByte("SkullType", item.damage)
				.putInt("x", block.floorX)
				.putInt("y", block.floorY)
				.putInt("z", block.floorZ)
				.putByte("Rot", Math.floor(player!!.yaw * 16 / 360 + 0.5).toInt() and 0x0f)
		if (item.hasCustomBlockData()) {
			for (aTag in item.customBlockData.allTags) {
				nbt.put(aTag.name, aTag)
			}
		}
		val skull = BlockEntity.createBlockEntity(BlockEntity.SKULL, getLevel().getChunk(block.x.toInt() shr 4, block.z.toInt() shr 4), nbt) as BlockEntitySkull
				?: return false

		// TODO: 2016/2/3 SPAWN WITHER
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		val blockEntity = getLevel().getBlockEntity(this)
		var dropMeta = 0
		if (blockEntity != null) dropMeta = blockEntity.namedTag.getByte("SkullType")
		return arrayOf(
				ItemSkull(dropMeta)
		)
	}

	override fun toItem(): Item? {
		val blockEntity = getLevel().getBlockEntity(this)
		var itemMeta = 0
		if (blockEntity != null) itemMeta = blockEntity.namedTag.getByte("SkullType")
		return ItemSkull(itemMeta)
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}