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

public class SemanticVersionTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void compares_major_versions() {
        assertThat(SemanticVersion.parse("1.0.0"))
            .isEqualTo(SemanticVersion.parse("1.0.0"));
        assertThat(SemanticVersion.parse("1.0.0"))
            .isLessThan(SemanticVersion.parse("2.0.0"));
        assertThat(SemanticVersion.parse("3.0.0"))
            .isGreaterThan(SemanticVersion.parse("2.0.0"));
    }

    @Test
    public void compares_minor_versions() {
        assertThat(SemanticVersion.parse("0.1.0"))
            .isEqualTo(SemanticVersion.parse("0.1.0"));
        assertThat(SemanticVersion.parse("0.1.0"))
            .isLessThan(SemanticVersion.parse("0.2.0"));
        assertThat(SemanticVersion.parse("0.3.0"))
            .isGreaterThan(SemanticVersion.parse("0.2.0"));
    }

    @Test
    public void compares_patch_versions() {
        assertThat(SemanticVersion.parse("0.0.1"))
            .isEqualTo(SemanticVersion.parse("0.0.1"));
        assertThat(SemanticVersion.parse("0.0.1"))
            .isLessThan(SemanticVersion.parse("0.0.2"));
        assertThat(SemanticVersion.parse("0.0.3"))
            .isGreaterThan(SemanticVersion.parse("0.0.2"));
    }

    @Test
    public void fails_to_parses_invalid_method() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("he version must be stable and follow the pattern x.y.z. Actual: 1.2.3-SNAPSHOT");

        SemanticVersion.parse("1.2.3-SNAPSHOT");
    }
}
