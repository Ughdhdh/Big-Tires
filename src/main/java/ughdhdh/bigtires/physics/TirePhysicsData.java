package ughdhdh.bigtires.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tire physics parameters — DataComponent on the item stack.
 ---
 *  grip              — arctan(grip) = maximum climbable slope angle:
 *                      1.0→45°, 1.8→61°, 0.25→14°
 *  driveForce        — RPM-to-thrust multiplier
 *  rollingResistance — passive drag (limits top speed)
 *  lateralStiffness  — side grip (< 1.0 = drift, > 1.0 = rigid)
 */

        //lateralStiffness=0.0 → factor=0  → нет боковой коррекции (дрифт)
        //    lateralStiffness=0.6 → factor без изменений
        //    lateralStiffness=1.0 → коррекция ×1.67 (цепкая резина)
        //    lateralStiffness=2.0 → коррекция ×3.33 (трактор)


public record TirePhysicsData(
        float grip,
        float driveForce,
        float rollingResistance,
        float lateralStiffness
) {
    public static final Codec<TirePhysicsData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("grip",               1.0f).forGetter(TirePhysicsData::grip),
            Codec.FLOAT.optionalFieldOf("drive_force",        1.0f).forGetter(TirePhysicsData::driveForce),
            Codec.FLOAT.optionalFieldOf("rolling_resistance", 0.2f).forGetter(TirePhysicsData::rollingResistance),
            Codec.FLOAT.optionalFieldOf("lateral_stiffness",  0.6f).forGetter(TirePhysicsData::lateralStiffness)
    ).apply(i, TirePhysicsData::new));

    public static final TirePhysicsData HUGE         = new TirePhysicsData(1.2f, 1.0f, 0.5f, 1.2f);
    public static final TirePhysicsData HUGE_WIDE    = new TirePhysicsData(1.4f, 0.9f, 0.6f, 1.4f);
    public static final TirePhysicsData ROWING       = new TirePhysicsData(0.5f, 0.7f, 0.6f, 0.6f);
    public static final TirePhysicsData ROWING_WIDE  = new TirePhysicsData(0.5f, 0.6f, 0.7f, 0.6f);
    public static final TirePhysicsData BIG_TRACTOR  = new TirePhysicsData(2.0f, 0.6f, 0.75f, 1.8f);
    public static final TirePhysicsData TRACTOR      = new TirePhysicsData(1.8f, 0.7f, 0.65f, 1.6f);
    public static final TirePhysicsData TRUCK        = new TirePhysicsData(1.1f, 1.1f, 0.4f, 0.9f);
    public static final TirePhysicsData NARROW_TRUCK = new TirePhysicsData(1.1f, 1.1f, 0.4f, 0.9f);
    public static final TirePhysicsData SMALL_TRUCK  = new TirePhysicsData(1.0f, 1.2f, 0.4f, 0.85f);
    public static final TirePhysicsData MONSTER_JAM  = new TirePhysicsData(0.6f, 0.65f, 0.5f, 0.5f);
    public static final TirePhysicsData BAMBOO       = new TirePhysicsData(0.85f, 0.9f, 0.55f, 0.45f);
    public static final TirePhysicsData VINTAGE      = new TirePhysicsData(1.0f, 0.95f, 0.5f, 0.35f);
    public static final TirePhysicsData DRIFT        = new TirePhysicsData(0.1f, 1.7f, 0.05f, 0.15f);
    public static final TirePhysicsData WOODEN       = new TirePhysicsData(1.0f, 1.0f, 0.45f, 0.6f);

}
