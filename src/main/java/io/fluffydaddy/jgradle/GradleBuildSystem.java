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
import io.fluffydaddy.jhelper.files.FileHandle;
import io.fluffydaddy.jutils.collection.Unit;
import io.fluffydaddy.reactive.impl.Subscriber;
import org.gradle.wrapper.BootstrapMainStarter;

public class GradleBuildSystem<R> extends Subscriber<BuildListener<R>> implements BuildSystem<R> {
    private final Gradle _gradle;
    
    public GradleBuildSystem(BootstrapMainStarter starter) {
        _gradle = Gradle.create(starter);
    }
    
    /**
     * @return The name of the build system.
     */
    @Override
    public String getName() {
        return "gradle";
    }
    
    /**
     * @return The display name of the build system.
     */
    @Override
    public String getDisplayName() {
        return "Gradle";
    }
    
    /*
     * @return The build daemon is activated.
     */
    //boolean isAlive();
    
    /**
     * Installs the build system using the project configuration.
     *
     * @return The path to the installed build system.
     */
    @Override
    public FileHandle install() {
        try {
            return new FileHandle(_gradle.install());
        } catch (Exception e) {
            forEach((Unit<BuildListener<R>>) it -> it.buildFailure(GradleBuildSystem.this, e));
            return new FileHandle(_gradle.getGradleHome());
        }
    }
    
    /**
     * Use config on the build system in {@code userHome} using the project configuration in {@code
     * projectDir}.
     *
     * @param userHome   The user's home directory.
     * @param projectDir The path to the project directory.
     * @return Return the current build system.
     */
    @Override
    public GradleBuildSystem<R> useBuildSystem(FileHandle userHome, FileHandle projectDir) {
        _gradle.setGradle(userHome, projectDir);
        return this;
    }
    
    /**
     * Sets a listener for build system events.
     *
     * @param buildListener The listener for build system events.
     * @return Returns the current build system.
     */
    @Override
    public GradleBuildSystem<R> useBuildListener(BuildListener<R> buildListener) {
        subscribe(buildListener);
        return this;
    }
    
    /**
     * Executes the build system with the specified arguments {@code args}.
     *
     * @param args The arguments passed to the build system.
     * @return Returns the current build system.
     */
    @Override
    public GradleBuildSystem<R> execute(String... args) {
        try {
            _gradle.execute(args);
        } catch (Exception e) {
            forEach((Unit<BuildListener<R>>) it -> it.buildFailure(GradleBuildSystem.this, e));
        }
        
        return this;
    }
}
