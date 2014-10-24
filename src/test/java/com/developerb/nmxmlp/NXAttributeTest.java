package com.developerb.nmxmlp;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Kim A. Betti
 */
public class NXAttributeTest {

    private final String xml = "<root><person firstName='Nasse' lastName='Nøff' age='10' /></root>";


    @Test
    public void readingAttributeAsText() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);
        NX.Cursor person = root.to("person");

        assertEquals("Nasse", person.attr("firstName").text());
        assertEquals("Nøff", person.attr("lastName").text());
    }

    @Test
    public void attemptingToGetMissingAttributeShouldThrowException() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);
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
    public void textContentOfMissingAttributeShouldBeNull() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);
        NX.Cursor person = root.to("person");

        NX.Attribute noSuchAttribute = person.optionalAttr("no-such-attribute");
        assertNull(noSuchAttribute.text());
    }

    @Test
    public void updatingTextOfMissingAttributeShouldDoNothing() throws Exception {
        NX nx = new NX();
        NX.Cursor root = nx.from(xml);
        NX.Cursor person = root.to("person");

        NX.Attribute noSuchAttribute = person.optionalAttr("no-such-attribute");
        noSuchAttribute.text("this text will be dropped on the floor...");
    }

}
