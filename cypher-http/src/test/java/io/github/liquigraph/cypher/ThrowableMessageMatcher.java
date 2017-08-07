package io.github.liquigraph.cypher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ThrowableMessageMatcher extends BaseMatcher<Throwable> {

    private final String expectedCauseMessage;

    private ThrowableMessageMatcher(String expectedCauseMessage) {
        this.expectedCauseMessage = expectedCauseMessage;
    }

    public static ThrowableMessageMatcher hasMessage(String expectedCauseMessage) {
        return new ThrowableMessageMatcher(expectedCauseMessage);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("Expected cause to have message: %s", expectedCauseMessage));
    }

    @Override
    public boolean matches(Object o) {
        if (!(o instanceof Throwable)) {
            return false;
        }
        Throwable actualCause = (Throwable) o;
        return expectedCauseMessage.equals(actualCause.getMessage());
    }
}
