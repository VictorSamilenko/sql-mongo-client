package client.parser;

import com.mongodb.client.model.Sorts;
import com.mongodb.operation.OrderBy;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Query {
    private String tableName;
    private ArrayList<Pair> orderFields = new ArrayList();
    private ArrayList<String> fields = new ArrayList();
    private int skip;
    private int limit;
    private Bson condition;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addOrderFields(String field) {
        this.orderFields.add(new Pair(field, OrderBy.ASC));
    }

    public void addFields(String field) {
        this.fields.add(field);
    }

    public void addOrderType(OrderBy orderBy) {
        this.orderFields.get(this.orderFields.size() - 1).setValue(orderBy);
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String toString() {
        String result = String.format("SELECT %s FROM %s ", this.fields.stream().collect(Collectors.joining(", ")), this.tableName);
        if (this.orderFields.size() > 0) {
            result = result + "ORDER BY " + this.orderFields.stream().map(pair -> pair.getKey() + " " + (Object) pair.getValue()).collect(Collectors.joining(", "));
        }
        if (this.skip > 0) {
            result = result + " SKIP " + this.skip;
        }
        if (this.limit > 0) {
            result = result + " LIMIT " + this.limit;
        }
        return result;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<Bson> getOrderFields() {
        return this.orderFields.stream().map(pair -> {
                    if (OrderBy.DESC.equals(pair.getValue())) {
                        return Sorts.descending((String[]) new String[]{pair.getKey()});
                    }
                    return Sorts.ascending((String[]) new String[]{pair.getKey()});
                }
        ).collect(Collectors.toList());
    }

    public int getSkip() {
        return this.skip;
    }

    public int getLimit() {
        return this.limit;
    }

    public Bson getCondition() {
        return this.condition;
    }

    public void setCondition(Bson condition) {
        this.condition = condition;
    }

    public ArrayList<String> getFields() {
        return this.fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        if (skip != query.skip) return false;
        if (limit != query.limit) return false;
        if (tableName != null ? !tableName.equals(query.tableName) : query.tableName != null) return false;
        if (orderFields != null ? !orderFields.equals(query.orderFields) : query.orderFields != null) return false;
        if (fields != null ? !fields.equals(query.fields) : query.fields != null) return false;
        return condition != null ? condition.toString().equals(query.condition.toString()) : query.condition == null;
    }

    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + (orderFields != null ? orderFields.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        result = 31 * result + skip;
        result = 31 * result + limit;
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        return result;
    }

    private class Pair {
        private String key;
        private OrderBy value;

        public Pair(String key, OrderBy value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public OrderBy getValue() {
            return value;
        }

        public void setValue(OrderBy value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
            return value == pair.value;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}