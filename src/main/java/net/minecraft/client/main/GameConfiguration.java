package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import java.io.File;
import java.net.Proxy;
import net.minecraft.util.Session;

public record GameConfiguration(UserInformation userInfo, DisplayInformation displayInfo, FolderInformation folderInfo,
                                GameInformation gameInfo, ServerInformation serverInfo) {

    public record DisplayInformation(int width, int height, boolean fullscreen, boolean checkGlErrors) {
    }

    public record FolderInformation(File mcDataDir, File resourcePacksDir, File assetsDir, String assetIndex) {
    }

    public static class GameInformation {
        public final String version;

        public GameInformation(String versionIn) {
            this.version = "1.8.9"; // TODO
        }
    }

    public record ServerInformation(String serverName, int serverPort) {
    }

    public record UserInformation(Session session, PropertyMap userProperties, PropertyMap profileProperties,
                                  Proxy proxy) {
    }
}
