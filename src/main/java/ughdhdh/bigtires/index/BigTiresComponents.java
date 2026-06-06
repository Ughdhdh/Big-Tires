package ughdhdh.bigtires.index;

import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.physics.BuoyantTireData;
import ughdhdh.bigtires.physics.TirePhysicsData;
import com.mojang.serialization.Codec;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import java.util.function.UnaryOperator;

/**
 * Analog of OffroadDataComponents — registers DataComponentType via Veil's RegistrationProvider.
 * Fields are stored directly as DataComponentType<T>, not as Supplier.
 */
public class BigTiresComponents {

    private static final RegistrationProvider<DataComponentType<?>> REGISTRY =
            RegistrationProvider.get(Registries.DATA_COMPONENT_TYPE, BigTires.MOD_ID);

    /**
     * Tire physics parameters: grip, driveForce, rollingResistance, lateralStiffness.
     * Present on all BigTires tires.
     */
    public static final DataComponentType<TirePhysicsData> TIRE_PHYSICS =
            create("tire_physics", b -> b.persistent(TirePhysicsData.CODEC));

    /**
     * Buoyancy and paddling. Only on huge_rowing_tire and huge_rowing_wide_tire.
     */
    public static final DataComponentType<BuoyantTireData> BUOYANCY =
            create("buoyancy", b -> b.persistent(BuoyantTireData.CODEC));

    /**
     * Флип колеса (зеркальное отражение по Z-оси).
     * Присутствие компонента означает перевёрнутое колесо; отсутствие — нормальное.
     * Устанавливается гаечным ключом (WheelWrenchItem).
     */
    public static final DataComponentType<Boolean> FLIPPED =
            create("flipped", b -> b.persistent(Codec.BOOL));

    private static <T> DataComponentType<T> create(
            final String name,
            final UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        REGISTRY.register(name, () -> type);
        return type;
    }

    public static void init() {
        // no-op — initializes the static fields above
    }
}
