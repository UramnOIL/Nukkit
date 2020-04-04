package cn.nukkit.math

import java.util.*

/**
 * Copyright 2017 lmlstarqaq
 * All rights reserved.
 */
class Angle : Comparable<Angle> {
	fun sin(): Double {
		return Math.sin(asDoubleRadian())
	}

	fun cos(): Double {
		return Math.cos(asDoubleRadian())
	}

	fun tan(): Double {
		return Math.tan(asDoubleRadian())
	}

	fun asFloatRadian(): Float {
		return if (isOriginDouble) {
			if (isDegree) (doubleValue * Math.PI / 180.0).toFloat() else doubleValue.toFloat()
		} else {
			if (isDegree) floatValue * Math.PI.toFloat() / 180.0f else floatValue
		}
	}

	fun asDoubleRadian(): Double {
		return if (isOriginDouble) {
			if (isDegree) doubleValue * Math.PI / 180.0 else doubleValue
		} else {
			if (isDegree) floatValue * Math.PI / 180.0 else floatValue.toDouble()
		}
	}

	fun asFloatDegree(): Float {
		return if (isOriginDouble) {
			if (isDegree) doubleValue.toFloat() else (doubleValue * 180.0 / Math.PI).toFloat()
		} else {
			if (isDegree) floatValue else floatValue * 180.0f / Math.PI.toFloat()
		}
	}

	fun asDoubleDegree(): Double {
		return if (isOriginDouble) {
			if (isDegree) doubleValue else doubleValue * 180.0 / Math.PI
		} else {
			if (isDegree) floatValue.toDouble() else floatValue * 180.0 / Math.PI
		}
	}

	/* -- Override -- */
	override fun toString(): String {
		return String.format(Locale.ROOT,
				"Angle[%s, %f%s = %f%s] [%d]",
				if (isOriginDouble) "Double" else "Float",
				if (isOriginDouble) doubleValue else floatValue,
				if (isDegree) "deg" else "rad",
				if (isDegree) if (isOriginDouble) asDoubleRadian() else asFloatRadian() else if (isOriginDouble) asDoubleDegree() else asFloatDegree(),
				if (isDegree) "rad" else "deg",
				hashCode()
		)
	}

	override fun compareTo(o: Angle): Int {
		return java.lang.Double.compare(asDoubleRadian(), o.asDoubleRadian())
	}

	override fun equals(obj: Any?): Boolean {
		return obj is Angle && this.compareTo(obj) == 0
	}

	override fun hashCode(): Int {
		var hash: Int
		hash = if (isOriginDouble) java.lang.Double.hashCode(doubleValue) else java.lang.Float.hashCode(floatValue)
		if (isDegree) hash = hash xor -0x5432edcc
		return hash
	}

	/* -- Internal Part -- */
	private val floatValue: Float
	private val doubleValue: Double
	private val isDegree: Boolean
	private val isOriginDouble: Boolean

	private constructor(floatValue: Float, isDegree: Boolean) {
		isOriginDouble = false
		this.floatValue = floatValue
		doubleValue = 0.0
		this.isDegree = isDegree
	}

	private constructor(doubleValue: Double, isDegree: Boolean) {
		isOriginDouble = true
		floatValue = 0.0f
		this.doubleValue = doubleValue
		this.isDegree = isDegree
	}

	companion object {
		fun fromDegree(floatDegree: Float): Angle {
			return Angle(floatDegree, true)
		}

		fun fromDegree(doubleDegree: Double): Angle {
			return Angle(doubleDegree, true)
		}

		fun fromRadian(floatRadian: Float): Angle {
			return Angle(floatRadian, false)
		}

		fun fromRadian(doubleRadian: Double): Angle {
			return Angle(doubleRadian, false)
		}

		fun asin(v: Double): Angle {
			return fromRadian(Math.asin(v))
		}

		fun acos(v: Double): Angle {
			return fromRadian(Math.acos(v))
		}

		fun atan(v: Double): Angle {
			return fromRadian(Math.atan(v))
		}

		fun compare(a: Angle, b: Angle): Int {
			return a.compareTo(b)
		}
	}
}