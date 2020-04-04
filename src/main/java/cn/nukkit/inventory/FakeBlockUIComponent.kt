package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.event.inventory.InventoryOpenEvent
import cn.nukkit.level.Position
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.ContainerClosePacket
import cn.nukkit.network.protocol.ContainerOpenPacket

open class FakeBlockUIComponent internal constructor(playerUI: PlayerUIInventory, type: InventoryType, offset: Int, position: Position) : PlayerUIComponent(playerUI, offset, type.defaultSize) {
	private override val type: InventoryType
	override var holder: InventoryHolder?
		get() = field as FakeBlockMenu?
		set(holder) {
			super.holder = holder
		}

	override fun open(who: Player): Boolean {
		val ev = InventoryOpenEvent(this, who)
		who.getServer().pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return false
		}
		onOpen(who)
		return true
	}

	override fun onOpen(who: Player) {
		super.onOpen(who)
		val pk = ContainerOpenPacket()
		pk.windowId = who.getWindowId(this)
		pk.type = type.getNetworkType()
		val holder: InventoryHolder = this.holder
		if (holder != null) {
			pk.x = (holder as Vector3).getX().toInt()
			pk.y = (holder as Vector3).getY().toInt()
			pk.z = (holder as Vector3).getZ().toInt()
		} else {
			pk.z = 0
			pk.y = pk.z
			pk.x = pk.y
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

	init {
		this.type = type
		this.holder = FakeBlockMenu(this, position)
	}
}