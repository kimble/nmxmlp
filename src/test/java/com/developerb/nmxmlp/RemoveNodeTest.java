package com.developerb.nmxmlp;

import org.junit.Test;

import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;


public class RemoveNodeTest extends AbstractNXTest {

    @Test
    public void attemptToNavigateToMissingNodeResultsInExceptionWithHelpfulMessage() {
        NX.Cursor cursor = parse("<root><a>a</a><b>b</b></root>");
        cursor.to("a").remove();

        NX.Cursor mutated = parse(cursor.dumpXml(UTF_8));
        assertEquals("Node a should be gone", 0, mutated.count("a"));
        assertEquals("Node b should still be there", 1, mutated.count("b"));
    }

    @Test
    public void removingNodeFromEmptyCursorDoesNothing() {
        NX.Cursor cursor = parse("<root><a>a</a><b>b</b></root>");
        cursor.toOptional("no-such-node").remove();
    }

}
