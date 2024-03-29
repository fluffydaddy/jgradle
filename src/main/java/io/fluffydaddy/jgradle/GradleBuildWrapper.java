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

import io.fluffydaddy.jbuildsystem.build.BuildWrapper;
import io.fluffydaddy.jhelper.files.FileHandle;
import io.fluffydaddy.jreactive.impl.Subscriber;
import org.gradle.wrapper.BootstrapMainStarter;

import java.io.File;

public abstract class GradleBuildWrapper<R> extends Subscriber<GradleWrapperListener> implements BuildWrapper<R> {
    /**
     * Connects to the build system.
     *
     * @param userHome   The user's home directory.
     * @param projectDir The path to the project directory.
     * @return A new instance of the BuildSystem class.
     */
    @Override
    public GradleBuildSystem<R> connect(File userHome, File projectDir) {
        FileHandle userDir = new FileHandle(userHome);
        FileHandle project = new FileHandle(projectDir);
        GradleBuildSystem<R> gradleBuildSystem = new GradleBuildSystem<>(hasLogging(), createBootstrapMainStarter(project), this);
        gradleBuildSystem.useBuildSystem(userDir, project);
        return gradleBuildSystem;
    }
    
    /**
     * Connects to the build system.
     *
     * @param userHome   The user's home directory.
     * @param projectDir The path to the project directory.
     * @return A new instance of the BuildSystem class.
     */
    @Override
    public GradleBuildSystem<R> connect(String userHome, String projectDir) {
        FileHandle userDir = new FileHandle(userHome);
        FileHandle project = new FileHandle(projectDir);
        GradleBuildSystem<R> gradleBuildSystem = new GradleBuildSystem<>(hasLogging(), createBootstrapMainStarter(project), this);
        gradleBuildSystem.useBuildSystem(userDir, project);
        return gradleBuildSystem;
    }
    
    /**
     * Connects to the build system.
     *
     * @param userHome   The user's home directory.
     * @param projectDir The path to the project directory.
     * @return A new instance of the BuildSystem class.
     */
    @Override
    public GradleBuildSystem<R> connect(FileHandle userHome, FileHandle projectDir) {
        GradleBuildSystem<R> gradleBuildSystem = new GradleBuildSystem<>(hasLogging(), createBootstrapMainStarter(projectDir), this);
        gradleBuildSystem.useBuildSystem(userHome, projectDir);
        return gradleBuildSystem;
    }
    
    public abstract BootstrapMainStarter createBootstrapMainStarter(FileHandle projectDir);
    
    public abstract boolean hasLogging();
}
