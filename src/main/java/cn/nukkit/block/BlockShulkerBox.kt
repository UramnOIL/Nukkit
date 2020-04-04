package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created by PetteriM1
 */
class BlockShulkerBox @JvmOverloads constructor(override var damage: Int = 0) : BlockUndyedShulkerBox() {
	override val id: Int
		get() = BlockID.Companion.SHULKER_BOX

	override val name: String
		get() = dyeColor.name + " Shulker Box"

	override val color: BlockColor
		get() = dyeColor.color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)

	override val fullId: Int
		get() = (id shl 4) + damage

}