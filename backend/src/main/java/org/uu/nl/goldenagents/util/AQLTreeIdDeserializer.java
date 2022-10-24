package org.uu.nl.goldenagents.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.uu.nl.goldenagents.aql.AQLTree;

import java.io.IOException;

public class AQLTreeIdDeserializer extends StdDeserializer<AQLTree.ID> {

    public AQLTreeIdDeserializer() {
        this(null);
    }

    public AQLTreeIdDeserializer(Class<AQLTree.ID> id) {
        super(id);
    }

    @Override
    public AQLTree.ID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return AQLTree.ID.fromString(jsonParser.readValueAs(String.class));
    }
}
