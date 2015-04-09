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

public class NavigationTest extends AbstractNXTest {

    private final String messageXml = "<message><header><id>id-123</id></header><body><person><name>Nasse Nøff</name><age>12</age></person></body></message>";

    private final String headersXml = "<headers><header><k>k1</k><v>v1</v></header><header><k>k2</k><v>v2</v></header></headers>";



    @Test
    public void simpleNavigationUsingTo() throws NX.Ex {
        NX.Cursor message = parse(messageXml);

        assertEquals("id-123", message.to("header", "id").text());
        assertEquals("id-123", message.to("header").to("id").text());
    }

    @Test
    public void toStringWillDescribePath() {
        NX.Cursor message = parse(messageXml);
        String describedPath = message.toString();

        assertEquals("message", describedPath);
    }

    @Test
    public void describePathRoot() {
        NX.Cursor message = parse(messageXml);
        String describedPath = message.describePath();

        assertEquals("message", describedPath);
    }

    @Test
    public void describePathLevelOne() {
        NX.Cursor message = parse(messageXml);
        String describedPath = message.to("header").describePath();

        assertEquals("message >> header", describedPath);
    }

    @Test
    public void describePathLevelTwo() {
        NX.Cursor message = parse(messageXml);
        String describedPath = message.to("header", "id").describePath();

        assertEquals("message >> header >> id", describedPath);
    }

    @Test
    public void describePathMissingNodeUnderLevelTwo() {
        NX.Cursor message = parse(messageXml);
        NX.Cursor id = message.to("header", "id");
        String describedPath = id.toOptional("no-such-node").describePath();

        assertEquals("message >> header >> id >> ???", describedPath);
    }

    @Test
    public void describePathSecondNode() {
        NX.Cursor headers = parse(headersXml);
        String describedPath = headers.to(1, "header").describePath();

        assertEquals("headers >> header[1]", describedPath);
    }

    @Test
    public void describePathFirstNode() {
        NX.Cursor headers = parse(headersXml);
        String describedPath = headers.to(0, "header").describePath();

        assertEquals("headers >> header", describedPath);
    }

    @Test
    public void ambiguousNavigationShouldResultInException() throws Exception {
        NX.Cursor headers = parse(headersXml);

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
        NX.Cursor message = parse(headersXml);

        assertEquals("headers >> header >> k", message.to(0, "header").to("k").describePath());
        assertEquals("headers >> header[1] >> k", message.to(1, "header").to("k").describePath());
    }

    @Test
    public void navigatingToMissingNodeShouldOfferHelpfulExceptionMessage() throws Exception {
        NX.Cursor message = parse(messageXml);

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
        NX.Cursor message = parse(messageXml);
        NX.Cursor missingNode = message.to("header").toOptional("no-such-node");

        assertNull(missingNode.name());
        assertNull(missingNode.text());
        assertEquals(0, missingNode.count("x"));
    }

    @Test
    public void deepNavigationWithOptionalNodesOneMissing() throws Exception {
        NX.Cursor message = parse(messageXml);
        NX.Cursor missingNode = message.toOptional("header", "no-such-node");

        assertNull(missingNode.text());
    }

    @Test
    public void deepNavigationWithOptionalNodesBothMissing() throws Exception {
        NX.Cursor message = parse(messageXml);
        NX.Cursor missingNode = message.toOptional("no-such-node", "another-missing-node");

        assertNull(missingNode.text());
    }

    @Test
    public void deepNavigationWithOptionalNodesBothExisting() throws Exception {
        NX.Cursor message = parse(messageXml);
        NX.Cursor existingNode = message.toOptional("header", "id");

        assertEquals("id-123", existingNode.text());
    }

    @Test
    public void dumpingXmlFromAMissingNodeDoesntMakeSense() throws Exception {
        NX.Cursor message = parse(messageXml);
        NX.Cursor missingNode = message.to("header").toOptional("no-such-node");

        try {
            missingNode.dumpXml();
            fail("Dumping xml from a missing node doesn't make sense");
        }
        catch (UnsupportedOperationException ex) {
            assertThat(ex)
                    .as("The expected exception")
                    .hasMessage("Can't dump empty cursor");
        }
    }

    @Test
    public void navigatingToMissingNodeIndex() {
        NX.Cursor message = parse(headersXml);

        try {
            message.to(2, "header");
        }
        catch (NX.MissingNode ex) {
            assertThat(ex)
                    .as("Expected exception")
                    .hasMessage("headers -- Unable to find 'header' with index 2 - Did you mean: header?");
        }
    }

}