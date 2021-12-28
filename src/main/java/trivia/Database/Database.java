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
    public static int getValidTriviaID(User user) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        MongoCollection<Document> Preguntas = Database.getCollection("Preguntas");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            Usuarios.insertOne(new Document("ID", user.getId()).append("Completadas", new ArrayList<String>()).append("Puntos", 0).append("Correctas", 0).append("Incorrectas", 0));
            int ID = (int) (Math.random() *  Preguntas.find().into(new ArrayList<>()).size());
            List<Document> PreguntasList = Preguntas.find().into(new ArrayList<>());
            return PreguntasList.get(ID).getInteger("ID");
        } else {
            List<Document> preguntasSinCompletar = new ArrayList<>();
            for (Document pregunta : Preguntas.find()) {
                if (!Usuario.getList("Completadas", String.class).contains(""+pregunta.getInteger("ID"))) {
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
    public static Document getTriviaByID(long ID) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Preguntas = Database.getCollection("Preguntas");
        return Preguntas.find(new Document("ID", ID)).first();
    }
    public static int getUserPoints(User user) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            return 0;
        } else {
            return Usuario.getInteger("Puntos");
        }
    }
    public static void addUserPoints(User user, int points) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            if(points == -1) points = 0;
            Usuarios.insertOne(new Document("ID", user.getId()).append("Completadas", new ArrayList<String>()).append("Puntos", points).append("Correctas", 0).append("Incorrectas", 0));
        } else {
            if(Usuario.getInteger("Puntos") + points < 0) points = 0;
            Usuarios.updateOne(new Document("ID", user.getId()), new Document("$inc", new Document("Puntos", points)));
        }
    }
    public static void addCompletedTrivia(User user, long ID) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            Usuarios.insertOne(new Document("ID", user.getId()).append("Completadas", new ArrayList<String>().add(ID+" ".trim())).append("Puntos", 0).append("Correctas", 0).append("Incorrectas", 0));
        }
        Usuarios.updateOne(new Document("ID", user.getId()), new Document("$addToSet", new Document("Completadas", ID+" ".trim())));
    }

    public static void addIncorrectTrivia(User user) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            Usuarios.insertOne(new Document("ID", user.getId()).append("Completadas", new ArrayList<String>()).append("Puntos", 0).append("Correctas", 0).append("Incorrectas", 1));
        } else {
            Usuarios.updateOne(new Document("ID", user.getId()), new Document("$inc", new Document("Incorrectas", 1)));
        }
    }

    public static void addCorrectTrivia(User user) {
        MongoDatabase Database = getDatabase();
        MongoCollection<Document> Usuarios = Database.getCollection("Usuarios");
        Document Usuario = Usuarios.find(new Document("ID", user.getId())).first();
        if (Usuario == null) {
            Usuarios.insertOne(new Document("ID", user.getId()).append("Completadas", new ArrayList<String>()).append("Puntos", 0).append("Correctas", 1).append("Incorrectas", 0));
        } else {
            Usuarios.updateOne(new Document("ID", user.getId()), new Document("$inc", new Document("Correctas", 1)));
        }
    }
}
