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

[![Build Status](https://snap-ci.com/kimble/nmxmlp/branch/master/build_image)](https://snap-ci.com/kimble/nmxmlp/branch/master)
[![Coverage Status](https://coveralls.io/repos/kimble/nmxmlp/badge.svg?branch=master)](https://coveralls.io/r/kimble/nmxmlp?branch=master)


Design goals - In order of importance
-------------------------------------
1. Provide a convenient API to extract and update data in small-ish XML documents.
2. Don't depend on any external XML APIs.

[![endorse](https://api.coderwall.com/kimble/endorsecount.png)](https://coderwall.com/kimble)

Non goals and constraints
-------------------------
1. Be "correct" - This library is case insensitive and doesn't care about namespaces.
2. Handle large documents - Everything have to fit in memory.

This will never be efficient on large documents.
All I wanted was a convenient API for extracting and updating data in small documents.

It requires Java 6 and depends on Guava. That last dependency should be easy enough to get rid of if really don't
like it, but in my mind it's just a part of the JDK.

Teaser
------
Have a look at the [test cases](https://github.com/kimble/nmxmlp/tree/master/src/test/java) for more examples.

    @Test
    public void updateText() throws Exception {
        NX nx = new NX();
        NX.Cursor person = nx.from("<person><name>Donald Duck</name><age>30</age></person>");
        person.to("name").text("Mikke Mus");

        NX.Cursor reloaded = nx.from(person.dumpXml());
        assertEquals("Mikke Mus", reloaded.to("name").text());
    }

Repository
----------
The latest build is published to Bintray / JCenter. Oh, btw, all the code is implemented as a
single [.java](https://github.com/kimble/nmxmlp/blob/master/src/main/java/com/developerb/nmxmlp/NX.java)
file so depending on your project you might consider just copy'n'pasting the whole thing.

[![Download](https://api.bintray.com/packages/kim-betti/maven/nmxmlp/images/download.png) ](https://bintray.com/kim-betti/maven/nmxmlp/_latestVersion)


Use cases
---------
A short list of things I've successfully applied this project.

### Read any kind of data directly into domain objects

These two approaches are frequent when extracting data from xml with Java.

1. Xml data mapped into hand coded Java classes either assisted by annotations or xml mapping / configuration.
2. Xml data mapped into Java classes auto generated from xsd files.

Common for both approaches is that one rarely want business code to depend upon these classes so you end
up with a hand coded translation layer where one maps data from this object graph and into domain objects.

With this library I skip the entire DTO layer and write code (NX.Extractor) to extract data from xml and
directly into useful domain / business objects.


### Super lightweight soap client

I frequently have to access some SOAP service, but don't want to go through all the hassle of
code generation and re-discovering all the obscurities involved with configuring Java soap clients.

One way to work around this hassle is to use something like Soap-UI to create a sample request for the
service you are calling, place that xml file on classpath and use it as a prototype for requests.


### Super lightweight soap server

From time to time you're forced to expose services to parties refusing to consume anything else
then soap. Use the same approach as outlined above, make sample request and response xml files
available on classpath and set up a Java servlet to expose the service.

By the way, it's a fair amount of hassle and only worth it if you have a small number of really
simple services to expose.



Roadmap 2.0
-----------
1. Java 8
2. Drop Guava
