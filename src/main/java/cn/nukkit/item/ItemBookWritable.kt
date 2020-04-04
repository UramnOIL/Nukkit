package cn.nukkit.item

import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import com.google.common.base.Preconditions

abstract class ItemBookWritable : Item {
	protected constructor(id: Int) : super(id) {}
	protected constructor(id: Int, meta: Int?) : super(id, meta) {}
	protected constructor(id: Int, meta: Int?, count: Int) : super(id, meta, count) {}
	protected constructor(id: Int, meta: Int?, count: Int, name: String) : super(id, meta, count, name) {}

	/**
	 * Returns whether the given page exists in this book.
	 */
	fun pageExists(pageId: Int): Boolean {
		Preconditions.checkArgument(pageId >= 0 && pageId < 50, "Page number $pageId is out of range")
		if (hasCompoundTag()) {
			val tag = this.namedTag
			if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
				return tag.getList("pages", CompoundTag::class.java).size() > pageId
			}
		}
		return false
	}

	/**
	 * Returns a string containing the content of a page (which could be empty), or null if the page doesn't exist.
	 */
	fun getPageText(pageId: Int): String? {
		Preconditions.checkArgument(pageId >= 0 && pageId < 50, "Page number $pageId is out of range")
		if (hasCompoundTag()) {
			val tag = this.namedTag
			if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
				val pages = tag.getList("pages", CompoundTag::class.java)
				if (pages.size() > pageId) {
					return pages[pageId].getString("text")
				}
			}
		}
		return null
	}

	/**
	 * Sets the text of a page in the book. Adds the page if the page does not yet exist.
	 * @return boolean indicating success
	 */
	fun setPageText(pageId: Int, pageText: String): Boolean {
		Preconditions.checkArgument(pageId >= 0 && pageId < 50, "Page number $pageId is out of range")
		val tag: CompoundTag?
		tag = if (hasCompoundTag()) {
			this.namedTag
		} else if (pageText.isEmpty()) {
			return false
		} else {
			CompoundTag()
		}
		val pages: ListTag<CompoundTag>
		if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
			pages = ListTag("pages")
			tag.putList(pages)
		} else {
			pages = tag.getList("pages", CompoundTag::class.java)
		}
		if (pages.size() <= pageId) {
			for (current in pages.size()..pageId) {
				pages.add(createPageTag())
			}
		}
		pages[pageId].putString("text", pageText)
		this.setCompoundTag(tag)
		return true
	}

	/**
	 * Adds a new page with the given page ID.
	 * Creates a new page for every page between the given ID and existing pages that doesn't yet exist.
	 * @return boolean indicating success
	 */
	fun addPage(pageId: Int): Boolean {
		Preconditions.checkArgument(pageId >= 0 && pageId < 50, "Page number $pageId is out of range")
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		val pages: ListTag<CompoundTag>
		if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
			pages = ListTag("pages")
			tag.putList(pages)
		} else {
			pages = tag.getList("pages", CompoundTag::class.java)
		}
		for (current in pages.size()..pageId) {
			pages.add(createPageTag())
		}
		this.setCompoundTag(tag)
		return true
	}

	/**
	 * Deletes an existing page with the given page ID.
	 * @return boolean indicating success
	 */
	fun deletePage(pageId: Int): Boolean {
		Preconditions.checkArgument(pageId >= 0 && pageId < 50, "Page number $pageId is out of range")
		if (hasCompoundTag()) {
			val tag = this.namedTag
			if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
				val pages = tag.getList("pages", CompoundTag::class.java)
				if (pages.size() > pageId) {
					pages.remove(pageId)
					this.setCompoundTag(tag)
				}
			}
		}
		return true
	}
	/**
	 * Inserts a new page with the given text and moves other pages upwards.
	 * @return boolean indicating success
	 */
	/**
	 * Inserts a new page with the given text and moves other pages upwards.
	 * @return boolean indicating success
	 */
	@JvmOverloads
	fun insertPage(pageId: Int, pageText: String? = ""): Boolean {
		Preconditions.checkArgument(pageId >= 0 && pageId < 50, "Page number $pageId is out of range")
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		val pages: ListTag<CompoundTag>
		if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
			pages = ListTag("pages")
			tag.putList(pages)
		} else {
			pages = tag.getList("pages", CompoundTag::class.java)
		}
		if (pages.size() <= pageId) {
			for (current in pages.size()..pageId) {
				pages.add(createPageTag())
			}
			pages[pageId].putString("text", pageText)
		} else {
			pages.add(pageId, createPageTag(pageText))
		}
		this.setCompoundTag(tag)
		return true
	}

	/**
	 * Switches the text of two pages with each other.
	 * @return boolean indicating success
	 */
	fun swapPages(pageId1: Int, pageId2: Int): Boolean {
		Preconditions.checkArgument(pageId1 >= 0 && pageId1 < 50, "Page number $pageId1 is out of range")
		Preconditions.checkArgument(pageId2 >= 0 && pageId2 < 50, "Page number $pageId2 is out of range")
		if (hasCompoundTag()) {
			val tag = this.namedTag
			if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
				val pages = tag.getList("pages", CompoundTag::class.java)
				if (pages.size() > pageId1 && pages.size() > pageId2) {
					val pageContents1 = pages[pageId1].getString("text")
					val pageContents2 = pages[pageId2].getString("text")
					pages[pageId1].putString("text", pageContents2)
					pages[pageId2].putString("text", pageContents1)
					return true
				}
			}
		}
		return false
	}

	/**
	 * Returns an list containing all pages of this book.
	 */
	val pages: List<*>
		get() {
			val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
			val pages: ListTag<CompoundTag>
			if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
				pages = ListTag("pages")
				tag.putList(pages)
			} else {
				pages = tag.getList("pages", CompoundTag::class.java)
			}
			return pages.parseValue()
		}

	companion object {
		protected fun createPageTag(pageText: String? = ""): CompoundTag {
			return CompoundTag()
					.putString("text", pageText)
					.putString("photoname", "")
		}
	}
}