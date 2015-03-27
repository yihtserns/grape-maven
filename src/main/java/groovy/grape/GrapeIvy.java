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
package groovy.grape;

import com.github.yihtserns.grape.maven.GrapeMaven;

/**
 * Since there's no way to extends Grape to use a different GrapeEngine, we'd
 * just have to fool Groovy to think this is groovy.grape.GrapeIvy, either by relying on jar loading order
 * or manually deleting org.grape.GrapeIvy that is in Groovy's jar
 *
 * @author yihtserns
 */
public class GrapeIvy extends GrapeMaven {
}
