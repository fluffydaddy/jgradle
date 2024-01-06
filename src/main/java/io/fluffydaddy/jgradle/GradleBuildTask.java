/*
 * Copyright © 2024 fluffydaddy
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

package io.fluffydaddy.jgradle;

import io.fluffydaddy.jbuildsystem.build.BuildListener;
import io.fluffydaddy.jbuildsystem.build.BuildSystem;
import io.fluffydaddy.jbuildsystem.build.service.BuildTask;
import io.fluffydaddy.jutils.collection.Unit;

public class GradleBuildTask<R> extends BuildTask<R> {
    /**
     * @param buildSystem The current build system.
     * @param arguments   The arguments passed to the builder.
     * @return The result of the execution.
     * @throws Exception All possible exceptions.
     */
    @Override
    public R taskBuild(BuildSystem<R> buildSystem, String... arguments) throws Exception {
        buildSystem.forEach((Unit<BuildListener<R>>) it -> it.buildStarted(buildSystem));
        try {
            R result = buildSystem.execute(arguments);
            buildSystem.forEach((Unit<BuildListener<R>>) it -> it.buildComplete(buildSystem, result));
            return result;
        } catch (Exception e) {
            buildSystem.forEach((Unit<BuildListener<R>>) it -> it.buildFailure(buildSystem, e));
        }
        
        return null;
    }
}