package cn.nukkit

import cn.nukkit.entity.Attribute
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityRegainHealthEvent
import cn.nukkit.event.player.PlayerFoodLevelChangeEvent
import cn.nukkit.item.food.Food
import cn.nukkit.potion.Effect

/**
 * Created by funcraft on 2015/11/11.
 */
class PlayerFood(val player: Player, foodLevel: Int, foodSaturationLevel: Float) {
	private var foodLevel = 20
	val maxLevel: Int
	private var foodSaturationLevel = 20f
	private var foodTickTimer = 0
	private var foodExpLevel = 0.0

	var level: Int
		get() = foodLevel
		set(foodLevel) {
			setLevel(foodLevel, -1f)
		}

	fun setLevel(foodLevel: Int, saturationLevel: Float) {
		var foodLevel = foodLevel
		if (foodLevel > 20) {
			foodLevel = 20
		}
		if (foodLevel < 0) {
			foodLevel = 0
		}
		if (foodLevel <= 6 && level > 6) {
			if (player.isSprinting) {
				player.isSprinting = false
			}
		}
		val ev = PlayerFoodLevelChangeEvent(player, foodLevel, saturationLevel)
		player.server.pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			sendFoodLevel(level)
			return
		}
		val foodLevel0 = ev.foodLevel
		var fsl = ev.foodSaturationLevel
		this.foodLevel = foodLevel
		if (fsl != -1f) {
			if (fsl > foodLevel) fsl = foodLevel.toFloat()
			foodSaturationLevel = fsl
		}
		this.foodLevel = foodLevel0
		sendFoodLevel()
	}

	fun getFoodSaturationLevel(): Float {
		return foodSaturationLevel
	}

	fun setFoodSaturationLevel(fsl: Float) {
		var fsl = fsl
		if (fsl > level) fsl = level.toFloat()
		if (fsl < 0) fsl = 0f
		val ev = PlayerFoodLevelChangeEvent(player, level, fsl)
		player.server.pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return
		}
		fsl = ev.foodSaturationLevel
		foodSaturationLevel = fsl
	}

	@JvmOverloads
	fun useHunger(amount: Int = 1) {
		val sfl = getFoodSaturationLevel()
		val foodLevel = level
		if (sfl > 0) {
			var newSfl = sfl - amount
			if (newSfl < 0) newSfl = 0f
			setFoodSaturationLevel(newSfl)
		} else {
			level = foodLevel - amount
		}
	}

	fun addFoodLevel(food: Food) {
		this.addFoodLevel(food.restoreFood, food.restoreSaturation)
	}

	fun addFoodLevel(foodLevel: Int, fsl: Float) {
		setLevel(level + foodLevel, getFoodSaturationLevel() + fsl)
	}

	fun reset() {
		foodLevel = 20
		foodSaturationLevel = 20f
		foodExpLevel = 0.0
		foodTickTimer = 0
		sendFoodLevel()
	}

	@JvmOverloads
	fun sendFoodLevel(foodLevel: Int = level) {
		if (player.spawned) {
			player.setAttribute(Attribute.getAttribute(Attribute.MAX_HUNGER).setValue(foodLevel.toFloat()))
		}
	}

	fun update(tickDiff: Int) {
		if (!player.isFoodEnabled) return
		if (player.isAlive) {
			val diff = Server.instance!!.getDifficulty()
			if (level > 17) {
				foodTickTimer += tickDiff
				if (foodTickTimer >= 80) {
					if (player.health < player.maxHealth) {
						val ev = EntityRegainHealthEvent(player, 1, EntityRegainHealthEvent.CAUSE_EATING)
						player.heal(ev)
						//this.updateFoodExpLevel(3);
					}
					foodTickTimer = 0
				}
			} else if (level == 0) {
				foodTickTimer += tickDiff
				if (foodTickTimer >= 80) {
					val ev = EntityDamageEvent(player, DamageCause.HUNGER, 1)
					val now = player.health
					if (diff == 1) {
						if (now > 10) player.attack(ev)
					} else if (diff == 2) {
						if (now > 1) player.attack(ev)
					} else {
						player.attack(ev)
					}
					foodTickTimer = 0
				}
			}
			if (player.hasEffect(Effect.HUNGER)) {
				updateFoodExpLevel(0.025)
			}
		}
	}

	fun updateFoodExpLevel(use: Double) {
		if (!player.isFoodEnabled) return
		if (Server.instance!!.getDifficulty() == 0) return
		if (player.hasEffect(Effect.SATURATION)) return
		foodExpLevel += use
		if (foodExpLevel > 4) {
			useHunger(1)
			foodExpLevel = 0.0
		}
	}

	/**
	 * @param foodLevel level
	 */
	@Deprecated("""use {@link #setLevel(int)} instead
      """)
	fun setFoodLevel(foodLevel: Int) {
		level = foodLevel
	}

	/**
	 * @param foodLevel level
	 * @param saturationLevel saturation
	 */
	@Deprecated("""use {@link #setLevel(int, float)} instead
      """)
	fun setFoodLevel(foodLevel: Int, saturationLevel: Float) {
		setLevel(foodLevel, saturationLevel)
	}

	init {
		this.foodLevel = foodLevel
		maxLevel = 20
		this.foodSaturationLevel = foodSaturationLevel
	}
}