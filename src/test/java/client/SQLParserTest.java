package client;

import client.parser.Query;
import client.parser.SQLParser;
import com.mongodb.client.model.Filters;
import com.mongodb.operation.OrderBy;
import org.bson.BSON;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class SQLParserTest {
    private static final String CONDITIONS_STR = "a = 2 and (a = 0 or b = 3 or c = 4 and (q = 7 or z = 0)) and (l = 8 or k <> 8)";
    private static final String OUTER_CONDITION = "a = 2 and () and ()";
    private static final String INNER_CONDITION1 = "a = 0 or b = 3 or c = 4 and (q = 7 or z = 0)";
    private static final String INNER_CONDITION2 = "l = 8 or k <> 8";
    private static final String TEST_BSON_CONDITION1 = "a = 8";
    private static final String TEST_BSON_CONDITION2 = "a > 8 and z <> \"9\"";
    private static final String TEST_BSON_CONDITION3 = "a > 8 and z <> \"9\" and (s = 0 or x <> 4)";
    private static final String TEST_SQL = "SELECT a,b , c FROM table where " + TEST_BSON_CONDITION2 + " ORDER BY a DESC, c SKIP 50000 LIMIT 5";

    private static final Bson TEST_BSON_1;
    private static final Bson TEST_BSON_2;
    private static final Bson TEST_BSON_3;

    private static final Query TEST_QUERY;

    static {
        TEST_BSON_1 = Filters.eq("a",8);
        TEST_BSON_2 = Filters.and(Filters.gt("a", 8), Filters.ne("z","9"));
        TEST_BSON_3 = Filters.and(
                Filters.and(
                        Filters.gt("a", 8),
                        Filters.ne("z","9")
                        ),
                Filters.or(
                        Filters.eq("s",0),
                        Filters.ne("x", 4)
                )
        );

        TEST_QUERY = new Query();
        TEST_QUERY.setTableName("table");
        TEST_QUERY.setCondition(TEST_BSON_2);
        TEST_QUERY.addFields("a");
        TEST_QUERY.addFields("b");
        TEST_QUERY.addFields("c");
        TEST_QUERY.addOrderFields("a");
        TEST_QUERY.addOrderType(OrderBy.DESC);
        TEST_QUERY.addOrderFields("c");
        TEST_QUERY.setLimit(5);
        TEST_QUERY.setSkip(50000);
        TEST_QUERY.setCondition(TEST_BSON_2);
    }

    @Test
    public void parse() throws Exception {
        SQLParser sqlParser = new SQLParser(TEST_SQL);
        Query query = sqlParser.parse();
        Assert.assertEquals(TEST_QUERY, query);
    }

    @Test
    public void parseCondition() throws Exception {
        SQLParser sqlParser = new SQLParser("");
        Bson bson = sqlParser.parseCondition(TEST_BSON_CONDITION1);
        Assert.assertEquals(TEST_BSON_1.toString(), bson.toString());
        bson = sqlParser.parseCondition(TEST_BSON_CONDITION2);
        Assert.assertEquals(TEST_BSON_2.toString(), bson.toString());
        bson = sqlParser.parseCondition(TEST_BSON_CONDITION3);
        Assert.assertEquals(TEST_BSON_3.toString(), bson.toString());
    }

    @Test
    public void splitCondition() throws Exception {
        SQLParser sqlParser = new SQLParser("");
        ArrayList<String> innerConditions = new ArrayList<>();
        String outerConditions = sqlParser.splitCondition(CONDITIONS_STR, innerConditions);
        assert OUTER_CONDITION.equals(outerConditions);
        assert INNER_CONDITION1.equals(innerConditions.get(0));
        assert INNER_CONDITION2.equals(innerConditions.get(1));
    }
}
