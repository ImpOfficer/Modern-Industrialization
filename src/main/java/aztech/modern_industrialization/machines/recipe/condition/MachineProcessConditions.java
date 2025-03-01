/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.recipe.condition;

import aztech.modern_industrialization.MI;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class MachineProcessConditions {
    private static final BiMap<ResourceLocation, MapCodec<? extends MachineProcessCondition>> MAP = HashBiMap.create();

    public static void register(ResourceLocation id, MapCodec<? extends MachineProcessCondition> serializer) {
        if (MAP.get(id) != null || MAP.inverse().get(serializer) != null) {
            throw new IllegalArgumentException("Duplicate registration for process condition " + id);
        }

        MAP.put(id, serializer);
    }

    @Nullable
    public static MapCodec<? extends MachineProcessCondition> get(ResourceLocation id) {
        return MAP.get(id);
    }

    public static ResourceLocation getId(MapCodec<? extends MachineProcessCondition> serializer) {
        return MAP.inverse().get(serializer);
    }

    static {
        register(MI.id("dimension"), DimensionProcessCondition.CODEC);
        register(MI.id("adjacent_block"), AdjacentBlockProcessCondition.CODEC);
        register(MI.id("biome"), BiomeProcessCondition.CODEC);
        register(MI.id("custom"), CustomProcessCondition.CODEC);
    }
}
