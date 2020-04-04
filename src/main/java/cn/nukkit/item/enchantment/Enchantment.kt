package cn.nukkit.item.enchantment

import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.bow.EnchantmentBowFlame
import cn.nukkit.item.enchantment.bow.EnchantmentBowInfinity
import cn.nukkit.item.enchantment.bow.EnchantmentBowKnockback
import cn.nukkit.item.enchantment.bow.EnchantmentBowPower
import cn.nukkit.item.enchantment.damage.EnchantmentDamageAll
import cn.nukkit.item.enchantment.damage.EnchantmentDamageArthropods
import cn.nukkit.item.enchantment.damage.EnchantmentDamageSmite
import cn.nukkit.item.enchantment.loot.EnchantmentLootDigging
import cn.nukkit.item.enchantment.loot.EnchantmentLootFishing
import cn.nukkit.item.enchantment.loot.EnchantmentLootWeapon
import cn.nukkit.item.enchantment.protection.*
import cn.nukkit.item.enchantment.trident.EnchantmentTridentChanneling
import cn.nukkit.item.enchantment.trident.EnchantmentTridentImpaling
import cn.nukkit.item.enchantment.trident.EnchantmentTridentLoyalty
import cn.nukkit.item.enchantment.trident.EnchantmentTridentRiptide
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class Enchantment protected constructor(val id: Int, protected val name: String, val weight: Int, var type: EnchantmentType) : Cloneable {
	var level = 1
		protected set

	fun setLevel(level: Int): Enchantment {
		return this.setLevel(level, true)
	}

	fun setLevel(level: Int, safe: Boolean): Enchantment {
		if (!safe) {
			this.level = level
			return this
		}
		if (level > maxLevel) {
			this.level = maxLevel
		} else if (level < minLevel) {
			this.level = minLevel
		} else {
			this.level = level
		}
		return this
	}

	val minLevel: Int
		get() = 1

	open val maxLevel: Int
		get() = 1

	open val maxEnchantableLevel: Int
		get() = maxLevel

	open fun getMinEnchantAbility(level: Int): Int {
		return 1 + level * 10
	}

	open fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 5
	}

	open fun getProtectionFactor(event: EntityDamageEvent): Float {
		return 0
	}

	open fun getDamageBonus(entity: Entity?): Double {
		return 0
	}

	open fun doPostAttack(attacker: Entity, entity: Entity) {}
	fun doPostHurt(attacker: Entity?, entity: Entity?) {}
	open fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return this !== enchantment
	}

	open fun getName(): String {
		return "%enchantment." + name
	}

	open fun canEnchant(item: Item): Boolean {
		return type.canEnchantItem(item)
	}

	open val isMajor: Boolean
		get() = false

	override fun clone(): Enchantment {
		return try {
			super.clone() as Enchantment
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	private class UnknownEnchantment(id: Int) : Enchantment(id, "unknown", 0, EnchantmentType.ALL)
	companion object {
		protected var enchantments: Array<Enchantment?>

		//http://minecraft.gamepedia.com/Enchanting#Aqua_Affinity
		const val ID_PROTECTION_ALL = 0
		const val ID_PROTECTION_FIRE = 1
		const val ID_PROTECTION_FALL = 2
		const val ID_PROTECTION_EXPLOSION = 3
		const val ID_PROTECTION_PROJECTILE = 4
		const val ID_THORNS = 5
		const val ID_WATER_BREATHING = 6
		const val ID_WATER_WALKER = 7
		const val ID_WATER_WORKER = 8
		const val ID_DAMAGE_ALL = 9
		const val ID_DAMAGE_SMITE = 10
		const val ID_DAMAGE_ARTHROPODS = 11
		const val ID_KNOCKBACK = 12
		const val ID_FIRE_ASPECT = 13
		const val ID_LOOTING = 14
		const val ID_EFFICIENCY = 15
		const val ID_SILK_TOUCH = 16
		const val ID_DURABILITY = 17
		const val ID_FORTUNE_DIGGING = 18
		const val ID_BOW_POWER = 19
		const val ID_BOW_KNOCKBACK = 20
		const val ID_BOW_FLAME = 21
		const val ID_BOW_INFINITY = 22
		const val ID_FORTUNE_FISHING = 23
		const val ID_LURE = 24
		const val ID_FROST_WALKER = 25
		const val ID_MENDING = 26
		const val ID_BINDING_CURSE = 27
		const val ID_VANISHING_CURSE = 28
		const val ID_TRIDENT_IMPALING = 29
		const val ID_TRIDENT_RIPTIDE = 30
		const val ID_TRIDENT_LOYALTY = 31
		const val ID_TRIDENT_CHANNELING = 32
		fun init() {
			enchantments = arrayOfNulls(256)
			enchantments[ID_PROTECTION_ALL] = EnchantmentProtectionAll()
			enchantments[ID_PROTECTION_FIRE] = EnchantmentProtectionFire()
			enchantments[ID_PROTECTION_FALL] = EnchantmentProtectionFall()
			enchantments[ID_PROTECTION_EXPLOSION] = EnchantmentProtectionExplosion()
			enchantments[ID_PROTECTION_PROJECTILE] = EnchantmentProtectionProjectile()
			enchantments[ID_THORNS] = EnchantmentThorns()
			enchantments[ID_WATER_BREATHING] = EnchantmentWaterBreath()
			enchantments[ID_WATER_WORKER] = EnchantmentWaterWorker()
			enchantments[ID_WATER_WALKER] = EnchantmentWaterWalker()
			enchantments[ID_DAMAGE_ALL] = EnchantmentDamageAll()
			enchantments[ID_DAMAGE_SMITE] = EnchantmentDamageSmite()
			enchantments[ID_DAMAGE_ARTHROPODS] = EnchantmentDamageArthropods()
			enchantments[ID_KNOCKBACK] = EnchantmentKnockback()
			enchantments[ID_FIRE_ASPECT] = EnchantmentFireAspect()
			enchantments[ID_LOOTING] = EnchantmentLootWeapon()
			enchantments[ID_EFFICIENCY] = EnchantmentEfficiency()
			enchantments[ID_SILK_TOUCH] = EnchantmentSilkTouch()
			enchantments[ID_DURABILITY] = EnchantmentDurability()
			enchantments[ID_FORTUNE_DIGGING] = EnchantmentLootDigging()
			enchantments[ID_BOW_POWER] = EnchantmentBowPower()
			enchantments[ID_BOW_KNOCKBACK] = EnchantmentBowKnockback()
			enchantments[ID_BOW_FLAME] = EnchantmentBowFlame()
			enchantments[ID_BOW_INFINITY] = EnchantmentBowInfinity()
			enchantments[ID_FORTUNE_FISHING] = EnchantmentLootFishing()
			enchantments[ID_LURE] = EnchantmentLure()
			enchantments[ID_FROST_WALKER] = EnchantmentFrostWalker()
			enchantments[ID_MENDING] = EnchantmentMending()
			enchantments[ID_BINDING_CURSE] = EnchantmentBindingCurse()
			enchantments[ID_VANISHING_CURSE] = EnchantmentVanishingCurse()
			enchantments[ID_TRIDENT_IMPALING] = EnchantmentTridentImpaling()
			enchantments[ID_TRIDENT_RIPTIDE] = EnchantmentTridentRiptide()
			enchantments[ID_TRIDENT_LOYALTY] = EnchantmentTridentLoyalty()
			enchantments[ID_TRIDENT_CHANNELING] = EnchantmentTridentChanneling()
		}

		operator fun get(id: Int): Enchantment {
			var enchantment: Enchantment? = null
			if (id >= 0 && id < enchantments.size) {
				enchantment = enchantments[id]
			}
			return enchantment ?: UnknownEnchantment(id)
		}

		fun getEnchantment(id: Int): Enchantment {
			return get(id).clone()
		}

		fun getEnchantments(): Array<Enchantment> {
			val list = ArrayList<Enchantment>()
			for (enchantment in enchantments) {
				if (enchantment == null) {
					break
				}
				list.add(enchantment)
			}
			return list.toTypedArray()
		}

		val words = arrayOf("the", "elder", "scrolls", "klaatu", "berata", "niktu", "xyzzy", "bless", "curse", "light", "darkness", "fire", "air", "earth", "water", "hot", "dry", "cold", "wet", "ignite", "snuff", "embiggen", "twist", "shorten", "stretch", "fiddle", "destroy", "imbue", "galvanize", "enchant", "free", "limited", "range", "of", "towards", "inside", "sphere", "cube", "self", "other", "ball", "mental", "physical", "grow", "shrink", "demon", "elemental", "spirit", "animal", "creature", "beast", "humanoid", "undead", "fresh", "stale")
		val randomName: String
			get() {
				val count = ThreadLocalRandom.current().nextInt(3, 6)
				val set = HashSet<String>()
				while (set.size < count) {
					set.add(words[ThreadLocalRandom.current().nextInt(0, words.size)])
				}
				val words = set.toTypedArray()
				return java.lang.String.join(" ", *words)
			}
	}

}