package cn.nukkit.blockentity

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.block.SignChangeEvent
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.TextFormat
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockEntitySign(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	var text: Array<String?>?
		private set

	override fun initBlockEntity() {
		text = arrayOfNulls(4)
		if (!namedTag.contains("Text")) {
			for (i in 1..4) {
				val key = "Text$i"
				if (namedTag.contains(key)) {
					val line = namedTag.getString(key)
					text!![i - 1] = line
					namedTag.remove(key)
				}
			}
		} else {
			val lines = namedTag.getString("Text").split("\n", 4.toBoolean()).toTypedArray()
			for (i in text!!.indices) {
				if (i < lines.size) text!![i] = lines[i] else text!![i] = ""
			}
		}

		// Check old text to sanitize
		if (text != null) {
			sanitizeText(text!!)
		}
		super.initBlockEntity()
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag.remove("Creator")
	}

	override val isBlockEntityValid: Boolean
		get() {
			val blockID = block.id
			return blockID == Block.SIGN_POST || blockID == Block.WALL_SIGN
		}

	fun setText(vararg lines: String?): Boolean {
		for (i in 0..3) {
			if (i < lines.size) text!![i] = lines[i] else text!![i] = ""
		}
		namedTag.putString("Text", java.lang.String.join("\n", *text))
		spawnToAll()
		if (chunk != null) {
			setDirty()
		}
		return true
	}

	override fun updateCompoundTag(nbt: CompoundTag, player: Player): Boolean {
		if (nbt.getString("id") != BlockEntity.Companion.SIGN) {
			return false
		}
		val lines = arrayOfNulls<String>(4)
		Arrays.fill(lines, "")
		val splitLines = nbt.getString("Text").split("\n", 4.toBoolean()).toTypedArray()
		System.arraycopy(splitLines, 0, lines, 0, splitLines.size)
		sanitizeText(lines)
		val signChangeEvent = SignChangeEvent(this.block, player, lines)
		if (!namedTag.contains("Creator") || player.getUniqueId().toString() != namedTag.getString("Creator")) {
			signChangeEvent.setCancelled()
		}
		if (player.removeFormat) {
			for (i in lines.indices) {
				lines[i] = TextFormat.clean(lines[i])
			}
		}
		server.pluginManager.callEvent(signChangeEvent)
		if (!signChangeEvent.isCancelled) {
			setText(*signChangeEvent.lines)
			return true
		}
		return false
	}

	override val spawnCompound: CompoundTag?
		get() = CompoundTag()
				.putString("id", BlockEntity.Companion.SIGN)
				.putString("Text", namedTag.getString("Text"))
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())

	companion object {
		private fun sanitizeText(lines: Array<String?>) {
			for (i in lines.indices) {
				// Don't allow excessive text per line.
				if (lines[i] != null) {
					lines[i] = lines[i]!!.substring(0, Math.min(255, lines[i]!!.length))
				}
			}
		}
	}
}