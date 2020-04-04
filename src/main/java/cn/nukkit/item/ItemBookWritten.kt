package cn.nukkit.item

import cn.nukkit.item.ItemBookWritable
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag

class ItemBookWritten @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemBookWritable(ItemID.Companion.WRITTEN_BOOK, 0, count, "Written Book") {
	override val maxStackSize: Int
		get() = 16

	fun writeBook(author: String?, title: String?, pages: Array<String?>): Item? {
		val pageList = ListTag<CompoundTag>("pages")
		for (page in pages) {
			pageList.add(ItemBookWritable.Companion.createPageTag(page))
		}
		return writeBook(author, title, pageList)
	}

	fun writeBook(author: String?, title: String?, pages: ListTag<CompoundTag>): Item? {
		if (pages.size() > 50 || pages.size() <= 0) return this //Minecraft does not support more than 50 pages
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		tag!!.putString("author", author)
		tag.putString("title", title)
		tag.putList(pages)
		tag.putInt("generation", GENERATION_ORIGINAL)
		tag.putString("xuid", "")
		return setNamedTag(tag)
	}

	fun signBook(title: String?, author: String?, xuid: String?, generation: Int): Boolean {
		this.namedTag = (if (hasCompoundTag()) this.namedTag else CompoundTag())
				.putString("title", title)
				.putString("author", author)
				.putInt("generation", generation)
				.putString("xuid", xuid)
		return true
	}

	/**
	 * Returns the generation of the book.
	 * Generations higher than 1 can not be copied.
	 */
	/**
	 * Sets the generation of a book.
	 */
	var generation: Int
		get() = if (hasCompoundTag()) this.namedTag.getInt("generation") else -1
		set(generation) {
			this.namedTag = (if (hasCompoundTag()) this.namedTag else CompoundTag()).putInt("generation", generation)
		}

	/**
	 * Returns the author of this book.
	 * This is not a reliable way to get the name of the player who signed this book.
	 * The author can be set to anything when signing a book.
	 */
	/**
	 * Sets the author of this book.
	 */
	var author: String?
		get() = if (hasCompoundTag()) this.namedTag.getString("author") else ""
		set(author) {
			this.namedTag = (if (hasCompoundTag()) this.namedTag else CompoundTag()).putString("author", author)
		}

	/**
	 * Returns the title of this book.
	 */
	/**
	 * Sets the title of this book.
	 */
	var title: String?
		get() = if (hasCompoundTag()) this.namedTag.getString("title") else "Written Book"
		set(title) {
			this.namedTag = (if (hasCompoundTag()) this.namedTag else CompoundTag()).putString("title", title)
		}

	/**
	 * Returns the author's XUID of this book.
	 */
	/**
	 * Sets the author's XUID of this book.
	 */
	var xUID: String?
		get() = if (hasCompoundTag()) this.namedTag.getString("xuid") else ""
		set(title) {
			this.namedTag = (if (hasCompoundTag()) this.namedTag else CompoundTag()).putString("xuid", title)
		}

	companion object {
		const val GENERATION_ORIGINAL = 0
		const val GENERATION_COPY = 1
		const val GENERATION_COPY_OF_COPY = 2
		const val GENERATION_TATTERED = 3
	}
}