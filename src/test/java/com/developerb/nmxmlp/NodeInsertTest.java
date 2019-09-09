package com.developerb.nmxmlp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeInsertTest extends AbstractNXTest {

    private final String xml = "<root></root>";

    @Test
    void insert_node() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.append("person");
        person.setAttr("name", "Per");
        person.setAttr("age", "66");

        NX.Cursor pet = person.append("pet");
        pet.setAttr("name", "Pluto");

        assertEquals("root >> person >> pet", pet.describePath());

        NX.Cursor newPetNode = person.to("pet");
        assertEquals("Pluto", newPetNode.attr("name").text());
    }

}
