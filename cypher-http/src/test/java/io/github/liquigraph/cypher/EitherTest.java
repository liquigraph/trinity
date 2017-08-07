/*
 * Copyright 2017 the original author or authors.
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
package io.github.liquigraph.cypher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static io.github.liquigraph.cypher.ThrowableMessageMatcher.hasMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.isA;

public class EitherTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void returns_left_value() {
        Either<String, Void> left = Either.left("foobar");

        assertThat(left.isLeft()).as("Left either must be left").isTrue();
        assertThat(left.isRight()).as("Left either must not be right").isFalse();
        assertThat(left.getLeft()).isEqualTo("foobar");
    }

    @Test
    public void returns_right_value() {
        Either<Void, Integer> right = Either.right(42);

        assertThat(right.isLeft()).as("Right either must not be left").isFalse();
        assertThat(right.isRight()).as("Right either must be right").isTrue();
        assertThat(right.getRight()).isEqualTo(42);
    }

    @Test
    public void fails_at_returning_right_value_from_left_either() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(equalTo("Attempting to get unset right"));

        Either.left(42).getRight();
    }

    @Test
    public void fails_at_returning_left_value_from_right_either() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(equalTo("Attempting to get unset left"));

        Either.right(42).getLeft();
    }

    @Test
    public void fails_at_creating_either_with_both_null_values() throws Exception {
        thrown.expect(InvocationTargetException.class);
        thrown.expectCause(isA(IllegalArgumentException.class));
        thrown.expectCause(hasMessage("Only left or right must be set, not none, not both\n" +
                "Left: null\n" +
                "Right: null"));

        eitherConstructor().newInstance(null, null);
    }

    @Test
    public void fails_at_creating_either_with_both_non_null_values() throws Exception {
        thrown.expect(InvocationTargetException.class);
        thrown.expectCause(isA(IllegalArgumentException.class));
        thrown.expectCause(hasMessage("Only left or right must be set, not none, not both\n" +
                "Left: foo\n" +
                "Right: bar"));

        eitherConstructor().newInstance("foo", "bar");
    }

    @SuppressWarnings("unchecked")
    private Constructor<Either<?, ?>> eitherConstructor() {
        Constructor<Either<?, ?>> result = (Constructor<Either<?, ?>>) Either.class.getDeclaredConstructors()[0];
        result.setAccessible(true);
        return result;
    }

}
