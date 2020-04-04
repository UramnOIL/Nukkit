package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.entity.EntityHuman
import cn.nukkit.entity.EntityHumanType
import cn.nukkit.event.entity.EntityArmorChangeEvent
import cn.nukkit.event.entity.EntityInventoryChangeEvent
import cn.nukkit.event.player.PlayerItemHeldEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.network.protocol.InventoryContentPacket
import cn.nukkit.network.protocol.InventorySlotPacket
import cn.nukkit.network.protocol.MobArmorEquipmentPacket
import cn.nukkit.network.protocol.MobEquipmentPacket
import cn.nukkit.network.protocol.types.ContainerIds

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PlayerInventory(player: EntityHumanType?) : BaseInventory(player, InventoryType.PLAYER) {
	protected var itemInHandIndex = 0
	private val hotbar: IntArray
	override var size: Int
		get() = super.getSize() - 4
		set(size) {
			super.setSize(size + 4)
			this.sendContents(getViewers())
		}

	/**
	 * Called when a client equips a hotbar inventorySlot. This method should not be used by plugins.
	 * This method will call PlayerItemHeldEvent.
	 *
	 * @param slot hotbar slot Number of the hotbar slot to equip.
	 * @return boolean if the equipment change was successful, false if not.
	 */
	fun equipItem(slot: Int): Boolean {
		if (!isHotbarSlot(slot)) {
			this.sendContents(this.holder as Player?)
			return false
		}
		if (this.holder is Player) {
			val player = this.holder as Player?
			val ev = PlayerItemHeldEvent(player, getItem(slot)!!, slot)
			this.holder.level.getServer().pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				this.sendContents(getViewers())
				return false
			}
			if (player!!.fishing != null) {
				if (getItem(slot) != player.fishing!!.rod) {
					player.stopFishing(false)
				}
			}
		}
		setHeldItemIndex(slot, false)
		return true
	}

	private fun isHotbarSlot(slot: Int): Boolean {
		return slot >= 0 && slot <= hotbarSize
	}

	@Deprecated("")
	fun getHotbarSlotIndex(index: Int): Int {
		return index
	}

	@Deprecated("")
	fun setHotbarSlotIndex(index: Int, slot: Int) {
	}

	var heldItemIndex: Int
		get() = itemInHandIndex
		set(index) {
			setHeldItemIndex(index, true)
		}

	fun setHeldItemIndex(index: Int, send: Boolean) {
		if (index >= 0 && index < hotbarSize) {
			itemInHandIndex = index
			if (this.holder is Player && send) {
				this.sendHeldItem((this.holder as Player?)!!)
			}
			this.sendHeldItem(this.holder.viewers.values)
		}
	}

	val itemInHand: Item
		get() {
			val item = getItem(heldItemIndex)
			return item ?: ItemBlock(get(BlockID.AIR), 0, 0)
		}

	fun setItemInHand(item: Item?): Boolean {
		return this.setItem(heldItemIndex, item)
	}

	@get:Deprecated("")
	var heldItemSlot: Int
		get() = itemInHandIndex
		set(slot) {
			if (!isHotbarSlot(slot)) {
				return
			}
			itemInHandIndex = slot
			if (this.holder is Player) {
				this.sendHeldItem((this.holder as Player?)!!)
			}
			this.sendHeldItem(getViewers())
		}

	fun sendHeldItem(vararg players: Player) {
		val item = itemInHand
		val pk = MobEquipmentPacket()
		pk.item = item
		pk.hotbarSlot = heldItemIndex
		pk.inventorySlot = pk.hotbarSlot
		for (player in players) {
			pk.eid = this.holder.id
			if (player.equals(this.holder)) {
				pk.eid = player.id
				this.sendSlot(heldItemIndex, player)
			}
			player.dataPacket(pk)
		}
	}

	fun sendHeldItem(players: Collection<Player?>?) {
		this.sendHeldItem(*players!!.toTypedArray())
	}

	override fun onSlotChange(index: Int, before: Item?, send: Boolean) {
		val holder: EntityHuman = this.holder
		if (holder is Player && !holder.spawned) {
			return
		}
		if (index >= this.size) {
			this.sendArmorSlot(index, getViewers())
			this.sendArmorSlot(index, this.holder.viewers.values)
		} else {
			super.onSlotChange(index, before, send)
		}
	}

	val hotbarSize: Int
		get() = 9

	fun getArmorItem(index: Int): Item? {
		return getItem(this.size + index)
	}

	fun setArmorItem(index: Int, item: Item?): Boolean {
		return this.setArmorItem(index, item, false)
	}

	fun setArmorItem(index: Int, item: Item?, ignoreArmorEvents: Boolean): Boolean {
		return this.setItem(this.size + index, item, ignoreArmorEvents)
	}

	val helmet: Item?
		get() = getItem(this.size)

	val chestplate: Item?
		get() = getItem(this.size + 1)

	val leggings: Item?
		get() = getItem(this.size + 2)

	val boots: Item?
		get() = getItem(this.size + 3)

	fun setHelmet(helmet: Item?): Boolean {
		return this.setItem(this.size, helmet)
	}

	fun setChestplate(chestplate: Item?): Boolean {
		return this.setItem(this.size + 1, chestplate)
	}

	fun setLeggings(leggings: Item?): Boolean {
		return this.setItem(this.size + 2, leggings)
	}

	fun setBoots(boots: Item?): Boolean {
		return this.setItem(this.size + 3, boots)
	}

	override fun setItem(index: Int, item: Item?): Boolean {
		return setItem(index, item, true, false)
	}

	private fun setItem(index: Int, item: Item?, send: Boolean, ignoreArmorEvents: Boolean): Boolean {
		var item = item
		if (index < 0 || index >= this.size) {
			return false
		} else if (item!!.id == 0 || item.getCount() <= 0) {
			return this.clear(index)
		}

		//Armor change
		item = if (!ignoreArmorEvents && index >= this.size) {
			val ev = EntityArmorChangeEvent(this.holder, getItem(index)!!, item!!, index)
			Server.instance!!.pluginManager.callEvent(ev)
			if (ev.isCancelled && this.holder != null) {
				this.sendArmorSlot(index, getViewers())
				return false
			}
			ev.newItem
		} else {
			val ev = EntityInventoryChangeEvent(this.holder, getItem(index)!!, item!!, index)
			Server.instance!!.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				this.sendSlot(index, getViewers())
				return false
			}
			ev.newItem
		}
		val old = getItem(index)
		slots[index] = item.clone()
		onSlotChange(index, old, send)
		return true
	}

	override fun clear(index: Int, send: Boolean): Boolean {
		if (slots.containsKey(index)) {
			var item: Item = ItemBlock(get(BlockID.AIR), null, 0)
			val old = slots[index]
			item = if (index >= this.size && index < this.size) {
				val ev = EntityArmorChangeEvent(this.holder, old!!, item, index)
				Server.instance!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					if (index >= this.size) {
						this.sendArmorSlot(index, getViewers())
					} else {
						this.sendSlot(index, getViewers())
					}
					return false
				}
				ev.newItem
			} else {
				val ev = EntityInventoryChangeEvent(this.holder, old!!, item, index)
				Server.instance!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					if (index >= this.size) {
						this.sendArmorSlot(index, getViewers())
					} else {
						this.sendSlot(index, getViewers())
					}
					return false
				}
				ev.newItem
			}
			if (item.id != Item.AIR) {
				slots[index] = item.clone()
			} else {
				slots.remove(index)
			}
			onSlotChange(index, old, send)
		}
		return true
	}

	var armorContents: Array<Item?>
		get() {
			val armor = arrayOfNulls<Item>(4)
			for (i in 0..3) {
				armor[i] = getItem(this.size + i)
			}
			return armor
		}
		set(items) {
			var items = items
			if (items.size < 4) {
				val newItems = arrayOfNulls<Item>(4)
				System.arraycopy(items, 0, newItems, 0, items.size)
				items = newItems
			}
			for (i in 0..3) {
				if (items[i] == null) {
					items[i] = ItemBlock(get(BlockID.AIR), null, 0)
				}
				if (items[i]!!.id == Item.AIR) {
					this.clear(this.size + i)
				} else {
					this.setItem(this.size + i, items[i])
				}
			}
		}

	override fun clearAll() {
		val limit: Int = this.size + 4
		for (index in 0 until limit) {
			this.clear(index)
		}
		holder.offhandInventory.clearAll()
	}

	fun sendArmorContents(player: Player?) {
		this.sendArmorContents(arrayOf(player))
	}

	fun sendArmorContents(players: Array<Player?>) {
		val armor = armorContents
		val pk = MobArmorEquipmentPacket()
		pk.eid = this.holder.id
		pk.slots = armor
		pk.encode()
		pk.isEncoded = true
		for (player in players) {
			if (player!!.equals(this.holder)) {
				val pk2 = InventoryContentPacket()
				pk2.inventoryId = InventoryContentPacket.SPECIAL_ARMOR
				pk2.slots = armor
				player.dataPacket(pk2)
			} else {
				player.dataPacket(pk)
			}
		}
	}

	fun sendArmorContents(players: Collection<Player?>) {
		this.sendArmorContents(players.toTypedArray())
	}

	fun sendArmorSlot(index: Int, player: Player) {
		this.sendArmorSlot(index, arrayOf(player))
	}

	fun sendArmorSlot(index: Int, players: Array<Player>) {
		val armor = armorContents
		val pk = MobArmorEquipmentPacket()
		pk.eid = this.holder.id
		pk.slots = armor
		pk.encode()
		pk.isEncoded = true
		for (player in players) {
			if (player.equals(this.holder)) {
				val pk2 = InventorySlotPacket()
				pk2.inventoryId = InventoryContentPacket.SPECIAL_ARMOR
				pk2.slot = index - this.size
				pk2.item = getItem(index)
				player.dataPacket(pk2)
			} else {
				player.dataPacket(pk)
			}
		}
	}

	fun sendArmorSlot(index: Int, players: Collection<Player?>?) {
		this.sendArmorSlot(index, players!!.toTypedArray())
	}

	override fun sendContents(player: Player?) {
		this.sendContents(arrayOf(player))
	}

	override fun sendContents(players: Collection<Player?>?) {
		this.sendContents(players!!.toTypedArray())
	}

	override fun sendContents(players: Array<Player?>) {
		val pk = InventoryContentPacket()
		pk.slots = arrayOfNulls(this.size)
		for (i in 0 until this.size) {
			pk.slots[i] = getItem(i)
		}

		/*//Because PE is stupid and shows 9 less slots than you send it, give it 9 dummy slots so it shows all the REAL slots.
        for(int i = this.getSize(); i < this.getSize() + this.getHotbarSize(); ++i){
            pk.slots[i] = new ItemBlock(Block.get(BlockID.AIR));
        }
            pk.slots[i] = new ItemBlock(Block.get(BlockID.AIR));
        }*/for (player in players) {
			val id = player!!.getWindowId(this)
			if (id == -1 || !player.spawned) {
				if (this.holder !== player) close(player)
				continue
			}
			pk.inventoryId = id
			player.dataPacket(pk.clone())
		}
	}

	override fun sendSlot(index: Int, player: Player?) {
		this.sendSlot(index, *arrayOf(player))
	}

	override fun sendSlot(index: Int, players: Collection<Player?>?) {
		this.sendSlot(index, *players!!.toTypedArray())
	}

	override fun sendSlot(index: Int, vararg players: Player) {
		val pk = InventorySlotPacket()
		pk.slot = index
		pk.item = getItem(index)!!.clone()
		for (player in players) {
			if (player.equals(this.holder)) {
				pk.inventoryId = ContainerIds.INVENTORY
				player.dataPacket(pk)
			} else {
				val id = player.getWindowId(this)
				if (id == -1) {
					close(player)
					continue
				}
				pk.inventoryId = id
				player.dataPacket(pk.clone())
			}
		}
	}

	fun sendCreativeContents() {
		if (this.holder !is Player) {
			return
		}
		val p = this.holder as Player?
		val pk = InventoryContentPacket()
		pk.inventoryId = ContainerIds.CREATIVE
		if (!p!!.isSpectator) { //fill it for all gamemodes except spectator
			pk.slots = Item.getCreativeItems().toTypedArray()
		}
		p.dataPacket(pk)
	}

	override var holder: InventoryHolder?
		get() = super.getHolder() as EntityHuman
		set(holder) {
			super.holder = holder
		}

	init {
		hotbar = IntArray(hotbarSize)
		for (i in hotbar.indices) {
			hotbar[i] = i
		}
	}
}