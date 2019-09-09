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
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static com.google.common.base.Charsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class XmlLoadingTest extends AbstractNXTest {

    @Test
    void loadInvalidXML() {
        try {
            parse("<root><unclosedTag></root>");

            fail("Should not have accepted invalid xml");
        }
        catch (NX.Ex ex) {
            assertThat(ex)
                    .as("Expected exception")
                    .hasMessage("Failed to initialize xml cursor");
        }
    }

    @Test
    void loadInvalidXMLFromBytes() {
        try {
            parse(ByteSource.wrap("<root><unclosedTag></root>".getBytes(UTF_8)));

            fail("Should not have accepted invalid xml");
        }
        catch (NX.Ex ex) {
            assertThat(ex)
                    .as("Expected exception")
                    .hasMessage("Failed to initialize xml cursor");
        }
    }

    @Test
    void readingNodeWithDuplicateAttributeTriggersException() {
        try {
            parse("<people><person name='First' name='Second' /></people>");
        }
        catch (NX.Ex ex) {
            assertThat(ex.getCause().getCause())
                    .as("Expected exception")
                    .hasMessageContaining("Attribute \"name\" was already specified for element \"person\"");
        }
    }

    @Test
    void loadValidXML() {
        NX.Cursor cursor = parse("<root><a /></root>");

        assertNotNull(cursor);
        assertEquals("root", cursor.name());
    }

    @Test
    void failingToReadFromByteSource() {
        try {
            parse(new ByteSource() {

                @Override
                public InputStream openStream() {
                    throw new IllegalStateException("Oups...");
                }

            });
        }
        catch (NX.Ex ex) {
            assertThat(ex)
                    .as("Expected exception")
                    .hasMessage("Failed to initialize xml cursor");

            assertThat(ex.getCause())
                    .as("Cause of the expected exception")
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Oups...");
        }
    }

}