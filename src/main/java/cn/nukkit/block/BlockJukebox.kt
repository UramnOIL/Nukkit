package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityJukebox
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemRecord
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.BlockColor

/**
 * Created by CreeperFace on 7.8.2017.
 */
class BlockJukebox : BlockSolid() {
	override val name: String
		get() = "Jukebox"

	override val id: Int
		get() = BlockID.Companion.JUKEBOX

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		var blockEntity = getLevel().getBlockEntity(this)
		if (blockEntity !is BlockEntityJukebox) {
			blockEntity = createBlockEntity()
		}
		val jukebox = blockEntity as BlockEntityJukebox
		if (jukebox.recordItem.id != 0) {
			jukebox.dropItem()
		} else if (item is ItemRecord) {
			jukebox.recordItem = item
			jukebox.play()
			player!!.inventory.decreaseCount(player.inventory.heldItemIndex)
		}
		return false
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (super.place(item, block, target, face, fx, fy, fz, player)) {
			createBlockEntity()
			return true
		}
		return false
	}

	override fun onBreak(item: Item): Boolean {
		if (super.onBreak(item)) {
			val blockEntity = level.getBlockEntity(this)
			if (blockEntity is BlockEntityJukebox) {
				blockEntity.dropItem()
			}
			return true
		}
		return false
	}

	private fun createBlockEntity(): BlockEntity {
		val nbt = CompoundTag()
				.putList(ListTag("Items"))
				.putString("id", BlockEntity.JUKEBOX)
				.putInt("x", floorX)
				.putInt("y", floorY)
				.putInt("z", floorZ)
		return BlockEntity.createBlockEntity(BlockEntity.JUKEBOX, level.getChunk(floorX shr 4, floorZ shr 4), nbt)
	}

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}