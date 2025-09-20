package my.flike.client.render;

import my.flike.client.render.transforms.*;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;


public class BackItemTransform {

    private static Transformation makeDefaultTransform() {
        return new Transformation(
                new Vector3f(0f, 0f, 270f),
                new Vector3f(0f, 0.25f, 0.17f),
                new Vector3f(1f, 1f, 1f)
        );
    }


    private static final ModelTransformationMode default_transform_mode = ModelTransformationMode.FIXED;

    public boolean enabled = true;

    public Transformation transform;

    public ModelTransformationMode transform_mode;

    public BackItemTransform() { this(makeDefaultTransform(), default_transform_mode); }

    public BackItemTransform(Transformation transform) {
        this(transform, default_transform_mode);
    }

    public BackItemTransform(ModelTransformationMode transform_mode) {
        this(makeDefaultTransform(), transform_mode);
    }

    public BackItemTransform(Transformation transform, ModelTransformationMode transform_mode) {
        this.transform = transform != null ? transform : makeDefaultTransform();
        this.transform_mode = transform_mode != null ? transform_mode : default_transform_mode;
    }

    public static BackItemTransform of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new BackItemTransform(); // дефолт

        Item item = stack.getItem();

        if (item instanceof BowItem) {
            return new BowBackItemTransform();
        } else if (item instanceof CrossbowItem) {
            return new CrossbowBackItemTransform();
        } else if (item instanceof ShieldItem) {
            return new ShieldBackItemTransform();
        } else if (item instanceof TridentItem) {
            return new TridentBackItemTransform();
        } else if (item instanceof ToolItem) {
            return new ToolBackItemTransform();
        } else if (item instanceof SwordItem) {
            return new SwordBackItemTransform();
        } else {
            return new BackItemTransform(); // fallback
        }
    }

    public void applyTransform(MatrixStack matrices) {

        matrices.translate(
                transform.translation.x,
                transform.translation.y,
                transform.translation.z
        );

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(transform.rotation.x));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(transform.rotation.y));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(transform.rotation.z));

        matrices.scale(
                transform.scale.x,
                transform.scale.y,
                transform.scale.z
        );
    }
}

