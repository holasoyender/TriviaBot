package trivia.Eventos;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import trivia.Utils.Command;
import trivia.Utils.CommandManager;

import java.util.List;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class Internal extends ListenerAdapter {

    private final org.slf4j.Logger Logger = LoggerFactory.getLogger("Event");

    public void onReady(@NotNull ReadyEvent event) {
        Logger.info("Cliente iniciado como {}", event.getJDA().getSelfUser().getAsTag());

        Guild Guild = event.getJDA().getGuildById("704029755975925841");
        if (Guild == null) {
            System.out.println("No existe el servidor de tests");
            return;
        }
        CommandListUpdateAction Commands = Guild.updateCommands();

        CommandManager manager = new CommandManager();
        List<Command> commands = manager.getCommands();

        int i = 0;
        for (Command command : commands) {
            i = i + 1;

            if (command.getSlashData() == null) {

                Commands.addCommands(new CommandData(command.getName(), command.getDescription()));

            } else {
                Commands.addCommands(command.getSlashData());
            }

        }
        Commands.queue();
        Logger.info("Se han cargado " + i + " comandos.");
    }
}
