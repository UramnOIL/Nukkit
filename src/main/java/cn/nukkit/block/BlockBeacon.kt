package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBeacon
import cn.nukkit.inventory.BeaconInventory
import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * author: Angelic47 Nukkit Project
 */
class BlockBeacon : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.BEACON

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 15

	override val lightLevel: Int
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Beacon"

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val t = getLevel().getBlockEntity(this)
			val beacon: BlockEntityBeacon
			if (t is BlockEntityBeacon) {
				beacon = t
			} else {
				val nbt = CompoundTag("")
						.putString("id", BlockEntity.BEACON)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
				beacon = BlockEntity.createBlockEntity(BlockEntity.BEACON, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityBeacon
				if (beacon == null) {
					return false
				}
			}
			player.addWindow(BeaconInventory(player.uIInventory, this), Player.BEACON_WINDOW_ID)
		}
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val blockSuccess = super.place(item, block, target, face, fx, fy, fz, player)
		if (blockSuccess) {
			val nbt = CompoundTag("")
					.putString("id", BlockEntity.BEACON)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
			val beacon = BlockEntity.createBlockEntity(BlockEntity.BEACON, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityBeacon
					?: return false
		}
		return blockSuccess
	}

	override fun canBePushed(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.DIAMOND_BLOCK_COLOR
}