package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.math.BlockFace.Plane
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag

class BlockTrappedChest @JvmOverloads constructor(meta: Int = 0) : BlockChest(meta) {
	override val id: Int
		get() = BlockID.Companion.TRAPPED_CHEST

	override val name: String
		get() = "Trapped Chest"

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = intArrayOf(2, 5, 3, 4)
		var chest: BlockEntityChest? = null
		this.setDamage(faces[player?.direction?.horizontalIndex ?: 0])
		for (side in Plane.HORIZONTAL) {
			if ((this.damage == 4 || this.damage == 5) && (side == BlockFace.WEST || side == BlockFace.EAST)) {
				continue
			} else if ((this.damage == 3 || this.damage == 2) && (side == BlockFace.NORTH || side == BlockFace.SOUTH)) {
				continue
			}
			val c = this.getSide(side)
			if (c is BlockTrappedChest && c.getDamage() == this.damage) {
				val blockEntity = getLevel().getBlockEntity(c)
				if (blockEntity is BlockEntityChest && !blockEntity.isPaired) {
					chest = blockEntity
					break
				}
			}
		}
		getLevel().setBlock(block, this, true, true)
		val nbt = CompoundTag("")
				.putList(ListTag("Items"))
				.putString("id", BlockEntity.CHEST)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
		if (item.hasCustomName()) {
			nbt.putString("CustomName", item.customName)
		}
		if (item.hasCustomBlockData()) {
			val customData = item.customBlockData.tags
			for ((key, value) in customData) {
				nbt.put(key, value)
			}
		}
		val blockEntity = BlockEntity.createBlockEntity(BlockEntity.CHEST, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityChest
				?: return false
		if (chest != null) {
			chest.pairWith(blockEntity)
			blockEntity.pairWith(chest)
		}
		return true
	}

	override fun getWeakPower(face: BlockFace): Int {
		var playerCount = 0
		val blockEntity = level.getBlockEntity(this)
		if (blockEntity is BlockEntityChest) {
			playerCount = blockEntity.inventory.viewers.size
		}
		return if (playerCount < 0) 0 else if (playerCount > 15) 15 else playerCount
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (side == BlockFace.UP) getWeakPower(side) else 0
	}

	override val isPowerSource: Boolean
		get() = true
}