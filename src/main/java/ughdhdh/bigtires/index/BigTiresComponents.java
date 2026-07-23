package ughdhdh.bigtires.index;

import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.physics.BuoyantTireData;
import ughdhdh.bigtires.physics.TirePhysicsData;
import com.mojang.serialization.Codec;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import java.util.function.UnaryOperator;

public class BigTiresComponents {

    private static final RegistrationProvider<DataComponentType<?>> REGISTRY =
            RegistrationProvider.get(Registries.DATA_COMPONENT_TYPE, BigTires.MOD_ID);

    public static final DataComponentType<TirePhysicsData> TIRE_PHYSICS =
            create("tire_physics", b -> b.persistent(TirePhysicsData.CODEC));

    public static final DataComponentType<BuoyantTireData> BUOYANCY =
            create("buoyancy", b -> b.persistent(BuoyantTireData.CODEC));

    public static final DataComponentType<Boolean> FLIPPED =
            create("flipped", b -> b.persistent(Codec.BOOL));

    /**
     * Цвет резины (0xRRGGBB). Отсутствует = не крашено, overlay-проход пропускается.
     * Устанавливается через WheelDyeStation (слот шины) или сбрасывается котлом.
     */
    public static final DataComponentType<Integer> TIRE_COLOR =
            create("tire_color", b -> b.persistent(Codec.INT));

    /**
     * Цвет диска (0xRRGGBB). Отсутствует = не крашено, overlay-проход пропускается.
     * Устанавливается через WheelDyeStation (слот диска) или сбрасывается котлом.
     */
    public static final DataComponentType<Integer> RIM_COLOR =
            create("rim_color", b -> b.persistent(Codec.INT));

    private static <T> DataComponentType<T> create(
            final String name,
            final UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        REGISTRY.register(name, () -> type);
        return type;
    }

    public static void init() {}
}
