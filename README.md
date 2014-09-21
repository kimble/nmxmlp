No more xml please! (nmxmlp)
============================

This mini project was born out of a frustrated hate of XML, JAXB, XJC, bindings, namespaces, code generation, xpath factories
and the ironically named SimpleNamespaceContext class. Not to mention all the possible ways these technologies makes it 
insanely difficult and time consuming to debug problems... 

I don't want to deal with xpath factories, namespace contexts, compilation of xpath expressions or any of those things!
I'm pretty sure that they're designed by the devil and I want nothing to with any of them.  All I want is a simple 
way to extract and updated data in small-ish XML documents. Not too much to ask for right? None of the existing Java XML 
libraries seems to be able to do this in a convenient, reliable and straight forward way so I created this very 
non-enterprisy API for just this. 
 
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
    
Roadmap
-------
This library has already solved my problem so I may never get around to improve it, but if I do, 
here is a list of things that I'd like to look into. 

1. Reduce the number of methods in the `NX.Cursor` interface.
2. Improve the API dealing with "optional" nodes and the associated dummy implementations of `NX.Cursor` and `NX.Attribute`.
3. Overall I'm quite happy with the API, but it could probably be improved in a number of area.  
4. Increase test coverage. 