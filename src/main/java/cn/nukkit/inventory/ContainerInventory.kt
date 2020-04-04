package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.ContainerClosePacket
import cn.nukkit.network.protocol.ContainerOpenPacket

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class ContainerInventory : BaseInventory {
	constructor(holder: InventoryHolder?, type: InventoryType) : super(holder, type) {}
	constructor(holder: InventoryHolder?, type: InventoryType, items: Map<Int?, Item?>) : super(holder, type, items) {}
	constructor(holder: InventoryHolder?, type: InventoryType, items: Map<Int?, Item?>, overrideSize: Int?) : super(holder, type, items, overrideSize) {}
	constructor(holder: InventoryHolder?, type: InventoryType, items: Map<Int?, Item?>, overrideSize: Int?, overrideTitle: String?) : super(holder, type, items, overrideSize, overrideTitle) {}

	override fun onOpen(who: Player) {
		super.onOpen(who)
		val pk = ContainerOpenPacket()
		pk.windowId = who.getWindowId(this)
		pk.type = getType().networkType
		val holder = getHolder()
		if (holder is Vector3) {
			pk.x = (holder as Vector3).getX().toInt()
			pk.y = (holder as Vector3).getY().toInt()
			pk.z = (holder as Vector3).getZ().toInt()
		} else {
			pk.z = 0
			pk.y = pk.z
			pk.x = pk.y
		}
		if (holder is Entity) {
			pk.entityId = (holder as Entity).id
		}
		who.dataPacket(pk)
		this.sendContents(who)
	}

	override fun onClose(who: Player) {
		val pk = ContainerClosePacket()
		pk.windowId = who.getWindowId(this)
		who.dataPacket(pk)
		super.onClose(who)
	}

	companion object {
		fun calculateRedstone(inv: Inventory?): Int {
			return if (inv == null) {
				0
			} else {
				var itemCount = 0
				var averageCount = 0f
				for (slot in 0 until inv.size) {
					val item = inv.getItem(slot)
					if (item!!.id != 0) {
						averageCount += item.getCount().toFloat() / Math.min(inv.maxStackSize, item.maxStackSize).toFloat()
						++itemCount
					}
				}
				averageCount = averageCount / inv.size as Float
				NukkitMath.floorFloat(averageCount * 14) + if (itemCount > 0) 1 else 0
			}
		}
	}
}