package io.github.liquigraph.cypher;

public final class Either<KO, OK> {

    private final KO left;
    private final OK right;

    public static <KO, OK> Either<KO, OK> left(KO error) {
        return new Either<>(error, null);
    }

    public static <KO, OK> Either<KO, OK> right(OK value) {
        return new Either<>(null, value);
    }

    private Either(KO left, OK right) {
        this.left = left;
        this.right = right;
        if ((this.left == null) == (this.right == null)) {
            throw new IllegalArgumentException(String.format("Only left or right must be set, not none, not both%n" +
                    "Left: %s%n" +
                    "Right: %s", this.left, this.right));
        }
    }

    public KO getLeft() {
        if (!isLeft()) {
            throw new IllegalStateException("Attempting to get unset left");
        }
        return left;
    }

    public boolean isLeft() {
        return left != null;
    }

    public OK getRight() {
        if (!isRight()) {
            throw new IllegalStateException("Attempting to get unset right");
        }
        return right;
    }

    public boolean isRight() {
        return right != null;
    }
}
