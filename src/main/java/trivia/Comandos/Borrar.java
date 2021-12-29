package trivia.Comandos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.Document;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

public class Borrar implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {
        OptionMapping CommandOption = context.getOption("id");
        if (CommandOption == null) {

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign:  Debes de especificar un ID de pregunta.**");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        long ID = CommandOption.getAsLong();
        Document pregunta = Database.getTriviaByID(ID);
        if (pregunta == null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign:  No se ha encontrado una pregunta con esa ID.**");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        Database.getDatabase().getCollection("Preguntas").deleteOne(pregunta);
        context.reply("<:externalcontent:830859377463656479>  La pregunta con ID `" + ID + "` ha sido borrada.").setEphemeral(false).queue();

    }

    @Override
    public String getName() {
        return "borrar";
    }

    @Override
    public String getDescription() {
        return "Borrar una pregunta del Trivia";
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
                .addOptions(new OptionData(OptionType.INTEGER, "id", "ID de la pregunta a borrar", true));
    }
}
