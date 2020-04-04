package cn.nukkit.entity.passive

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemDye
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.DyeColor
import java.util.concurrent.ThreadLocalRandom

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntitySheep(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	var sheared = false
	var color = 0
	override val width: Float
		get() = if (this.isBaby) {
			0.45f
		} else 0.9f

	override val height: Float
		get() = if (isBaby) {
			0.65f
		} else 1.3f

	override val name: String?
		get() = "Sheep"

	public override fun initEntity() {
		maxHealth = 8
		if (!namedTag!!.contains("Color")) {
			setColor(randomColor())
		} else {
			setColor(namedTag!!.getByte("Color"))
		}
		if (!namedTag!!.contains("Sheared")) {
			namedTag!!.putByte("Sheared", 0)
		} else {
			sheared = namedTag!!.getBoolean("Sheared")
		}
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_SHEARED, sheared)
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putByte("Color", color)
		namedTag!!.putBoolean("Sheared", sheared)
	}

	override fun onInteract(player: Player, item: Item, clickedPos: Vector3?): Boolean {
		if (item.id == Item.DYE) {
			setColor((item as ItemDye).dyeColor.woolData)
			return true
		}
		return item.id == Item.SHEARS && shear()
	}

	fun shear(): Boolean {
		if (sheared) {
			return false
		}
		sheared = true
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_SHEARED, true)
		level.dropItem(this, Item.get(Item.WOOL, getColor(), ThreadLocalRandom.current().nextInt(2) + 1))
		return true
	}

	override val drops: Array<Item?>
		get() = if (lastDamageCause is EntityDamageByEntityEvent) {
			arrayOf(Item.get(Item.WOOL, getColor(), 1))
		} else arrayOfNulls(0)

	fun setColor(color: Int) {
		this.color = color
		this.setDataProperty(ByteEntityData(Entity.Companion.DATA_COLOUR, color))
		namedTag!!.putByte("Color", this.color)
	}

	fun getColor(): Int {
		return namedTag!!.getByte("Color")
	}

	private fun randomColor(): Int {
		val random = ThreadLocalRandom.current()
		val rand = random.nextDouble(1.0, 100.0)
		if (rand <= 0.164) {
			return DyeColor.PINK.woolData
		}
		return if (rand <= 15) {
			if (random.nextBoolean()) DyeColor.BLACK.woolData else if (random.nextBoolean()) DyeColor.GRAY.woolData else DyeColor.LIGHT_GRAY.woolData
		} else DyeColor.WHITE.woolData
	}

	companion object {
		const val networkId = 13
	}
}