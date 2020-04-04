package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created by CreeperFace on 7.8.2017.
 */
class BlockGlassStained @JvmOverloads constructor(override var damage: Int = 0) : BlockGlass() {
	override val fullId: Int
		get() = (id shl 4) + damage

	override val id: Int
		get() = BlockID.Companion.STAINED_GLASS

	override val name: String
		get() = dyeColor.name + " Stained Glass"

	override val color: BlockColor
		get() = DyeColor.getByWoolData(damage).color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)

	override fun canSilkTouch(): Boolean {
		return true
	}

}