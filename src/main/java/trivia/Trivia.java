package trivia;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import trivia.Eventos.Interactions;
import trivia.Eventos.Internal;
import trivia.Eventos.Messages;
import trivia.Eventos.Slashes;

import javax.security.auth.login.LoginException;

public class Trivia {

    public static void main(String[] args) {

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(null);
        Config config = new Config();

        builder.disableCache(
                CacheFlag.MEMBER_OVERRIDES,
                CacheFlag.VOICE_STATE,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ACTIVITY,
                CacheFlag.ONLINE_STATUS,
                CacheFlag.ROLE_TAGS
        );
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setCompression(Compression.NONE);
        builder.setMemberCachePolicy(MemberCachePolicy.DEFAULT);
        builder.setAutoReconnect(true);
        builder.setChunkingFilter(ChunkingFilter.NONE);

        builder.disableIntents(
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_MESSAGES
        );
        builder.enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGES
        );

        builder.addEventListeners(
                new Internal(),
                new Slashes(),
                new Messages(),
                new Interactions()
        );
        builder.setLargeThreshold(50);
        builder.setShardsTotal(-1);

        builder.setToken(config.getToken());
        builder.setActivity(Activity.watching("preguntas de LA CABRA!"));

        try {
            builder.build();
        }catch(LoginException e) {
            e.printStackTrace();
        }
    }
}
