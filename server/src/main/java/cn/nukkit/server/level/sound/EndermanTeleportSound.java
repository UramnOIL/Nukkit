package cn.nukkit.server.level.sound;

import cn.nukkit.server.math.Vector3;

/**
 * Created on 2015/11/21 by xtypr.
 * Package cn.nukkit.server.level.sound in project Nukkit .
 */
public class EndermanTeleportSound extends LevelEventSound {
    public EndermanTeleportSound(Vector3 pos) {
        this(pos, 0);
    }

    public EndermanTeleportSound(Vector3 pos, float pitch) {
        super(pos, LevelEventPacket.EVENT_SOUND_ENDERMAN_TELEPORT, pitch);
    }
}