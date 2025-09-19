package my.flike.client.render;

import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;


public class BackItemTransform {

    private static final Transformation default_transform = new Transformation(
            new Vector3f(0f, 0.2f, 0.16f),  // translate X, Y, Z
            new  Vector3f(0f, 0f, 270f),    // rotate X, Y, Z
            new  Vector3f(1f, 1f, 1f)       // scale X, Y, Z
    );

    private static final ModelTransformationMode default_transform_mode = ModelTransformationMode.FIXED;

    public Transformation transform = default_transform;

    public ModelTransformationMode transform_mode = default_transform_mode;

    BackItemTransform() {}

    BackItemTransform(Transformation transform){
        this.transform = transform;
    }

    BackItemTransform(ModelTransformationMode transform_mode){
        this.transform_mode = transform_mode;
    }

     BackItemTransform(Transformation transform, ModelTransformationMode transform_mode){
        this.transform = transform;
        this.transform_mode = transform_mode;
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

//        if (item instanceof ShieldItem) {
//            // Щит — вертикально, прижат
//            matrices.translate(0.0, 0.25, 0.32);
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180));
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(180));
//            matrices.scale(1.15f, 1.15f, 1.15f);
//        } else if (item instanceof TridentItem) {
//            // Трезубец — диагональ
//            matrices.translate(0.05, 0.38, 0.34);
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(120));
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(200));
//            matrices.scale(0.95f, 0.95f, 0.95f);
//        } else if (item instanceof SwordItem || item instanceof AxeItem) { // фиктивный интерфейс, если реализован у модовых предметов
//            // Инструмент — под углом, как топор/меч
//            matrices.translate(-0.11, 0.22, 0.27);
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(200));
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(190));
//            matrices.scale(0.90f, 0.90f, 0.90f);
//        } else if (item instanceof BowItem) {
//            // Лук — боком, чуть выше
//            matrices.translate(0.12, 0.21, 0.31);
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180));
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(245));
//            matrices.scale(0.97f, 0.97f, 0.97f);
//        } else if (item instanceof ToolItem) {
//            // Остальные стандартные инструменты
//            matrices.translate(-0.13, 0.19, 0.27);
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(210));
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(195));
//            matrices.scale(0.85f, 0.85f, 0.85f);
//        } else {
//            // Просто ItemStack: рюкзак, предмет, в т.ч. модовый
//            matrices.translate(0.0, 0.26, 0.29);
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180));
//            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(180));
//            matrices.scale(0.95f, 0.95f, 0.95f);
//        }
    }
}
