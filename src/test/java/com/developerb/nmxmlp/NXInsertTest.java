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

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Kim A. Betti
 */
public class NXInsertTest {

    @Test
    public void insertCollectionNoNamespaces() throws NX.Ex {
        NX nx = new NX();
        NX.Cursor peopleCursor = nx.from("<people><person name='Prototype' /></people>");

        List<Person> people = Arrays.asList (
                new Person("Nasse Nøff"),
                new Person("Donald Duck")
        );

        peopleCursor.insertCollection("person", people, new PersonInserter());


        NX.Cursor reloadedPeopleCursor = nx.from(peopleCursor.dumpXml());
        List<Person> extractedPeople = reloadedPeopleCursor.extractCollection("person", new PersonExtractor());

        assertEquals(people, extractedPeople);
    }

    @Test
    public void insertNamespacedSvg() throws NX.Ex {
        URL svgResource = Resources.getResource("svg/simple-svg.xhtml");
        ByteSource svgByteSource = Resources.asByteSource(svgResource);

        NX nx = new NX();
        NX.Cursor cursor = nx.from(svgByteSource);
        NX.Cursor svg = cursor.to("body").to("svg");

        List<Circle> circles = Arrays.asList (
            new Circle("red", 100, 100, 20),
            new Circle("red", 50, 50, 25),
            new Circle("red", 70, 120, 30)
        );

        svg.insertCollection("circle", circles, new CircleInserter());

        assertThat(cursor.dumpXml())
                .as("XML output")
                .contains("<svg:circle cx=\"100\" cy=\"100\" fill=\"red\" r=\"20\"/>")
                .contains("<svg:circle cx=\"50\" cy=\"50\" fill=\"red\" r=\"25\"/>")
                .contains("<svg:circle cx=\"70\" cy=\"120\" fill=\"red\" r=\"30\"/>");
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
        public Person transform(NX.NodeCursor cursor) throws NX.Ex {
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
