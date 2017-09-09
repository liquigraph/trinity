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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Incomplete semantic version datastructure
 * It only checks for major.minor.patch and discard additional information
 * such as pre-release version and build metadata.
 * <a href="http://semver.org/spec/v2.0.0.html">SemVer 2.0.0</a>
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
    private static final Pattern STABLE_VERSION = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private final int major;
    private final int minor;
    private final int patch;

    public static SemanticVersion parse(String version) {
        Matcher matcher = STABLE_VERSION.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format(
                "The version must be stable and follow the pattern x.y.z. Actual: %s",
                version
            ));
        }
        return new SemanticVersion(
            Integer.parseInt(matcher.group(1), 10),
            Integer.parseInt(matcher.group(2), 10),
            Integer.parseInt(matcher.group(3), 10)
        );
    }

    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public int compareTo(SemanticVersion version) {
        int majorDiff = this.major - version.major;
        if (majorDiff != 0) {
            return majorDiff;
        }
        int minorDiff = this.minor - version.minor;
        if (minorDiff != 0) {
            return minorDiff;
        }
        return this.patch - version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SemanticVersion other = (SemanticVersion) obj;
        return other.compareTo(this) == 0;
    }

    @Override
    public String toString() {
        return String.format("Version %d.%d.%d", major, minor, patch);
    }
}
