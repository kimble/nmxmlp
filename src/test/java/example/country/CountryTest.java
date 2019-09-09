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
package example.country;

import com.developerb.nmxmlp.AbstractNXTest;
import com.developerb.nmxmlp.NX;
import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class CountryTest extends AbstractNXTest {

    @Override
    protected void withNx(NX nx) {
        nx.registerExtractor(CountryInformation.Continent.class, cursor ->
                        new CountryInformation.Continent(cursor.attr("continent").text(), cursor.attr("continentName").text())
        );

        nx.registerExtractor(CountryInformation.Country.class, cursor ->
                        new CountryInformation.Country(cursor.attr("countryCode").text(), cursor.attr("countryName").text())
        );
    }

    @Test
    void countCountriesWithMoreThenEightLanguages() {
        long number = streamCountries()
                .filter(info -> info.languages().size() > 8)
                .count();

        assertThat(number)
                .as("Number of countries with more then eight languages")
                .isEqualTo(4);
    }



    private Stream<CountryInformation> streamCountries() {
        URL resource = Resources.getResource("data/countries.xml");
        ByteSource byteSource = Resources.asByteSource(resource);
        NX.Cursor cursor = parse(byteSource);


        return cursor.extractCollection("country", nxCountry -> {
            CountryInformation.Country country = nxCountry.extract(CountryInformation.Country.class);
            CountryInformation.Continent continent = nxCountry.extract(CountryInformation.Continent.class);

            String capital = nxCountry.attr("capital").text();
            String currencyCode = nxCountry.attr("currencyCode").text();
            Integer population = nxCountry.attr("population").text(Integer::parseInt);
            Float area = nxCountry.attr("areaInSqKm").text(Float::parseFloat);
            Iterable<String> languages = nxCountry.attr("languages").text(raw -> Splitter.on(",").split(raw));

            return new CountryInformation (
                    country, continent, capital, population, area, currencyCode, languages
            );
        }).stream();
    }

}
