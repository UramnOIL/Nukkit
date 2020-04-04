package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.item.Item

open class PlayerUIComponent internal constructor(private val playerUI: PlayerUIInventory, private val offset: Int, size: Int) : BaseInventory(playerUI.holder, InventoryType.UI, emptyMap(), size) {
	private override val size: Int
	override fun getSize(): Int {
		return size
	}

	override var maxStackSize: Int
		get() = 64
		set(size) {
			throw UnsupportedOperationException()
		}

	override val title: String?
		get() {
			throw UnsupportedOperationException()
		}

	override fun getItem(index: Int): Item? {
		return playerUI.getItem(index + offset)
	}

	override fun setItem(index: Int, item: Item?, send: Boolean): Boolean {
		return playerUI.setItem(index + offset, item, send)
	}

	override fun getContents(): Map<Int?, Item?>? {
		val contents = playerUI.contents
		contents!!.keys.removeIf { slot: Int? -> slot!! < offset || slot > offset + size }
		return contents
	}

	override fun sendContents(vararg players: Player) {
		playerUI.sendContents(*players)
	}

	override fun sendSlot(index: Int, vararg players: Player) {
		playerUI.sendSlot(index + offset, *players)
	}

	override fun getViewers(): Set<Player?>? {
		return playerUI.viewers
	}

	override fun getType(): InventoryType? {
		return playerUI.type
	}

	override fun onOpen(who: Player) {}
	override fun open(who: Player): Boolean {
		return false
	}

	override fun close(who: Player) {}
	override fun onClose(who: Player) {}
	override fun onSlotChange(index: Int, before: Item?, send: Boolean) {
		playerUI.onSlotChange(index + offset, before, send)
	}

	init {
		this.size = size
	}
}