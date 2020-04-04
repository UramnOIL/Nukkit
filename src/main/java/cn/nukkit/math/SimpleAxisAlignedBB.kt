package cn.nukkit.math

/**
 * auth||: MagicDroidX
 * Nukkit Project
 */
class SimpleAxisAlignedBB : AxisAlignedBB {
	override var minX: Double
	override var minY: Double
	override var minZ: Double
	override var maxX: Double
	override var maxY: Double
	override var maxZ: Double

	constructor(pos1: Vector3, pos2: Vector3) {
		minX = Math.min(pos1.x, pos2.x)
		minY = Math.min(pos1.y, pos2.y)
		minZ = Math.min(pos1.z, pos2.z)
		maxX = Math.max(pos1.x, pos2.x)
		maxY = Math.max(pos1.y, pos2.y)
		maxZ = Math.max(pos1.z, pos2.z)
	}

	constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) {
		this.minX = minX
		this.minY = minY
		this.minZ = minZ
		this.maxX = maxX
		this.maxY = maxY
		this.maxZ = maxZ
	}

	override fun toString(): String {
		return "AxisAlignedBB(" + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ")"
	}

	override fun clone(): AxisAlignedBB {
		return SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
	}
}