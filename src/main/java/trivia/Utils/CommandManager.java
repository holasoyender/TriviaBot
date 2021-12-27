package trivia.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import trivia.Comandos.*;
import trivia.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {


    private final List<Command> commands = new ArrayList<>();
    private final Config config = new Config();

    public List<Command> getCommands() {
        return commands;
    }

    public CommandManager() {
        addCommand(new Jugar());
        addCommand(new Preguntas());
        addCommand(new Add());
    }

    public void addCommand(Command cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("Ya existe un comando con ese nombre!");
        }

        commands.add(cmd);
    }

    @Nullable
    public Command getCommand(String search) {
        String searchLower = search.toLowerCase();

        for (Command command : this.commands) {
            if (command.getName().equals(searchLower)) {
                return command;
            }
        }

        return null;
    }

    public void run(SlashCommandEvent event) {

        if (event.getGuild() == null || event.getMember() == null) {
            event.reply("No puedo ejecutar comandos en mensajes privados!").queue();
            return;
        }

        String invoke = event.getName();
        Command cmd = this.getCommand(invoke);

        if (cmd != null) {

            if(cmd.needsPermission()) {

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
            }
            if(cmd.onlyInChannel()) {

                List<String> channelIds = config.getChannelIds();
                if(channelIds == null) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(0xFF4334)
                            .setDescription("**:no_entry_sign:  Este comando no puede ser usado en este canal!**");
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }
                boolean isChannel = false;
                List<String> Channels = new ArrayList<>();

                if (event.getMember() != null)
                    for (String channelID : channelIds) {
                        if (event.getChannel().getId().equals(channelID))
                            isChannel = true;
                        Channels.add(Objects.requireNonNull(event.getGuild().getGuildChannelById(channelID)).getName());
                    }


                if (!isChannel) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(0xFF4334)
                            .setDescription("**:no_entry_sign:  Este comando no puede ser usado en este canal!**\n\nCanales permitidos:```\n"+String.join(",\n", Channels)+"```");
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }

            }

            cmd.run(event, config);
        }
    }

}
