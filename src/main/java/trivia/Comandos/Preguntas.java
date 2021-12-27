package trivia.Comandos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bson.Document;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

import java.util.ArrayList;
import java.util.List;

public class Preguntas implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {

        OptionMapping CommandOption = context.getOption("id");
        if (CommandOption == null) {
            List<Document> allQ = Database.getAllQuestions().into(new ArrayList<>());

            if (allQ.isEmpty()) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(0xFF4334)
                        .setDescription("**:no_entry_sign:  No hay preguntas a mostrar!**");
                context.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }

            List<List<Document>> allQSplit = new ArrayList<>();
            int i = 0;
            while (i < allQ.size()) {
                allQSplit.add(allQ.subList(i, Math.min(i + 5, allQ.size())));
                i += 5;
            }

            EmbedBuilder Embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setAuthor("Lista de preguntas del Trivia", null, context.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail("https://cdn.discordapp.com/attachments/755000173922615336/925119230565810266/emoji.png")
                    .setFooter("Página 1 de " + allQSplit.size());

            for (Document doc : allQSplit.get(0)) {
                Embed.addField(" - Pregunta #" + doc.get("ID"), "```" + doc.get("Pregunta") + "```\n**<:externalcontent:830859377463656479>  Respuesta correcta**: `" + doc.getString("Respuesta-correcta") + "`\n**:x:  Respuesta(s) incorrecta(s)**: ```\n" + String.join(",\n", doc.getList("Respuestas-incorrectas", String.class)) + "```\n**:chart_with_upwards_trend:  Dificultad**: `" + doc.get("Dificultad") + "`", true);
            }

            context.replyEmbeds(Embed.build()).setEphemeral(false).addActionRow(
                    Button.primary("cmd:list:0:back:" + context.getUser().getId(), "◀"),
                    Button.primary("cmd:list:0:next:" + context.getUser().getId(), "▶")
            ).queue();
            return;
        }
        long id = CommandOption.getAsLong();
        Document doc = Database.getQuestion(id);
        if (doc == null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign:  No existe ninguna pregunta con esa ID!**");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        List<String> respuestas = doc.getList("Respuestas-incorrectas", String.class);

        EmbedBuilder Embed = new EmbedBuilder()
                .setColor(config.getColor())
                .setAuthor("Pregunta añadida al trivia!", null, context.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
                .setDescription("Información sobre la pregunta con ID: `"+doc.getInteger("ID")+"`")
                .addField(":grey_question:  Pregunta:", "```"+doc.getString("Pregunta")+"```", false)
                .addField(":bulb:  Respuesta correcta:", "```"+doc.getString("Respuesta-correcta")+"```", false)
                .addField(":x:  Respuesta(s) incorrecta(s)", "```\n"+String.join("\n", respuestas)+"```", false)
                .addField(":chart_with_upwards_trend:  Dificultad", "```Fácil```", false);

        context.replyEmbeds(Embed.build()).setEphemeral(false).addActionRow(
                Button.primary("cmd:list:1:back:" + context.getUser().getId(), "Todas las preguntas")
        ).queue();
    }

    @Override
    public String getName() {
        return "preguntas";
    }

    @Override
    public String getDescription() {
        return "Lista de todas las preguntas del trivia";
    }

    @Override
    public boolean needsPermission() {
        return true;
    }

    @Override
    public boolean onlyInChannel() {
        return false;
    }

    @Override
    public CommandData getSlashData() {
        return new CommandData(this.getName(), this.getDescription())
                .addOptions(new OptionData(OptionType.INTEGER, "id", "ID de la pregunta a mostrar"));
    }
}
