/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.trinity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void empty_optional_is_not_present() {
        Optional<String> subject = Optional.empty();

        assertThat(subject.isPresent())
            .overridingErrorMessage("Empty optional must not be present")
            .isFalse();
    }

    @Test
    public void non_empty_optional_is_present() {
        Optional<String> subject = Optional.of("foo");

        assertThat(subject.isPresent())
            .overridingErrorMessage("Non-empty optional must be present")
            .isTrue();
    }

    @Test
    public void optional_created_from_null_value_is_not_present() {
        Optional<String> subject = Optional.ofNullable(null);

        assertThat(subject.isPresent())
            .overridingErrorMessage("Optional from null value must not be present")
            .isFalse();
    }

    @Test
    public void optional_created_from_non_null_value_is_present() {
        Optional<String> subject = Optional.ofNullable("bar");

        assertThat(subject.isPresent())
            .overridingErrorMessage("Optional from non-null value must be present")
            .isTrue();
    }

    @Test
    public void getting_a_value_of_an_empty_optional_fails() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot call get() on empty optional");
        Optional<String> subject = Optional.empty();

        subject.get();
    }

    @Test
    public void getting_a_value_of_an_optional_created_from_a_null_value_fails() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cannot call get() on empty optional");
        Optional<String> subject = Optional.ofNullable(null);

        subject.get();
    }

    @Test
    public void a_value_of_an_optional_equals_the_one_it_was_created_from() {
        Object value = new Object();

        Optional<Object> subject = Optional.of(value);

        assertThat(subject.get())
            .overridingErrorMessage("Retrieved value of non-empty optional equals the one it was created with")
            .isEqualTo(value);
    }

    @Test
    public void a_value_of_an_optional_equals_the_one_it_was_created_from_() {
        Object value = new Object();

        Optional<Object> subject = Optional.ofNullable(value);

        assertThat(subject.get())
            .overridingErrorMessage("Retrieved value of non-empty optional equals the one it was created with")
            .isEqualTo(value);
    }
}
