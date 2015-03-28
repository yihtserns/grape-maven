/*
 * Copyright 2015 yihtserns.
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

package com.github.yihtserns.grape.maven

import groovy.grape.Grape
import groovy.grape.GrapeIvy
import org.junit.Test


/**
 * @author yihtserns
 */
class GrapeMavenTest {

    /**
     * @see https://github.com/groovy/groovy-core/blob/GROOVY_2_3_2/subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleIvyPlugin.groovy#L41
     */
    @Test
    void 'can pretend allow Ivy listener to be added to our fake GrapeIvy'() {
        ((GrapeIvy) Grape.instance).ivyInstance.eventManager.addIvyListener([progress: {}])
    }
}
