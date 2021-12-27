package trivia.Manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.bson.Document;
import trivia.Database.Database;

import java.util.ArrayList;

public class TriviaManager {

    public static void createTriviaManager(SlashCommandEvent event, Message message, int id) {

        MongoDatabase db = Database.getDatabase();
        MongoCollection<Document> Preguntas = db.getCollection("Preguntas");

        Document doc = new Document("OwnerID", event.getUser().getId())
                .append("ID", id)
                .append("Paso", 0)
                .append("MessageID", message.getId())
                .append("Pregunta", "")
                .append("Respuesta-correcta", "")
                .append("Respuestas-incorrectas", new ArrayList<String>())
                .append("Dificultad", "");

        Preguntas.insertOne(doc);
    }
}
