package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created by CreeperFace on 7.8.2017.
 */
class BlockGlassPaneStained @JvmOverloads constructor(override var damage: Int = 0) : BlockGlassPane() {
	override val fullId: Int
		get() = (id shl 4) + damage

	override val id: Int
		get() = BlockID.Companion.STAINED_GLASS_PANE

	override val name: String
		get() = dyeColor.name + " stained glass pane"

	override val color: BlockColor
		get() = dyeColor.color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)

	override fun canSilkTouch(): Boolean {
		return true
	}

}