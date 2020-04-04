package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.entity.EntityHanging
import cn.nukkit.entity.item.EntityPainting.Motive
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.item.ItemPainting
import cn.nukkit.level.GameRule
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.AddPaintingPacket
import cn.nukkit.network.protocol.DataPacket
import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityPainting(chunk: FullChunk?, nbt: CompoundTag?) : EntityHanging(chunk, nbt) {
	private var motive: Motive? = null

	override fun initEntity() {
		super.initEntity()
		motive = getMotive(namedTag!!.getString("Motive"))
	}

	public override fun createAddEntityPacket(): DataPacket {
		val addPainting = AddPaintingPacket()
		addPainting.entityUniqueId = getId()
		addPainting.entityRuntimeId = getId()
		addPainting.x = x.toFloat()
		addPainting.y = y.toFloat()
		addPainting.z = z.toFloat()
		addPainting.direction = getDirection()!!.horizontalIndex
		addPainting.title = namedTag!!.getString("Motive")
		return addPainting
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		return if (super.attack(source)) {
			if (source is EntityDamageByEntityEvent) {
				val damager = source.damager
				if (damager is Player && damager.isSurvival && level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
					level.dropItem(this, ItemPainting())
				}
			}
			close()
			true
		} else {
			false
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putString("Motive", motive!!.title)
	}

	val art: Motive?
		get() = getMotive()

	fun getMotive(): Motive? {
		return Motive.BY_NAME[namedTag!!.getString("Motive")]
	}

	enum class Motive(val title: String, val width: Int, val height: Int) {
		KEBAB("Kebab", 1, 1), AZTEC("Aztec", 1, 1), ALBAN("Alban", 1, 1), AZTEC2("Aztec2", 1, 1), BOMB("Bomb", 1, 1), PLANT("Plant", 1, 1), WASTELAND("Wasteland", 1, 1), WANDERER("Wanderer", 1, 2), GRAHAM("Graham", 1, 2), POOL("Pool", 2, 1), COURBET("Courbet", 2, 1), SUNSET("Sunset", 2, 1), SEA("Sea", 2, 1), CREEBET("Creebet", 2, 1), MATCH("Match", 2, 2), BUST("Bust", 2, 2), STAGE("Stage", 2, 2), VOID("Void", 2, 2), SKULL_AND_ROSES("SkullAndRoses", 2, 2), WITHER("Wither", 2, 2), FIGHTERS("Fighters", 4, 2), SKELETON("Skeleton", 4, 3), DONKEY_KONG("DonkeyKong", 4, 3), POINTER("Pointer", 4, 4), PIG_SCENE("Pigscene", 4, 4), FLAMING_SKULL("Flaming Skull", 4, 4);

		companion object {
			val BY_NAME: MutableMap<String, Motive> = HashMap()

			init {
				for (motive in values()) {
					BY_NAME[cn.nukkit.entity.item.motive.title] = cn.nukkit.entity.item.motive
				}
			}
		}

	}

	companion object {
		const val networkId = 83
		val motives = Motive.values()
		fun getMotive(name: String): Motive {
			return Motive.BY_NAME.getOrDefault(name, Motive.KEBAB)
		}
	}
}