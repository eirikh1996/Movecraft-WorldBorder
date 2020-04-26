package io.github.eirikh1996.movecraftworldborder;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldBorder;
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
    private Method GET_WORLD;

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
        if (!I18nSupport.initialize()){
            return;
        }
        Plugin movecraft = getServer().getPluginManager().getPlugin("Movecraft");
        //Load up Movecraft
        if (movecraft instanceof Movecraft){
            getLogger().info(I18nSupport.getInternationalizedString("Startup - Movecraft found"));
            movecraftPlugin = (Movecraft) movecraft;
        }
        try {
            GET_WORLD = CraftTranslateEvent.class.getDeclaredMethod("getWorld");
        } catch (NoSuchMethodException e) {
            GET_WORLD = null;
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
        getServer().getPluginManager().registerEvents(this, this);
        UpdateChecker.getInstance().start();

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftTranslate(CraftTranslateEvent event){
        World world = event.getCraft().getW();
        if (GET_WORLD != null) {
            try {
                world = (World) GET_WORLD.invoke(event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                world = event.getCraft().getW();
            }
        }
        BorderData data = worldBorderPlugin.getWorldBorder(world.getName());
        if (data == null){
            return;
        }
        final HitBox newHitBox;
        try {
            Method getNewHitBox = CraftTranslateEvent.class.getDeclaredMethod("getNewHitBox");
            newHitBox = (HitBox) getNewHitBox.invoke(event);
        } catch (Exception e) {
            return;
        }
        for (MovecraftLocation ml : newHitBox){
            if (event.getOldHitBox().contains(ml)){
                continue;
            }
            if (data.insideBorder(ml.toBukkit(world))){
                continue;
            }
            event.setFailMessage(I18nSupport.getInternationalizedString("Translation - Can't go past world border"));
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        World world = event.getCraft().getW();
        BorderData data = Config.Border(world.getName());
        if (data == null){
            return;
        }
        final HitBox newHitBox;
        try {
            Method getNewHitBox = CraftRotateEvent.class.getDeclaredMethod("getNewHitBox");
            newHitBox = (HitBox) getNewHitBox.invoke(event);
        } catch (Exception e) {
            return;
        }
        for (MovecraftLocation ml : newHitBox){
            if (event.getOldHitBox().contains(ml)){
                continue;
            }
            if (data.insideBorder(ml.toBukkit(world))){
                continue;
            }
            event.setFailMessage(I18nSupport.getInternationalizedString("Rotation - Can't go past world border"));
            event.setCancelled(true);
            break;
        }
    }

    public static MWBMain getInstance() {
        return instance;
    }
}
