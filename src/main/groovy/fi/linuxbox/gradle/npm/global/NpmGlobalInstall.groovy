package fi.linuxbox.gradle.npm.global

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.ysb33r.gradle.nodejs.tasks.NpmCmdlineTask

/**
 * A custom task type for doing {@code npm install -g some-package}.
 */
@CompileStatic
abstract class NpmGlobalInstall extends NpmCmdlineTask {

    /**
     * The source location of the NPM package.
     *
     * Typically the NPM package name and version, separated by an {@code @},
     * but can be something else if using an alias or installing from a URL.
     *
     * Basically, this can be anything that can be written in
     * {@code npm install -g <location>}.
     */
    @Input
    abstract Property<String> getLocation()

    /**
     * The destination directory name under {@code node_modules}.
     *
     * Typically the NPM package name, but can be something else if using an
     * alias or installing from a URL.
     */
    @Input
    abstract Property<String> getLocalName()

    NpmGlobalInstall() {
        super()

        //getForce().convention(false)

        logger.debug "instantiated NpmGlobalInstall task"
        group = 'NPM Global'
        description = "Install and cache the NPM package"

        logger.debug "setting arguments (providers)"
        // NpmCmdLineTask ctor adds 'npm'
        addArgs(['install', '-g'])
        addArgs(location)
        canBeUpToDate = true
        outputs.dir(outputDir(nodejs.executable, localName))
    }

    @PackageScope
    static final Provider<String> outputDir(Provider<File> node,
                                            Provider<String> localName) {
        node.zip(localName, (executable, module) -> {
            executable.parentFile.parentFile
                      .toPath()
                      .resolve("lib")
                      .resolve("node_modules")
                      .resolve(module)
                      .toFile()
                      .canonicalPath
        })
    }

}
