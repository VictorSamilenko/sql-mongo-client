package client;

import client.parser.SQLParser;
import org.junit.Test;

import static client.parser.SQLParser.Operator.*;

public class OperatorTest {
    @Test
    public void getByString() throws Exception {
        assert EQ == SQLParser.Operator.getByString("=");
        assert LT == SQLParser.Operator.getByString("<");
        assert LTE == SQLParser.Operator.getByString("<=");
        assert GT == SQLParser.Operator.getByString(">");
        assert GTE == SQLParser.Operator.getByString(">=");
        assert NE == SQLParser.Operator.getByString("<>");
    }
}
