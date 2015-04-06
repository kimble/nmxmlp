No more xml please! (nmxmlp)
============================

This mini project was born out of a frustrated hate of XML, JAXB, XJC, bindings, namespaces, code generation, xpath factories
and the ironically named SimpleNamespaceContext class. Not to mention all the possible ways these technologies makes it 
insanely difficult and time consuming to debug problems... 

I don't want to deal with xpath factories, namespace contexts, compilation of xpath expressions or any of those things!
I'm pretty sure that they are all designed by the devil and I want nothing to with any of them.  All I want is a simple
way to extract and updated data in small-ish XML documents. Not too much to ask for right? None of the existing Java XML
libraries seems to be able to do this in a convenient, reliable and straight forward way so I created this very non-enterprisy
API doing just that.

[![Build Status](https://drone.io/github.com/kimble/nmxmlp/status.png)](https://drone.io/github.com/kimble/nmxmlp/latest)
[![Download](https://api.bintray.com/packages/kim-betti/maven/nmxmlp/images/download.png) ](https://bintray.com/kim-betti/maven/nmxmlp/_latestVersion)
[![endorse](https://api.coderwall.com/kimble/endorsecount.png)](https://coderwall.com/kimble)


Design goals - In order of importance
-------------------------------------
1. Provide a convenient API to extract and update data in small-ish XML documents.
2. Don't depend on any external XML APIs.

Non design goals and constraints
--------------------------------
1. Be "correct" - This library is case insensitive and doesn't care about namespaces.
2. Handle large documents - Everything have to fit in memory.

This will never be efficient on large documents.
All I wanted was a convenient API for extracting and updating data in small documents.

It requires Java 6 and depends on Guava. That last dependency should be easy enough to get rid of if really don't
like it, but in my mind it's just a part of the JDK.

Teaser
------
Have a look at the test cases for more examples

    @Test
    public void updateText() throws Exception {
        NX nx = new NX();
        NX.Cursor person = nx.from("<person><name>Donald Duck</name><age>30</age></person>");
        person.to("name").text("Mikke Mus");

        NX.Cursor reloaded = nx.from(person.dumpXml());
        assertEquals("Mikke Mus", reloaded.to("name").text());
    }

Roadmap 1.0
-----------
This library has already solved my problems so I may never get around to improve it, but if I do,
here is a list of things that I'd like to look into.

1. Implement some real world examples to flush out api mistakes
2. Add support for extracting different data types then String from attributes
3. Further improvements to error messages
