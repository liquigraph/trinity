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
package org.liquigraph.trinity.internal;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.liquigraph.trinity.Either;

public final class FunctionalEither<KO, OK> implements Either<KO, OK> {

    private final Optional<KO> left;
    private final Optional<OK> right;

    public static <KO, OK> FunctionalEither<KO, OK> left(KO error) {
        return new FunctionalEither<>(Optional.ofNullable(error), Optional.empty());
    }
    public static <KO, OK> FunctionalEither<KO, OK> right(OK value) {
        return new FunctionalEither<>(Optional.empty(), Optional.ofNullable(value));
    }

    private FunctionalEither(Optional<KO> left, Optional<OK> right) {
        this.left = left;
        this.right = right;
        if (this.left.isPresent() == this.right.isPresent()) {
            throw new IllegalArgumentException(String.format("Only left or right must be set, not none, not both%n" +
                "Left: %s%n" +
                "Right: %s", this.left.orElse(null), this.right.orElse(null)));
        }
    }

    public <L> FunctionalEither<L, OK> mapLeft(Function<KO, L> ifError) {
        return fold(ifError, Function.identity());
    }

    public <R> FunctionalEither<KO, R> mapRight(Function<OK, R> ifOK) {
        return fold(Function.identity(), ifOK);
    }

    public <L,R> FunctionalEither<L, R> fold(Function<KO, L> ifError, Function<OK, R> ifOk) {
        if (isLeft()) {
            return new FunctionalEither<>(Optional.ofNullable(ifError.apply(getLeft())), Optional.empty());
        }
        return new FunctionalEither<>(Optional.empty(), Optional.ofNullable(ifOk.apply(getRight())));
    }

    public void consume(Consumer<KO> ifError, Consumer<OK> ifOk) {
        if (isLeft()) {
            ifError.accept(getLeft());
            return;
        }
        ifOk.accept(getRight());
    }

    @Override
    public KO getLeft() {
        return left.orElse(null);
    }

    @Override
    public boolean isLeft() {
        return left.isPresent();
    }

    @Override
    public OK getRight() {
        return right.orElse(null);
    }

    @Override
    public boolean isRight() {
        return right.isPresent();
    }

    @Override
    public String toString() {
        return "Either{" +
            "left=" + left +
            ", right=" + right +
            '}';
    }
}
