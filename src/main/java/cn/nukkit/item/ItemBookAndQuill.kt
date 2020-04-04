package cn.nukkit.item

class ItemBookAndQuill @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemBookWritable(ItemID.Companion.BOOK_AND_QUILL, 0, count, "Book & Quill") {
	override val maxStackSize: Int
		get() = 1
}