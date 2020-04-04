package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.ClientboundMapItemDataPacket
import cn.nukkit.utils.MainLogger
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Created by CreeperFace on 18.3.2017.
 */
class ItemMap @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.MAP, 0, count, "Map") {
	// not very pretty but definitely better than before.
	private var image: BufferedImage? = null

	@Throws(IOException::class)
	fun setImage(file: File?) {
		setImage(ImageIO.read(file))
	}

	fun setImage(image: BufferedImage) {
		try {
			if (image.height != 128 || image.width != 128) { //resize
				this.image = BufferedImage(128, 128, image.type)
				val g = this.image!!.createGraphics()
				g.drawImage(image, 0, 0, 128, 128, null)
				g.dispose()
			} else {
				this.image = image
			}
			val baos = ByteArrayOutputStream()
			ImageIO.write(this.image, "png", baos)
			this.namedTag.putByteArray("Colors", baos.toByteArray())
		} catch (e: IOException) {
			MainLogger.getLogger().logException(e)
		}
	}

	protected fun loadImageFromNBT(): BufferedImage? {
		try {
			val data = namedTag.getByteArray("Colors")
			image = ImageIO.read(ByteArrayInputStream(data))
			return image
		} catch (e: IOException) {
			MainLogger.getLogger().logException(e)
		}
		return null
	}

	val mapId: Long
		get() = namedTag.getLong("map_uuid")

	fun sendImage(p: Player) {
		// don't load the image from NBT if it has been done before.
		val image = if (image != null) image else loadImageFromNBT()
		val pk = ClientboundMapItemDataPacket()
		pk.mapId = mapId
		pk.update = 2
		pk.scale = 0
		pk.width = 128
		pk.height = 128
		pk.offsetX = 0
		pk.offsetZ = 0
		pk.image = image
		p.dataPacket(pk)
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override val maxStackSize: Int
		get() = 1

	companion object {
		var mapCount = 0
	}

	init {
		if (!hasCompoundTag() || !namedTag.contains("map_uuid")) {
			val tag = CompoundTag()
			tag.putLong("map_uuid", mapCount++.toLong())
			this.namedTag = tag
		}
	}
}