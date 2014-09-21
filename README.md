No more xml please! (nmxmlp)
============================

This mini project was born out of a frustrated hate of XML, JAXB, XJC, bindings, namespaces, code generation, xpath factories
and the ironically named SimpleNamespaceContext class. Not to mention all the possible ways these technologies makes it 
insanely difficult and time consuming to debug problems... 

I don't want to deal with xpath factories, namespace contexts, compilation of xpath expressions or any of those things!
I'm pretty sure that they're designed by the devil and I want nothing to with either of them.  All I wanted was a simple 
way to extract some data from a small-ish sized xml document. Not too much to ask for right? None of the existing Java XML 
libraries seems to be able to do this in a convenient and straight forward way so I created this very non-enterprisy API for just this. 
 
[![Build Status](https://drone.io/github.com/kimble/nmxmlp/status.png)](https://drone.io/github.com/kimble/nmxmlp/latest)


Design goals - In order of importance
-------------------------------------
1. Provide a convenient API to extract and update data from small-ish XML documents.
2. Don't depend on any external XML apis. 

Non design goals
----------------
1. Be "correct" - This library is case insensitive and doesn't care about namespaces. 
2. Handle large documents - Everything have to fit in memory. 


Constraints
-----------
This will never be efficient on large documents. 
All I wanted was a convenient API for extracting and updating data in small documents.

It requires Java 7 and depends on Guava and Commons-Lang3. These dependencies should be easy enough to get rid of if really don't 
want them, but in my mind they're just a part of the JDK. 

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