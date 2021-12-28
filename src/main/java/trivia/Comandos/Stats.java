package trivia.Comandos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.Document;
import trivia.Config;
import trivia.Database.Database;
import trivia.Utils.Command;

public class Stats implements Command {
    @Override
    public void run(SlashCommandEvent context, Config config) {
        OptionMapping CommandOption = context.getOption("usuario");
        if (CommandOption == null) {
            Document User = Database.getDatabase().getCollection("Usuarios").find(new Document("ID", context.getUser().getId())).first();
            if (User == null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(0xFF4334)
                        .setDescription("**:no_entry_sign:  No has jugado al trivia aún!**");
                context.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
            int total = User.getInteger("Correctas")+User.getInteger("Incorrectas");
            EmbedBuilder Embed = new EmbedBuilder()
                    .setAuthor("Estadísticas de " + context.getUser().getName(), null, context.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail(context.getUser().getAvatarUrl())
                    .addField("Preguntas totales respondidas", "**"+total+"** Preguntas", true)
                    .addField("Preguntas acertadas", "**"+User.getInteger("Correctas")+"** Preguntas correctas", true)
                    .addField("Preguntas falladas", "**"+User.getInteger("Incorrectas")+"** Preguntas incorrectas", true)
                    .addField("Puntos totales", "**"+User.getInteger("Puntos")+"** Puntos", true)
                    .setColor(config.getColor());
            context.replyEmbeds(Embed.build()).setEphemeral(true).queue();
            return;
        }
        User GuildUser = CommandOption.getAsUser();
        Document User = Database.getDatabase().getCollection("Usuarios").find(new Document("ID", GuildUser.getId())).first();
        if (User == null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(0xFF4334)
                    .setDescription("**:no_entry_sign: Ese usuario no ha jugado al trivia aún!**");
            context.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        EmbedBuilder Embed = new EmbedBuilder()
                .setAuthor("Estadísticas de " + GuildUser.getName(), null, context.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(GuildUser.getAvatarUrl())
                .addField("Preguntas totales respondidas", "**"+User.getInteger("Correctas")+User.getInteger("Incorrectas")+"** Preguntas", true)
                .addField("Preguntas acertadas", "**"+User.getInteger("Correctas")+"** Preguntas correctas", true)
                .addField("Preguntas falladas", "**"+User.getInteger("Incorrectas")+"** Preguntas incorrectas", true)
                .addField("Puntos totales", "**"+User.getInteger("Puntos")+"** Puntos", true)
                .setColor(config.getColor());
        context.replyEmbeds(Embed.build()).setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "Muestra las estadísticas de un usuario";
    }

    @Override
    public boolean needsPermission() {
        return false;
    }

    @Override
    public boolean onlyInChannel() {
        return false;
    }

    @Override
    public CommandData getSlashData() {
        return new CommandData(this.getName(), this.getDescription())
                .addOptions(new OptionData(OptionType.USER, "usuario", "Usuario para mostrar las estadísticas"));
    }
}
