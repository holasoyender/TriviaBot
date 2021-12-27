package trivia.Eventos;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import trivia.Config;
import trivia.Database.Database;

import java.util.ArrayList;
import java.util.List;

public class Messages extends ListenerAdapter {

    Config config = new Config();

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {

        if(event.getAuthor().isBot()) return;

        MongoDatabase db = Database.getDatabase();
        FindIterable<Document> Preguntas = db.getCollection("Preguntas").find(new Document("OwnerID", event.getAuthor().getId()));
        if(Preguntas.first() == null)
            return;

        List<Document> sinAcabar = new ArrayList<>();
        for(Document doc : Preguntas) {
            if(doc.getInteger("Paso") != 10)
                sinAcabar.add(doc);
        }

        if(sinAcabar.size() == 0) return;
        Document doc = sinAcabar.get(0);
        int paso = doc.getInteger("Paso");
        if(paso == 0) {

            String Pregunta = event.getMessage().getContentRaw();
            if(Pregunta.length() > 255) {
                event.getChannel().sendMessage("La pregunta no puede tener más de 255 caracteres").queue();
                return;
            }

            if(Pregunta.equalsIgnoreCase("cancel")) {
                EmbedBuilder Embed = new EmbedBuilder()
                        .setAuthor("Creación de pregunta cancelada", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(0xFF4334);
                event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
                db.getCollection("Preguntas").findOneAndDelete(new Document("_id", doc.getObjectId("_id")));
                return;
            }

            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Añadir una pregunta al trivia", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925053012475543612/emoji.png")
                    .setDescription("Bienvenido al proceso para añadir una pregunta al trivia.\n" +
                            "Si tienes alguna duda sobre este proceso, puedes mandarle un MD a <@!"+config.getOwnerID()+">\n" +
                            "La ID de esta pregunta es: `"+doc.getInteger("ID")+"`")
                    .addField("<:done:925056756290289694>  Paso nº 1", "Pregunta:```"+Pregunta+"```", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 2", "Escribe la respuesta correcta a la pregunta `(15 Caracteres máximo)`", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 3", "Escribe separando por comas, 2 o más respuestas incorrectas a la pregunta `(4 Respuestas máximo/15 Caracteres por respuesta)`", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 4", "Define el nivel de dificultad de la pregunta", false)
                    .setFooter("Para cancelar el proceso, escribe \"cancel\"", null);

            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 1).append("Pregunta", Pregunta)));
        }
        if(paso == 1) {

            String Respuesta = event.getMessage().getContentRaw();
            if(Respuesta.length() > 15) {
                event.getChannel().sendMessage("La respuesta no puede tener más de 15 caracteres").queue();
                return;
            }
            if(Respuesta.equalsIgnoreCase("cancel")) {
                EmbedBuilder Embed = new EmbedBuilder()
                        .setAuthor("Creación de pregunta cancelada", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(0xFF4334);
                event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
                db.getCollection("Preguntas").findOneAndDelete(new Document("_id", doc.getObjectId("_id")));
                return;
            }

            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Añadir una pregunta al trivia", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925053012475543612/emoji.png")
                    .setDescription("Bienvenido al proceso para añadir una pregunta al trivia.\n" +
                            "Si tienes alguna duda sobre este proceso, puedes mandarle un MD a <@!"+config.getOwnerID()+">\n" +
                            "La ID de esta pregunta es: `"+doc.getInteger("ID")+"`")
                    .addField("<:done:925056756290289694>  Paso nº 1", "Pregunta:```"+doc.getString("Pregunta")+"```", false)
                    .addField("<:done:925056756290289694>  Paso nº 2", "Respuesta correcta: ```"+Respuesta+"```", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 3", "Escribe separando por comas, 2 o más respuestas incorrectas a la pregunta `(4 Respuestas máximo/15 Caracteres por respuesta)`", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 4", "Define el nivel de dificultad de la pregunta", false)
                    .setFooter("Para cancelar el proceso, escribe \"cancel\"", null);

            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 2).append("Respuesta-correcta", Respuesta)));

        }
        if(paso == 2) {
            String[] Respuestas = event.getMessage().getContentRaw().split(",");
            if(Respuestas.length > 4) {
                event.getChannel().sendMessage("No puedes tener más de 4 respuestas incorrectas!").queue();
                return;
            }
            if(Respuestas.length < 1) {
                event.getChannel().sendMessage("Debes tener al menos 1 respuesta incorrecta!").queue();
                return;
            }

            if(event.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
                EmbedBuilder Embed = new EmbedBuilder()
                        .setAuthor("Creación de pregunta cancelada", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(0xFF4334);
                event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();                db.getCollection("Preguntas").findOneAndDelete(new Document("_id", doc.getObjectId("_id")));
                return;
            }

            List<String> RespuestasList = new ArrayList<>();
            boolean invalid = false;
            for(String s : Respuestas) {
                if(s.length() > 15) {
                    event.getChannel().sendMessage("Las respuestas incorrectas no pueden tener más de 15 caracteres!").queue();
                    invalid = true;
                }
                RespuestasList.add(s.trim());
            }
            if(invalid) return;
            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Añadir una pregunta al trivia", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925053012475543612/emoji.png")
                    .setDescription("Bienvenido al proceso para añadir una pregunta al trivia.\n" +
                            "Si tienes alguna duda sobre este proceso, puedes mandarle un MD a <@!"+config.getOwnerID()+">\n" +
                            "La ID de esta pregunta es: `"+doc.getInteger("ID")+"`")
                    .addField("<:done:925056756290289694>  Paso nº 1", "Pregunta:```"+doc.getString("Pregunta")+"```", false)
                    .addField("<:done:925056756290289694>  Paso nº 2", "Respuesta correcta: ```"+doc.getString("Respuesta-correcta")+"```", false)
                    .addField("<:done:925056756290289694>  Paso nº 3", "Respuestas incorrectas: ```\n"+String.join("\n", RespuestasList)+"```", false)
                    .addField("<:notdone:925056756059627631>  Paso nº 4", "Define el nivel de dificultad de la pregunta", false)
                    .setFooter("Para cancelar el proceso, escribe \"cancel\"", null);

            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 3).append("Respuestas-incorrectas", RespuestasList)));

            event.getChannel().sendMessage("Selecciona el nivel de dificultad de la pregunta:").setActionRow(
                    Button.danger("hard", "Difícil"),
                    Button.primary("medium", "Medio"),
                    Button.success("easy", "Fácil"),
                    Button.secondary("cancel", "Cancelar")
            ).queue();
        }

    }
}
