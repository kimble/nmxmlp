No more xml please! (nmxmlp)
============================

This mini project was born of a frustrated hate of XML, JAXB, XJC, bindings, namespaces and the number of ways these technologies
makes it insanely difficult and time consuming to debug problems...


Design goals
------------
These are the design goals behind this mini library in order of importance. 

1. Provide a convenient API to extract and update data from small-ish XML documents.


Constraints
-----------
This will never be efficient on large documents. 
All I wanted was a convenient API for extracting and updating data in small documents.

It requires Java 7 and depends on Joda-Time and Guava. 
These dependencies should be easy enough to get rid of if really don't want them, but in my mind they're just a part of the JDK. 