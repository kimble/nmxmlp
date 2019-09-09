package com.developerb.nmxmlp;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetainDtdTest {

  @Test
  void print_xml_with_dtd() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<!DOCTYPE root PUBLIC \"NONE\" \"test://validation.dtd\">\n" +
        "<root><name>test</name></root>";

    String dtd = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<!ELEMENT root (name)>\n" +
        "<!ELEMENT name (#PCDATA)>\n" +
        "";


    NX nx = new NX(Sets.newHashSet(NX.ConfigFeature.VALIDATING));
    NX.Cursor cursor = nx.from(xml, new NX.ReadContext((publicId, systemId) -> new InputSource(new StringReader(dtd))));

    String dumpedXml = cursor.dumpXml(StandardCharsets.UTF_8, NX.Feature.RETAIN_DTD);
    assertEquals(xml, dumpedXml);
  }

}
