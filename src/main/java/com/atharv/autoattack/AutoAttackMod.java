package com.atharv.autoattack;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class AutoAttackMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("AutoAttackShieldBreakerMod Initialized!");
        // Start the auto-attack check in a separate thread
        new Thread(this::attackCycle).start();
    }

    // Main loop to check for attack cooldown and automatically hit
    private void attackCycle() {
        MinecraftClient client = MinecraftClient.getInstance();
        while (true) {
            try {
                Thread.sleep(100); // Check every 100ms
                if (client.player != null && !client.player.isUsingItem()) {
                    autoAttack(client);
                }
            } catch (InterruptedException ignored) {}
        }
    }

    // Method that automatically hits the target once cooldown is finished
    private void autoAttack(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || player.world == null) return;

        // Check cooldown: Only attack if cooldown is finished
        if (player.getAttackCooldownProgress(0.5f) >= 1.0f) {
            // Detect if player is holding an axe
            if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof AxeItem) {
                // Find the nearest target and attack if they are blocking with shield
                for (LivingEntity entity : client.world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10), e -> e != player)) {
                    if (isShieldBlocking(entity)) {
                        // Send packet to attack enemy and disable shield
                        attackEntity(entity);
                        break;
                    }
                }
            }
        }
    }

    // Method to check if the enemy is blocking with a shield
    private boolean isShieldBlocking(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            return player.getEquippedStack(EquipmentSlot.OFFHAND).getItem() instanceof ShieldItem;
        }
        return false;
    }

    // Method to attack the entity (auto-click)
    private void attackEntity(LivingEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player != null) {
            client.interactionManager.attackEntity(player, entity);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
}
