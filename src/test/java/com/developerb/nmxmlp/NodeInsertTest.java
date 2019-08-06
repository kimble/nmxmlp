package com.developerb.nmxmlp;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class NodeInsertTest extends AbstractNXTest {

  private final String xml = "<root></root>";

  @Test
  public void insert_node() {
    NX.Cursor root = parse(xml);
    NX.Cursor person = root.append("person");
    person.setAttr("name", "Per");
    person.setAttr("age", "66");

    NX.Cursor pet = person.append("pet");
    pet.setAttr("name", "Pluto");

    String result = root.dumpXml(StandardCharsets.UTF_8);
    System.out.println(result);
  }

}
