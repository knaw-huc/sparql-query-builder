package org.uu.nl.goldenagents.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.uu.nl.goldenagents.aql.AQLTree;

import java.io.IOException;

public class AQLTreeIdSerializer extends StdSerializer<AQLTree.ID> {

    public AQLTreeIdSerializer() {
        this(null);
    }

    public AQLTreeIdSerializer(Class<AQLTree.ID> id) {
        super(id);
    }

    @Override
    public void serialize(AQLTree.ID id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(id.toString());
    }
}
