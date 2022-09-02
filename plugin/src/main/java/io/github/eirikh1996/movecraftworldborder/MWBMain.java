package io.github.eirikh1996.movecraftworldborder;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldBorder;
import io.github.eirikh1996.movecraftworldborder.movecraft7.Movecraft7Handler;
import io.github.eirikh1996.movecraftworldborder.movecraft8.Movecraft8Handler;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MWBMain extends JavaPlugin implements Listener {
    private static MWBMain instance;
    private Movecraft movecraftPlugin;
    private WorldBorder worldBorderPlugin;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        UpdateChecker.initialize();
        MWBMain.getInstance().saveResource("localisation/mwblang_en.properties", false);
        Settings.locale = getConfig().getString("locale", "en");
        if (!I18nSupport.initialize(this)){
            return;
        }
        Plugin movecraft = getServer().getPluginManager().getPlugin("Movecraft");
        //Load up Movecraft
        boolean movecraft8 = false;
        if (movecraft instanceof Movecraft){
            getLogger().info(I18nSupport.getInternationalizedString("Startup - Movecraft found"));
            movecraftPlugin = (Movecraft) movecraft;
            getLogger().info(I18nSupport.getInternationalizedString("Startup - Movecraft detected") + " " + movecraftPlugin.getDescription().getVersion());
        }
        //worldborder
        Plugin wb = getServer().getPluginManager().getPlugin("WorldBorder");
        if (wb instanceof WorldBorder){
            getLogger().info(I18nSupport.getInternationalizedString("Startup - WorldBorder found"));
            worldBorderPlugin = (WorldBorder) wb;
        }

        //If Movecraft was not found or is disabled, disable this plugin
        if (movecraftPlugin == null || !movecraftPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalizedString("Startup - Movecraft not found or disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //If WorldBorder was not found or is disabled, disable this plugin
        if (worldBorderPlugin == null || !worldBorderPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalizedString("Startup - WorldBorder not found or disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Listener listener;
        try {
            Class.forName("net.countercraft.movecraft.craft.BaseCraft");
            listener = new Movecraft8Handler(worldBorderPlugin);
        } catch (ClassNotFoundException e) {
            listener = new Movecraft7Handler(worldBorderPlugin);
        }
        getServer().getPluginManager().registerEvents(listener, this);
        UpdateChecker.getInstance().start();

    }



    public static MWBMain getInstance() {
        return instance;
    }
}
