package com.developerb.nmxmlp;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author Kim A. Betti
 */
public class NXIterationTest {

    @Test
    public void iterate() throws NX.Ex {
        NX nx = new NX();
        NX.Cursor numbersCursor = nx.from("<numbers><n>1</n><n>2</n></numbers>");

        final AtomicInteger sum = new AtomicInteger(0);

        numbersCursor.iterateCollection("n", new NX.Iterator() {

            @Override
            public void on(NX.Cursor cursor) throws NX.Ex {
                sum.addAndGet(cursor.extract(NX.asInteger()));
            }

        });

        assertEquals(3, sum.get());
    }


}
