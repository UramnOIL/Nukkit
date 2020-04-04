package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.block.BlockID
import cn.nukkit.blockentity.BlockEntityShulkerBox
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * Created by PetteriM1
 */
class ShulkerBoxInventory(box: BlockEntityShulkerBox?) : ContainerInventory(box, InventoryType.SHULKER_BOX) {
	override var holder: InventoryHolder?
		get() = field as BlockEntityShulkerBox?
		set(holder) {
			super.holder = holder
		}

	override fun onOpen(who: Player) {
		super.onOpen(who)
		if (getViewers()!!.size == 1) {
			val pk = BlockEventPacket()
			pk.x = this.holder.getX() as Int
			pk.y = this.holder.getY() as Int
			pk.z = this.holder.getZ() as Int
			pk.case1 = 1
			pk.case2 = 2
			val level: Level = this.holder.getLevel()
			if (level != null) {
				level.addLevelSoundEvent(this.holder.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_SHULKERBOX_OPEN)
				level.addChunkPacket(this.holder.getX() as Int shr 4, this.holder.getZ() as Int shr 4, pk)
			}
		}
	}

	override fun onClose(who: Player) {
		if (getViewers()!!.size == 1) {
			val pk = BlockEventPacket()
			pk.x = this.holder.getX() as Int
			pk.y = this.holder.getY() as Int
			pk.z = this.holder.getZ() as Int
			pk.case1 = 1
			pk.case2 = 0
			val level: Level = this.holder.getLevel()
			if (level != null) {
				level.addLevelSoundEvent(this.holder.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_SHULKERBOX_CLOSED)
				level.addChunkPacket(this.holder.getX() as Int shr 4, this.holder.getZ() as Int shr 4, pk)
			}
		}
		super.onClose(who)
	}

	override fun canAddItem(item: Item): Boolean {
		return if (item.id == BlockID.SHULKER_BOX || item.id == BlockID.UNDYED_SHULKER_BOX) {
			// Do not allow nested shulker boxes.
			false
		} else super.canAddItem(item)
	}

	override fun sendSlot(index: Int, vararg players: Player) {
		super.sendSlot(index, *players)
	}
}