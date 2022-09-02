package io.github.eirikh1996.movecraftworldborder;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class I18nSupport {
    private static Properties languageFile;

    public static boolean initialize(Plugin main){
        File langFile = new File(main.getDataFolder().getAbsolutePath() + "/localisation/mwblang_" + Settings.locale + ".properties");
        try {
        languageFile = new Properties();
        languageFile.load(new FileInputStream(langFile));
        } catch (Exception e){
            e.printStackTrace();
            main.getServer().getPluginManager().disablePlugin(main);
            return false;
        }
        return true;
    }
    public static String getInternationalizedString(String key){
        return languageFile.getProperty(key) != null ? languageFile.getProperty(key) : key;
    }
}
