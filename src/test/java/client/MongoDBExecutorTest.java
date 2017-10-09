package client;

import client.parser.MongoDBExecutor;
import client.parser.Query;
import client.parser.SQLParser;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MongoDBExecutorTest {
    private static final String DATABASE_NAME = "test";
    private static final String HOST = "localhost";
    private static final String SQL = "SELECT index, balance, age, gender,company, name FROM client";
    private static final int PORT = 12346;
    private static final List<Document> DOCUMENTS = new ArrayList<>();
    private static final Query QUERY;
    private static MongoDatabase db;
    private static MongodExecutable mongodExecutable;
    static  {
        QUERY = new Query();
        QUERY.addFields("index");
        QUERY.addFields("balance");
        QUERY.addFields("age");
        QUERY.addFields("gender");
        QUERY.addFields("company");
        QUERY.addFields("name");
        QUERY.setTableName("client");
    }

    @BeforeClass
    public static void initDB() throws IOException {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        IMongodConfig config = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(HOST, PORT, Network.localhostIsIPv6()))
                .build();
        mongodExecutable = starter.prepare(config);
        mongodExecutable.start();
        MongoClient mongo = new MongoClient(HOST, PORT);
        db = mongo.getDatabase(DATABASE_NAME);
        db.createCollection("client");

        BufferedReader reader = new BufferedReader(new InputStreamReader(MongoDBExecutorTest.class.getClassLoader().getResourceAsStream("import_file.json")));
        String s;
        StringBuilder json = new StringBuilder();

        while ((s = reader.readLine()) != null) {
            json.append(s);
            if (s.contains("},")) {
                DOCUMENTS.add(Document.parse(json.toString().replace(',',' ')));
                json = new StringBuilder();
            }
        }
        MongoCollection<Document> client = db.getCollection("client");
        client.insertMany(DOCUMENTS);
    }

    @AfterClass
    public static void destroyDB() {
        if (mongodExecutable != null)
            mongodExecutable.stop();
    }
    @Test
    public void execute() throws Exception {
        Query query = new SQLParser(SQL).parse();
        Assert.assertEquals(QUERY, query);
    }
}
