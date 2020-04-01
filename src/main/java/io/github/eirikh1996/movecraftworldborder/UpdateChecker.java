package io.github.eirikh1996.movecraftworldborder;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

public class UpdateChecker extends BukkitRunnable implements Listener {
    private static UpdateChecker instance;
    private boolean running = false;

    private UpdateChecker(){}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (checkUpdate() == null){
            return;
        }
        if (!event.getPlayer().hasPermission("mwb.update"))
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().sendMessage(String.format("A new update of Movecraft-WorldBorder (v%s) is available.", checkUpdate()));
                event.getPlayer().sendMessage(String.format("You are currently on v%f", getCurrentVersion()));
                event.getPlayer().sendMessage("Download at: https://dev.bukkit.org/projects/movecraft-worldborder");
            }
        }.runTaskLater(MWBMain.getInstance(), 5);


    }

    @Override
    public void run() {

        MWBMain.getInstance().getLogger().info("Checking for updates");
        new BukkitRunnable() {
            @Override
            public void run() {
                String newVersion = checkUpdate();
                if (newVersion != null){

                    sendUpdateMessage();
                    MWBMain.getInstance().getLogger().info("Version " + newVersion + " is available from https://dev.bukkit.org/projects/movecraft-worldborder");

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

    public void sendUpdateMessage(){
        if (checkUpdate() == null){
            return;
        }
        Bukkit.broadcast(String.format("A new update of Movecraft-WorldBorder (v%s) is available.", checkUpdate()), "mwb.update");
        Bukkit.broadcast(String.format("You are currently on v%f", getCurrentVersion()), "mwb.update");
        Bukkit.broadcast("Download at: https://dev.bukkit.org/projects/movecraft-worldborder", "mwb.update");
    }

    public double getCurrentVersion(){
        return Double.parseDouble(MWBMain.getInstance().getDescription().getVersion());
    }

    public String checkUpdate(){
        try {
            URL url = new URL("https://servermods.forgesvc.net/servermods/files?projectids=342391");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Movecraft-WorldBorder Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final Gson gson = new Gson();
            ArrayList list = gson.fromJson(response, ArrayList.class);
            if (list.size() == 0) {
                MWBMain.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
                return null;
            }
            Map<String, Object> data = (Map<String, Object>) list.get(list.size() - 1);
            String versionName = ((String) data.get("name"));
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            int nv = Integer.parseInt(newVersion.replace(".", ""));
            int cv = Integer.parseInt(MWBMain.getInstance().getDescription().getVersion().replace("v", "").replace(".", ""));
            if (nv > cv) {
                return newVersion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
