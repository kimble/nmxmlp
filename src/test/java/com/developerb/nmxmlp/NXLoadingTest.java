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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class NXLoadingTest {

    @Test
    public void loadInvalidXML() throws Exception {
        try {
            NX nx = new NX();
            nx.from("<root><unclosedTag></root>");

            fail("Should not have accepted invalid xml");
        }
        catch (NX.Ex ex) {
            assertThat(ex)
                    .as("Expected exception")
                    .hasMessage("Failed to initialize xml cursor");
        }
    }

    @Test
    public void loadValidXML() throws Exception {
        NX nx = new NX();
        NX.Cursor cursor = nx.from("<root><a /></root>");

        assertNotNull(cursor);
        assertEquals("root", cursor.name());
    }

}