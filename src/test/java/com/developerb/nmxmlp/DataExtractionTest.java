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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class DataExtractionTest extends AbstractNXTest {

    private final String peopleXml = "<people>" +
            "<person><name>Nasse Nøff</name><age>5</age></person>" +
            "<person><name>Ole Brumm</name><age>7</age></person>" +
            "</people>";

    private final String personXml = "<person>" +
            "<name>Nasse Nøff</name>" +
            "<age>5</age>" +
            "</person>";


    @Test
    public void simpleDataExtraction() throws Exception {
        NX.Cursor person = parse(personXml);

        assertEquals("Nasse Nøff", person.to("name").text());
        assertEquals(5, (int) person.to("age").extract(Integer.class));
    }

    @Test
    public void extractDouble() throws Exception {
        NX.Cursor root = parse("<root><double>3.14159265</double></root>");
        Double extracted = root.to("double").extract(Double.class);

        assertEquals(extracted, extracted, 0.00001);
    }

    @Test
    public void extractFloat() throws Exception {
        NX.Cursor root = parse("<root><float>3.14159265</float></root>");
        Float extracted = root.to("float").extract(Float.class);

        assertEquals(extracted, extracted, 0.00001);
    }

    @Test
    public void extractLong() throws Exception {
        NX.Cursor root = parse("<root><long>314159265</long></root>");
        Long extracted = root.to("long").extract(Long.class);

        assertThat(extracted)
                .as("Extracted long")
                .isEqualTo(314159265);
    }

    @Test
    public void countElements() throws Exception {
        NX.Cursor people = parse(peopleXml);

        assertEquals(2, people.count("person"));
    }

    @Test
    public void extractSingleObject() throws Exception {
        NX.Cursor person = parse(personXml);

        NX.Extractor<Person> extractor = new PersonExtractor();
        Person nasseNoff = person.extract(extractor);
        Person expected = new Person("Nasse Nøff", 5);

        assertEquals(expected, nasseNoff);
    }

    @Test
    public void extractObjectCollection() throws Exception {
        NX.Cursor person = parse(peopleXml);

        NX.Extractor<Person> extractor = new PersonExtractor();
        List<Person> people = person.extractCollection("person", extractor);

        assertThat(people)
                .as("Person instances extracted from xml")
                .containsExactly (
                        new Person("Nasse Nøff", 5),
                        new Person("Ole Brumm", 7)
                );
    }


    static class Person {

        public final String name;
        public final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person person = (Person) o;
            return age == person.age && !(name != null ? !name.equals(person.name) : person.name != null);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + age;
            return result;
        }

    }

    static class PersonExtractor implements NX.Extractor<Person> {

        @Override
        public Person transform(NX.Cursor cursor) throws NX.Ex {
            return new Person (
                    cursor.to("name").text(),
                    cursor.to("age").extract(Integer.class)
            );
        }

    }

}