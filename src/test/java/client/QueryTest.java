package client;

import client.parser.Query;
import com.mongodb.client.model.Sorts;
import com.mongodb.operation.OrderBy;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class QueryTest {
    private static final List<Bson> BSON_LIST;
    static {
        BSON_LIST = new ArrayList<>();
        BSON_LIST.add(Sorts.descending("a"));
        BSON_LIST.add(Sorts.ascending("b"));
        BSON_LIST.add(Sorts.descending("c"));
    }
    @Test
    public void getOrderFields() throws Exception {
        Query query = new Query();
        query.addOrderFields("a");
        query.addOrderType(OrderBy.DESC);
        query.addOrderFields("b");
        query.addOrderFields("c");
        query.addOrderType(OrderBy.DESC);
        Assert.assertEquals(BSON_LIST, query.getOrderFields());
    }

}
