package cn.nukkit.entity.item

import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.potion.PotionCollideEvent
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.Particle
import cn.nukkit.level.particle.SpellParticle
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.potion.Potion

/**
 * @author xtypr
 */
class EntityPotion : EntityProjectile {
	var potionId = 0

	constructor(chunk: FullChunk?, nbt: CompoundTag?) : super(chunk, nbt) {}
	constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity?) : super(chunk, nbt, shootingEntity) {}

	override fun initEntity() {
		super.initEntity()
		potionId = namedTag!!.getShort("PotionId")
		dataProperties!!.putShort(Entity.Companion.DATA_POTION_AUX_VALUE, potionId)

		/*Effect effect = Potion.getEffect(potionId, true); TODO: potion color

        if(effect != null) {
            int count = 0;
            int[] c = effect.getColor();
            count += effect.getAmplifier() + 1;

            int r = ((c[0] * (effect.getAmplifier() + 1)) / count) & 0xff;
            int g = ((c[1] * (effect.getAmplifier() + 1)) / count) & 0xff;
            int b = ((c[2] * (effect.getAmplifier() + 1)) / count) & 0xff;

            this.setDataProperty(new IntEntityData(Entity.DATA_UNKNOWN, (r << 16) + (g << 8) + b));
        }*/
	}

	override val width: Float
		get() = 0.25f

	override val length: Float
		get() = 0.25f

	override val height: Float
		get() = 0.25f

	protected override val gravity: Float
		protected get() = 0.05f

	protected override val drag: Float
		protected get() = 0.01f

	override fun onCollideWithEntity(entity: Entity) {
		splash(entity)
	}

	private fun splash(collidedWith: Entity?) {
		var potion = Potion.getPotion(potionId)
		val event = PotionCollideEvent(potion, this)
		server!!.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return
		}
		close()
		potion = event.potion
		if (potion == null) {
			return
		}
		potion.isSplash = true
		val particle: Particle
		val r: Int
		val g: Int
		val b: Int
		val effect = Potion.getEffect(potion.id, true)
		if (effect == null) {
			r = 40
			g = 40
			b = 255
		} else {
			val colors = effect.color
			r = colors[0]
			g = colors[1]
			b = colors[2]
		}
		particle = SpellParticle(this, r, g, b)
		getLevel().addParticle(particle)
		getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_GLASS)
		val entities = getLevel().getNearbyEntities(getBoundingBox().grow(4.125, 2.125, 4.125))
		for (anEntity in entities) {
			val distance = anEntity.distanceSquared(this)
			if (distance < 16) {
				val d: Double = if (anEntity == collidedWith) 1 else 1 - Math.sqrt(distance) / 4
				potion.applyPotion(anEntity, d)
			}
		}
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		timing!!.startTiming()
		var hasUpdate = super.onUpdate(currentTick)
		if (age > 1200) {
			kill()
			hasUpdate = true
		} else if (isCollided) {
			splash(null)
			hasUpdate = true
		}
		timing!!.stopTiming()
		return hasUpdate
	}

	companion object {
		const val networkId = 86
		const val DATA_POTION_ID = 37
	}
}