package trivia.Comandos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

public class Bloquear implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {
        OptionMapping CommandOption = context.getOption("usuario");
        if (CommandOption == null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign:  Debes de especificar un usuario!**");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        User user = CommandOption.getAsUser();
        boolean isBlocked = Database.blockUser(user);
        if (isBlocked) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(config.getColor())
                    .setDescription("<:externalcontent:830859377463656479>  El usuario **" + user.getAsTag() + "** ha sido bloqueado!");
            context.replyEmbeds(embed.build()).setEphemeral(false).queue();
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription(":no_entry_sign:  El usuario **" + user.getAsTag() + "** ya está bloqueado!");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "bloquear";
    }

    @Override
    public String getDescription() {
        return "Bloquear a un usuario de añadir preguntas al trivia";
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
                .addOptions(new OptionData(OptionType.USER, "usuario", "Usuario para bloquear", true));
    }
}
