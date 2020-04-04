package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.Position
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class AnvilInventory(playerUI: PlayerUIInventory, position: Position) : FakeBlockUIComponent(playerUI, InventoryType.ANVIL, 1, position) {
	fun onRename(player: Player, resultItem: Item): Boolean {
		val local = getItem(TARGET)
		val second = getItem(SACRIFICE)
		if (!resultItem.equals(local, true, false) || resultItem.getCount() != local!!.getCount()) {
			//Item does not match target item. Everything must match except the tags.
			return false
		}
		if (local == resultItem) {
			//just item transaction
			return true
		}
		if (local!!.id != 0 && second!!.id == 0) { //only rename
			local.customName = resultItem.customName
			setItem(RESULT, local)
			player.inventory.addItem(local)
			clearAll()
			player.inventory.sendContents(player)
			sendContents(player)
			player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE)
			return true
		} else if (local.id != 0 && second!!.id != 0) { //enchants combining
			if (!local.equals(second, true, false)) {
				return false
			}
			if (local.id != 0 && second.id != 0) {
				val result = local.clone()
				var enchants = 0
				val enchantments = ArrayList(Arrays.asList(*second.enchantments))
				val baseEnchants = ArrayList<Enchantment>()
				for (ench in local.enchantments) {
					if (ench.isMajor) {
						baseEnchants.add(ench)
					}
				}
				for (enchantment in enchantments) {
					if (enchantment.level < 0 || enchantment.getId() < 0) {
						continue
					}
					if (enchantment.isMajor) {
						var same = false
						var another = false
						for (baseEnchant in baseEnchants) {
							if (baseEnchant.id == enchantment.id) same = true else {
								another = true
							}
						}
						if (!same && another) {
							continue
						}
					}
					val localEnchantment = local.getEnchantment(enchantment.getId())
					if (localEnchantment != null) {
						var level = Math.max(localEnchantment.level, enchantment.level)
						if (localEnchantment.level == enchantment.level) level++
						enchantment.level = level
						result.addEnchantment(enchantment)
						continue
					}
					result.addEnchantment(enchantment)
					enchants++
				}
				result.customName = resultItem.customName
				player.inventory.addItem(result)
				player.inventory.sendContents(player)
				clearAll()
				sendContents(player)
				player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE)
				return true
			}
		}
		return false
	}

	override fun onClose(who: Player) {
		super.onClose(who)
		who.craftingType = Player.CRAFTING_SMALL
		who.resetCraftingGridType()
		for (i in 0..1) {
			getHolder().getLevel().dropItem(getHolder().add(0.5, 0.5, 0.5), getItem(i))
			this.clear(i)
		}
	}

	override fun onOpen(who: Player) {
		super.onOpen(who)
		who.craftingType = Player.CRAFTING_ANVIL
	}

	companion object {
		const val TARGET = 0
		const val SACRIFICE = 1
		const val RESULT = 50
	}
}