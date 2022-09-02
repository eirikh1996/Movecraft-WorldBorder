package io.github.eirikh1996.movecraftworldborder.movecraft8;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;
import io.github.eirikh1996.movecraftworldborder.I18nSupport;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Movecraft8Handler implements Listener {
    private final WorldBorder worldBorderPlugin;

    public Movecraft8Handler(WorldBorder worldBorderPlugin) {
        this.worldBorderPlugin = worldBorderPlugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftTranslate(CraftTranslateEvent event){
        World world = event.getWorld();
        BorderData data = worldBorderPlugin.getWorldBorder(world.getName());
        if (data == null){
            return;
        }
        for (MovecraftLocation ml : event.getNewHitBox()){
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
        BorderData data = worldBorderPlugin.getWorldBorder(world.getName());
        if (data == null){
            return;
        }
        for (MovecraftLocation ml : event.getNewHitBox()){
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
}
