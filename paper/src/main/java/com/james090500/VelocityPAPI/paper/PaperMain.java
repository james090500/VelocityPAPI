package com.james090500.VelocityPAPI.paper;

import com.james090500.VelocityPAPI.shared.VPObject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

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

        VPObject vpObject = VPObject.readPending(message);
        vpObject.setResult(PlaceholderAPI.setPlaceholders(player, vpObject.getMessage()));

        player.sendPluginMessage(this, endChannel, vpObject.writeComplete());
    }
}
