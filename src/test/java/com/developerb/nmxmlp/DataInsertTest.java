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

import java.util.Arrays;
import java.util.List;

import static com.developerb.nmxmlp.NX.Feature.DUMP_WITHOUT_XML_DECLARATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataInsertTest extends AbstractNXTest {

    @Test
    void insertCollectionNoNamespaces() throws NX.Ex {
        NX.Cursor peopleCursor = parse("<people><person name='Prototype' /></people>");

        List<Person> people = Arrays.asList(
            new Person("Nasse Nøff"),
            new Person("Donald Duck")
        );

        peopleCursor.insertCollection("person", people, new PersonInserter());


        NX.Cursor reloadedPeopleCursor = parse(peopleCursor.dumpXml(UTF_8));
        List<Person> extractedPeople = reloadedPeopleCursor.extractCollection("person", new PersonExtractor());

        assertEquals(people, extractedPeople);
    }

    @Test
    void insertNamespacedSvg() throws NX.Ex {
        NX.Cursor cursor = parseResource("svg/simple-svg.xhtml");
        NX.Cursor svg = cursor.to("body").to("svg");

        List<Circle> circles = Arrays.asList(
            new Circle("red", 100, 100, 20),
            new Circle("red", 50, 50, 25),
            new Circle("red", 70, 120, 30)
        );

        svg.insertCollection("circle", circles, new CircleInserter());

        assertThat(cursor.dumpXml(UTF_8))
            .as("XML output")
            .contains("<svg:circle cx=\"100\" cy=\"100\" fill=\"red\" r=\"20\"/>")
            .contains("<svg:circle cx=\"50\" cy=\"50\" fill=\"red\" r=\"25\"/>")
            .contains("<svg:circle cx=\"70\" cy=\"120\" fill=\"red\" r=\"30\"/>");
    }

    @Test
    void updateSingleNodeTreeFromObject() throws Exception {
        NX.Cursor soapEnvelope = parseResource("soap/soap-request.xml");

        NX.Cursor requestHeader = soapEnvelope.to("header", "requestHeader");
        RequestHeader someHeader = new RequestHeader("n-code", "app-name");
        requestHeader.update(someHeader, new SoapRequestHeaderInserter());

        NX.Cursor reloadedSoapEnvelope = parse(soapEnvelope.dumpXml(UTF_8));
        assertEquals("n-code", reloadedSoapEnvelope.to("header", "requestHeader", "networkCode").text());
        assertEquals("app-name", reloadedSoapEnvelope.to("header", "requestHeader", "applicationName").text());
    }


    @Test
    void insertedElementsInConnectionShouldBePositionedInTheSamePlaceAsThePrototypeElement() throws NX.Ex {
        NX.Cursor root = parse("<root><repeatMe /><fixed>should-not-move</fixed></root>");

        root.insertCollection("repeatMe", Arrays.asList("hei", "på", "deg"), NX.Cursor::text);

        assertThat(root.dumpXml(UTF_8))
            .as("Generated XML")
            .contains("<root><repeatMe");
    }

    @Test
    void insertCollectionMissingPrototypeNode() {
        NX.Cursor peopleCursor = parse("<people><person name='Prototype' /></people>");
        Iterable<String> pirates = Arrays.asList("Sabeltann", "Sortebill");

        try {
            peopleCursor.insertCollection("pirate", pirates, (cursor, input) -> {
                // Doesnt matter
            });
        } catch (NX.MissingNode expected) {
            assertThat(expected)
                .as("Expected exception")
                .hasMessage("people -- Unable to find 'Expected a node named pirate to be used as a prototype'");
        }
    }

    @Test
    void insertAfter() {
        NX.Cursor cursor = parse("<root><a /><c /></root>");
        cursor.appendAfter("b", (c) -> c.name().equals("a"));


        String expectedXml = "<root><a/><b/><c/></root>";
        assertEquals(expectedXml, cursor.dumpXml(UTF_8, DUMP_WITHOUT_XML_DECLARATION));
    }

    @Test
    void insertAfterNoMatch() {
        NX.Cursor cursor = parse("<root><a /><c /></root>");
        cursor.appendAfter("b", (c) -> c.name().equals("no-such-node"));


        String expectedXml = "<root><a/><c/><b/></root>";
        assertEquals(expectedXml, cursor.dumpXml(UTF_8, DUMP_WITHOUT_XML_DECLARATION));
    }


    @Test
    void appendChildToInsertedNode() {
        NX.Cursor cursor = parse("<root><a /><c /></root>");
        NX.Cursor newCursor = cursor.appendAfter("b", (c) -> c.name().equals("c"));

        newCursor.append("child").text("Hello");

        String expectedXml = "<root><a/><c/><b><child>Hello</child></b></root>";
        assertEquals(expectedXml, cursor.dumpXml(UTF_8, DUMP_WITHOUT_XML_DECLARATION));
    }


    static class RequestHeader {

        public final String networkCode;
        public final String applicationName;

        RequestHeader(String networkCode, String applicationName) {
            this.networkCode = networkCode;
            this.applicationName = applicationName;
        }

    }

    static class SoapRequestHeaderInserter implements NX.Inserter<RequestHeader> {

        @Override
        public void insert(NX.Cursor cursor, RequestHeader input) throws NX.Ex {
            cursor.to("applicationName").text(input.applicationName);
            cursor.to("networkCode").text(input.networkCode);
        }

    }


    static class Circle {

        final String color;
        final int centerX, centerY, radius;

        Circle(String color, int centerX, int centerY, int radius) {
            this.color = color;
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }

    }


    static class Person {

        public final String name;

        Person(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person person = (Person) o;
            return name.equals(person.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }

    }


    private static class PersonInserter implements NX.Inserter<Person> {

        @Override
        public void insert(NX.Cursor cursor, Person input) throws NX.Ex {
            cursor.attr("name").text(input.name);
        }

    }

    private static class PersonExtractor implements NX.Extractor<Person> {

        @Override
        public Person transform(NX.Cursor cursor) throws NX.Ex {
            return new Person(cursor.attr("name").text());
        }

    }

    private static class CircleInserter implements NX.Inserter<Circle> {

        @Override
        public void insert(NX.Cursor cursor, Circle circle) throws NX.Ex {
            cursor.attr("fill").text(circle.color);
            cursor.attr("cx").text(String.valueOf(circle.centerX));
            cursor.attr("cy").text(String.valueOf(circle.centerY));
            cursor.attr("r").text(String.valueOf(circle.radius));
        }

    }

}
