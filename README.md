# 🎙️ VelocityPAPI
Use PAPI Placeholders in velocity with zero config, setup or messing around!

## 💻 How to use
1. Add the plugin to Velocity
2. Add the plugin to all backend servers
3. Reboot everything
4. Profit!

***Developers will need to implement VelocityAPI in their code***

## 📑 API
    CompletableFuture<String> VelocityPAPI.setPlaceholders(UUID uuid, String message)
    CompletableFuture<String> VelocityPAPI.setPlaceholders(Player player, String message)