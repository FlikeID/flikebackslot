package my.flike.client.render.transforms;

import my.flike.client.render.BackItemTransform;
import net.minecraft.client.render.model.json.Transformation;
import org.joml.Vector3f;

public class ShieldBackItemTransform extends BackItemTransform {
    public ShieldBackItemTransform() {
        super(makeDefaultTransform());
    }
    private static Transformation makeDefaultTransform() {
        return new Transformation(
                new Vector3f(180f, 0f, 30f),
                new Vector3f(0f, 0.3f, 0.12f),
                new Vector3f(1.7f, 1.7f, 1.7f)
        );
    }
}

