package cn.nukkit.item.randomitem

import cn.nukkit.block.Block.Companion.get
import cn.nukkit.item.Item

/**
 * Created by Snake1999 on 2016/1/15.
 * Package cn.nukkit.item.randomitem in project nukkit.
 */
class ConstantItemSelector(val item: Item, parent: Selector?) : Selector(parent) {

	constructor(id: Int, parent: Selector?) : this(id, 0, parent) {}
	constructor(id: Int, meta: Int?, parent: Selector?) : this(id, meta, 1, parent) {}
	constructor(id: Int, meta: Int?, count: Int, parent: Selector?) : this(get(id, meta, count), parent) {}

	override fun select(): Any {
		return item
	}

}