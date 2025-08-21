package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

import static com.hbm.render.block.BlockBakeFrame.BlockForm.FULL_CUSTOM;

public class BlockConcreteColoredExt extends BlockEnumMeta {

    public BlockConcreteColoredExt(Material material, SoundType type, String name, Class<? extends Enum<?>> enumClass, boolean multiName, boolean multiTex) {
        super(material, type, name, enumClass, multiName, multiTex);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        BlockBakeFrame[] frames = new BlockBakeFrame[EnumConcreteType.values().length];
        for (EnumConcreteType type : EnumConcreteType.values()) {
            String name = registryName + "." + type.name().toLowerCase(Locale.US);
            if (type == EnumConcreteType.MACHINE_STRIPE) {
                String machine = registryName + "." + EnumConcreteType.MACHINE.name().toLowerCase(Locale.US);
                frames[type.ordinal()] = new BlockBakeFrame(FULL_CUSTOM,
                        machine, // up
                        machine, // down
                        name,  // north
                        name,  // south
                        name,  // west
                        name   // east
                );
            } else {
                frames[type.ordinal()] = new BlockBakeFrame(name);
            }
        }
        return frames;
    }
    public enum EnumConcreteType {
        MACHINE,
        MACHINE_STRIPE,
        INDIGO,
        PURPLE,
        PINK,
        HAZARD,
        SAND,
        BRONZE
    }
}
