package my.flike.client.render.transforms;

import my.flike.client.render.BackItemTransform;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import org.joml.Vector3f;

public class TridentBackItemTransform extends BackItemTransform {
    public TridentBackItemTransform() {
        super(makeDefaultTransform(),ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
    }
    private static Transformation makeDefaultTransform() {
        return new Transformation(
                new Vector3f(90f, 50f, 270f),
                new Vector3f(0f, 0.45f, 0.2f),
                new Vector3f(1f, 1f, 1f)
        );
    }
}
