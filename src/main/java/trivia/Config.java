package trivia;

import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Config {

    private final Map<String, Object> config;

    public Config() {
        Yaml yaml = new Yaml();
        File file = new File("config.yml");

        String defaultConfig = """
                #  ████████╗ ██████╗  ██╗ ██╗   ██╗ ██╗  █████╗
                #  ╚══██╔══╝ ██╔══██╗ ██║ ██║   ██║ ██║ ██╔══██╗
                #     ██║    ██████╔╝ ██║ ╚██╗ ██╔╝ ██║ ███████║
                #     ██║    ██╔══██╗ ██║  ╚████╔╝  ██║ ██╔══██║
                #     ██║    ██║  ██║ ██║   ╚██╔╝   ██║ ██║  ██║
                #     ╚═╝    ╚═╝  ╚═╝ ╚═╝    ╚═╝    ╚═╝ ╚═╝  ╚═╝
                 
                Token: "T0K3N"    # El token de tu bot de Discord (Obligatorio)
                MongoDB: "mongodb://localhost:27017/Trivia"    # La URL de tu base de datos en MongoDB (Obligatorio)
                
                AdminRoles: ["901213736784773222"] # Entre comillas la(s) ID(s) de los roles que pueden administrar el bot (Obligatorio)
                OwnerID: "396683727868264449"    # La ID de usuario del propietario del bot (Obligatorio)
                ChannelIDs: ["759535966179426314"]     # La ID del canal donde se podrá jugar al Trivia (Obligatorio)
                
                EmbedColor: "#71FFB2"    # El color de los embeds enviados por el bot (Opcional)
                """;

        if (!file.exists()) {
            try {
                Files.writeString(file.toPath(), defaultConfig);
                System.out.println("No se ha encontrado el archivo de configuración, se ha creado uno por defecto.");
                System.exit(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (InputStream is = new FileInputStream(file)) {
            this.config = yaml.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getToken() {
        return config.get("Token").toString();
    }
    public Color getColor() {
        if(config.get("EmbedColor") == null || config.get("EmbedColor") == "") return Color.decode("#71FFB2");
        return Color.decode(config.get("EmbedColor").toString());
    }
    public List<String> getAdminRoles() {
        return (List<String>) config.get("AdminRoles");
    }
    public List<String> getChannelIds() {
        return (List<String>) config.get("ChannelIDs");
    }

    public String getOwnerID() {
        return config.get("OwnerID").toString();
    }
    public String getMongoDB() {
        return config.get("MongoDB").toString();
    }

}
