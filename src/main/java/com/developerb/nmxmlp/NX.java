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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

/**
 * No more xml please!
 *
 * @author Kim A. Betti
 */
public class NX {

    private final DocumentBuilderFactory docBuilderFactory;
    private final Transformer transformer;
    private final Map<Class<?>, Extractor<?>> extractors = Maps.newHashMap();

    public NX(Feature... features) throws Ex {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilderFactory.setNamespaceAware(true); // Without this "localName" won't work for namespaced documents

            for (Feature feature : features) {
                feature.applyTo(transformer);
            }

            this.docBuilderFactory = docBuilderFactory;
            this.transformer = transformer;

            extractors.put(Integer.class, new IntegerExtractor());
            extractors.put(Long.class, new LongExtractor());
            extractors.put(Float.class, new FloatExtractor());
            extractors.put(Double.class, new DoubleExtractor());
        }
        catch (Exception ex) {
            throw new Ex("Failed to initialize library", ex);
        }
    }

    public <R> NX registerExtractor(Class<R> type, Extractor<R> extractor) {
        extractors.put(type, extractor);
        return this;
    }

    public Cursor from(final String xml) throws Ex {
        return from(new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return new ByteArrayInputStream(xml.getBytes());
            }

        });
    }

    public Cursor from(ByteSource source) throws Ex {
        InputStream stream = null;

        try {
            stream = source.openStream();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document document = docBuilder.parse(stream);
            return new NodeCursor(document.getDocumentElement());
        }
        catch (Exception ex) {
            throw new Ex("Failed to initialize xml cursor", ex);
        }
        finally {
            Closeables.closeQuietly(stream);
        }
    }


    public static interface Extractor<R> {

        R transform(Cursor cursor) throws Ex;

    }

    public static interface Iterator {

        void on(Cursor cursor) throws Ex;

    }

    public static interface Inserter<R> {

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

        int count(String tagName);

        <R> R extract(Extractor<R> extractor) throws Ex;

        <R> R extract(Class<R> type) throws Ex;

        <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex;

        <R> List<R> extractCollection(String needle, Class<R> type) throws Ex;

        void iterateCollection(String needle, Iterator extractor) throws Ex;

        String text();

        String describePath();

        String name();

        Cursor text(String updatedText);

        String dumpXml() throws Ex;

        Attribute attr(String name) throws Ambiguous, MissingAttribute;

        Attribute optionalAttr(String name) throws Ambiguous;

        /**
         * This method will expect the xml to contain a single node that will be used
         * as a prototype when inserting data from the collection.
         *
         * I've found this to be useful when building soap requests with repeatable
         * elements.
         *
         * @param prototypeName name of the node to be used as a prototype.
         * @param input collection to be inserted
         * @param inserter will be called once for every element in the collection
         */
        <R> void insertCollection(String prototypeName, Iterable<R> input, Inserter<R> inserter) throws Ex;

        /**
         * Update a single node
         *
         * @param payload to be inserted into a node
         * @param inserter that'll update the node based on the payload content
         */
        <R> void update(R payload, Inserter<R> inserter) throws Ex;

    }

    public static interface Attribute {

        /**
         * @return The text value of the attribute
         */
        String text();

        /**
         * Map the text value of the attribute by applying the given function.
         *
         * Warning: At some point I'll make this library Java 8 only and replace
         * the Guava function with the Java 8 interface.
         */
        <R> R text(Function<String, R> func);

        /**
         * @param text the new attribute text
         */
        void text(String text);

    }

    public static class RealAttribute implements Attribute {

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

    public static class NullAttribute implements Attribute {

        @Override
        public <R> R text(Function<String, R> func) { return null; }

        @Override
        public String text() { return null; }

        @Override
        public void text(String text) { }

    }



    public class EmptyCursor implements Cursor {

        @Override
        public Cursor to(String firstName, String... remainingNames) throws Ex { return this; }

        @Override
        public Cursor toOptional(String firstNeedle, String... reamainingNeedles) throws Ex { return this; }

        @Override
        public Cursor to(int position, String tagName) throws MissingNode { return this; }

        @Override
        public int count(String tagName) { return 0; }

        @Override
        public void iterateCollection(String needle, Iterator extractor) throws Ex { /* ...*/ }

        @Override
        public <R> R extract(Extractor<R> extractor) throws Ex { return null; }

        @Override
        public <R> R extract(Class<R> type) throws Ex { return null; }

        @Override
        public <R> List<R> extractCollection(String needle, Class<R> type) throws Ex {
            return Lists.newArrayList();
        }

        @Override
        public <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex {
            return Lists.newArrayList();
        }

        @Override
        public String text() { return null; }

        @Override
        public String name() { return null; }

        @Override
        public Cursor text(String updatedText) { return this; }

        @Override
        public String describePath() {
            throw new UnsupportedOperationException("Can't describe path to empty node");
        }

        @Override
        public String dumpXml() throws Ex {
            throw new UnsupportedOperationException("Can't dump empty cursor");
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
            throw new UnsupportedOperationException("Can't insert collection in empty cursor");
        }

        @Override
        public <R> void update(R payload, Inserter<R> inserter) {
            throw new UnsupportedOperationException("Can't insert in empty cursor");
        }

    }


    public class NodeCursor implements Cursor {

        private final Node node;
        private final List<NodeCursor> ancestors;
        private final int index;

        NodeCursor(Node node) {
            this(new ArrayList<NodeCursor>(), node, 0);
        }

        NodeCursor(List<NodeCursor> ancestors, Node node, int index) {
            Preconditions.checkNotNull(ancestors, "Ancestors can't be null");
            Preconditions.checkArgument(index >= 0, "Index must be greater or equal to zero");
            Preconditions.checkNotNull(node, "Node can't be null");

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
        public Cursor toOptional(String firstNeedle, String... remainingNeedles) throws Ex {
            Optional<Node> result = findSingleNode(firstNeedle);
            Cursor cursor = result.isPresent()
                    ? to(firstNeedle)
                    : new EmptyCursor();

            for (String remainingNeedle : remainingNeedles) {
                cursor = cursor.toOptional(remainingNeedle);
            }

            return cursor;
        }



        private NodeCursor to(String tagName) throws Ex {
            final Optional<Node> found = findSingleNode(tagName);

            if (found.isPresent()) {
                final List<NodeCursor> newAncestorList = newAncestorList();
                return new NodeCursor(newAncestorList, found.get(), 0);
            }
            else {
                throw new MissingNode(this, tagName, node.getChildNodes());
            }
        }

        private Optional<Node> findSingleNode(String tagName) throws Ambiguous {
            Node found = null;

            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                final String localName = childNode.getLocalName();

                if (localName != null && localName.equalsIgnoreCase(tagName)) {
                    if (found != null) {
                        throw new Ambiguous(this, tagName);
                    }
                    else {
                        found = childNode;
                    }
                }
            }

            return Optional.fromNullable(found);
        }

        @Override
        public Cursor to(int position, String tagName) throws MissingNode {
            int count = 0;
            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                final String localName = childNode.getLocalName();

                if (localName != null && localName.equalsIgnoreCase(tagName)) {
                    count++;

                    if (count == position + 1) {
                        final List<NodeCursor> newAncestorList = newAncestorList();
                        return new NodeCursor(newAncestorList, childNode, position);
                    }
                }
            }

            throw new MissingNode(this, tagName, childNodes); // Todo... position..
        }

        private List<NodeCursor> newAncestorList() {
            final List<NodeCursor> newAncestorList = Lists.newArrayList(ancestors);
            newAncestorList.add(this);

            return newAncestorList;
        }

        @Override
        public int count(String tagName) {
            int count = 0;
            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                final String localName = childNode.getLocalName();

                if (localName != null && localName.equalsIgnoreCase(tagName)) {
                    count++;
                }
            }

            return count;
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
            }
            else {
                return extractor;
            }
        }

        @Override
        public <R> R extract(Extractor<R> extractor) throws Ex {
            return extractor.transform(this);
        }


        @Override
        public <R> List<R> extractCollection(String needle, final Extractor<R> extractor) throws Ex {
            final List<R> result = Lists.newArrayList();

            iterateCollection(needle, new Iterator() {

                @Override
                public void on(Cursor cursor) throws Ex {
                    final R converted = cursor.extract(extractor);
                    result.add(converted);
                }

            });

            return result;
        }

        @Override
        public void iterateCollection(String needle, Iterator iterator) throws Ex {
            final NodeList childNodes = node.getChildNodes();

            int count = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                final String localName = childNode.getLocalName();

                if (localName != null && localName.equalsIgnoreCase(needle)) {
                    final List<NodeCursor> newAncestorList = newAncestorList();
                    final Cursor cursor = new NodeCursor(newAncestorList, childNode, count++);
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
                    final Cursor inputCursor = new NodeCursor(newAncestorList(), inputNode, count++);
                    inserter.insert(inputCursor, input);

                    node.insertBefore(inputNode, originalPrototype);
                }

                node.removeChild(originalPrototype);
            }
            else {
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
            }
            else {
                throw new MissingAttribute(this, needle);
            }
        }

        @Override
        public Attribute optionalAttr(String needle) throws Ambiguous {
            final Optional<Node> attribute = findAttribute(needle);

            if (attribute.isPresent()) {
                return new RealAttribute(attribute.get());
            }
            else {
                return new NullAttribute();
            }
        }

        private Optional<Node> findAttribute(String needle) throws Ambiguous {
            NamedNodeMap attributes = node.getAttributes();

            for (int i=0; i<attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);
                final String localName = attribute.getLocalName();

                if (localName.equalsIgnoreCase(needle)) {
                    return Optional.of(attribute);
                }
            }

            return Optional.absent();
        }

        @Override
        public String text() {
            return node.getTextContent();
        }

        @Override
        public String name() {
            return node.getLocalName();
        }

        @Override
        public String toString() {
            return describePath();
        }


        @Override
        public String describePath() {
            StringBuilder builder = new StringBuilder("");

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
        public String dumpXml() throws Ex {
            try {
                StringWriter sw = new StringWriter();
                synchronized (transformer) {
                    transformer.transform(new DOMSource(node), new StreamResult(sw));
                }

                return sw.toString();
            }
            catch (Exception ex) {
                throw new Ex(this, "Technical difficulties", ex);
            }
        }

    }


    public static class Ex extends RuntimeException {

        protected Ex(Cursor cursor, String message) {
            super(cursor.describePath() + " -- " + message);
        }

        protected Ex(Cursor cursor, String message, Throwable cause) {
            super(cursor.describePath() + " -- " + message, cause);
        }

        protected Ex(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static class NoExtractor extends Ex {

        public NoExtractor(Cursor cursor, Class<?> type) {
            super(cursor, "No extractor for: " + type.getName());
        }

    }

    public static class MissingAttribute extends Ex {

        MissingAttribute(Cursor cursor, String attributeName) {
            super(cursor, "Unable to find attribute named '" + attributeName + "'");
        }

    }

    public static class MissingNode extends Ex {
        MissingNode(Cursor cursor, String needle, NodeList childNodes) {
            super(cursor, "Unable to find '" + needle + "' - Did you mean: " + summarize(childNodes) + "?");
        }

        public MissingNode(Cursor cursor, String needle) {
            super(cursor, "Unable to find '" + needle + "'");
        }

        private static String summarize(NodeList childNodes) {
            final Set<String> names = Sets.newTreeSet();
            for (int i=0; i<childNodes.getLength(); i++) {
                final Node item = childNodes.item(i);
                if (item.getLocalName() != null) {
                    names.add(item.getLocalName());
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
    }

    public static enum Feature {

        DUMP_INDENTED_XML {
            @Override
            void applyTo(Transformer transformer) {
                transformer.setOutputProperty(INDENT, "yes");
            }
        },

        DUMP_WITHOUT_XML_DECLARATION {
            @Override
            void applyTo(Transformer transformer) {
                transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
            }
        }

        ;

        abstract void applyTo(Transformer t);

    }


}
