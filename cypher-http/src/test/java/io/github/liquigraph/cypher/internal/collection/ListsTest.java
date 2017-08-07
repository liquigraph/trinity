/*
 * Copyright 2014-2016 the original author or authors.
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
package io.github.liquigraph.cypher.internal.collection;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class ListsTest {

    @Property
    public void prepends_head_to_list(String head, ArrayList<String> strings) {
        String[] tail = strings.toArray(new String[strings.size()]);

        List<String> result = Lists.prepend(head, tail);

        assertThat(result)
                .hasSize(1 + tail.length)
                .startsWith(head);

        // open issue: assertj-core/issues/1052
        if (tail.length > 0) {
            assertThat(result).endsWith(tail);
        }
    }
}
