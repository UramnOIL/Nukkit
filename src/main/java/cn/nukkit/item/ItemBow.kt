package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.EntityShootBowEvent
import cn.nukkit.event.entity.ProjectileLaunchEvent
import cn.nukkit.inventory.Inventory
import cn.nukkit.item.ItemTool
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBow @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.BOW, meta, count, "Bow") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_BOW

	override val enchantAbility: Int
		get() = 1

	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		return player.getInventory().contains(get(ItemID.Companion.ARROW)) || player.isCreative
	}

	override fun onRelease(player: Player, ticksUsed: Int): Boolean {
		val itemArrow = get(ItemID.Companion.ARROW, 0, 1)
		var inventory: Inventory? = player.offhandInventory
		if (!inventory!!.contains(itemArrow!!) && !player.getInventory().also { inventory = it }.contains(itemArrow) && player.isSurvival) {
			player.offhandInventory!!.sendContents(player)
			inventory!!.sendContents(player)
			return false
		}
		var damage = 2.0
		val bowDamage = this.getEnchantment(Enchantment.Companion.ID_BOW_POWER)
		if (bowDamage != null && bowDamage.level > 0) {
			damage += 0.25 * (bowDamage.level + 1)
		}
		val flameEnchant = this.getEnchantment(Enchantment.Companion.ID_BOW_FLAME)
		val flame = flameEnchant != null && flameEnchant.level > 0
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", player.x))
						.add(DoubleTag("", player.y + player.eyeHeight))
						.add(DoubleTag("", player.z)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", -Math.sin(player.yaw / 180 * Math.PI) * Math.cos(player.pitch / 180 * Math.PI)))
						.add(DoubleTag("", -Math.sin(player.pitch / 180 * Math.PI)))
						.add(DoubleTag("", Math.cos(player.yaw / 180 * Math.PI) * Math.cos(player.pitch / 180 * Math.PI))))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", (if (player.yaw > 180) 360 else 0) - player.yaw.toFloat()))
						.add(FloatTag("", (-player.pitch).toFloat())))
				.putShort("Fire", if (flame) 45 * 60 else 0)
				.putDouble("damage", damage)
		val p = ticksUsed.toDouble() / 20
		val f = Math.min((p * p + p * 2) / 3, 1.0) * 2
		val arrow = Entity.createEntity("Arrow", player.chunk, nbt, player, f == 2.0) as EntityArrow? ?: return false
		val entityShootBowEvent = EntityShootBowEvent(player, this, arrow, f)
		if (f < 0.1 || ticksUsed < 3) {
			entityShootBowEvent.setCancelled()
		}
		Server.instance!!.pluginManager.callEvent(entityShootBowEvent)
		if (entityShootBowEvent.isCancelled) {
			entityShootBowEvent.getProjectile().kill()
			player.getInventory().sendContents(player)
			player.offhandInventory!!.sendContents(player)
		} else {
			entityShootBowEvent.getProjectile().setMotion(entityShootBowEvent.getProjectile().motion.multiply(entityShootBowEvent.force))
			val infinityEnchant = this.getEnchantment(Enchantment.Companion.ID_BOW_INFINITY)
			val infinity = infinityEnchant != null && infinityEnchant.level > 0
			var projectile: EntityProjectile
			if (infinity && entityShootBowEvent.getProjectile().also { projectile = it } is EntityArrow) {
				(projectile as EntityArrow).pickupMode = EntityArrow.PICKUP_CREATIVE
			}
			if (player.isSurvival) {
				if (!infinity) {
					inventory!!.removeItem(itemArrow)
				}
				if (!this.isUnbreakable) {
					val durability = this.getEnchantment(Enchantment.Companion.ID_DURABILITY)
					if (!(durability != null && durability.level > 0 && 100 / (durability.level + 1) <= Random().nextInt(100))) {
						setDamage(damage + 1)
						if (damage >= maxDurability) {
							count--
						}
						player.getInventory().setItemInHand(this)
					}
				}
			}
			if (entityShootBowEvent.getProjectile() != null) {
				val projectev = ProjectileLaunchEvent(entityShootBowEvent.getProjectile())
				Server.instance!!.pluginManager.callEvent(projectev)
				if (projectev.isCancelled) {
					entityShootBowEvent.getProjectile().kill()
				} else {
					entityShootBowEvent.getProjectile().spawnToAll()
					player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BOW)
				}
			}
		}
		return true
	}
}