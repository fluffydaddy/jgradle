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

import io.fluffydaddy.feature.IFeature;
import io.fluffydaddy.feature.ITask;
import io.fluffydaddy.feature.impl.FeatureImpl;
import io.fluffydaddy.jhelper.files.FileHandle;
import io.fluffydaddy.jreactive.Scheduler;
import io.fluffydaddy.jreactive.impl.Subscriber;
import io.fluffydaddy.jutils.collection.Unit;
import org.gradle.wrapper.BootstrapMainStarter;
import org.gradle.wrapper.Download;
import org.gradle.wrapper.DownloadProgressListener;
import org.gradle.wrapper.Install;
import org.gradle.wrapper.Logger;
import org.gradle.wrapper.PathAssembler;
import org.gradle.wrapper.WrapperExecutor;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class Gradle extends Subscriber<GradleWrapperListener> implements DownloadProgressListener, ITask<String[], Void> {
    private static final Map<String, File> sCacheFiles = new ConcurrentHashMap<>();
    
    private static synchronized File getFromCache(String path) {
        if (sCacheFiles.containsKey(path)) {
            return sCacheFiles.get(path);
        }
        File file = new File(path);
        sCacheFiles.put(path, file);
        return file;
    }
    
    public static synchronized Gradle create(boolean hasLogs, BootstrapMainStarter starter) {
        return new Gradle("gradlew", Download.UNKNOWN_VERSION, hasLogs, starter);
    }
    
    private static Map<String, String> convertSystemProperties(Properties properties) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue() == null ? null : entry.getValue().toString());
        }
        return result;
    }
    
    private final BootstrapMainStarter _starter;
    private final Logger _logger;
    private final Download _download;
    
    private Install _install;
    private PathAssembler _assembler;
    private WrapperExecutor _executor;
    private File _gradleUserHome;
    private File _projectDirectory;
    
    private Gradle(String appName, String appVersion, boolean hasLogs, BootstrapMainStarter starter) {
        _starter = starter;
        _logger = new Logger(!hasLogs);
        _download = new Download(_logger, this, appName, appVersion, convertSystemProperties(System.getProperties()));
    }
    
    public Gradle setGradle(File gradleUserHome, File projectDirectory) {
        _executor = WrapperExecutor.forProjectDirectory(projectDirectory);
        _assembler = new PathAssembler(gradleUserHome, projectDirectory);
        _install = new Install(_logger, _download, _assembler);
        _gradleUserHome = gradleUserHome;
        _projectDirectory = projectDirectory;
        return this;
    }
    
    public Gradle setGradle(String gradleUserHome, String projectDirectory) {
        return setGradle(getFromCache(gradleUserHome), getFromCache(projectDirectory));
    }
    
    public Gradle setGradle(FileHandle gradleUserHome, FileHandle projectDirectory) {
        return setGradle(gradleUserHome.file(), projectDirectory.file());
    }
    
    public File getGradleUserHome() {
        return _gradleUserHome;
    }
    
    public File getProjectDirectory() {
        return _projectDirectory;
    }
    
    public PathAssembler getPathAssembler() {
        return _assembler;
    }
    
    @Override
    public void downloadStatusChanged(URI address, long contentLength, long downloaded) {
        int progress = (int) Math.min(1, contentLength / downloaded) * 100;
        forEach((Unit<GradleWrapperListener>) it -> it.onDownload(address, contentLength, progress));
        if (progress == 100) {
            forEach((Unit<GradleWrapperListener>) it -> it.onFinished(address, contentLength, downloaded));
        }
    }
    
    @Override
    public IFeature<String[], Void> schedule(Scheduler scheduler, String... args) {
        return new FeatureImpl<>(a -> {
            execute(a);
            return null;
        }, scheduler, args);
    }
    
    public File install() throws Exception {
        return _install.createDist(_executor.getConfiguration());
    }
    
    public void execute(String... args) throws Exception {
        _executor.execute(args, _install, _starter); // установка/запуск команды.
    }
}