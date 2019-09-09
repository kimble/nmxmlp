/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.developerb.nmxmlp;


import org.junit.jupiter.api.Test;

import static com.developerb.nmxmlp.NX.Feature.DUMP_INDENTED_XML;
import static com.developerb.nmxmlp.NX.Feature.DUMP_WITHOUT_XML_DECLARATION;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DumpXmlTest extends AbstractNXTest {

    private String xml = "<a><b><c>value</c></b></a>";

    @Test
    void dumpXmlWithoutXmlDeclaration() {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);

        assertEquals("<a><b><c>value</c></b></a>", root.dumpXml(UTF_8, DUMP_WITHOUT_XML_DECLARATION));
    }

    @Test
    void dumpXmlFromChildNode() {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);

        assertEquals("<b><c>value</c></b>", root.to("b").dumpXml(UTF_8, DUMP_WITHOUT_XML_DECLARATION));
    }

    @Test
    void dumpIndentedXml() {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);

        String xml = root.to("b").dumpXml(UTF_8, DUMP_WITHOUT_XML_DECLARATION, DUMP_INDENTED_XML);
        assertThat(xml)
                .as("Dumped xml")
                .containsSequence("    <c>value")
                .isXmlEqualTo("<b><c>value</c></b>");
    }

    @Test
    void dumpXmlWithXmlDeclaration() {
        NX.Cursor root = parse(xml);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><a><b><c>value</c></b></a>", root.dumpXml(UTF_8));
    }

    @Test
    void dumpXmlLatin1() {
        NX.Cursor root = parse(xml);

        String utf8 = root.dumpXml(UTF_8);
        String latin1 = root.dumpXml(ISO_8859_1);

        assertThat(latin1)
                .as("Latin1 encoded xml")
                .isNotEqualTo(utf8)
                .containsSequence("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
    }

}