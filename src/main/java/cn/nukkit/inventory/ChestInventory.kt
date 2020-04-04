package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.level.Level
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ChestInventory(chest: BlockEntityChest?) : ContainerInventory(chest, InventoryType.CHEST) {
	var doubleInventory: DoubleChestInventory? = null
	override var holder: InventoryHolder?
		get() = field as BlockEntityChest?
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
				level.addLevelSoundEvent(this.holder.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_CHEST_OPEN)
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
				level.addLevelSoundEvent(this.holder.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_CHEST_CLOSED)
				level.addChunkPacket(this.holder.getX() as Int shr 4, this.holder.getZ() as Int shr 4, pk)
			}
		}
		super.onClose(who)
	}

	override fun sendSlot(index: Int, vararg players: Player) {
		if (doubleInventory != null) {
			doubleInventory!!.sendSlot(this, index, *players)
		} else {
			super.sendSlot(index, *players)
		}
	}
}