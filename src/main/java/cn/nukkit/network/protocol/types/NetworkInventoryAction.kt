package cn.nukkit.network.protocol.types

import cn.nukkit.Player
import cn.nukkit.inventory.AnvilInventory
import cn.nukkit.inventory.BeaconInventory
import cn.nukkit.inventory.EnchantInventory
import cn.nukkit.inventory.Inventory
import cn.nukkit.inventory.transaction.action.*
import cn.nukkit.item.Item
import cn.nukkit.item.ItemID
import cn.nukkit.network.protocol.InventoryTransactionPacket
import lombok.ToString
import java.util.Optional
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * @author CreeperFace
 */
@ToString
class NetworkInventoryAction {
	var sourceType = 0
	var windowId = 0
	var unknown: Long = 0
	var inventorySlot = 0
	var oldItem: Item? = null
	var newItem: Item? = null
	fun read(packet: InventoryTransactionPacket?): NetworkInventoryAction? {
		sourceType = packet.getUnsignedVarInt() as Int
		when (sourceType) {
			SOURCE_CONTAINER -> windowId = packet.getVarInt()
			SOURCE_WORLD -> unknown = packet.getUnsignedVarInt()
			SOURCE_CREATIVE -> {
			}
			SOURCE_CRAFT_SLOT, SOURCE_TODO -> {
				windowId = packet.getVarInt()
				when (windowId) {
					SOURCE_TYPE_CRAFTING_RESULT, SOURCE_TYPE_CRAFTING_USE_INGREDIENT -> packet!!.isCraftingPart = true
				}
			}
		}
		inventorySlot = packet.getUnsignedVarInt() as Int
		oldItem = packet.getSlot()
		newItem = packet.getSlot()
		return this
	}

	fun write(packet: InventoryTransactionPacket?) {
		packet.putUnsignedVarInt(sourceType)
		when (sourceType) {
			SOURCE_CONTAINER -> packet.putVarInt(windowId)
			SOURCE_WORLD -> packet.putUnsignedVarInt(unknown)
			SOURCE_CREATIVE -> {
			}
			SOURCE_CRAFT_SLOT, SOURCE_TODO -> packet.putVarInt(windowId)
		}
		packet.putUnsignedVarInt(inventorySlot)
		packet.putSlot(oldItem)
		packet.putSlot(newItem)
	}

	fun createInventoryAction(player: Player?): InventoryAction? {
		return when (sourceType) {
			SOURCE_CONTAINER -> {
				if (windowId == ContainerIds.ARMOR) {
					//TODO: HACK!
					inventorySlot += 36
					windowId = ContainerIds.INVENTORY
				}
				val window: Inventory = player.getWindowById(windowId)
				if (window != null) {
					return SlotChangeAction(window, inventorySlot, oldItem, newItem)
				}
				player.getServer().getLogger().debug("Player " + player.getName().toString() + " has no open container with window ID " + windowId)
				null
			}
			SOURCE_WORLD -> {
				if (inventorySlot != InventoryTransactionPacket.ACTION_MAGIC_SLOT_DROP_ITEM) {
					player.getServer().getLogger().debug("Only expecting drop-item world actions from the client!")
					return null
				}
				DropItemAction(oldItem, newItem)
			}
			SOURCE_CREATIVE -> {
				val type: Int
				type = when (inventorySlot) {
					InventoryTransactionPacket.ACTION_MAGIC_SLOT_CREATIVE_DELETE_ITEM -> CreativeInventoryAction.TYPE_DELETE_ITEM
					InventoryTransactionPacket.ACTION_MAGIC_SLOT_CREATIVE_CREATE_ITEM -> CreativeInventoryAction.TYPE_CREATE_ITEM
					else -> {
						player.getServer().getLogger().debug("Unexpected creative action type " + inventorySlot)
						return null
					}
				}
				CreativeInventoryAction(oldItem, newItem, type)
			}
			SOURCE_CRAFT_SLOT, SOURCE_TODO -> {
				when (windowId) {
					SOURCE_TYPE_CRAFTING_ADD_INGREDIENT, SOURCE_TYPE_CRAFTING_REMOVE_INGREDIENT -> return SlotChangeAction(player.getCraftingGrid(), inventorySlot, oldItem, newItem)
					SOURCE_TYPE_CONTAINER_DROP_CONTENTS -> {
						val inventory: Optional<Inventory?> = player.getTopWindow()
						return if (!inventory.isPresent()) {
							// No window open?
							null
						} else SlotChangeAction(inventory.get(), inventorySlot, oldItem, newItem)
					}
					SOURCE_TYPE_CRAFTING_RESULT -> return CraftingTakeResultAction(oldItem, newItem)
					SOURCE_TYPE_CRAFTING_USE_INGREDIENT -> return CraftingTransferMaterialAction(oldItem, newItem, inventorySlot)
				}
				if (windowId >= SOURCE_TYPE_ANVIL_OUTPUT && windowId <= SOURCE_TYPE_ANVIL_INPUT) { //anvil actions
					val inv: Inventory = player.getWindowById(Player.ANVIL_WINDOW_ID)
					if (inv !is AnvilInventory) {
						player.getServer().getLogger().debug("Player " + player.getName().toString() + " has no open anvil inventory")
						return null
					}
					val anvil: AnvilInventory = inv as AnvilInventory
					when (windowId) {
						SOURCE_TYPE_ANVIL_INPUT -> {
							//System.out.println("action input");
							inventorySlot = 0
							return SlotChangeAction(anvil, inventorySlot, oldItem, newItem)
						}
						SOURCE_TYPE_ANVIL_MATERIAL -> {
							//System.out.println("material");
							inventorySlot = 1
							return SlotChangeAction(anvil, inventorySlot, oldItem, newItem)
						}
						SOURCE_TYPE_ANVIL_OUTPUT -> {
						}
						SOURCE_TYPE_ANVIL_RESULT -> {
							inventorySlot = 2
							anvil.clear(0)
							val material: Item = anvil.getItem(1)
							if (!material.isNull()) {
								material.setCount(material.getCount() - 1)
								anvil.setItem(1, material)
							}
							anvil.setItem(2, oldItem)
							//System.out.println("action result");
							return SlotChangeAction(anvil, inventorySlot, oldItem, newItem)
						}
					}
				}
				if (windowId >= SOURCE_TYPE_ENCHANT_OUTPUT && windowId <= SOURCE_TYPE_ENCHANT_INPUT) {
					val inv: Inventory = player.getWindowById(Player.ENCHANT_WINDOW_ID)
					if (inv !is EnchantInventory) {
						player.getServer().getLogger().debug("Player " + player.getName().toString() + " has no open enchant inventory")
						return null
					}
					val enchant: EnchantInventory = inv as EnchantInventory
					when (windowId) {
						SOURCE_TYPE_ENCHANT_INPUT -> if (inventorySlot != 0) {
							// Input should only be in slot 0.
							return null
						}
						SOURCE_TYPE_ENCHANT_MATERIAL -> if (inventorySlot != 1) {
							// Material should only be in slot 1.
							return null
						}
						SOURCE_TYPE_ENCHANT_OUTPUT -> {
							if (inventorySlot != 0) {
								// Outputs should only be in slot 0.
								return null
							}
							if (Item.get(ItemID.DYE, 4).equals(newItem, true, false)) {
								inventorySlot = 2 // Fake slot to store used material
								if (newItem.getCount() < 1 || newItem.getCount() > 3) {
									// Invalid material
									return null
								}
								val material: Item = enchant.getItem(1)
								// Material to take away.
								val toRemove: Int = newItem.getCount()
								if (material.getId() !== ItemID.DYE && material.getDamage() !== 4 && material.getCount() < toRemove) {
									// Invalid material or not enough
									return null
								}
							} else {
								val toEnchant: Item = enchant.getItem(0)
								val material: Item = enchant.getItem(1)
								if (toEnchant.equals(newItem, true, true) &&
										(material.getId() === ItemID.DYE && material.getDamage() === 4 || player.isCreative())) {
									inventorySlot = 3 // Fake slot to store the resultant item.

									//TODO: Check (old) item has valid enchantments
									enchant.setItem(3, oldItem, false)
								} else {
									return null
								}
							}
						}
					}
					return SlotChangeAction(enchant, inventorySlot, oldItem, newItem)
				}
				if (windowId == SOURCE_TYPE_BEACON) {
					val inv: Inventory = player.getWindowById(Player.BEACON_WINDOW_ID)
					if (inv !is BeaconInventory) {
						player.getServer().getLogger().debug("Player " + player.getName().toString() + " has no open beacon inventory")
						return null
					}
					val beacon: BeaconInventory = inv as BeaconInventory
					inventorySlot = 0
					return SlotChangeAction(beacon, inventorySlot, oldItem, newItem)
				}

				//TODO: more stuff
				player.getServer().getLogger().debug("Player " + player.getName().toString() + " has no open container with window ID " + windowId)
				null
			}
			else -> {
				player.getServer().getLogger().debug("Unknown inventory source type " + sourceType)
				null
			}
		}
	}

	companion object {
		const val SOURCE_CONTAINER = 0
		const val SOURCE_WORLD = 2 //drop/pickup item entity
		const val SOURCE_CREATIVE = 3
		const val SOURCE_TODO = 99999
		const val SOURCE_CRAFT_SLOT = 100

		/**
		 * Fake window IDs for the SOURCE_TODO type (99999)
		 *
		 *
		 * These identifiers are used for inventory source types which are not currently implemented server-side in MCPE.
		 * As a general rule of thumb, anything that doesn't have a permanent inventory is client-side. These types are
		 * to allow servers to track what is going on in client-side windows.
		 *
		 *
		 * Expect these to change in the future.
		 */
		const val SOURCE_TYPE_CRAFTING_ADD_INGREDIENT = -2
		const val SOURCE_TYPE_CRAFTING_REMOVE_INGREDIENT = -3
		const val SOURCE_TYPE_CRAFTING_RESULT = -4
		const val SOURCE_TYPE_CRAFTING_USE_INGREDIENT = -5
		const val SOURCE_TYPE_ANVIL_INPUT = -10
		const val SOURCE_TYPE_ANVIL_MATERIAL = -11
		const val SOURCE_TYPE_ANVIL_RESULT = -12
		const val SOURCE_TYPE_ANVIL_OUTPUT = -13
		const val SOURCE_TYPE_ENCHANT_INPUT = -15
		const val SOURCE_TYPE_ENCHANT_MATERIAL = -16
		const val SOURCE_TYPE_ENCHANT_OUTPUT = -17
		const val SOURCE_TYPE_TRADING_INPUT_1 = -20
		const val SOURCE_TYPE_TRADING_INPUT_2 = -21
		const val SOURCE_TYPE_TRADING_USE_INPUTS = -22
		const val SOURCE_TYPE_TRADING_OUTPUT = -23
		const val SOURCE_TYPE_BEACON = -24

		/**
		 * Any client-side window dropping its contents when the player closes it
		 */
		const val SOURCE_TYPE_CONTAINER_DROP_CONTENTS = -100
	}
}