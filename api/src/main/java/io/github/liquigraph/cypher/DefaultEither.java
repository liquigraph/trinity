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

public final class DefaultEither<KO, OK> implements Either<KO, OK> {

    private final KO left;
    private final OK right;

    public static <KO, OK> DefaultEither<KO, OK> left(KO error) {
        return new DefaultEither<>(error, null);
    }

    public static <KO, OK> DefaultEither<KO, OK> right(OK value) {
        return new DefaultEither<>(null, value);
    }

    private DefaultEither(KO left, OK right) {
        this.left = left;
        this.right = right;
        if ((this.left == null) == (this.right == null)) {
            throw new IllegalArgumentException(String.format("Only left or right must be set, not none, not both%n" +
                    "Left: %s%n" +
                    "Right: %s", this.left, this.right));
        }
    }

    @Override
    public KO getLeft() {
        if (!isLeft()) {
            throw new IllegalStateException("Attempting to get unset left");
        }
        return left;
    }

    @Override
    public boolean isLeft() {
        return left != null;
    }

    @Override
    public OK getRight() {
        if (!isRight()) {
            throw new IllegalStateException("Attempting to get unset right");
        }
        return right;
    }

    @Override
    public boolean isRight() {
        return right != null;
    }

    @Override
    public String toString() {
        return "Either{" +
            "left=" + left +
            ", right=" + right +
            '}';
    }
}
