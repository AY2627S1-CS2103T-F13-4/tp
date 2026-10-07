package seedu.address.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/** Rejects scalar coercion for new tuition JSON fields without changing legacy contact parsing. */
public class StrictStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
            throw context.mappingException("Expected a JSON string for a tuition field");
        }
        return parser.getText();
    }
}
