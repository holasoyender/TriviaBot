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
                SubmitChannelID: "923548627706736701"       # La ID del canal donde se enviarán las preguntas a revisión (Obligatorio)
                
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
        if(config.get("Token") == null || config.get("Token") == "") {
            System.out.println("No se ha encontrado el token en la configuración.");
            System.exit(0);
        }
        return config.get("Token").toString();
    }
    public Color getColor() {
        if(config.get("EmbedColor") == null || config.get("EmbedColor") == "") return Color.decode("#71FFB2");
        return Color.decode(config.get("EmbedColor").toString());
    }
    public List<String> getAdminRoles() {
        if (config.get("AdminRoles") == null || config.get("AdminRoles") == "") {
            System.out.println("No se ha encontrado el(los) rol(es) de administrador en la configuración.");
            System.exit(0);
        }

        return (List<String>) config.get("AdminRoles");
    }
    public List<String> getChannelIds() {
        if(config.get("ChannelIDs") == null || config.get("ChannelIDs") == "") {
            System.out.println("No se ha encontrado el(los) canal(es) de juego en la configuración.");
            System.exit(0);
        }

        return (List<String>) config.get("ChannelIDs");
    }

    public String getOwnerID() {
        return config.get("OwnerID").toString();
    }
    public String getMongoDB() {
        if(config.get("MongoDB") == null || config.get("MongoDB") == "") {
            System.out.println("No se ha encontrado la URL de la base de datos en la configuración.");
            System.exit(0);
        }

        return config.get("MongoDB").toString();
    }
    public String getSubmitChannelID() {
        if(config.get("SubmitChannelID") == null || config.get("SubmitChannelID") == "") {
            System.out.println("No se ha encontrado el canal de envío de preguntas en la configuración.");
            System.exit(0);
        }

        return config.get("SubmitChannelID").toString();
    }

}
