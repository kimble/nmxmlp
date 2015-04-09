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

import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class CountryInformation {

    private final Country country;
    private final Continent continent;
    private final String capital;

    private final int population;
    private final float areaInSqKm;

    private final String currencyCode;
    private final Set<String> languages;

    public CountryInformation(Country country, Continent continent, String capital, int population, float areaInSqKm, String currencyCode, Iterable<String> languages) {
        this.country = checkNotNull(country, "country");
        this.continent = checkNotNull(continent, "continent");
        this.capital = checkNotNull(capital, "capital");
        this.population = checkNotNull(population, "population");
        this.areaInSqKm = checkNotNull(areaInSqKm, "area");
        this.currencyCode = checkNotNull(currencyCode, "currency code");
        this.languages = Sets.newTreeSet(checkNotNull(languages, "languages"));
    }

    public Country country() {
        return country;
    }

    public Continent continent() {
        return continent;
    }

    public String capital() {
        return capital;
    }

    public int population() {
        return population;
    }

    public float areaInSqKm() {
        return areaInSqKm;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public Set<String> languages() {
        return languages;
    }


    @Override
    public String toString() {
        return String.format("%s in %s with capital %s, languages %s", country.name(), continent.name(), capital, languages);
    }

    public static class Country {

        private final String code, name;

        public Country(String code, String name) {
            this.code = checkNotNull(code, "code");
            this.name = checkNotNull(name, "name");
        }

        public String code() {
            return code;
        }

        public String name() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Country country = (Country) o;
            return code.equals(country.code) && name.equals(country.name);
        }

        @Override
        public int hashCode() {
            int result = code.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

    }

    public static class Continent {

        private final String code, name;

        public Continent(String code, String name) {
            this.code = checkNotNull(code, "code");
            this.name = checkNotNull(name, "name");
        }

        public String code() {
            return code;
        }

        public String name() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Continent continent = (Continent) o;
            return code.equals(continent.code) && name.equals(continent.name);
        }

        @Override
        public int hashCode() {
            int result = code.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

    }

}
