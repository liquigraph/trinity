package io.github.liquigraph.cypher;

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
