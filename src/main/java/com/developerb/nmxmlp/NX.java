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

import com.google.common.base.Joiner;
import com.google.common.io.ByteSource;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

/**
 * No more xml please!
 *
 * @author Kim A. Betti
 */
public class NX {

    private final DocumentBuilderFactory docBuilderFactory;
    private final TransformerFactory transformerFactory;
    private final Map<Class<?>, Extractor<?>> extractors = new HashMap<>();

    public NX() {
        this(Collections.emptySet());
    }

    public NX(Set<ConfigFeature> features) {
        transformerFactory = TransformerFactory.newInstance();
        docBuilderFactory = DocumentBuilderFactory.newInstance();

        // Without this "localName" won't work for namespaced documents
        docBuilderFactory.setNamespaceAware(true);

        docBuilderFactory.setIgnoringElementContentWhitespace(features.contains(ConfigFeature.IGNORE_WHITESPACE));
        docBuilderFactory.setValidating(features.contains(ConfigFeature.VALIDATING));

        // Default extractors
        extractors.put(Integer.class, new IntegerExtractor());
        extractors.put(Long.class, new LongExtractor());
        extractors.put(Float.class, new FloatExtractor());
        extractors.put(Double.class, new DoubleExtractor());
    }


    public <R> NX registerExtractor(Class<R> type, Extractor<R> extractor) {
        extractors.put(type, extractor);
        return this;
    }

    public Cursor from(String xml) throws Ex {
        return from(xml, new ReadContext(null));
    }

    public Cursor from(final String xml, ReadContext context) throws Ex {
        return from(new ByteSource() {

            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream(xml.getBytes());
            }

        }, context);
    }

    public Cursor from(ByteSource source, ReadContext context) throws Ex {
        try {
            return from(source.openStream(), context);
        } catch (Exception ex) {
            throw new Ex("Failed to initialize xml cursor", ex);
        }
    }

    public Cursor from(InputStream stream, ReadContext context) throws Ex {
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) {
                    throw new Ex("Parser warning: " + exception.getMessage(), exception);
                }

                @Override
                public void error(SAXParseException exception) {
                    throw new Ex("Parser error: " + exception.getMessage(), exception);
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    throw new Ex("Parser fatal error: " + exception.getMessage(), exception);
                }

            });

            if (context != null) {
                if (context.entityResolver != null) {
                    docBuilder.setEntityResolver(context.entityResolver);
                }
            }

            final Document document = docBuilder.parse(stream);
            return new NodeCursor(document, document.getDocumentElement());
        } catch (Exception ex) {
            throw new Ex("Failed to initialize xml cursor", ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }


    public interface Extractor<R> {

        R transform(Cursor cursor) throws Ex;

    }

    public interface Iterator {

        void on(Cursor cursor) throws Ex;

    }

    public interface Inserter<R> {

        void insert(Cursor cursor, R input) throws Ex;

    }

    private static class IntegerExtractor implements Extractor<Integer> {

        @Override
        public Integer transform(Cursor cursor) throws Ex {
            return Integer.parseInt(cursor.text());
        }

    }


    private static class LongExtractor implements Extractor<Long> {

        @Override
        public Long transform(Cursor cursor) throws Ex {
            return Long.parseLong(cursor.text());
        }

    }

    private static class DoubleExtractor implements Extractor<Double> {

        @Override
        public Double transform(Cursor cursor) throws Ex {
            return Double.parseDouble(cursor.text());
        }

    }

    private static class FloatExtractor implements Extractor<Float> {

        @Override
        public Float transform(Cursor cursor) throws Ex {
            return Float.parseFloat(cursor.text());
        }

    }


    /**
     * This is the main concept in this API. It's basically a convenient wrapper around
     * a node is a xml document. The cursor can be used to navigate, extract and insert data.
     */
    public interface Cursor {

        Cursor to(String firstName, String... remainingNames) throws Ex;

        Cursor toOptional(String firstNeedle, String... remainingNeedles) throws Ex;

        Cursor to(int position, String tagName) throws MissingNode;

        Cursor append(String nodeName) throws Ex;

        /**
         * Insert a new node after the first predicate match.
         * The node will be inserted at the end if the predicate never matches.
         */
        Cursor appendAfter(String nodeName, Predicate<Cursor> predicate) throws Ex;

        void setAttr(String name, String value) throws Ex;

        int count(String tagName);

        <R> R extract(Extractor<R> extractor) throws Ex;

        <R> R extract(Class<R> type) throws Ex;

        <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex;

        <R> List<R> extractCollection(String needle, Class<R> type) throws Ex;

        void iterateCollection(String needle, Iterator extractor) throws Ex;

        void remove() throws Ex;

        String text();

        String describePath();

        String name();

        void removeAttr(String name) throws Ex;

        Cursor text(String updatedText);

        String dumpXml(Charset charset, Feature... features) throws Ex;

        void dumpXml(OutputStream output, Charset charset, Feature... features) throws Ex;

        Attribute attr(String name) throws Ambiguous, MissingAttribute;

        Attribute optionalAttr(String name) throws Ambiguous;

        /**
         * This method will expect the xml to contain a single node that will be used
         * as a prototype when inserting data from the collection.
         * <p>
         * I've found this to be useful when building soap requests with repeatable
         * elements.
         *
         * @param prototypeName name of the node to be used as a prototype.
         * @param input         collection to be inserted
         * @param inserter      will be called once for every element in the collection
         */
        <R> void insertCollection(String prototypeName, Iterable<R> input, Inserter<R> inserter) throws Ex;

        /**
         * Update a single node
         *
         * @param payload  to be inserted into a node
         * @param inserter that'll update the node based on the payload content
         */
        <R> void update(R payload, Inserter<R> inserter) throws Ex;

        /**
         * @param attributeName
         * @return True if the node has an attribute with the given name
         */
        boolean hasAttr(String attributeName);

        boolean hasChildNode(String name);

        /**
         * @param predicate All child nodes will be passed to this predicate
         * @return A cursor pointing to the only node matching the predicate
         * @throws Ex If the predicate matches zero or more then one node
         */
        NX.Cursor require(Predicate<Cursor> predicate) throws Ex;

    }

    public interface Attribute {

        /**
         * @return The text value of the attribute
         */
        String text();

        /**
         * Map the text value of the attribute by applying the given function.
         * <p>
         * Warning: At some point I'll make this library Java 8 only and replace
         * the Guava function with the Java 8 interface.
         */
        <R> R text(Function<String, R> func);

        /**
         * @param text the new attribute text
         */
        void text(String text);

    }

    private static class RealAttribute implements Attribute {

        private final Node node;

        public RealAttribute(Node node) {
            this.node = node;
        }

        @Override
        public <R> R text(Function<String, R> func) {
            return func.apply(text());
        }

        @Override
        public String text() {
            return node.getTextContent();
        }

        @Override
        public void text(String text) {
            node.setTextContent(text);
        }

    }

    private static class NullAttribute implements Attribute {

        @Override
        public <R> R text(Function<String, R> func) {
            return null;
        }

        @Override
        public String text() {
            return null;
        }

        @Override
        public void text(String text) {
        }

    }


    private class EmptyCursor implements Cursor {

        private final NodeCursor lastKnownCursor;

        EmptyCursor(NodeCursor lastKnownCursor) {
            this.lastKnownCursor = lastKnownCursor;
        }

        @Override
        public Cursor to(String firstName, String... remainingNames) throws Ex {
            return this;
        }

        @Override
        public Cursor toOptional(String firstNeedle, String... reamainingNeedles) throws Ex {
            return this;
        }

        @Override
        public Cursor to(int position, String tagName) throws MissingNode {
            return this;
        }

        @Override
        public int count(String tagName) {
            return 0;
        }

        @Override
        public void iterateCollection(String needle, Iterator extractor) throws Ex { /* ...*/ }

        @Override
        public <R> R extract(Extractor<R> extractor) throws Ex {
            return null;
        }

        @Override
        public <R> R extract(Class<R> type) throws Ex {
            return null;
        }

        @Override
        public <R> List<R> extractCollection(String needle, Class<R> type) throws Ex {
            return new ArrayList<>();
        }

        @Override
        public <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex {
            return new ArrayList<>();
        }

        @Override
        public String text() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public Cursor text(String updatedText) {
            return this;
        }

        @Override
        public String describePath() {
            return lastKnownCursor.describePath() + " >> ???";
        }

        @Override
        public void removeAttr(String name) throws Ex {
        }


        @Override
        public void setAttr(String name, String value) throws Ex {
            throw new UnsupportedOperationException("Can't insert attribute on empty cursor");
        }

        @Override
        public String dumpXml(Charset charset, Feature... features) throws Ex {
            throw new UnsupportedOperationException("Can't dump empty cursor");
        }

        @Override
        public void dumpXml(OutputStream output, Charset charset, Feature... features) throws Ex {
            throw new UnsupportedOperationException("Can't dump empty cursor");
        }

        @Override
        public Cursor append(String nodeName) throws Ex {
            throw new UnsupportedOperationException("Can't insert child node under empty cursor");
        }

        @Override
        public Cursor appendAfter(String nodeName, Predicate<Cursor> predicate) throws Ex {
            throw new UnsupportedOperationException("Can't insert child node under empty cursor");
        }

        @Override
        public Attribute attr(String name) {
            return new NullAttribute();
        }

        @Override
        public Attribute optionalAttr(String name) {
            return new NullAttribute();
        }

        @Override
        public <R> void insertCollection(String prototype, Iterable<R> people, Inserter<R> inserter) throws Ex {
        }

        @Override
        public <R> void update(R payload, Inserter<R> inserter) {
        }

        @Override
        public void remove() throws Ex {
        }

        @Override
        public boolean hasAttr(String attributeName) {
            return false;
        }

        @Override
        public boolean hasChildNode(String name) {
            return false;
        }

        @Override
        public Cursor require(Predicate<Cursor> predicate) throws Ex {
            throw new Ex(this, "Empty cursor, no child nodes");
        }

    }


    private class NodeCursor implements Cursor {

        private final Node node;
        private final List<NodeCursor> ancestors;
        private final int index;
        private final Document document;

        NodeCursor(Document document, Node node) {
            this(document, new ArrayList<>(), node, 0);
        }

        NodeCursor(Document document, List<NodeCursor> ancestors, Node node, int index) {
            if (ancestors == null) {
                throw new IllegalArgumentException("Ancestors can't be null");
            }
            if (node == null) {
                throw new IllegalArgumentException("Node can't be null");
            }
            if (index < 0) {
                throw new IllegalArgumentException("Index must be greater or equal to zero");
            }

            this.document = document;
            this.ancestors = ancestors;
            this.index = index;
            this.node = node;
        }

        @Override
        public Cursor to(String firstName, String... remainingNames) throws Ex {
            Cursor cursor = to(firstName);
            for (String nextName : remainingNames) {
                cursor = cursor.to(nextName);
            }

            return cursor;
        }

        @Override
        public void setAttr(String name, String value) throws Ex {
            ((Element) node).setAttribute(name, value);
        }

        @Override
        public Cursor append(String tagName) throws Ex {
            Element element = document.createElement(tagName);
            Node newNode = node.appendChild(element);

            List<NodeCursor> ancestors = new ArrayList<>(this.ancestors);
            ancestors.add(this);

            return new NodeCursor(document, ancestors, newNode, 0);
        }

        @Override
        public Cursor appendAfter(String tagName, Predicate<Cursor> predicate) throws Ex {
            NodeCursor match = findNode(predicate);

            if (match == null) {
                return append(tagName);
            } else {
                Element element = document.createElement(tagName);
                Node nextSibling = match.node.getNextSibling();
                node.insertBefore(element, nextSibling);

                List<NodeCursor> ancestors = new ArrayList<>(this.ancestors);
                ancestors.add(this);

                return new NodeCursor(document, ancestors, element, 0);
            }
        }

        @Override
        public Cursor toOptional(String firstNeedle, String... remainingNeedles) throws Ex {
            Optional<Node> result = findSingleNode(firstNeedle);
            Cursor cursor = result.isPresent()
                ? to(firstNeedle)
                : new EmptyCursor(this);

            for (String remainingNeedle : remainingNeedles) {
                cursor = cursor.toOptional(remainingNeedle);
            }

            return cursor;
        }

        @Override
        public void removeAttr(String name) throws Ex {
            node.getAttributes().removeNamedItem(name);
        }

        private NodeCursor to(String tagName) throws Ex {
            final Optional<Node> found = findSingleNode(tagName);

            if (found.isPresent()) {
                final List<NodeCursor> newAncestorList = newAncestorList();
                return new NodeCursor(document, newAncestorList, found.get(), 0);
            } else {
                throw new MissingNode(this, tagName, node.getChildNodes());
            }
        }

        private Optional<Node> findSingleNode(String tagName) throws Ambiguous {
            Node found = null;

            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);

                if (isNamed(childNode, tagName)) {
                    if (found != null) {
                        throw new Ambiguous(this, tagName);
                    } else {
                        found = childNode;
                    }
                }
            }

            return Optional.ofNullable(found);
        }

        private boolean isNamed(Node childNode, String needle) {
            String nodeName = childNode.getNodeName();
            String localName = childNode.getLocalName();

            return (nodeName != null && nodeName.equalsIgnoreCase(needle)) || (localName != null && localName.equalsIgnoreCase(needle));
        }

        @Override
        public Cursor to(int position, String tagName) throws MissingNode {
            int count = 0;
            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);

                if (isNamed(childNode, tagName)) {
                    count++;

                    if (count == position + 1) {
                        final List<NodeCursor> newAncestorList = newAncestorList();
                        return new NodeCursor(document, newAncestorList, childNode, position);
                    }
                }
            }

            throw new MissingNode(this, tagName, position, childNodes);
        }

        private List<NodeCursor> newAncestorList() {
            final List<NodeCursor> newAncestorList = new ArrayList<>(ancestors);
            newAncestorList.add(this);

            return newAncestorList;
        }

        @Override
        public int count(String tagName) {
            int count = 0;
            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);

                if (isNamed(childNode, tagName)) {
                    count++;
                }
            }

            return count;
        }

        @Override
        public void remove() throws Ex {
            node.getParentNode().removeChild(node);
        }

        @Override
        public <R> R extract(Class<R> type) throws Ex {
            final Extractor<R> extractor = extractorFor(type);
            return extract(extractor);
        }

        @Override
        public <R> List<R> extractCollection(String needle, Class<R> type) throws Ex {
            final Extractor<R> extractor = extractorFor(type);
            return extractCollection(needle, extractor);
        }

        @SuppressWarnings("unchecked")
        private <R> Extractor<R> extractorFor(Class<R> type) throws NoExtractor {
            final Extractor<R> extractor = (Extractor<R>) extractors.get(type);

            if (extractor == null) {
                throw new NoExtractor(this, type);
            } else {
                return extractor;
            }
        }

        @Override
        public <R> R extract(Extractor<R> extractor) throws Ex {
            return extractor.transform(this);
        }


        @Override
        public <R> List<R> extractCollection(String needle, final Extractor<R> extractor) throws Ex {
            final List<R> result = new ArrayList<>();

            iterateCollection(needle, cursor -> {
                final R converted = cursor.extract(extractor);
                result.add(converted);
            });

            return result;
        }

        @Override
        public void iterateCollection(String needle, Iterator iterator) throws Ex {
            final NodeList childNodes = node.getChildNodes();

            int count = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);

                if (isNamed(childNode, needle)) {
                    final List<NodeCursor> newAncestorList = newAncestorList();
                    final Cursor cursor = new NodeCursor(document, newAncestorList, childNode, count++);
                    iterator.on(cursor);
                }
            }
        }


        @Override
        public <I> void insertCollection(String prototypeName, Iterable<I> inputCollection, Inserter<I> inserter) throws Ex {
            final Optional<Node> prototypeNode = findSingleNode(prototypeName);

            if (prototypeNode.isPresent()) {
                Node originalPrototype = prototypeNode.get();
                Node prototype = originalPrototype.cloneNode(true);

                int count = 0;
                for (I input : inputCollection) {
                    final Node inputNode = prototype.cloneNode(true);
                    final Cursor inputCursor = new NodeCursor(document, newAncestorList(), inputNode, count++);
                    inserter.insert(inputCursor, input);

                    node.insertBefore(inputNode, originalPrototype);
                }

                node.removeChild(originalPrototype);
            } else {
                throw new MissingNode(this, "Expected a node named " + prototypeName + " to be used as a prototype");
            }
        }

        @Override
        public <R> void update(R payload, Inserter<R> inserter) throws Ex {
            inserter.insert(this, payload);
        }

        @Override
        public Attribute attr(String needle) throws Ambiguous, MissingAttribute {
            final Optional<Node> attribute = findAttribute(needle);

            if (attribute.isPresent()) {
                return new RealAttribute(attribute.get());
            } else {
                throw new MissingAttribute(this, needle);
            }
        }

        @Override
        public Attribute optionalAttr(String needle) throws Ambiguous {
            final Optional<Node> attribute = findAttribute(needle);

            if (attribute.isPresent()) {
                return new RealAttribute(attribute.get());
            } else {
                return new NullAttribute();
            }
        }

        @Override
        public boolean hasAttr(String attributeName) {
            return findAttribute(attributeName).isPresent();
        }

        @Override
        public boolean hasChildNode(String name) {
            return findSingleNode(name).isPresent();
        }

        @Override
        public Cursor require(Predicate<Cursor> predicate) throws Ex {
            Cursor match = findNode(predicate);

            if (match == null) {
                throw new MissingNode(this, "predicate");
            } else {
                return match;
            }
        }

        private NodeCursor findNode(Predicate<Cursor> predicate) {
            final NodeList childNodes = node.getChildNodes();
            NodeCursor match = null;

            int count = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                String name = childNode.getLocalName();

                if (name == null) {
                    name = childNode.getNodeName();
                }

                if (name != null) {
                    final List<NodeCursor> newAncestorList = newAncestorList();
                    final NodeCursor cursor = new NodeCursor(document, newAncestorList, childNode, count++);

                    if (predicate.test(cursor)) {
                        if (match != null) {
                            throw new Ambiguous(this);
                        }

                        match = cursor;
                    }
                }
            }

            return match;
        }

        private Optional<Node> findAttribute(String needle) throws Ambiguous {
            NamedNodeMap attributes = node.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);

                if (isNamed(attribute, needle)) {
                    return Optional.of(attribute);
                }
            }

            return Optional.empty();
        }

        @Override
        public String text() {
            return node.getTextContent();
        }

        @Override
        public String name() {
            return node.getNodeName();
        }

        @Override
        public String toString() {
            return describePath();
        }


        @Override
        public String describePath() {
            StringBuilder builder = new StringBuilder();

            for (NodeCursor ancestor : ancestors) {
                builder.append(ancestor.name());

                if (ancestor.index > 0) {
                    builder.append("[")
                        .append(ancestor.index)
                        .append("]");
                }

                builder.append(" >> ");
            }

            builder.append(name());

            if (index > 0) {
                builder.append("[")
                    .append(index)
                    .append("]");
            }

            return builder.toString();
        }

        @Override
        public Cursor text(String updatedText) {
            node.setTextContent(updatedText);
            return this;
        }

        @Override
        public String dumpXml(Charset charset, Feature... features) throws Ex {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            dumpXml(output, charset, features);

            return new String(output.toByteArray(), charset);
        }

        @Override
        public void dumpXml(OutputStream output, Charset charset, Feature... features) throws Ex {
            try {
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, charset.name());

                for (Feature feature : features) {
                    feature.applyTo(transformer, document);
                }

                StreamResult result = new StreamResult(output);
                transformer.transform(new DOMSource(node), result);
            } catch (Exception ex) {
                throw new Ex(this, "Technical difficulties", ex);
            }
        }

    }


    public static class Ex extends RuntimeException {

        Ex(Cursor cursor, String message) {
            super(cursor.describePath() + " -- " + message);
        }

        Ex(Cursor cursor, String message, Throwable cause) {
            super(cursor.describePath() + " -- " + message, cause);
        }

        Ex(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static class NoExtractor extends Ex {

        NoExtractor(Cursor cursor, Class<?> type) {
            super(cursor, "No extractor for: " + type.getName());
        }

    }

    public static class MissingAttribute extends Ex {

        MissingAttribute(Cursor cursor, String attributeName) {
            super(cursor, "Unable to find attribute named '" + attributeName + "'");
        }

    }

    public static class MissingNode extends Ex {
        MissingNode(Cursor cursor, String needle, int position, NodeList childNodes) {
            super(cursor, "Unable to find '" + needle + "' with index " + position + " - Did you mean: " + summarize(childNodes) + "?");
        }

        MissingNode(Cursor cursor, String needle, NodeList childNodes) {
            super(cursor, "Unable to find '" + needle + "' - Did you mean: " + summarize(childNodes) + "?");
        }

        MissingNode(Cursor cursor, String needle) {
            super(cursor, "Unable to find '" + needle + "'");
        }

        private static String summarize(NodeList childNodes) {
            final Set<String> names = new TreeSet<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node item = childNodes.item(i);
                String name = item.getLocalName();

                if (name == null) {
                    name = item.getNodeName();
                }

                if (name != null) {
                    names.add(name);
                }
            }

            return Joiner.on(", ")
                .skipNulls()
                .join(names);
        }
    }

    public static class Ambiguous extends Ex {
        Ambiguous(Cursor cursor, String needle) {
            super(cursor, "Expected to find a single instance of " + needle);
        }

        Ambiguous(Cursor cursor) {
            super(cursor, "Predicate matched more then one child node");
        }
    }

    public enum Feature {

        DUMP_INDENTED_XML {
            @Override
            void applyTo(Transformer transformer, Document document) {
                transformer.setOutputProperty(INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }
        },

        DUMP_WITHOUT_XML_DECLARATION {
            @Override
            void applyTo(Transformer transformer, Document document) {
                transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
            }
        },

        RETAIN_DTD {
            @Override
            void applyTo(Transformer t, Document document) {
                DocumentType documentType = document.getDoctype();
                if (documentType != null) {
                    if (documentType.getSystemId() != null) {
                        t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, documentType.getSystemId());
                    }
                    if (documentType.getPublicId() != null) {
                        t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, documentType.getPublicId());
                    }
                }
            }
        };

        abstract void applyTo(Transformer t, Document document);

    }

    public static class ReadContext {

        private final EntityResolver entityResolver;

        public ReadContext(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
        }

    }

    public enum ConfigFeature {

        IGNORE_WHITESPACE,

        VALIDATING

    }

}
