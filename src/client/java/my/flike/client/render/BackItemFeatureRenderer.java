package my.flike.client.render;

import my.flike.Backslot;
import my.flike.client.BackslotLogic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class BackItemFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public BackItemFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        Backslot.LOG("1",player);
        // Проверяем, не невидим ли игрок для клиента (например, spectator/невидимость).
        if (player.isInvisibleTo(MinecraftClient.getInstance().player)) return;
        //Получаем предмет
        Backslot.LOG("2",player);
        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        if (backStack.isEmpty()) return;

        Backslot.LOG("3",player);
        // Применяем трансформацию
        BackItemTransform backItemTransform = BackItemRenderConfig.getBackItemTransform(backStack);
        backItemTransform.applyTransform(matrices);
        Backslot.LOG("4",player);
        // Рендер ItemStack через ItemRenderer
        MinecraftClient.getInstance().getItemRenderer().renderItem(backStack, backItemTransform.transform_mode, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, player.getWorld(), 0);
    }
}
