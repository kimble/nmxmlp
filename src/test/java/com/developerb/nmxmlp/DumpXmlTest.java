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

import org.junit.Test;

import static com.developerb.nmxmlp.NX.Feature.DUMP_INDENTED_XML;
import static com.developerb.nmxmlp.NX.Feature.DUMP_WITHOUT_XML_DECLARATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class DumpXmlTest extends AbstractNXTest {

    private String xml = "<a><b><c>value</c></b></a>";

    @Test
    public void dumpXmlWithoutXmlDeclaration() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);

        assertEquals("<a><b><c>value</c></b></a>", root.dumpXml(DUMP_WITHOUT_XML_DECLARATION));
    }

    @Test
    public void dumpXmlFromChildNode() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);

        assertEquals("<b><c>value</c></b>", root.to("b").dumpXml(DUMP_WITHOUT_XML_DECLARATION));
    }

    @Test
    public void dumpIndentedXml() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);

        String xml = root.to("b").dumpXml(DUMP_WITHOUT_XML_DECLARATION, DUMP_INDENTED_XML);
        assertThat(xml)
                .as("Dumped xml")
                .containsSequence("    <c>value")
                .isXmlEqualTo("<b><c>value</c></b>");
    }

    @Test
    public void dumpXmlWithXmlDeclaration() throws Exception {
        NX.Cursor root = parse(xml);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><a><b><c>value</c></b></a>", root.dumpXml());
    }

}