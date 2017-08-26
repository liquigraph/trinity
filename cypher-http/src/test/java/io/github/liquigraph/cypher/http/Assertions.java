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
package io.github.liquigraph.cypher.http;

import io.github.liquigraph.cypher.Either;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Preconditions;

public class Assertions extends org.assertj.core.api.Assertions {

    public static <L,R> EitherAssert<L,R> assertThat(Either<L,R> either) {
        return new EitherAssert<>(either);
    }
}

class EitherAssert<L,R> extends AbstractAssert<EitherAssert<L,R>, Either<L,R>> {

    public EitherAssert(Either<L, R> either) {
        super(either, EitherAssert.class);
    }

    public EitherAssert<L,R> isLeft() {
        Preconditions.checkNotNull(actual);
        if (!actual.isLeft()) {
            throw new AssertionError(String.format("Expected %s to be left but was right", actual));
        }
        return myself;
    }

    public EitherAssert<L,R> isRight() {
        Preconditions.checkNotNull(actual);
        if (!actual.isRight()) {
            throw new AssertionError(String.format("Expected %s to be right but was left", actual));
        }
        return myself;
    }
}
