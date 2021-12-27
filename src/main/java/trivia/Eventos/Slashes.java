package trivia.Eventos;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import trivia.Utils.CommandManager;

public class Slashes extends ListenerAdapter {

    private final CommandManager manager = new CommandManager();

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        User user = event.getUser();

        if (user.isBot() || event.getGuild() == null) return;

        manager.run(event);
    }

}
