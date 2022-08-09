package com.james090500.VelocityPAPI.velocity.api;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.james090500.VelocityPAPI.shared.VPObject;
import com.james090500.VelocityPAPI.velocity.VelocityMain;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VelocityPAPI {

    private static VelocityMain velocityMain;

    /**
     * Loads the API
     * @param velocityMain
     */
    public static void load(VelocityMain velocityMain) {
        VelocityPAPI.velocityMain = velocityMain;
    }

    private static String getMessageKey() {
        String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        StringBuilder builder = new StringBuilder();
        Random rnd = new SecureRandom();
        for (int i = 0; i < 12; i++) {
            builder.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return builder.toString();
    }

    /**
     * Converts placeholders to a real value
     * @param uuid The player to reference
     * @param message The place holder
     * @return A CompletableFuture as this can sometimes take a few seconds
     */
    public static CompletableFuture<String> setPlaceholders(UUID uuid, String message) {
        return performPlaceholderReplacement(velocityMain.getServer().getPlayer(uuid).get(), message);
    }

    /**
     * Converts placeholders to a real value
     * @param player The player to reference
     * @param message The place holder
     * @return A CompletableFuture as this can sometimes take a few seconds
     */
    public static CompletableFuture<String> setPlaceholders(Player player, String message) {
        return performPlaceholderReplacement(player, message);
    }

    /**
     * Does the actual hard core coding replacement
     * @param player The player to reference
     * @param message The place holder
     * @return A CompletableFuture as this can sometimes take a few seconds
     */
    private static CompletableFuture<String> performPlaceholderReplacement(Player player, String message) {
        CompletableFuture<String> futureResult = new CompletableFuture<>();
        velocityMain.getServer().getScheduler().buildTask(velocityMain, () -> {
            // Build message object
            final String messageKey = getMessageKey();
            VPObject vpObject = new VPObject(messageKey, player.getUniqueId(), message);

            // Store the object
            VelocityMain.getPendingReplacements().put(vpObject.getMessageKey(), vpObject);

            // Send Packet
            ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF(vpObject.getMessageKey());
            buf.writeUTF(vpObject.getUuid().toString());
            buf.writeUTF(vpObject.getMessage());
            player.getCurrentServer().get().sendPluginMessage(VelocityMain.getStartChannel(), buf.toByteArray());

            // Wait for result by looping until it's found
            CompletableFuture<VPObject> vpObjectCompletableFuture = new CompletableFuture<>();
            ScheduledTask pendingTask = velocityMain.getServer().getScheduler().buildTask(velocityMain, () -> {
                //Check we have something to loop
                if(VelocityMain.getPendingReplacements().containsKey(vpObject.getMessageKey())) {
                    //Look for a result and return it if found
                    VPObject pendingObject = VelocityMain.getPendingReplacements().get(vpObject.getMessageKey());
                    if (pendingObject.getResult() != null) vpObjectCompletableFuture.complete(pendingObject);
                } else {
                    //Fail the completable
                    vpObjectCompletableFuture.complete(null);
                }
            }).repeat(1, TimeUnit.SECONDS).schedule();

            //Once the loop has finished
            vpObjectCompletableFuture.thenAccept(completedObject -> {
                //Stop looping
                pendingTask.cancel();

                //See if it failed
                if(completedObject != null) {
                    // Complete Future
                    VelocityMain.getPendingReplacements().remove(completedObject.getMessageKey());
                    futureResult.complete(completedObject.getResult());
                } else {
                    velocityMain.getLogger().error("Failed to convert placeholders for " + vpObject);
                    futureResult.complete("");
                }
            });
        }).schedule();
        return futureResult;
    }

}
