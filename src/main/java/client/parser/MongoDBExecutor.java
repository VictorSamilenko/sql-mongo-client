package client.parser;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoDBExecutor {
    private MongoDatabase database;

    public MongoDBExecutor(@Autowired MongoDatabase database) {
        this.database = database;
    }

    /**
     * Execute query and return set of Documents
     *
     * @param query {@link Query}
     * @return founded documents {@link Document}
     */
    public FindIterable<Document> execute(Query query) {
        MongoCursor collectIterator = database.listCollectionNames().iterator();
        int countMatches = 0;
        while (collectIterator.hasNext()) {
            if (!(collectIterator.next()).equals(query.getTableName())) continue;
            ++countMatches;
            break;
        }
        if (countMatches == 0) {
            if (query.getTableName() == null)
                throw new RuntimeException("check syntax");
            throw new RuntimeException(String.format(" table with name \"%s\" not found! ", query.getTableName()));
        }
        MongoCollection collection = database.getCollection(query.getTableName());
        Bson bson = query.getCondition();
        FindIterable documents = collection.find();
        if (bson != null) {
            documents.filter(bson);
        }
        return documents
                .sort(Sorts.orderBy(query.getOrderFields()))
                .skip(query.getSkip())
                .limit(query.getLimit());
    }
}


