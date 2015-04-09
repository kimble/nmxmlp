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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MissingNodeTest extends AbstractNXTest {

    private final String xml = "<root><person firstName='Nasse' lastName='Nøff' age='10' /></root>";

    @Test
    public void optionalButExistingAttribute() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        Integer age = person.optionalAttr("age").text(Integer::parseInt);

        assertThat(age)
                .as("Extracted age")
                .isEqualTo(10);
    }

    @Test
    public void attributeOnMissingOptionalNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate"); // Does not exist

        Integer numberOfLegs = pirate.attr("legs").text(Integer::parseInt);
        assertNull(numberOfLegs);
    }

    @Test
    public void optionalAttributeOnMissingOptionalNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate"); // Does not exist

        Integer numberOfLegs = pirate.optionalAttr("legs").text(Integer::parseInt);
        assertNull(numberOfLegs);
    }

    @Test
    public void toMultipleLevelsMissing() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirateLegs = root.toOptional("pirate", "legs"); // Does not exist

        assertNull(pirateLegs.text());
    }

    @Test
    public void updatingTextOfMissingNodesDoesNothing() {
        NX.Cursor root = parse(xml);
        String before = root.dumpXml();

        NX.Cursor pirate = root.toOptional("pirate"); // Does not exist
        pirate.text("who cares");

        assertEquals(before, root.dumpXml());
    }

    @Test
    public void extractCollectionUnderMissingNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate");
        List<Object> birds = pirate.extractCollection("birds", (NX.Cursor cursor) -> null);

        assertThat(birds)
                .as("Collection extracted from a missing node")
                .isEmpty();
    }

    @Test
    public void extractCollectionUnderMissingNodeRegisteredExtractor() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate");
        List<Integer> birds = pirate.extractCollection("birds", Integer.class);

        assertThat(birds)
                .as("Collection extracted from a missing node")
                .isEmpty();
    }

    @Test
    public void extractFromMissingNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate");

        Integer age = pirate.to("age").extract(Integer.class);
        assertNull(age);
    }


    @Test
    public void extractorFromMissingNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate");

        Integer age = pirate.to("age").extract((NX.Cursor cursor) -> Integer.parseInt(cursor.text()));
        assertNull(age);
    }

    @Test
    public void iterateUnderMissingNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate");

        AtomicInteger counter = new AtomicInteger(0);
        pirate.iterateCollection("birds", (NX.Cursor cursor) -> counter.incrementAndGet());

        assertEquals(0, counter.get());
    }

    @Test
    public void toNumberedElementUnderMissingNode() {
        NX.Cursor root = parse(xml);
        NX.Cursor pirate = root.toOptional("pirate");

        String nameOfBirdNumber11 = pirate.to(10, "bird").to("name").text();
        assertNull(nameOfBirdNumber11);
    }

    @Test
    public void insertingCollectionUnderMissingElementDoesNothing() {
        NX.Cursor root = parse(xml);
        String before = root.dumpXml();

        NX.Cursor pirate = root.toOptional("pirate"); // Does not exist
        pirate.insertCollection("bird", Arrays.asList("Polly"), (cursor, input) -> { });

        assertEquals(before, root.dumpXml());
    }

    @Test
    public void updatingNodeUnderMissingElementDoesNothing() {
        NX.Cursor root = parse(xml);
        String before = root.dumpXml();

        NX.Cursor pirate = root.toOptional("pirate"); // Does not exist
        pirate.to(2, "bird").update("Polly", (cursor, input) -> { });

        assertEquals(before, root.dumpXml());
    }

}
