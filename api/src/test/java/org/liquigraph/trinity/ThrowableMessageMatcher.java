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
package org.liquigraph.trinity;

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
