package client.parser;

import com.mongodb.client.model.Filters;
import com.mongodb.operation.OrderBy;
import org.bson.conversions.Bson;

import java.util.ArrayList;

import static client.parser.SQLParser.KeyWords.BY;
import static com.mongodb.QueryOperators.AND;
import static com.mongodb.QueryOperators.OR;

public class SQLParser {
    private String sql;

    public SQLParser(String sql) {
        this.sql = sql;
    }

    public Query parse() {
        String[] strings = sql.split("(\\s*,\\s*|\\s+)");
        Query query = new Query();
        KeyWords lastKeyWord = null;
        for (int i = 0; i < strings.length; i++) {
            if (KeyWords.getByString(strings[i]) != null) {
                lastKeyWord = KeyWords.getByString(strings[i]);
                switch (lastKeyWord) {
                    case ASC:
                    case DESC:
                        query.addOrderType(OrderBy.valueOf(strings[i].toUpperCase()));
                        lastKeyWord = BY;
                        break;
                }
                continue;
            }
            if (lastKeyWord == null)
                throw new RuntimeException("Check syntax " + sql);
            switch (lastKeyWord) {
                case SELECT:
                    String[] ss = strings[i].split("\\.");
                    if (ss.length > 1)
                        query.addFields(ss[1]);
                    else query.addFields(strings[i]);
                    break;
                case FROM: query.setTableName(strings[i]); break;
                case BY:   query.addOrderFields(strings[i]); break;
                case SKIP: query.setSkip(Integer.parseInt(strings[i])); break;
                case LIMIT:query.setLimit(Integer.parseInt(strings[i])); break;
            }
        }

        if (query.getFields().size() == 0) {
            throw new RuntimeException("No fields for select");
        }
        int whereIndex = sql.toUpperCase().indexOf(String.valueOf(KeyWords.WHERE));
        if (whereIndex > 0) {
            int lastIndex = sql.toUpperCase().indexOf(String.valueOf(KeyWords.ORDER));
            int i = sql.toUpperCase().indexOf(String.valueOf(KeyWords.LIMIT));
            lastIndex = i > -1 && i < lastIndex ? i : lastIndex;
            i = sql.toUpperCase().indexOf(String.valueOf(KeyWords.SKIP));
            lastIndex = i > -1 && i < lastIndex ? i : lastIndex;
            String conditions = lastIndex == -1 ? sql.substring(whereIndex + 5).trim() : sql.substring(whereIndex + 5, lastIndex).trim();
            query.setCondition(parseCondition(conditions));
        }
        return query;
    }

    public Bson getCondition(String fieldName, Operator operator, Object fieldValue) {
        switch (operator) {
            case EQ: return Filters.eq(fieldName, fieldValue);
            case NE: return Filters.ne(fieldName, fieldValue);
            case GT: return Filters.gt(fieldName, fieldValue);
            case GTE: return Filters.gte(fieldName, fieldValue);
            case LT: return Filters.lt(fieldName, fieldValue);
            case LTE: return Filters.lte(fieldName, fieldValue);
        }
        throw new RuntimeException(String.format("Operator %s not found!", operator.name()));
    }

    public Bson parseCondition(String conditionsStr) {
        ArrayList<String> innerConditions = new ArrayList<>();
        String outerConditions = splitCondition(conditionsStr, innerConditions);
        String[] split = outerConditions.split("\\s+");
        int flag = 0;
        String fieldName = null;
        Operator operator = null;
        int countInnerConditions = 0;
        Bson prevBson = null;
        KeyWords currentOperator = null;
        for (int i = 0; i < split.length; i++) {
            switch (split[i].toUpperCase()) {
                case "AND":
                    currentOperator = KeyWords.AND;
                    continue;
                case "OR":
                    currentOperator = KeyWords.OR;
                    continue;
                case "()": {
                    if (prevBson == null) {
                        prevBson = parseCondition(innerConditions.get(countInnerConditions++));
                    } else {
                        switch (currentOperator) {
                            case OR:
                                prevBson = Filters.or(prevBson, parseCondition(innerConditions.get(countInnerConditions++)));
                                break;
                            case AND:
                                prevBson = Filters.and(prevBson, parseCondition(innerConditions.get(countInnerConditions++)));
                                break;
                        }
                    }
                    continue;
                }
            }
            switch (flag) {
                case 0:
                    fieldName = split[i];
                    flag++;
                    break;
                case 1:
                    operator = Operator.getByString(split[i]);
                    flag++;
                    break;
                case 2: {
                    Object val;
                    String strVal = split[i];
                    if (strVal.contains("\"")) {
                        val = strVal.replaceAll("\"", "");
                    } else {
                        try {
                            val = Integer.parseInt(split[i]);
                        } catch (Exception e) {
                            try {
                                val = Double.parseDouble(split[i]);
                            } catch (Exception ee) {
                                throw new RuntimeException(String.format(" Error parse SQL, check syntax %s %s %s", fieldName, operator, split[i]));
                            }
                        }
                    }
                    if (prevBson == null) {
                        prevBson = getCondition(fieldName, operator, val);
                    } else {
                        switch (currentOperator) {
                            case OR:
                                prevBson = Filters.or(prevBson, getCondition(fieldName, operator, val));
                                break;
                            case AND:
                                prevBson = Filters.and(prevBson, getCondition(fieldName, operator, val));
                                break;
                        }
                    }
                    flag = 0;
                    break;
                }
            }
        }
        return prevBson;
    }

    public String splitCondition(String conditionsStr, ArrayList<String> conditions) {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = new StringBuilder();
        int countBrackets = 0;
        for (int i = 0; i < conditionsStr.toCharArray().length; i++) {
            char c = conditionsStr.charAt(i);
            if (c == '(') {
                if (countBrackets++ == 0) {
                    result.append(c);
                    continue;
                }
            }
            if (c == ')') {
                countBrackets--;
                if (countBrackets == 0) {
                    conditions.add(sb.toString());
                    sb = new StringBuilder();
                    result.append(c);
                    continue;
                }
            }
            if (countBrackets > 0)
                sb.append(c);
            else result.append(c);
        }
        return result.toString();
    }

    public enum Operator {
        EQ("="), NE("<>"), GT(">"), GTE(">="), LT("<"), LTE("<=");

        private String stringEquivalent;

        Operator(String stringEquivalent) {
            this.stringEquivalent = stringEquivalent;
        }

        public static Operator getByString(String stringEquivalent) {
            for (Operator operator : Operator.values()) {
                if (operator.stringEquivalent.equals(stringEquivalent))
                    return operator;
            }
            return null;
        }
    }

    public enum KeyWords {
        SELECT, FROM, WHERE, AND, OR, ORDER, BY, ASC, DESC, SKIP, LIMIT;

        public static KeyWords getByString(String name) {
            for (KeyWords keyWord : KeyWords.values()) {
                if (keyWord.toString().equalsIgnoreCase(name)) {
                    return KeyWords.valueOf(name.toUpperCase());
                }
            }
            return null;
        }
    }
}

