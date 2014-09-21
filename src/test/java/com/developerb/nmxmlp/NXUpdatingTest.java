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

import com.google.common.io.Resources;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class NXUpdatingTest {


    @Test
    public void updateText() throws Exception {
        NX nx = new NX();
        NX.Cursor person = nx.from("<person><name>Donald Duck</name><age>30</age></person>");
        person.to("name").text("Mikke Mus");

        NX.Cursor reloaded = nx.from(person.dumpXml());
        assertEquals("Mikke Mus", reloaded.to("name").text());
    }

    @Test
    public void soapRequestPrototype() throws Exception {
        NX nx = new NX();

        URL soapRequestResource = Resources.getResource("soap/soap-request.xml");
        NX.Cursor envelope = nx.from(Resources.asByteSource(soapRequestResource));

        envelope.to("header")
                .to("requestheader")
                .to("networkcode")
                .text("my-network-code");

        NX.Cursor reloadedEnvelope = nx.from(envelope.dumpXml());
        assertEquals("my-network-code", reloadedEnvelope.to("header")
                .to("requestheader")
                .to("networkcode")
                .text());
    }

    @Test
    public void updateSoapRequestAttribute() throws Exception {
        NX nx = new NX();

        URL soapRequestResource = Resources.getResource("soap/soap-request.xml");
        NX.Cursor envelope = nx.from(Resources.asByteSource(soapRequestResource));

        assertEquals("0", envelope.to("header", "requestheader").attr("mustunderstand").text());

        envelope.to("header", "requestheader").attr("mustunderstand").text("1");

        NX.Cursor reloadedEnvelope = nx.from(envelope.dumpXml());
        assertEquals("1", reloadedEnvelope.to("header", "requestheader").attr("mustunderstand").text());
    }

}