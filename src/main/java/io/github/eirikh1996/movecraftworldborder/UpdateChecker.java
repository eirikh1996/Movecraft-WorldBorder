package io.github.eirikh1996.movecraftworldborder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker extends BukkitRunnable {
    private static UpdateChecker instance;
    private boolean running = false;

    private UpdateChecker(){}
    @Override
    public void run() {
        final double currentVersion = getCurrentVersion();
        final double newVersion = checkUpdate(currentVersion);
        MWBMain.getInstance().getLogger().info("Checking for updates");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (newVersion > currentVersion){

                    for (Player p : Bukkit.getOnlinePlayers()){
                        sendUpdateMessage(p);
                    }
                    return;
                }
                MWBMain.getInstance().getLogger().info("You are up to date");
            }
        }.runTaskLaterAsynchronously(MWBMain.getInstance(), 100);
    }

    public static void initialize(){
        instance = new UpdateChecker();
    }

    public static UpdateChecker getInstance() {
        return instance;
    }

    public void start(){
        if (running)
            return;
        runTaskTimerAsynchronously(MWBMain.getInstance(), 0, 100000000);
        running = true;
    }

    public void sendUpdateMessage(Player player){
        if (!player.hasPermission("mwb.update"))
            return;
        player.sendMessage(String.format("A new update of Movecraft-WorldBorder (v%f) is available.", checkUpdate(getCurrentVersion())));
        player.sendMessage(String.format("You are currently on v%f", getCurrentVersion()));
        player.sendMessage("Download at: ");
    }

    public double getCurrentVersion(){
        return Double.parseDouble(MWBMain.getInstance().getDescription().getVersion());
    }

    public double checkUpdate(double currentVersion){
        try {
            URL url = new URL("https://servermods.forgesvc.net/servermods/files?projectids=342391");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Movecraft-WorldBorder Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final JSONArray jsonArray = (JSONArray) JSONValue.parse(response);
            if (jsonArray.size() == 0) {
                MWBMain.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
                return currentVersion;
            }
            JSONObject jsonObject = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            String versionName = ((String) jsonObject.get("name"));
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            return Double.parseDouble(newVersion);
        } catch (Exception e) {
            e.printStackTrace();
            return currentVersion;
        }
    }
}
