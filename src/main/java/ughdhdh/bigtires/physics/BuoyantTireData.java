package ughdhdh.bigtires.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Buoyancy and paddle parameters.
 *  buoyancy    — lift coefficient (1.0 = counteracts gravity, >1.0 = floats upward)
 *  paddleForce — paddle thrust × RPM × submersionRatio
 */
public record BuoyantTireData(float buoyancy, float paddleForce) {

    public static final Codec<BuoyantTireData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("buoyancy",     1.0f).forGetter(BuoyantTireData::buoyancy),
            Codec.FLOAT.optionalFieldOf("paddle_force", 0.3f).forGetter(BuoyantTireData::paddleForce)
    ).apply(i, BuoyantTireData::new));

    public static final BuoyantTireData ROWING      = new BuoyantTireData(1.0f, 5.0f);
    public static final BuoyantTireData ROWING_WIDE = new BuoyantTireData(1.3f, 8.0f);
}
