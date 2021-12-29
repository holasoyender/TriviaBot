package trivia.Comandos;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bson.Document;
import trivia.Config;
import trivia.Database.Database;
import trivia.Manager.TriviaManager;
import trivia.Utils.Command;

import java.util.ArrayList;
import java.util.List;

public class Add implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {

        MongoDatabase db = Database.getDatabase();
        FindIterable<Document> Preguntas = db.getCollection("Preguntas").find(new Document("OwnerID", context.getUser().getId()));
        List<Document> sinAcabar = new ArrayList<>();

        for (Document pregunta : Preguntas) {
            if(pregunta.getInteger("Paso") != 10) {
                sinAcabar.add(pregunta);
            }
        }
        if(sinAcabar.size() > 0) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign:  Aún tienes una pregunta sin acabar!**\nRevisa tus MDs y vuelve a intentarlo.");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        List<String> adminRoles = config.getAdminRoles();
        boolean isAdmin = false;

        if (context.getMember() != null)
            for (String adminRole : adminRoles) {
                if (context.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(adminRole)))
                    isAdmin = true;
            }
        final boolean isAdminFinal = isAdmin;

        int id = Database.generateID();
        context.getUser().openPrivateChannel().queue(channel -> {
            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Añadir una pregunta al trivia", null, context.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925053012475543612/emoji.png")
                    .setDescription("Bienvenido al proceso para añadir una pregunta al trivia.\n" +
                            "Escribe un mensaje en el chat para rellenar cada apartado\n" +
                            "Si tienes alguna duda sobre este proceso, puedes mandarle un MD a <@!"+config.getOwnerID()+">\n" +
                            "La ID de esta pregunta es: `"+id+"`")
                    .addField("<:notdone:925056756059627631>  Paso nº 1", "Escribe la pregunta que quieres añadir `(256 Caracteres máximo)`", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 2", "Escribe la respuesta correcta a la pregunta `(15 Caracteres máximo)`", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 3", "Escribe separando por comas, 1 o más respuestas incorrectas a la pregunta `(4 Respuestas máximo/15 Caracteres por respuesta)`", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 4", "Define el nivel de dificultad de la pregunta", false)
                    .setFooter("Para cancelar el proceso, escribe \"cancel\"", null);

            channel.sendMessageEmbeds(Embed.build())
                    .queue(
                    message -> {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(config.getColor())
                                .setDescription("<:externalcontent:830859377463656479>  Te he enviado un **mensaje directo** con las instrucciones para crear una nueva pregunta en el trivia!");
                        context.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        TriviaManager.createTriviaManager(context, message, id, isAdminFinal);
                    },
                    error -> {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  No te he podido mandar un mensaje directo!**");
                        context.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    }
            );
        });

    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Añadir una pregunta al trivia";
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