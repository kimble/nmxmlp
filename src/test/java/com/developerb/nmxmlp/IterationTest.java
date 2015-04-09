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

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class IterationTest extends AbstractNXTest {

    @Test
    public void iterate() throws NX.Ex {
        NX.Cursor numbersCursor = parse("<numbers><n>1</n><n>2</n></numbers>");

        AtomicInteger sum = new AtomicInteger(0);
        numbersCursor.iterateCollection("n", cursor -> sum.addAndGet(cursor.extract(Integer.class)));

        assertEquals(3, sum.get());
    }

}
