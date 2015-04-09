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

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NXExtractorRegistry {


    @Test
    public void extract() throws NX.Ex {
        NX nx = new NX();
        Member member = nx.registerExtractor(Member.class, new MemberExtractor())
                .from("<person><first>Nasse</first><last>Nøff</last></person>")
                .extract(Member.class);

        Member expected = new Member("Nasse", "Nøff");
        assertEquals(expected, member);
    }

    @Test
    public void extractCollectionWithRegisteredExtractor() throws NX.Ex {
        NX nx = new NX();
        List<Member> members = nx.registerExtractor(Member.class, new MemberExtractor())
                .from("<members><person><first>Nasse</first><last>Nøff</last></person><person><first>Ole</first><last>Brumm</last></person></members>")
                .extractCollection("person", Member.class);


        assertThat(members)
                .as("Extracted member list")
                .containsOnly(
                        new Member("Nasse", "Nøff"),
                        new Member("Ole", "Brumm")
                );
    }

    @Test
    public void missingExtractor() throws NX.Ex {
        NX nx = new NX();

        try {
            nx.from("<person><first>Nasse</first><last>Nøff</last></person>").extract(Member.class);
            fail("Should not have worked");
        }
        catch (NX.NoExtractor ex) {
            assertThat(ex)
                    .as("Expected exception")
                    .hasMessage("person -- No extractor for: " + Member.class.getName());
        }
    }


    static class MemberExtractor implements NX.Extractor<Member> {

        @Override
        public Member transform(NX.Cursor cursor) throws NX.Ex {
            return new Member(
                    cursor.to("first").text(),
                    cursor.to("last").text()
            );
        }

    }


    static class Member {

        final String firstName;
        final String lastName;

        Member(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Member)) return false;

            Member member = (Member) o;

            if (firstName != null ? !firstName.equals(member.firstName) : member.firstName != null) return false;
            if (lastName != null ? !lastName.equals(member.lastName) : member.lastName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = firstName != null ? firstName.hashCode() : 0;
            result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName;
        }

    }

}
