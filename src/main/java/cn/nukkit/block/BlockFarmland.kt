package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockFarmland @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val name: String
		get() = "Farmland"

	override val id: Int
		get() = BlockID.Companion.FARMLAND

	override val resistance: Double
		get() = 3

	override val hardness: Double
		get() = 0.6

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override fun getMaxY(): Double {
		return y + 0.9375
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_RANDOM) {
			val v = Vector3()
			if (level.getBlock(v.setComponents(x, y + 1, z)) is BlockCrops) {
				return 0
			}
			if (level.getBlock(v.setComponents(x, y + 1, z)).isSolid) {
				level.setBlock(this, Block.Companion.get(BlockID.Companion.DIRT), false, true)
				return Level.BLOCK_UPDATE_RANDOM
			}
			var found = false
			if (level.isRaining) {
				found = true
			} else {
				run {
					var x = this.x.toInt() - 4
					while (x <= this.x + 4) {
						run {
							var z = this.z.toInt() - 4
							while (z <= this.z + 4) {
								run {
									var y = this.y.toInt()
									while (y <= this.y + 1) {
										if (z.toDouble() == this.z && x.toDouble() == this.x && y.toDouble() == this.y) {
											y++
											continue
										}
										v.setComponents(x.toDouble(), y.toDouble(), z.toDouble())
										val block = this.level.getBlockIdAt(v.floorX, v.floorY, v.floorZ)
										if (block == BlockID.Companion.WATER || block == BlockID.Companion.STILL_WATER) {
											found = true
											break
										}
										y++
									}
								}
								z++
							}
						}
						x++
					}
				}
			}
			val block = level.getBlock(v.setComponents(x, y - 1, z))
			if (found || block is BlockWater) {
				if (this.damage < 7) {
					this.setDamage(7)
					level.setBlock(this, this, false, false)
				}
				return Level.BLOCK_UPDATE_RANDOM
			}
			if (this.damage > 0) {
				this.setDamage(this.damage - 1)
				level.setBlock(this, this, false, false)
			} else {
				level.setBlock(this, Block.Companion.get(BlockID.Companion.DIRT), false, true)
			}
			return Level.BLOCK_UPDATE_RANDOM
		}
		return 0
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.DIRT))
	}

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}