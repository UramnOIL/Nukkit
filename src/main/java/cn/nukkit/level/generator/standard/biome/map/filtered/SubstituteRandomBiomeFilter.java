package cn.nukkit.level.generator.standard.biome.map.filtered;

import cn.nukkit.level.generator.standard.biome.GenerationBiome;
import cn.nukkit.level.generator.standard.misc.IntArrayAllocator;
import cn.nukkit.level.generator.standard.store.StandardGeneratorStores;
import cn.nukkit.utils.Identifier;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.daporkchop.lib.random.PRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Replaces specific biome with a replacement islandBiomes randomly selected from a list.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public class SubstituteRandomBiomeFilter extends AbstractBiomeFilter.Next {
    public static final Identifier ID = Identifier.fromString("nukkitx:substitute_random");

    protected final Int2ObjectMap<int[]> replacements = new Int2ObjectOpenHashMap<>();

    @JsonProperty
    protected Map<String, GenerationBiome[]> biomes;

    @Override
    public void init(long seed, PRandom random) {
        Objects.requireNonNull(this.biomes, "biomes must be set!").forEach((key, arr) -> {
            int replaceId = StandardGeneratorStores.generationBiome().find(Identifier.fromString(key)).getInternalId();
            int[] replacementIds = Arrays.stream(arr).mapToInt(GenerationBiome::getInternalId).toArray();
            this.replacements.put(replaceId, replacementIds);
        });

        super.init(seed, random);
    }

    @Override
    public Collection<GenerationBiome> getAllBiomes() {
        Collection<GenerationBiome> biomes = new ArrayList<>(this.next.getAllBiomes());
        this.biomes.values().forEach(arr -> Collections.addAll(biomes, arr));
        return biomes;
    }

    @Override
    public int[] get(int x, int z, int sizeX, int sizeZ, IntArrayAllocator alloc) {
        int[] below = this.next.get(x, z, sizeX, sizeZ, alloc);

        int[] out = alloc.get(sizeX * sizeZ);
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dz = 0; dz < sizeZ; dz++) {
                int id = below[dx * sizeZ + dz];

                int[] replacementIds = this.replacements.get(id);
                if (replacementIds != null) {
                    id = replacementIds[this.random(x + dx, z + dz, 0, replacementIds.length)];
                }

                out[dx * sizeZ + dz] = id;
            }
        }
        alloc.release(below);

        return out;
    }
}
