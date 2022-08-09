package com.james090500.VelocityPAPI.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.james090500.VelocityPAPI.shared.VPObject;
import com.james090500.VelocityPAPI.velocity.api.VelocityPAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChannelRegisterEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "velocitypapi", name = "VelocityPAPI", version = "1.0.0", description = "Placeholders everywhere", authors = { "james095000" })
public class VelocityMain {

    @Getter private final ProxyServer server;
    @Getter private final Logger logger;
    private final Path dataDirectory;

    private static final String CHANNEL = "VelocityPAPI";
    @Getter private static final ChannelIdentifier startChannel = MinecraftChannelIdentifier.from(CHANNEL + ":start");
    @Getter private static final ChannelIdentifier endChannel = MinecraftChannelIdentifier.from(CHANNEL + ":end");
    @Getter private static HashMap<String, VPObject> pendingReplacements = new HashMap<>();

    @Inject
    public VelocityMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        VelocityPAPI.load(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(startChannel);
        server.getChannelRegistrar().register(endChannel);
        server.getEventManager().register(this, this);

        //Loop old replacements
        server.getScheduler().buildTask(this, () -> {
            pendingReplacements.forEach((key, vpObject) -> {
                if(vpObject.getCreatedAt() + 20000 < System.currentTimeMillis()) {
                    pendingReplacements.remove(key);
                }
            });
        }).delay(1, TimeUnit.MINUTES).schedule();
    }

    @Subscribe
    public void onPluginMessageReceived(PluginMessageEvent event) {
        //Make sure the channel is correct
        if(!event.getSource().equals(endChannel)) return;

        //Make sure its not from a player
        if(event.getSource() instanceof Player) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String messageKey = in.readUTF();
        String result = in.readUTF();

        VPObject vpObject = getPendingReplacements().get(messageKey);
        vpObject.setResult(result);
        getPendingReplacements().put(messageKey, vpObject);
    }

}
