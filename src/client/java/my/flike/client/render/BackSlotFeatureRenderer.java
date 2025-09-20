package my.flike.client.render;

import my.flike.client.BackslotLogic;
import my.flike.client.config.BackItemRenderConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class BackSlotFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public BackSlotFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        // Проверяем, не невидим ли игрок для клиента (например, spectator/невидимость).
        if (player.isInvisibleTo(MinecraftClient.getInstance().player)) return;

        //Получаем предмет
        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        if (backStack.isEmpty()) return;
        BackItemTransform backItemTransform = BackItemRenderConfig.getBackItemTransform(backStack);
        if (!backItemTransform.enabled) return;

        // обязательно сохранить/восстановить стек матриц
        matrices.push();
        try {
        // Применяем трансформацию
            backItemTransform.applyTransform(matrices);
            // Рендер ItemStack через ItemRenderer
            MinecraftClient.getInstance().getItemRenderer().renderItem(
                    backStack,
                    backItemTransform.transform_mode,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    player.getWorld(),
                    0
            );
        } finally {
            matrices.pop();
        }
    }
}
