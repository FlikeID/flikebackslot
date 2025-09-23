package my.flike.client.render.transforms;

import my.flike.client.render.BackItemTransform;
import net.minecraft.client.render.model.json.Transformation;
import org.joml.Vector3f;

public class SwordBackItemTransform extends BackItemTransform {
    public SwordBackItemTransform() {
        super(makeDefaultTransform());
    }
    private static Transformation makeDefaultTransform() {
        return new Transformation(
                new Vector3f(0f, 0f, 270f),
                new Vector3f(0f, 0.35f, 0.2f),
                new Vector3f(1f, 1f, 1f)
        );
    }
}

