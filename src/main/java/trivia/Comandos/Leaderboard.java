package trivia.Comandos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bson.Document;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {

        List<String> adminRoles = config.getAdminRoles();
        boolean isAdmin = false;

        if (context.getMember() != null)
            for (String adminRole : adminRoles) {
                if (context.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(adminRole)))
                    isAdmin = true;
            }

        List<Document> Usuarios = Database.getAllUsers().into(new ArrayList<>());
        if(Usuarios.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign: Parece que no hay nadie en la lista!**");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        StringBuilder sb = new StringBuilder();

        Usuarios.sort((u1, u2) -> {
            int puntos1 = u1.getInteger("Puntos");
            int puntos2 = u2.getInteger("Puntos");
            return puntos2 - puntos1;
        });

        int i = 1;
        for (Document doc : Usuarios) {
            if(i > 10) break;
            sb.append(i).append(") [ ").append(doc.getString("Username")).append(" ] - ").append(doc.getInteger("Puntos")).append(" puntos\n\n");
            i++;
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setAuthor("Ranking de usuarios del Trivia", null, context.getJDA().getSelfUser().getEffectiveAvatarUrl())
            .setColor(config.getColor())
            .setDescription("""
                    ```css
                    """+sb+"""
                    ```""");

        context.replyEmbeds(embed.build()).setEphemeral(!isAdmin).queue();

    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public String getDescription() {
        return "Muestra el ranking de los usuarios";
    }

    @Override
    public boolean needsPermission() {
        return false;
    }

    @Override
    public boolean onlyInChannel() {
        return false;
    }

    @Override
    public CommandData getSlashData() {
        return null;
    }
}
