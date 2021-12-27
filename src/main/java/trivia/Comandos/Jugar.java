package trivia.Comandos;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

public class Jugar implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {

        long ID = Database.getValidTriviaID(context.getUser());

        context.reply(""+ID).queue();

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
