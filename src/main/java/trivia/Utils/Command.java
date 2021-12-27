package trivia.Utils;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import trivia.Config;

public interface Command {

    void run(SlashCommandEvent context, Config config);

    String getName();

    String getDescription();
    boolean needsPermission();
    boolean onlyInChannel();

    CommandData getSlashData();

}
