package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.entity.EntityHuman
import cn.nukkit.entity.EntityHumanType
import cn.nukkit.level.Level
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.network.protocol.ContainerClosePacket
import cn.nukkit.network.protocol.ContainerOpenPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket

class PlayerEnderChestInventory(player: EntityHumanType?) : BaseInventory(player, InventoryType.ENDER_CHEST) {
	override var holder: InventoryHolder?
		get() = field as EntityHuman?
		set(holder) {
			super.holder = holder
		}

	override fun onOpen(who: Player) {
		if (who !== this.holder) {
			return
		}
		super.onOpen(who)
		val containerOpenPacket = ContainerOpenPacket()
		containerOpenPacket.windowId = who.getWindowId(this)
		containerOpenPacket.type = getType().networkType
		val chest = who.viewingEnderChest
		if (chest != null) {
			containerOpenPacket.x = chest.getX().toInt()
			containerOpenPacket.y = chest.getY().toInt()
			containerOpenPacket.z = chest.getZ().toInt()
		} else {
			containerOpenPacket.z = 0
			containerOpenPacket.y = containerOpenPacket.z
			containerOpenPacket.x = containerOpenPacket.y
		}
		who.dataPacket(containerOpenPacket)
		this.sendContents(who)
		if (chest != null && chest.viewers.size == 1) {
			val blockEventPacket = BlockEventPacket()
			blockEventPacket.x = chest.getX().toInt()
			blockEventPacket.y = chest.getY().toInt()
			blockEventPacket.z = chest.getZ().toInt()
			blockEventPacket.case1 = 1
			blockEventPacket.case2 = 2
			val level: Level = this.holder.level
			if (level != null) {
				level.addLevelSoundEvent(this.holder.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_ENDERCHEST_OPEN)
				level.addChunkPacket(this.holder.x as Int shr 4, this.holder.z as Int shr 4, blockEventPacket)
			}
		}
	}

	override fun onClose(who: Player) {
		val containerClosePacket = ContainerClosePacket()
		containerClosePacket.windowId = who.getWindowId(this)
		who.dataPacket(containerClosePacket)
		super.onClose(who)
		val chest = who.viewingEnderChest
		if (chest != null && chest.viewers.size == 1) {
			val blockEventPacket = BlockEventPacket()
			blockEventPacket.x = chest.getX().toInt()
			blockEventPacket.y = chest.getY().toInt()
			blockEventPacket.z = chest.getZ().toInt()
			blockEventPacket.case1 = 1
			blockEventPacket.case2 = 0
			val level: Level = this.holder.getLevel()
			if (level != null) {
				level.addLevelSoundEvent(this.holder.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_ENDERCHEST_CLOSED)
				level.addChunkPacket(this.holder.getX() as Int shr 4, this.holder.getZ() as Int shr 4, blockEventPacket)
			}
			who.viewingEnderChest = null
		}
		super.onClose(who)
	}
}