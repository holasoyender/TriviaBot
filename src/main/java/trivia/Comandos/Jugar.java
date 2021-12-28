package trivia.Comandos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bson.Document;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

public class Jugar implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {

        long ID = Database.getValidTriviaID(context.getUser());

        if (ID == -1) {
            context.reply("Parece que ya has respondido a todas las preguntas del trivia!").setEphemeral(true).queue();
            return;
        }

        Document Trivia = Database.getTriviaByID(ID);

        if (Trivia == null) {
            context.reply("Parece que ya has respondido a todas las preguntas del trivia!").setEphemeral(true).queue();
            return;
        }

        String Dificultad = Trivia.getString("Dificultad");
        String footerImg = "https://i.imgur.com/XlARsD6.png";

        if(Dificultad.equals("Fácil"))
            footerImg = "https://i.imgur.com/XlARsD6.png";

        if(Dificultad.equals("Media"))
            footerImg = "https://i.imgur.com/521SKXF.png";

        if(Dificultad.equals("Difícil"))
            footerImg = "https://i.imgur.com/JD0MDyW.png";


        EmbedBuilder Embed = new EmbedBuilder()
                .setColor(config.getColor())
                .setAuthor("Pregunta de trivia para " + context.getUser().getName()+"!", null, context.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(context.getUser().getAvatarUrl())
                .setDescription("```CSS\n" + Trivia.getString("Pregunta") + "```")
                .setFooter("Dificultad: " + Dificultad+"  |  ID: "+Trivia.getInteger("ID"), footerImg);

        context.replyEmbeds(Embed.build()).queue();

    }

    @Override
    public String getName() {
        return "jugar";
    }

    @Override
    public String getDescription() {
        return "Juega a la trivia!";
    }

    @Override
    public boolean needsPermission() {
        return false;
    }

    @Override
    public boolean onlyInChannel() {
        return true;
    }

    @Override
    public CommandData getSlashData() {
        return null;
    }
}