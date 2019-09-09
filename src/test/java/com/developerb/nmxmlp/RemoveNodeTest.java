package com.developerb.nmxmlp;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;


class RemoveNodeTest extends AbstractNXTest {

    @Test
    void attemptToNavigateToMissingNodeResultsInExceptionWithHelpfulMessage() {
        NX.Cursor cursor = parse("<root><a>a</a><b>b</b></root>");
        cursor.to("a").remove();

        NX.Cursor mutated = parse(cursor.dumpXml(UTF_8));
        assertEquals(0, mutated.count("a"), "Node a should be gone");
        assertEquals(1, mutated.count("b"), "Node b should still be there");
    }

    @Test
    void removingNodeFromEmptyCursorDoesNothing() {
        NX.Cursor cursor = parse("<root><a>a</a><b>b</b></root>");
        cursor.toOptional("no-such-node").remove();
    }

}
