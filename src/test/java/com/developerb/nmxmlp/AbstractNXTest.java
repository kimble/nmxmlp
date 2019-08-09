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
import org.junit.Before;

import java.net.URL;


public abstract class AbstractNXTest {

    private NX nx;

    @Before
    public final void initialize() {
        this.nx = new NX();
        withNx(nx);
    }

    protected void withNx(NX nx) {
        // A good place for tests to add extractors etc.
    }

    protected NX.Cursor parseResource(String resourceName) {
        URL svgResource = Resources.getResource(resourceName);
        ByteSource svgByteSource = Resources.asByteSource(svgResource);

        return parse(svgByteSource);
    }

    protected NX.Cursor parse(String xml) {
        return nx.from(xml);
    }

    protected NX.Cursor parse(ByteSource source) {
        return nx.from(source, new NX.ReadContext(null));
    }

}
