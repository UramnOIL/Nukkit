package cn.nukkit.entity

import cn.nukkit.Player
import cn.nukkit.entity.data.IntPositionEntityData
import cn.nukkit.entity.data.Skin
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.AddPlayerPacket
import cn.nukkit.network.protocol.RemoveEntityPacket
import cn.nukkit.network.protocol.SetEntityLinkPacket
import cn.nukkit.utils.SerializedImage
import cn.nukkit.utils.SkinAnimation
import cn.nukkit.utils.Utils
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class EntityHuman(chunk: FullChunk?, nbt: CompoundTag?) : EntityHumanType(chunk, nbt) {
	var uniqueId: UUID? = null
		protected set
	var rawUniqueId: ByteArray
		protected set
	override val width: Float
		get() = 0.6f

	override val length: Float
		get() = 0.6f

	override val height: Float
		get() = 1.8f

	override val eyeHeight: Float
		get() = 1.62f

	protected override val baseOffset: Float
		protected get() = eyeHeight

	open var skin: Skin? = null
	override val networkId: Int
		get() = -1

	override fun initEntity() {
		this.setDataFlag(DATA_PLAYER_FLAGS, DATA_PLAYER_FLAG_SLEEP, false)
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_GRAVITY)
		this.setDataProperty(IntPositionEntityData(DATA_PLAYER_BED_POSITION, 0, 0, 0), false)
		if (this !is Player) {
			if (namedTag!!.contains("NameTag")) {
				this.nameTag = namedTag!!.getString("NameTag")
			}
			if (namedTag!!.contains("Skin") && namedTag!!["Skin"] is CompoundTag) {
				val skinTag = namedTag!!.getCompound("Skin")
				if (!skinTag.contains("Transparent")) {
					skinTag.putBoolean("Transparent", false)
				}
				val newSkin = Skin()
				if (skinTag.contains("ModelId")) {
					newSkin.skinId = skinTag.getString("ModelId")
				}
				if (skinTag.contains("Data")) {
					val data = skinTag.getByteArray("Data")
					if (skinTag.contains("SkinImageWidth") && skinTag.contains("SkinImageHeight")) {
						val width = skinTag.getInt("SkinImageWidth")
						val height = skinTag.getInt("SkinImageHeight")
						newSkin.skinData = SerializedImage(width, height, data)
					} else {
						newSkin.setSkinData(data)
					}
				}
				if (skinTag.contains("CapeId")) {
					newSkin.capeId = skinTag.getString("CapeId")
				}
				if (skinTag.contains("CapeData")) {
					val data = skinTag.getByteArray("CapeData")
					if (skinTag.contains("CapeImageWidth") && skinTag.contains("CapeImageHeight")) {
						val width = skinTag.getInt("CapeImageWidth")
						val height = skinTag.getInt("CapeImageHeight")
						newSkin.capeData = SerializedImage(width, height, data)
					} else {
						newSkin.setCapeData(data)
					}
				}
				if (skinTag.contains("GeometryName")) {
					newSkin.setGeometryName(skinTag.getString("GeometryName"))
				}
				if (skinTag.contains("SkinResourcePatch")) {
					newSkin.skinResourcePatch = String(skinTag.getByteArray("SkinResourcePatch"), StandardCharsets.UTF_8)
				}
				if (skinTag.contains("GeometryData")) {
					newSkin.geometryData = String(skinTag.getByteArray("GeometryData"), StandardCharsets.UTF_8)
				}
				if (skinTag.contains("AnimationData")) {
					newSkin.animationData = String(skinTag.getByteArray("AnimationData"), StandardCharsets.UTF_8)
				}
				if (skinTag.contains("PremiumSkin")) {
					newSkin.isPremium = skinTag.getBoolean("PremiumSkin")
				}
				if (skinTag.contains("PersonaSkin")) {
					newSkin.isPersona = skinTag.getBoolean("PersonaSkin")
				}
				if (skinTag.contains("CapeOnClassicSkin")) {
					newSkin.isCapeOnClassic = skinTag.getBoolean("CapeOnClassicSkin")
				}
				if (skinTag.contains("AnimatedImageData")) {
					val list = skinTag.getList("AnimatedImageData", CompoundTag::class.java)
					for (animationTag in list.all) {
						val frames = animationTag.getFloat("Frames")
						val type = animationTag.getInt("Type")
						val image = animationTag.getByteArray("Image")
						val width = animationTag.getInt("ImageWidth")
						val height = animationTag.getInt("ImageHeight")
						skin.getAnimations().add(SkinAnimation(SerializedImage(width, height, image), type, frames))
					}
				}
				skin = newSkin
			}
			uniqueId = Utils.dataToUUID(getId().toString().toByteArray(StandardCharsets.UTF_8), skin
					.getSkinData().data, this.nameTag.toByteArray(StandardCharsets.UTF_8))
		}
		super.initEntity()
	}

	override val name: String?
		get() = this.nameTag

	override fun saveNBT() {
		super.saveNBT()
		if (skin != null) {
			val skinTag = CompoundTag()
					.putByteArray("Data", skin!!.skinData.data)
					.putInt("SkinImageWidth", skin!!.skinData.width)
					.putInt("SkinImageHeight", skin!!.skinData.height)
					.putString("ModelId", skin!!.skinId)
					.putString("CapeId", skin.getCapeId())
					.putByteArray("CapeData", skin!!.capeData.data)
					.putInt("CapeImageWidth", skin!!.capeData.width)
					.putInt("CapeImageHeight", skin!!.capeData.height)
					.putByteArray("SkinResourcePatch", skin!!.skinResourcePatch.toByteArray(StandardCharsets.UTF_8))
					.putByteArray("GeometryData", skin.getGeometryData().toByteArray(StandardCharsets.UTF_8))
					.putByteArray("AnimationData", skin.getAnimationData().toByteArray(StandardCharsets.UTF_8))
					.putBoolean("PremiumSkin", skin!!.isPremium)
					.putBoolean("PersonaSkin", skin!!.isPersona)
					.putBoolean("CapeOnClassicSkin", skin!!.isCapeOnClassic)
			val animations = skin.getAnimations()
			if (!animations!!.isEmpty()) {
				val animationsTag = ListTag<CompoundTag>("AnimationImageData")
				for (animation in animations) {
					animationsTag.add(CompoundTag()
							.putFloat("Frames", animation!!.frames)
							.putInt("Type", animation!!.type)
							.putInt("ImageWidth", animation!!.image.width)
							.putInt("ImageHeight", animation!!.image.height)
							.putByteArray("Image", animation!!.image.data))
				}
				skinTag.putList(animationsTag)
			}
			namedTag!!.putCompound("Skin", skinTag)
		}
	}

	override fun addMovement(x: Double, y: Double, z: Double, yaw: Double, pitch: Double, headYaw: Double) {
		level.addPlayerMovement(this, x, y, z, yaw, pitch, headYaw)
	}

	override fun spawnTo(player: Player) {
		if (this !== player && !hasSpawned.containsKey(player.loaderId)) {
			hasSpawned[player.loaderId] = player
			check(skin!!.isValid) { this.javaClass.simpleName + " must have a valid skin set" }
			if (this is Player) server!!.updatePlayerListData(uniqueId, getId(), this.getName(), skin, this.loginChainData!!.xuid, arrayOf(player)) else server!!.updatePlayerListData(uniqueId, getId(), name, skin, arrayOf(player))
			val pk = AddPlayerPacket()
			pk.uuid = uniqueId
			pk.username = name
			pk.entityUniqueId = getId()
			pk.entityRuntimeId = getId()
			pk.x = x.toFloat()
			pk.y = y.toFloat()
			pk.z = z.toFloat()
			pk.speedX = motionX.toFloat()
			pk.speedY = motionY.toFloat()
			pk.speedZ = motionZ.toFloat()
			pk.yaw = yaw.toFloat()
			pk.pitch = pitch.toFloat()
			pk.item = getInventory().itemInHand
			pk.metadata = dataProperties
			player.dataPacket(pk)
			inventory!!.sendArmorContents(player)
			offhandInventory!!.sendContents(player)
			if (riding != null) {
				val pkk = SetEntityLinkPacket()
				pkk.vehicleUniqueId = riding.getId()
				pkk.riderUniqueId = getId()
				pkk.type = 1
				pkk.immediate = 1
				player.dataPacket(pkk)
			}
			if (this !is Player) {
				server!!.removePlayerListData(uniqueId, arrayOf(player))
			}
		}
	}

	override fun despawnFrom(player: Player) {
		if (hasSpawned.containsKey(player.loaderId)) {
			val pk = RemoveEntityPacket()
			pk.eid = getId()
			player.dataPacket(pk)
			hasSpawned.remove(player.loaderId)
		}
	}

	override fun close() {
		if (!closed) {
			if (inventory != null && (this !is Player || this.loggedIn)) {
				for (viewer in inventory!!.viewers) {
					viewer.removeWindow(inventory)
				}
			}
			super.close()
		}
	}

	companion object {
		const val DATA_PLAYER_FLAG_SLEEP = 1
		const val DATA_PLAYER_FLAG_DEAD = 2
		const val DATA_PLAYER_FLAGS = 26
		const val DATA_PLAYER_BED_POSITION = 28
		const val DATA_PLAYER_BUTTON_TEXT = 40
	}
}