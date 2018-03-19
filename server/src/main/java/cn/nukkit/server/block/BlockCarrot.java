package cn.nukkit.server.block;

import cn.nukkit.server.item.Item;
import cn.nukkit.server.item.ItemCarrot;

import java.util.Random;

/**
 * @author Nukkit Project Team
 */
public class BlockCarrot extends BlockCrops {

    public BlockCarrot(int meta) {
        super(meta);
    }

    public BlockCarrot() {
        this(0);
    }

    @Override
    public String getName() {
        return "Carrot BlockType";
    }

    @Override
    public int getId() {
        return CARROT_BLOCK;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (meta >= 0x07) {
            return new Item[]{
                    new ItemCarrot(0, new Random().nextInt(3) + 1)
            };
        }
        return new Item[]{
                new ItemCarrot()
        };
    }

    @Override
    public Item toItem() {
        return new ItemCarrot();
    }
}