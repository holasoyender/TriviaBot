package trivia.Eventos;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import trivia.Config;
import trivia.Database.Database;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Interactions extends ListenerAdapter {

    private final Config config = new Config();

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {

        String buttonID = event.getComponentId();

        String[] args = buttonID.split(":");
        if (args[0].equals("cmd")) {
            switch (args[1]) {
                case "list" -> {
                    if (event.getGuild() == null) return;

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
                        if (Page < 0) {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setColor(0xFF4334)
                                    .setDescription("**:no_entry_sign:  No puedes retroceder más!**");
                            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                            return;
                        }
                    }

                    if (Action.equals("next")) {
                        Page += 1;
                        if (Page > allQSplit.size() - 1) {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setColor(0xFF4334)
                                    .setDescription("**:no_entry_sign:  No se puede avanzar más!**");
                            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                            return;
                        }
                    }

                    int finalPage = Page + 1;

                    EmbedBuilder Embed = new EmbedBuilder()
                            .setColor(config.getColor())
                            .setAuthor("Lista de preguntas del Trivia", null, event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925119230565810266/emoji.png")
                            .setFooter("Página " + finalPage + " de " + allQSplit.size(), null);

                    for (Document lDoc : allQSplit.get(Page)) {
                        Embed.addField(" - Pregunta #" + lDoc.get("ID"), "```" + lDoc.get("Pregunta") + "```\n**<:externalcontent:830859377463656479>  Respuesta correcta**: `" + lDoc.getString("Respuesta-correcta") + "`\n**:x:  Respuesta(s) incorrecta(s)**: ```\n" + String.join(",\n", lDoc.getList("Respuestas-incorrectas", String.class)) + "```\n**:chart_with_upwards_trend:  Dificultad**: `" + lDoc.get("Dificultad") + "`", true);
                    }

                    event.replyEmbeds(Embed.build()).setEphemeral(false).addActionRow(
                            Button.primary("cmd:list:" + Page + ":back:" + event.getUser().getId(), "◀"),
                            Button.primary("cmd:list:" + Page + ":next:" + event.getUser().getId(), "▶")
                    ).queue();
                    return;
                }
                case "play" -> {
                    String ModID = args[2];
                    long QuestionID = Long.parseLong(args[3]);
                    String type = args[4];

                    if (!event.getUser().getId().equals(ModID)) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  No puedes usar este botón!**");
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }
                    Document Question = Database.getTriviaByID(QuestionID);
                    if (Question == null) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  Parece que no puedo encontrar ese trivia!**");
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }

                    if (type.equals("good")) {
                        String Dificultad = Question.getString("Dificultad");
                        String footerImg = "https://i.imgur.com/XlARsD6.png";
                        int Puntos = 0;

                        if (Dificultad.equals("Fácil")) {
                            footerImg = "https://i.imgur.com/XlARsD6.png";
                            Puntos = 1;
                        }

                        if (Dificultad.equals("Media")) {
                            footerImg = "https://i.imgur.com/521SKXF.png";
                            Puntos = 2;
                        }

                        if (Dificultad.equals("Difícil")) {
                            footerImg = "https://i.imgur.com/JD0MDyW.png";
                            Puntos = 3;
                        }
                        Database.addUserPoints(event.getUser(), Puntos);
                        Database.addCompletedTrivia(event.getUser(), QuestionID);
                        Database.addCorrectTrivia(event.getUser());

                        EmbedBuilder Embed = new EmbedBuilder()
                                .setColor(0x6BF47E)
                                .setAuthor("¡ Respuesta correcta de " + event.getUser().getName() + " !", null, event.getJDA().getSelfUser().getAvatarUrl())
                                .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925517686497247293/emoji..png")
                                .setDescription("```CSS\n" + Question.getString("Pregunta") + "```")
                                .addField("Puntos recibidos", "**" + Puntos + "** puntos", true)
                                .addField("Puntos totales", "**" + Database.getUserPoints(event.getUser()) + "** puntos", true)
                                .setFooter("Dificultad: " + Dificultad + "  |  ID: " + Question.getInteger("ID"), footerImg);

                        event.editComponents().setEmbeds(Embed.build()).queue();

                        return;
                    }
                    if (type.equals("bad")) {
                        String Dificultad = Question.getString("Dificultad");
                        String footerImg = "https://i.imgur.com/XlARsD6.png";

                        if (Dificultad.equals("Fácil")) {
                            footerImg = "https://i.imgur.com/XlARsD6.png";
                        }

                        if (Dificultad.equals("Media")) {
                            footerImg = "https://i.imgur.com/521SKXF.png";
                        }

                        if (Dificultad.equals("Difícil")) {
                            footerImg = "https://i.imgur.com/JD0MDyW.png";
                        }
                        Database.addUserPoints(event.getUser(), -1);
                        Database.addCompletedTrivia(event.getUser(), QuestionID);
                        Database.addIncorrectTrivia(event.getUser());

                        EmbedBuilder Embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setAuthor("¡ Respuesta incorrecta de " + event.getUser().getName() + " !", null, event.getJDA().getSelfUser().getAvatarUrl())
                                .setThumbnail("https://cdn.discordapp.com/attachments/726429501827055636/925494387457265744/emoji..png")
                                .setDescription("```CSS\n" + Question.getString("Pregunta") + "```")
                                .addField("Puntos recibidos", "**-1** puntos", true)
                                .addField("Puntos totales", "**" + Database.getUserPoints(event.getUser()) + "** puntos", true)
                                .setFooter("Dificultad: " + Dificultad + "  |  ID: " + Question.getInteger("ID"), footerImg);

                        event.editComponents().setEmbeds(Embed.build()).queue();
                        return;
                    }
                }
                case "deny" -> {

                    List<String> adminRoles = config.getAdminRoles();
                    boolean isAdmin = false;

                    if (event.getMember() != null)
                        for (String adminRole : adminRoles) {
                            if (event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(adminRole)))
                                isAdmin = true;
                        }

                    if (!isAdmin) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  No tienes permisos para usar este comando!**");
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }

                    String QueryID = args[2];
                    Document Trivia = Database.getTriviaByID(Long.parseLong(QueryID));
                    if (Trivia == null) {
                        event.reply("No se encontró la trivia con el ID: " + QueryID).setEphemeral(true).queue();
                        return;
                    }

                    List<String> respuestas = Trivia.getList("Respuestas-incorrectas", String.class);
                    EmbedBuilder Embed = new EmbedBuilder()
                            .setColor(0xFF4334)
                            .setAuthor("Pregunta eliminada del trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/726429501827055636/925494387457265744/emoji..png")
                            .setDescription("La pregunta se ha eliminado del trivia correctamente.\nLa ID de esta pregunta era: `" + Trivia.getInteger("ID") + "`")
                            .addField(":grey_question:  Pregunta:", "```" + Trivia.getString("Pregunta") + "```", false)
                            .addField(":bulb:  Respuesta correcta:", "```" + Trivia.getString("Respuesta-correcta") + "```", false)
                            .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                            .addField(":chart_with_upwards_trend:  Dificultad", "```Difícil```", false);

                    event.editComponents().setEmbeds(Embed.build()).queue();

                    User user = event.getJDA().getUserById(Trivia.getString("OwnerID"));
                    if(user != null) {
                        user.openPrivateChannel().queue(channel -> {
                            EmbedBuilder Embed2 = new EmbedBuilder()
                                    .setColor(0xFF4334)
                                    .setAuthor("Tu pregunta no ha sido aprobada", null, event.getJDA().getSelfUser().getAvatarUrl())
                                    .setThumbnail("https://cdn.discordapp.com/attachments/726429501827055636/925494387457265744/emoji..png")
                                    .setDescription("Tu pregunta con ID `" + Trivia.getInteger("ID") + "` ha sido eliminada del Trivia.");

                            channel.sendMessageEmbeds(Embed2.build())
                                    .queue(
                                            message -> {},
                                            error -> {}
                                    );
                        });
                    }

                    Database.getDatabase().getCollection("Preguntas").deleteOne(eq("ID", Trivia.getInteger("ID")));
                    return;
                }
                case "approve" -> {

                    List<String> adminRoles = config.getAdminRoles();
                    boolean isAdmin = false;

                    if (event.getMember() != null)
                        for (String adminRole : adminRoles) {
                            if (event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(adminRole)))
                                isAdmin = true;
                        }

                    if (!isAdmin) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(0xFF4334)
                                .setDescription("**:no_entry_sign:  No tienes permisos para usar este comando!**");
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }

                    String QueryID = args[2];
                    Document Trivia = Database.getTriviaByID(Long.parseLong(QueryID));
                    if (Trivia == null) {
                        event.reply("No se encontró la trivia con el ID: " + QueryID).setEphemeral(true).queue();
                        return;
                    }

                    List<String> respuestas = Trivia.getList("Respuestas-incorrectas", String.class);
                    EmbedBuilder Embed = new EmbedBuilder()
                            .setColor(config.getColor())
                            .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                            .setDescription("La pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `" + Trivia.getInteger("ID") + "`")
                            .addField(":grey_question:  Pregunta:", "```" + Trivia.getString("Pregunta") + "```", false)
                            .addField(":bulb:  Respuesta correcta:", "```" + Trivia.getString("Respuesta-correcta") + "```", false)
                            .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                            .addField(":chart_with_upwards_trend:  Dificultad", "```Difícil```", false);

                    event.editComponents().setEmbeds(Embed.build()).queue();

                    User user = event.getJDA().getUserById(Trivia.getString("OwnerID"));
                    if(user != null) {
                        user.openPrivateChannel().queue(channel -> {
                            EmbedBuilder Embed2 = new EmbedBuilder()
                                    .setColor(config.getColor())
                                    .setAuthor("Tu pregunta ha sido aprobada", null, event.getJDA().getSelfUser().getAvatarUrl())
                                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                                    .setDescription("Tu pregunta con ID `" + Trivia.getInteger("ID") + "` ha sido añadida al Trivia.");

                            channel.sendMessageEmbeds(Embed2.build())
                                    .queue(
                                            message -> {},
                                            error -> {}
                                    );
                        });
                    }

                    Database.getDatabase().getCollection("Preguntas").findOneAndUpdate(eq("ID", Trivia.getInteger("ID")), new Document("$set", new Document("Revisada", true)));
                    return;
                }

                default -> event.reply("Interacción desconocida!.").setEphemeral(true).queue();
            }
        }

        MongoDatabase db = Database.getDatabase();
        FindIterable<Document> Preguntas = db.getCollection("Preguntas").find(new Document("OwnerID", event.getUser().getId()));
        if (Preguntas.first() == null) {
            event.reply("Parece que ese botón no está asociado a ninguna pregunta.").setEphemeral(true).queue();
            return;
        }

        List<Document> sinAcabar = new ArrayList<>();
        for (Document doc : Preguntas) {
            if (doc.getInteger("Paso") != 10)
                sinAcabar.add(doc);
        }

        if (sinAcabar.size() == 0) return;
        Document doc = sinAcabar.get(0);
        boolean aprobar = doc.getBoolean("Revisada");
        if (buttonID.equals("hard")) {

            List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);
            EmbedBuilder Embed;
            if (aprobar) {

                Embed = new EmbedBuilder()
                        .setColor(config.getColor())
                        .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                        .setDescription("Tu pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                        .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                        .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                        .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                        .addField(":chart_with_upwards_trend:  Dificultad", "```Difícil```", false)
                        .setFooter("Para ver más información sobre esta pregunta usa el comando /preguntas " + doc.getInteger("ID"), null);

            } else {

                Embed = new EmbedBuilder()
                        .setColor(0xFAC42A)
                        .setAuthor("Pregunta añadida a revisión!", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925715477697814568/emoji.png")
                        .setDescription("Tu pregunta se ha añadido a revisión\nTe mandaremos un mensaje por MD cuando tu pregunta sea aprobada/denegada.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                        .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                        .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                        .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                        .addField(":chart_with_upwards_trend:  Dificultad", "```Difícil```", false);

                TextChannel Channel = (TextChannel) event.getJDA().getGuildChannelById(config.getSubmitChannelID());
                if (Channel != null) {
                    EmbedBuilder Embed2 = new EmbedBuilder()
                            .setColor(0xFAC42A)
                            .setAuthor("Nueva pregunta añadida a revisión", null, event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925715477697814568/emoji.png")
                            .setDescription("El usuario " + event.getUser().getAsMention() + " (" + event.getUser().getId() + ") ha añadido esta pregunta a revisión.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                            .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                            .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                            .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                            .addField(":chart_with_upwards_trend:  Dificultad", "```Difícil```", false)
                            .setFooter("Haz click en los botones para aprobar o denegar la pregunta.", null);

                    Channel.sendMessageEmbeds(Embed2.build()).setActionRow(
                            Button.danger("cmd:deny:" + doc.getInteger("ID"), "Denegar"),
                            Button.success("cmd:approve:" + doc.getInteger("ID"), "Aprobar")
                    ).queue();
                }
            }
            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            event.editMessage("El nivel de dificultad ha sido cambiado a **Difícil**!.").setActionRow(
                    Button.danger("hard", "Difícil").asDisabled(),
                    Button.primary("medium", "Medio").asDisabled(),
                    Button.success("easy", "Fácil").asDisabled(),
                    Button.secondary("cancel", "Cancelar").asDisabled()
            ).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 10).append("Dificultad", "Difícil")));
        }
        if (buttonID.equals("medium")) {
            List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);
            EmbedBuilder Embed;
            if (aprobar) {

                Embed = new EmbedBuilder()
                        .setColor(config.getColor())
                        .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                        .setDescription("Tu pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                        .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                        .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                        .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                        .addField(":chart_with_upwards_trend:  Dificultad", "```Media```", false)
                        .setFooter("Para ver más información sobre esta pregunta usa el comando /preguntas " + doc.getInteger("ID"), null);

            } else {

                Embed = new EmbedBuilder()
                        .setColor(0xFAC42A)
                        .setAuthor("Pregunta añadida a revisión!", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925715477697814568/emoji.png")
                        .setDescription("Tu pregunta se ha añadido a revisión\nTe mandaremos un mensaje por MD cuando tu pregunta sea aprobada/denegada.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                        .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                        .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                        .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                        .addField(":chart_with_upwards_trend:  Dificultad", "```Media```", false);

                TextChannel Channel = (TextChannel) event.getJDA().getGuildChannelById(config.getSubmitChannelID());
                if (Channel != null) {
                    EmbedBuilder Embed2 = new EmbedBuilder()
                            .setColor(0xFAC42A)
                            .setAuthor("Nueva pregunta añadida a revisión", null, event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925715477697814568/emoji.png")
                            .setDescription("El usuario " + event.getUser().getAsMention() + " (" + event.getUser().getId() + ") ha añadido esta pregunta a revisión.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                            .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                            .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                            .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                            .addField(":chart_with_upwards_trend:  Dificultad", "```Media```", false)
                            .setFooter("Haz click en los botones para aprobar o denegar la pregunta.", null);

                    Channel.sendMessageEmbeds(Embed2.build()).setActionRow(
                            Button.danger("cmd:deny:" + doc.getInteger("ID"), "Denegar"),
                            Button.success("cmd:approve:" + doc.getInteger("ID"), "Aprobar")
                    ).queue();
                }

            }
            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            event.editMessage("El nivel de dificultad ha sido cambiado a **Media**!.").setActionRow(
                    Button.danger("hard", "Difícil").asDisabled(),
                    Button.primary("medium", "Medio").asDisabled(),
                    Button.success("easy", "Fácil").asDisabled(),
                    Button.secondary("cancel", "Cancelar").asDisabled()
            ).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 10).append("Dificultad", "Media")));
        }
        if (buttonID.equals("easy")) {
            List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);
            EmbedBuilder Embed;
            if (aprobar) {

                Embed = new EmbedBuilder()
                        .setColor(config.getColor())
                        .setAuthor("Pregunta añadida al trivia!", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925105105068503040/emoji..png")
                        .setDescription("Tu pregunta se ha añadido al trivia correctamente.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                        .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                        .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                        .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                        .addField(":chart_with_upwards_trend:  Dificultad", "```Fácil```", false)
                        .setFooter("Para ver más información sobre esta pregunta usa el comando /preguntas " + doc.getInteger("ID"), null);

            } else {

                Embed = new EmbedBuilder()
                        .setColor(0xFAC42A)
                        .setAuthor("Pregunta añadida a revisión!", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925715477697814568/emoji.png")
                        .setDescription("Tu pregunta se ha añadido a revisión\nTe mandaremos un mensaje por MD cuando tu pregunta sea aprobada/denegada.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                        .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                        .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                        .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                        .addField(":chart_with_upwards_trend:  Dificultad", "```Fácil```", false);

                TextChannel Channel = (TextChannel) event.getJDA().getGuildChannelById(config.getSubmitChannelID());
                if (Channel != null) {
                    EmbedBuilder Embed2 = new EmbedBuilder()
                            .setColor(0xFAC42A)
                            .setAuthor("Nueva pregunta añadida a revisión", null, event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/923548627706736701/925715477697814568/emoji.png")
                            .setDescription("El usuario " + event.getUser().getAsMention() + " (" + event.getUser().getId() + ") ha añadido esta pregunta a revisión.\nLa ID de esta pregunta es: `" + doc.getInteger("ID") + "`")
                            .addField(":grey_question:  Pregunta:", "```" + doc.getString("Pregunta") + "```", false)
                            .addField(":bulb:  Respuesta correcta:", "```" + doc.getString("Respuesta-correcta") + "```", false)
                            .addField(":x:  Respuesta(s) incorrecta(s)", "```\n" + String.join("\n", respuestas) + "```", false)
                            .addField(":chart_with_upwards_trend:  Dificultad", "```Fácil```", false)
                            .setFooter("Haz click en los botones para aprobar o denegar la pregunta.", null);

                    Channel.sendMessageEmbeds(Embed2.build()).setActionRow(
                            Button.danger("cmd:deny:" + doc.getInteger("ID"), "Denegar"),
                            Button.success("cmd:approve:" + doc.getInteger("ID"), "Aprobar")
                    ).queue();
                }
            }
            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            event.editMessage("El nivel de dificultad ha sido cambiado a **Fácil**!.").setActionRow(
                    Button.danger("hard", "Difícil").asDisabled(),
                    Button.primary("medium", "Medio").asDisabled(),
                    Button.success("easy", "Fácil").asDisabled(),
                    Button.secondary("cancel", "Cancelar").asDisabled()
            ).queue();
            db.getCollection("Preguntas").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("Paso", 10).append("Dificultad", "Fácil")));
        }
        if (buttonID.equals("cancel")) {
            EmbedBuilder Embed = new EmbedBuilder()
                    .setAuthor("Creación de pregunta cancelada", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setColor(0xFF4334);
            event.getChannel().editMessageEmbedsById(doc.getString("MessageID"), Embed.build()).queue();
            db.getCollection("Preguntas").findOneAndDelete(new Document("_id", doc.getObjectId("_id")));
        }
    }
}