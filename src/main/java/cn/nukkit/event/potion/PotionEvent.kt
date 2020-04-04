package cn.nukkit.event.potion

import cn.nukkit.event.Event
import cn.nukkit.potion.Potion

/**
 * Created by Snake1999 on 2016/1/12.
 * Package cn.nukkit.event.potion in project nukkit
 */
abstract class PotionEvent(var potion: Potion) : Event()