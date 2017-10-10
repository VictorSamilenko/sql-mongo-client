package client;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedHashSet;
import java.util.List;

public class Utils {
    public static String DocumentsToJSON(FindIterable<Document> documents, List<String> mappingFields) {
        JSONObject result = new JSONObject();
        MongoCursor iterator = documents.iterator();
        LinkedHashSet<String> fields = new LinkedHashSet<String>(mappingFields);
        boolean allFlag = fields.contains("*");
        if (allFlag) {
            fields.remove("*");
        }
        JSONArray array = new JSONArray();
        while (iterator.hasNext()) {
            JSONObject object = new JSONObject();
            Document document = (Document)iterator.next();
            if (allFlag) {
                for (String field : document.keySet()) {
                    fields.add(field);
                    object.put(field, document.get(field));
                }
            } else {
                for (String field : fields) {
                    object.put(field, document.get(field));
                }
            }
            array.add(object);
        }
        JSONArray jsonFields = new JSONArray();
        fields.forEach(s -> jsonFields.add(s));
        result.put("fields", jsonFields);
        result.put("data", array);
        return result.toJSONString();
    }

}
