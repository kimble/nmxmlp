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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
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
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Kim A. Betti
 */
public class NX {

    private final DocumentBuilder docBuilder;
    private final Transformer transformer;

    public NX(Feature... features) throws Ex {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilderFactory.setNamespaceAware(true); // Without this "localName" won't work for namespaced documents

            for (Feature feature : features) {
                feature.applyTo(transformer);
            }

            this.docBuilder = docBuilderFactory.newDocumentBuilder();
            this.transformer = transformer;
        }
        catch (Exception ex) {
            throw new Ex("Failed to initialize library", ex);
        }
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
        try (InputStream stream = source.openStream()) {
            final Document document = docBuilder.parse(stream);
            return new NodeCursor(document.getDocumentElement());
        }
        catch (Exception ex) {
            throw new Ex("Failed to initialize xml cursor", ex);
        }
    }


    public static interface Extractor<R> {

        R transform(NodeCursor cursor) throws Ex;

    }

    public interface Cursor {

        Cursor to(String firstName, String... remainingNames) throws Ex;

        Cursor update(String tagName, String updatedValue) throws Ex;

        Cursor toOptional(String needle) throws Ex;

        Cursor to(String tagName) throws Ex;

        Cursor to(int position, String tagName) throws MissingNode;

        int count(String tagName);

        <R> R extract(Extractor<R> extractor) throws Ex;

        <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex;

        boolean hasText();

        String text();

        String describePath();

        String name();

        Cursor text(String updatedText);

        String dump() throws Ex;

        Integer integer();

    }


    public class EmptyCursor implements Cursor {


        @Override
        public Cursor to(String firstName, String... remainingNames) throws Ex { return this; }

        @Override
        public Cursor update(String tagName, String updatedValue) throws Ex { return this; }

        @Override
        public Cursor toOptional(String needle) throws Ex { return this; }

        @Override
        public Cursor to(String tagName) throws Ex { return this; }

        @Override
        public Cursor to(int position, String tagName) throws MissingNode { return this; }

        @Override
        public int count(String tagName) { return 0; }

        @Override
        public <R> R extract(Extractor<R> extractor) throws Ex { return null; }

        @Override
        public <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex {
            return Lists.newArrayList();
        }

        @Override
        public boolean hasText() { return false; }

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
        public String dump() throws Ex {
            throw new UnsupportedOperationException("Can't dump empty cursor");
        }

        @Override
        public Integer integer() {
            return null;
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
        public Cursor update(String tagName, String updatedValue) throws Ex {
            to(tagName).text(updatedValue);
            return this;
        }

        @Override
        public Cursor toOptional(String needle) throws Ex {
            final Optional<Node> result = findSingleNode(needle);

            if (result.isPresent()) {
                return to(needle);
            }
            else {
                return new EmptyCursor();
            }
        }

        @Override
        public NodeCursor to(String tagName) throws Ex {
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
        public <R> R extract(Extractor<R> extractor) throws Ex {
            return extractor.transform(this);
        }


        @Override
        public <R> List<R> extractCollection(String needle, Extractor<R> extractor) throws Ex {
            final List<R> result = Lists.newArrayList();
            final NodeList childNodes = node.getChildNodes();

            int count = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                final String localName = childNode.getLocalName();

                if (localName != null && localName.equalsIgnoreCase(needle)) {
                    final List<NodeCursor> newAncestorList = newAncestorList();
                    final Cursor cursor = new NodeCursor(newAncestorList, childNode, count++);
                    final R converted = cursor.extract(extractor);

                    result.add(converted);
                }
            }

            return result;
        }

        @Override
        public boolean hasText() {
            return isNotBlank(text());
        }

        @Override
        public Integer integer() {
            return Integer.parseInt(text());
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
        public String dump() throws Ex {
            try {
                StringWriter sw = new StringWriter();
                transformer.transform(new DOMSource(node), new StreamResult(sw));

                return sw.toString();
            }
            catch (Exception ex) {
                throw new Ex(this, "Technical difficulties", ex);
            }
        }

    }


    public static class Ex extends Exception {

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

            return Joiner.on(", ").join(names);
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
            void applyTo(Transformer t) {
                t.setOutputProperty(OutputKeys.INDENT, "yes");
            }
        },

        DUMP_WITHOUT_XML_DECLARATION {
            @Override
            void applyTo(Transformer t) {
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
        }

        ;

        abstract void applyTo(Transformer t);

    }


}
