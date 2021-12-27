package trivia.Eventos;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import trivia.Config;
import trivia.Database.Database;

import java.util.ArrayList;
import java.util.List;

public class Interactions extends ListenerAdapter {

    private final Config config = new Config();

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {

        String buttonID = event.getComponentId();

        String[] args = buttonID.split(":");
        if(args[0].equals("cmd")) {
            if(args[1].equals("list")) {
                if(event.getGuild() == null) return;

                int Page = Integer.parseInt(args[2]);
                String Action = args[3];
                String ModID = args[4];

                if (!event.getUser().getId().equals(ModID)) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(0xFF4334)
                            .setDescription("**:no_entry_sign:  No puedes usar este botón!**");
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }

                List<Document> allQ = Database.getAllQuestions().into(new ArrayList<>());

                if (allQ.isEmpty()) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(0xFF4334)
                            .setDescription("**:no_entry_sign:  No hay preguntas a mostrar!**");
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }

                List<List<Document>> allQSplit = new ArrayList<>();
                int i = 0;
                while (i < allQ.size()) {
                    allQSplit.add(allQ.subList(i, Math.min(i + 5, allQ.size())));
                    i += 5;
                }

                if (Action.equals("back")) {
                    Page -= 1;
                    if(Page < 0) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  No puedes retroceder más!**");
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }
                }

                if(Action.equals("next")) {
                    Page += 1;
                    if(Page > allQSplit.size() - 1) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  No se puede avanzar más!**");
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }
                }

                int finalPage = Page+1;

                EmbedBuilder Embed = new EmbedBuilder()
                        .setColor(config.getColor())
                        .setAuthor("Lista de preguntas del Trivia", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925119230565810266/emoji.png")
                        .setFooter("Página "+finalPage+" de "+allQSplit.size(), null);

                for (Document lDoc : allQSplit.get(Page)) {
                    Embed.addField(" - Pregunta #"+lDoc.get("ID"), "```"+lDoc.get("Pregunta")+"```\n**<:externalcontent:830859377463656479>  Respuesta correcta**: `"+ lDoc.getString("Respuesta-correcta")+"`\n**:x:  Respuesta(s) incorrecta(s)**: ```\n"+String.join(",\n",lDoc.getList("Respuestas-incorrectas", String.class))+"```\n**:chart_with_upwards_trend:  Dificultad**: `"+lDoc.get("Dificultad")+"`", true);
                }

                event.replyEmbeds(Embed.build()).setEphemeral(false).addActionRow(
                        Button.primary("cmd:list:"+Page+":back:"+event.getUser().getId(), "◀"),
                        Button.primary("cmd:list:"+Page+":next:"+event.getUser().getId(), "▶")
                ).queue();
            }
        }

        MongoDatabase db = Database.getDatabase();
        FindIterable<Document> Preguntas = db.getCollection("Preguntas").find(new Document("OwnerID", event.getUser().getId()));
        if(Preguntas.first() == null) {
            event.reply("Parece que ese botón no está asociado a ninguna pregunta.").setEphemeral(true).queue();
            return;
        }

        List<Document> sinAcabar = new ArrayList<>();
        for(Document doc : Preguntas) {
            if(doc.getInteger("Paso") != 10)
                sinAcabar.add(doc);
        }

        if(sinAcabar.size() == 0) return;
        Document doc = sinAcabar.get(0);
        if(buttonID.equals("hard")) {

            List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);

            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                    .setDescription("Tu pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `"+doc.getInteger("ID")+"`")
                    .addField(":grey_question:  Pregunta:", "```"+doc.getString("Pregunta")+"```", false)
                    .addField(":bulb:  Respuesta correcta:", "```"+doc.getString("Respuesta-correcta")+"```", false)
                    .addField(":x:  Respuesta(s) incorrecta(s)", "```\n"+String.join("\n", respuestas)+"```", false)
                    .addField(":chart_with_upwards_trend:  Dificultad", "```Difícil```", false)
                    .setFooter("Para ver más información sobre esta pregunta usa el comando /preguntas "+doc.getInteger("ID"), null);

            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            event.editMessage("El nivel de dificultad ha sido cambiado a **Difícil**!.").setActionRow(
                    Button.danger("hard", "Difícil").asDisabled(),
                    Button.primary("medium", "Medio").asDisabled(),
                    Button.success("easy", "Fácil").asDisabled(),
                    Button.secondary("cancel", "Cancelar").asDisabled()
            ).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 10).append("Dificultad", "Difícil")));
        }
        if(buttonID.equals("medium")) {

            List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);

            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                    .setDescription("Tu pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `"+doc.getInteger("ID")+"`")
                    .addField(":grey_question:  Pregunta:", "```"+doc.getString("Pregunta")+"```", false)
                    .addField(":bulb:  Respuesta correcta:", "```"+doc.getString("Respuesta-correcta")+"```", false)
                    .addField(":x:  Respuesta(s) incorrecta(s)", "```\n"+String.join("\n", respuestas)+"```", false)
                    .addField(":chart_with_upwards_trend:  Dificultad", "```Media```", false)
                    .setFooter("Para ver más información sobre esta pregunta usa el comando /preguntas "+doc.getInteger("ID"), null);

            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            event.editMessage("El nivel de dificultad ha sido cambiado a **Media**!.").setActionRow(
                    Button.danger("hard", "Difícil").asDisabled(),
                    Button.primary("medium", "Medio").asDisabled(),
                    Button.success("easy", "Fácil").asDisabled(),
                    Button.secondary("cancel", "Cancelar").asDisabled()
            ).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 10).append("Dificultad", "Media")));
        }
        if(buttonID.equals("easy")) {

            List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);

            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                    .setDescription("Tu pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `"+doc.getInteger("ID")+"`")
                    .addField(":grey_question:  Pregunta:", "```"+doc.getString("Pregunta")+"```", false)
                    .addField(":bulb:  Respuesta correcta:", "```"+doc.getString("Respuesta-correcta")+"```", false)
                    .addField(":x:  Respuesta(s) incorrecta(s)", "```\n"+String.join("\n", respuestas)+"```", false)
                    .addField(":chart_with_upwards_trend:  Dificultad", "```Fácil```", false)
                    .setFooter("Para ver más información sobre esta pregunta usa el comando /preguntas "+doc.getInteger("ID"), null);

            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            event.editMessage("El nivel de dificultad ha sido cambiado a **Fácil**!.").setActionRow(
                    Button.danger("hard", "Difícil").asDisabled(),
                    Button.primary("medium", "Medio").asDisabled(),
                    Button.success("easy", "Fácil").asDisabled(),
                    Button.secondary("cancel", "Cancelar").asDisabled()
            ).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 10).append("Dificultad", "Fácil")));
        }
        if(buttonID.equals("cancel")) {
            EmbedBuilder Embed = new EmbedBuilder()
                    .setAuthor("Creación de pregunta cancelada", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setColor(0xFF4334);
            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            db.getCollection("Preguntas").findOneAndDelete(new Document("_id", doc.getObjectId("_id")));
        }
    }
}