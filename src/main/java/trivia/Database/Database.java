package trivia.Database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;
import trivia.Config;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final Config config = new Config();

    private static final MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(config.getMongoDB())).build();
    private static final MongoClient mongoClient = MongoClients.create(settings);

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase("Trivia");
    }

    public static boolean idExists(int ID) {
        MongoDatabase DB = Database.getDatabase();
        MongoCollection<Document> Preguntas = DB.getCollection("Preguntas");
        Document Pregunta = Preguntas.find(new Document("ID", ID)).first();
        return Pregunta != null;
    }

    public static int generateID() {
        int ID = (int) (Math.random() * 1000000);
        while (idExists(ID)) {
            ID = (int) (Math.random() * 1000000);
        }
        return ID;
    }
    public static FindIterable<Document> getAllQuestions() {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Preguntas = Database.getCollection("Preguntas");
        return Preguntas.find();
    }
    public static Document getQuestion(long ID) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Preguntas = Database.getCollection("Preguntas");
        return Preguntas.find(new Document("ID", ID)).first();
    }
    public static int getValidTriviaID(User user) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        MongoCollection<Document> Preguntas = Database.getCollection("Preguntas");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            Usuarios.insertOne(new Document("ID", user.getId()).append("Completadas", new ArrayList<Integer>()).append("Correctas", 0).append("Incorrectas", 0));
            int ID = (int) (Math.random() *  Preguntas.find().into(new ArrayList<>()).size());
            List<Document> PreguntasList = Preguntas.find().into(new ArrayList<>());
            return PreguntasList.get(ID).getInteger("ID");
        } else {
            List<Document> preguntasSinCompletar = new ArrayList<>();
            for (Document pregunta : Preguntas.find()) {

                if (!Usuario.getList("Completadas", int.class).contains(pregunta.getInteger("ID"))) {
                    preguntasSinCompletar.add(pregunta);
                }
            }
            if (preguntasSinCompletar.size() == 0) {
                return -1;
            }
            int ID = (int) (Math.random() * preguntasSinCompletar.size());
            return preguntasSinCompletar.get(ID).getInteger("ID");
        }
    }
}
