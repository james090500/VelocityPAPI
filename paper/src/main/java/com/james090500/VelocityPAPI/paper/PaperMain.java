package com.james090500.VelocityPAPI.paper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PaperMain extends JavaPlugin implements PluginMessageListener {

    private static final String CHANNEL = "VelocityPAPI";
    private static final String startChannel = CHANNEL + ":start";
    private static final String endChannel = CHANNEL + ":end";

    @Override
    public void onEnable() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        //Register channels
        this.getServer().getMessenger().registerIncomingPluginChannel(this, startChannel, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, endChannel);
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        //Make sure the channel is correct
        if(!channel.equals(startChannel)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        UUID uuid = UUID.fromString(in.readUTF());
        String placeholders = in.readUTF();

        String convertedText = PlaceholderAPI.setPlaceholders(player, placeholders);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(convertedText);

        player.sendPluginMessage(this, endChannel, out.toByteArray());
    }
}
