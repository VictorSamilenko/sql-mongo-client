package client.service;

import client.Utils;
import client.parser.MongoDBExecutor;
import client.parser.Query;
import client.parser.SQLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    @Autowired
    MongoDBExecutor executor;

    public String getResult(String sql) {
        SQLParser parser = new SQLParser(sql);
        Query query = parser.parse();
        return Utils.DocumentsToJSON(executor.execute(query), query.getFields());
    }
}
