package cn.nukkit.entity

import cn.nukkit.Player
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityDamageEvent.DamageModifier
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.inventory.PlayerEnderChestInventory
import cn.nukkit.inventory.PlayerInventory
import cn.nukkit.inventory.PlayerOffhandInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.NukkitMath
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import java.util.*
import java.util.concurrent.ThreadLocalRandom

abstract class EntityHumanType(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), InventoryHolder {
	protected var inventory: PlayerInventory? = null
	var enderChestInventory: PlayerEnderChestInventory? = null
		protected set
	var offhandInventory: PlayerOffhandInventory? = null
		protected set

	override fun getInventory(): PlayerInventory {
		return inventory!!
	}

	override fun initEntity() {
		inventory = PlayerInventory(this)
		offhandInventory = PlayerOffhandInventory(this)
		if (namedTag!!.contains("Inventory") && namedTag!!["Inventory"] is ListTag<*>) {
			val inventoryList = namedTag!!.getList("Inventory", CompoundTag::class.java)
			for (item in inventoryList.all) {
				val slot = item.getByte("Slot")
				if (slot >= 0 && slot < 9) { //hotbar
					//Old hotbar saving stuff, remove it (useless now)
					inventoryList.remove(item)
				} else if (slot >= 100 && slot < 104) {
					inventory!!.setItem(inventory!!.size + slot - 100, NBTIO.getItemHelper(item))
				} else if (slot == -106) {
					offhandInventory!!.setItem(0, NBTIO.getItemHelper(item))
				} else {
					inventory!!.setItem(slot - 9, NBTIO.getItemHelper(item))
				}
			}
		}
		enderChestInventory = PlayerEnderChestInventory(this)
		if (namedTag!!.contains("EnderItems") && namedTag!!["EnderItems"] is ListTag<*>) {
			val inventoryList = namedTag!!.getList("EnderItems", CompoundTag::class.java)
			for (item in inventoryList.all) {
				enderChestInventory!!.setItem(item.getByte("Slot"), NBTIO.getItemHelper(item))
			}
		}
		super.initEntity()
	}

	override fun saveNBT() {
		super.saveNBT()
		var inventoryTag: ListTag<CompoundTag?>? = null
		if (inventory != null) {
			inventoryTag = ListTag("Inventory")
			namedTag!!.putList(inventoryTag)
			for (slot in 0..8) {
				inventoryTag.add(CompoundTag()
						.putByte("Count", 0)
						.putShort("Damage", 0)
						.putByte("Slot", slot)
						.putByte("TrueSlot", -1)
						.putShort("id", 0)
				)
			}
			val slotCount = Player.SURVIVAL_SLOTS + 9
			for (slot in 9 until slotCount) {
				val item = inventory!!.getItem(slot - 9)
				inventoryTag.add(NBTIO.putItemHelper(item, slot))
			}
			for (slot in 100..103) {
				val item = inventory!!.getItem(inventory!!.size + slot - 100)
				if (item != null && item.id != Item.AIR) {
					inventoryTag.add(NBTIO.putItemHelper(item, slot))
				}
			}
		}
		if (offhandInventory != null) {
			val item = offhandInventory!!.getItem(0)
			if (item.id != Item.AIR) {
				if (inventoryTag == null) {
					inventoryTag = ListTag("Inventory")
					namedTag!!.putList(inventoryTag)
				}
				inventoryTag.add(NBTIO.putItemHelper(item, -106))
			}
		}
		namedTag!!.putList(ListTag<CompoundTag>("EnderItems"))
		if (enderChestInventory != null) {
			for (slot in 0..26) {
				val item = enderChestInventory!!.getItem(slot)
				if (item != null && item.id != Item.AIR) {
					namedTag!!.getList("EnderItems", CompoundTag::class.java).add(NBTIO.putItemHelper(item, slot))
				}
			}
		}
	}

	override val drops: Array<Item?>
		get() {
			if (inventory != null) {
				val drops: MutableList<Item> = ArrayList(inventory!!.contents.values)
				drops.addAll(offhandInventory!!.contents.values)
				return drops.toTypedArray()
			}
			return arrayOfNulls(0)
		}

	override fun attack(source: EntityDamageEvent): Boolean {
		if (this.isClosed || !this.isAlive) {
			return false
		}
		if (source.cause != DamageCause.VOID && source.cause != DamageCause.CUSTOM && source.cause != DamageCause.MAGIC) {
			var armorPoints = 0
			var epf = 0
			val toughness = 0
			for (armor in inventory!!.armorContents) {
				armorPoints += armor.armorPoints
				epf += calculateEnchantmentProtectionFactor(armor, source).toInt()
				//toughness += armor.getToughness();
			}
			if (source.canBeReducedByArmor()) {
				source.setDamage(-source.finalDamage * armorPoints * 0.04f, DamageModifier.ARMOR)
			}
			source.setDamage(-source.finalDamage * Math.min(NukkitMath.ceilFloat(Math.min(epf, 25) * (ThreadLocalRandom.current().nextInt(50, 100).toFloat() / 100)), 20) * 0.04f,
					DamageModifier.ARMOR_ENCHANTMENTS)
			source.setDamage(-Math.min(getAbsorption(), source.finalDamage), DamageModifier.ABSORPTION)
		}
		return if (super.attack(source)) {
			var damager: Entity? = null
			if (source is EntityDamageByEntityEvent) {
				damager = source.damager
			}
			for (slot in 0..3) {
				val armor = inventory!!.getArmorItem(slot)
				if (armor.hasEnchantments()) {
					if (damager != null) {
						for (enchantment in armor.enchantments) {
							enchantment.doPostAttack(damager, this)
						}
					}
					val durability = armor.getEnchantment(Enchantment.ID_DURABILITY)
					if (durability != null && durability.level > 0 && 100 / (durability.level + 1) <= ThreadLocalRandom.current().nextInt(100)) continue
				}
				if (armor.isUnbreakable) {
					continue
				}
				armor.damage = armor.damage + 1
				if (armor.damage >= armor.maxDurability) {
					inventory!!.setArmorItem(slot, ItemBlock(get(BlockID.AIR)))
				} else {
					inventory!!.setArmorItem(slot, armor, true)
				}
			}
			true
		} else {
			false
		}
	}

	protected fun calculateEnchantmentProtectionFactor(item: Item, source: EntityDamageEvent?): Double {
		if (!item.hasEnchantments()) {
			return 0
		}
		var epf = 0.0
		for (ench in item.enchantments) {
			epf += ench.getProtectionFactor(source).toDouble()
		}
		return epf
	}

	override fun setOnFire(seconds: Int) {
		var seconds = seconds
		var level = 0
		for (armor in inventory!!.armorContents) {
			val fireProtection = armor.getEnchantment(Enchantment.ID_PROTECTION_FIRE)
			if (fireProtection != null && fireProtection.level > 0) {
				level = Math.max(level, fireProtection.level)
			}
		}
		seconds = (seconds * (1 - level * 0.15)).toInt()
		super.setOnFire(seconds)
	}
}