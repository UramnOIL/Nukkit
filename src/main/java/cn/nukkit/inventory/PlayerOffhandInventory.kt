package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.entity.EntityHuman
import cn.nukkit.entity.EntityHumanType
import cn.nukkit.item.Item
import cn.nukkit.network.protocol.InventoryContentPacket
import cn.nukkit.network.protocol.InventorySlotPacket
import cn.nukkit.network.protocol.MobEquipmentPacket
import cn.nukkit.network.protocol.types.ContainerIds

class PlayerOffhandInventory(holder: EntityHumanType?) : BaseInventory(holder, InventoryType.OFFHAND) {
	override var size: Int
		get() = super.size
		set(size) {
			throw UnsupportedOperationException("Offhand can only carry one item at a time")
		}

	override fun onSlotChange(index: Int, before: Item?, send: Boolean) {
		val holder: EntityHuman = this.holder
		if (holder is Player && !holder.spawned) {
			return
		}
		this.sendContents(getViewers())
		this.sendContents(holder.viewers.values)
	}

	override fun sendContents(vararg players: Player) {
		val item = getItem(0)
		val pk = createMobEquipmentPacket(item)
		for (player in players) {
			if (player === this.holder) {
				val pk2 = InventoryContentPacket()
				pk2.inventoryId = ContainerIds.OFFHAND
				pk2.slots = arrayOf(item)
				player.dataPacket(pk2)
			} else {
				player.dataPacket(pk)
			}
		}
	}

	override fun sendSlot(index: Int, vararg players: Player) {
		val item = getItem(0)
		val pk = createMobEquipmentPacket(item)
		for (player in players) {
			if (player === this.holder) {
				val pk2 = InventorySlotPacket()
				pk2.inventoryId = ContainerIds.OFFHAND
				pk2.item = item
				player.dataPacket(pk2)
			} else {
				player.dataPacket(pk)
			}
		}
	}

	private fun createMobEquipmentPacket(item: Item?): MobEquipmentPacket {
		val pk = MobEquipmentPacket()
		pk.eid = this.holder.id
		pk.item = item
		pk.inventorySlot = 1
		pk.windowId = ContainerIds.OFFHAND
		pk.encode()
		pk.isEncoded = true
		return pk
	}

	override var holder: InventoryHolder?
		get() = super.getHolder() as EntityHuman
		set(holder) {
			super.holder = holder
		}
}