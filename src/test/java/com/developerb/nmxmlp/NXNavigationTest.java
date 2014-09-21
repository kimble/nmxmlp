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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class NXNavigationTest {

    private final String messageXml = "<message><header><id>id-123</id></header><body><person><name>Nasse Nøff</name><age>12</age></person></body></message>";

    private final String headersXml = "<headers><header><k>k1</k><v>v1</v></header><header><k>k2</k><v>v2</v></header></headers>";


    @Test
    public void simpleNavigationUsingTo() throws NX.Ex {
        NX nx = new NX();
        NX.Cursor message = nx.from(messageXml);

        assertEquals("id-123", message.to("header", "id").text());
        assertEquals("id-123", message.to("header").to("id").text());
    }

    @Test
    public void ambiguousNavigationShouldResultInException() throws Exception {
        NX nx = new NX();
        NX.Cursor headers = nx.from(headersXml);

        try {
            headers.to("header");
            fail("Should not have been allowed to do ambiguous navigation");
        }
        catch (NX.Ambiguous ex) {
            assertThat(ex)
                    .as("The expected exception")
                    .hasMessage("headers -- Expected to find a single instance of header");
        }
    }

    @Test
    public void describeCursorPath() throws NX.Ex {
        NX nx = new NX();
        NX.Cursor message = nx.from(headersXml);

        assertEquals("headers >> header >> k", message.to(0, "header").to("k").describePath());
        assertEquals("headers >> header[1] >> k", message.to(1, "header").to("k").describePath());
    }

    @Test
    public void navigatingToMissingNodeShouldOfferHelpfulExceptionMessage() throws Exception {
        NX nx = new NX();
        NX.Cursor message = nx.from(messageXml);

        try {
            message.to("header").to("idd");
            fail("Should not have been allowed to navigate to non-existing node");
        }
        catch (NX.MissingNode ex) {
            assertThat(ex)
                    .as("The expected exception thrown when navigating to non-existing node")
                    .hasMessage("message >> header -- Unable to find 'idd' - Did you mean: id?");
        }
    }

    @Test
    public void optionalNavigationAndDefaultValues() throws Exception {
        NX nx = new NX();
        NX.Cursor message = nx.from(messageXml);
        NX.Cursor missingNode = message.to("header").toOptional("no-such-node");

        assertNull(missingNode.name());
        assertNull(missingNode.text());
        assertEquals(0, missingNode.count("x"));
    }

    @Test
    public void describingThePathToAMissingNodeDoesntMakeSense() throws Exception {
        NX nx = new NX();
        NX.Cursor message = nx.from(messageXml);
        NX.Cursor missingNode = message.to("header").toOptional("no-such-node");

        try {
            missingNode.describePath();
            fail("Describing the path to a missing node doesn't make sense");
        }
        catch (UnsupportedOperationException ex) {
            assertThat(ex)
                    .as("The expected exception")
                    .hasMessage("Can't describe path to empty node");
        }
    }

    @Test
    public void dumpingXmlFromAMissingNodeDoesntMakeSense() throws Exception {
        NX nx = new NX();
        NX.Cursor message = nx.from(messageXml);
        NX.Cursor missingNode = message.to("header").toOptional("no-such-node");

        try {
            missingNode.dump();
            fail("Dumping xml from a missing node doesn't make sense");
        }
        catch (UnsupportedOperationException ex) {
            assertThat(ex)
                    .as("The expected exception")
                    .hasMessage("Can't dump empty cursor");
        }
    }

}