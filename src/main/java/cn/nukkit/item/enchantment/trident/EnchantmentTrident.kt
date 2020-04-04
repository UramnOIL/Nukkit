package cn.nukkit.item.enchantment.trident

import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.item.enchantment.EnchantmentType

abstract class EnchantmentTrident protected constructor(id: Int, name: String, weight: Int) : Enchantment(id, name, weight, EnchantmentType.TRIDENT)