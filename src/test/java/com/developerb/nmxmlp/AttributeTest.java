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

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class AttributeTest extends AbstractNXTest {

    private final String xml = "<root><person firstName='Nasse' lastName='Nøff' age='10' /></root>";


    @Test
    public void removeAttribute() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        person.removeAttr("age");

        String modifiedXml = root.dumpXml(StandardCharsets.UTF_8);
        NX.Cursor parsed = parse(modifiedXml);
        assertNull(parsed.to("person").optionalAttr("age").text());
    }

    @Test
    public void hasAttr() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        assertTrue(person.hasAttr("firstName"));
        assertFalse(person.hasAttr("no-such-attr"));
        assertFalse(person.toOptional("no-such-node").hasAttr("no-such-attr"));
    }

    @Test
    public void readingAttributeAsText() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        assertEquals("Nasse", person.attr("firstName").text());
        assertEquals("Nøff", person.attr("lastName").text());
    }

    @Test
    public void attemptingToGetMissingAttributeShouldThrowException() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        try {
            person.attr("email");
            fail("Should fail..");
        }
        catch (NX.MissingAttribute ex) {
            assertThat(ex)
                    .as("Exception thrown due to non existing attribute")
                    .hasMessage("root >> person -- Unable to find attribute named 'email'");
        }
    }

    @Test
    public void textContentOfMissingAttributeShouldBeNull() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        NX.Attribute noSuchAttribute = person.optionalAttr("no-such-attribute");
        assertNull(noSuchAttribute.text());
    }

    @Test
    public void updatingTextOfMissingAttributeShouldDoNothing() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        NX.Attribute noSuchAttribute = person.optionalAttr("no-such-attribute");
        noSuchAttribute.text("this text will be dropped on the floor...");
    }

    @Test
    public void mapAttributeTextUsingFunction() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        Integer age = person.attr("age").text(Integer::parseInt);

        assertThat(age)
                .as("Extracted age")
                .isEqualTo(10);
    }

    @Test
    public void mapAttributeTextUsingMethodReference() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        Integer age = person.attr("age").text(Integer::parseInt);

        assertThat(age)
                .as("Extracted age")
                .isEqualTo(10);
    }

    @Test
    public void mapMissingOptionalAttributeReturnsNull() {
        NX.Cursor root = parse(xml);
        NX.Cursor person = root.to("person");

        Integer nothing = person.optionalAttr("no-such-attribute").text(Integer::parseInt);
        assertNull(nothing);
    }

}
