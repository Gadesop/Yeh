package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class AutoAttackMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        ClientPlayerEntity player = client.player;

        // 1. Screen / World checks
        if (player == null || client.currentScreen != null) return;

        // 2. Weapon Check (Sword or Axe check using registry paths)
        ItemStack mainHandItem = player.getMainHandStack();
        String itemPath = Registries.ITEM.getId(mainHandItem.getItem()).getPath();
        if (!itemPath.contains("sword") && !itemPath.contains("axe")) {
            return;
        }

        // 3. Crosshair Target Check
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) client.crosshairTarget;
            
            // 4. Target filter (Only target other living, alive players)
            if (entityHit.getEntity() instanceof PlayerEntity targetPlayer) {
                if (!targetPlayer.isAlive() || targetPlayer == player) return;

                // 5. Cooldown Check (equivalent to getAttackStrengthScale(0.5f) >= 1.0f)
                float attackProgress = player.getAttackCooldownProgressPerTick(0.5f);
                if (attackProgress >= 1.0f) {
                    
                    // 6. Execute the attack
                    if (client.interactionManager != null) {
                        client.interactionManager.attackEntity(player, targetPlayer);
                        player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        }
    }
}
