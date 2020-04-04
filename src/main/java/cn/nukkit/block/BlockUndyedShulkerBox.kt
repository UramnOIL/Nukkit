/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityShulkerBox
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.BlockColor

/**
 *
 * @author Reece Mackie
 */
open class BlockUndyedShulkerBox : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.UNDYED_SHULKER_BOX

	override val name: String
		get() = "Shulker Box"

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 10

	override fun canBeActivated(): Boolean {
		return true
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun toItem(): Item? {
		val item = ItemBlock(this, this.damage, 1)
		val t = getLevel().getBlockEntity(this) as BlockEntityShulkerBox
		if (t != null) {
			val i = t.realInventory
			if (!i.isEmpty) {
				var nbt = item.namedTag
				if (nbt == null) nbt = CompoundTag("")
				val items = ListTag<CompoundTag>()
				for (it in 0 until i.size) {
					if (i.getItem(it).id != Item.AIR) {
						val d = NBTIO.putItemHelper(i.getItem(it), it)
						items.add(d)
					}
				}
				nbt.put("Items", items)
				item.setCompoundTag(nbt)
			}
			if (t.hasName()) {
				item.customName = t.getName()
			}
		}
		return item
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		getLevel().setBlock(block, this, true)
		val nbt = BlockEntity.getDefaultCompound(this, BlockEntity.SHULKER_BOX)
				.putByte("facing", face.index)
		if (item.hasCustomName()) {
			nbt.putString("CustomName", item.customName)
		}
		val t = item.namedTag
		if (t != null) {
			if (t.contains("Items")) {
				nbt.putList(t.getList("Items"))
			}
		}
		val box = BlockEntity.createBlockEntity(BlockEntity.SHULKER_BOX, getLevel().getChunk(this.floorX shr 4, this.floorZ shr 4), nbt) as BlockEntityShulkerBox
		return box != null
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val t = getLevel().getBlockEntity(this)
			val box: BlockEntityShulkerBox
			if (t is BlockEntityShulkerBox) {
				box = t
			} else {
				val nbt = BlockEntity.getDefaultCompound(this, BlockEntity.SHULKER_BOX)
				box = BlockEntity.createBlockEntity(BlockEntity.SHULKER_BOX, getLevel().getChunk(this.floorX shr 4, this.floorZ shr 4), nbt) as BlockEntityShulkerBox
				if (box == null) {
					return false
				}
			}
			val block = this.getSide(BlockFace.fromIndex(box.namedTag.getByte("facing")))
			if (block !is BlockAir && block !is BlockLiquid && block !is BlockFlowable) {
				return true
			}
			player.addWindow(box.inventory)
		}
		return true
	}

	override val color: BlockColor
		get() = BlockColor.PURPLE_BLOCK_COLOR
}