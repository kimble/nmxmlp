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

}
