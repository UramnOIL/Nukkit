package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.ByteTag
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class ItemArmor : Item, ItemDurable {
	constructor(id: Int) : super(id) {}
	constructor(id: Int, meta: Int?) : super(id, meta) {}
	constructor(id: Int, meta: Int?, count: Int) : super(id, meta, count) {}
	constructor(id: Int, meta: Int?, count: Int, name: String) : super(id, meta, count, name) {}

	override val maxStackSize: Int
		get() = 1

	override val isArmor: Boolean
		get() = true

	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		var equip = false
		if (this.isHelmet && player.getInventory().helmet!!.isNull) {
			if (player.getInventory().setHelmet(this)) {
				equip = true
			}
		} else if (this.isChestplate && player.getInventory().chestplate!!.isNull) {
			if (player.getInventory().setChestplate(this)) {
				equip = true
			}
		} else if (this.isLeggings && player.getInventory().leggings!!.isNull) {
			if (player.getInventory().setLeggings(this)) {
				equip = true
			}
		} else if (this.isBoots && player.getInventory().boots!!.isNull) {
			if (player.getInventory().setBoots(this)) {
				equip = true
			}
		}
		if (equip) {
			player.getInventory().clear(player.getInventory().heldItemIndex)
			when (this.tier) {
				TIER_CHAIN -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_CHAIN)
				TIER_DIAMOND -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_DIAMOND)
				TIER_GOLD -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_GOLD)
				TIER_IRON -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_IRON)
				TIER_LEATHER -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_LEATHER)
				TIER_OTHER -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_GENERIC)
				else -> player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_GENERIC)
			}
		}
		return getCount() == 0
	}

	override val enchantAbility: Int
		get() {
			when (this.tier) {
				TIER_CHAIN -> return 12
				TIER_LEATHER -> return 15
				TIER_DIAMOND -> return 10
				TIER_GOLD -> return 25
				TIER_IRON -> return 9
			}
			return 0
		}

	override val isUnbreakable: Boolean
		get() {
			val tag = getNamedTagEntry("Unbreakable")
			return tag is ByteTag && tag.data > 0
		}

	companion object {
		const val TIER_LEATHER = 1
		const val TIER_IRON = 2
		const val TIER_CHAIN = 3
		const val TIER_GOLD = 4
		const val TIER_DIAMOND = 5
		const val TIER_OTHER = 6
	}
}