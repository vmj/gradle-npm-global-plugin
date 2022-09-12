package fi.linuxbox.gradle.npm.global


import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.tasks.NodeCmdlineTask

import javax.annotation.Nullable
import java.nio.file.Path

import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.npmPackageInstallTaskName
import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.npmPackageLocalName

/**
 * Base class for tasks that execute the entrypoint script from an NPM package.
 * <p>
 *     This class is meant to be subclassed.
 * </p>
 * <p>
 *     A typical subclass will override the
 *     {@link NpmPackageCmdlineTask#providesEntrypoint(NpmPackage) providesEntrypoint} and
 *     {@link NpmPackageCmdlineTask#resolveEntrypoint(Path) resolveEntrypoint} methods:
 * </p>
 * <pre lang="groovy"><code>
 *     import fi.linuxbox.gradle.npm.global.NpmPackage
 *     import fi.linuxbox.gradle.npm.global.NpmPackageCmdlineTask
 *     import java.nio.file.Path
 *
 *     abstract class CdkCmdlineTask extends NpmPackageCmdlineTask {
 *         CdkCmdlineTask() {
 *             super()
 *             group = 'CDK tasks'
 *             description = 'Runs cdk with --args'
 *         }
 *         protected boolean providesEntrypoint(NpmPackage pkg) {
 *             pkg.pkg.get() == 'cdk'
 *         }
 *         protected Path resolveEntrypoint(Path nodeModuleDirectory) {
 *             nodeModuleDirectory.resolve('bin').resolve('cdk')
 *         }
 *     }
 * </code></pre>
 * <p>
 *     If those two methods do not meet the needs, see
 *     {@link NpmPackageCmdlineTask#findInstallTask() findInstallTask} and
 *     {@link NpmPackageCmdlineTask#findEntrypoint() findEntrypoint}
 *     method documentation for more extension points.
 * </p>
 */
@CompileStatic
abstract class NpmPackageCmdlineTask extends NodeCmdlineTask {

    NpmPackageCmdlineTask() {
        super()
        group = 'NPM script tasks'
        description = 'Runs NPM script with --args'

        // Wrap in provider so that the NodeJSExtension and the NpmPackage
        // are searched at execution time.
        dependsOn(project.provider { findInstallTask() })
        addArgs(project.provider { findEntrypoint() })
    }

    /**
     * Given an {@link NpmPackage}, returns whether it provides the entrypoint
     * script.
     * <p>
     *     Default implementation returns an unconditional {@code false}.
     * </p>
     *
     * @param pkg An {@link NpmPackage}.
     * @return {@code true} if the {@code pkg} provides the entrypoint script,
     *          and {@code false} otherwise.
     */
    protected boolean providesEntrypoint(NpmPackage pkg) {
        false
    }

    /**
     * Given a path to the module directory, resolves the path to the entry
     * point script.
     * <p>
     *     The module directory corresponds to the {@link NpmPackage} selected
     *     by the {@link #providesEntrypoint} method.
     * </p>
     * <p>
     *     Default implementation throws a {@link RuntimeException}.
     * </p>
     *
     * @param nodeModuleDirectory The path to the node module directory.
     * @return The path to the entry point script.
     */
    protected Path resolveEntrypoint(Path nodeModuleDirectory) {
        throw new RuntimeException("${this.class.name}::resolveEntrypoint: implementation missing")
    }

    /**
     * Find the task that installs the NPM package.
     * <p>
     *     The returned task is added as a dependency of this task.
     * </p>
     * <p>
     *     The default implementation tries to find the {@link NpmPackage}
     *     that provides the entrypoint script. It is searched from the
     *     {@link NpmGlobalExtension} of this project, and parent projects
     *     recursively.  If another strategy for finding it is desired, subclass
     *     can override the {@link #findNpmPackage(Project)} method.
     * </p>
     * <p>
     *     The default implementation then tries to find the task that installs
     *     that package, first from this project and then from parent projects
     *     recursively.  If another strategy for finding the task is desired,
     *     subclass can override the {@link #findInstallTask(Project,String)}
     *     method.
     * </p>
     * <p>
     *     If the extension points mentioned above do not fit, a subclass can
     *     override this method altogether.
     * </p>
     *
     * @throws UnknownTaskException if the task is not found
     * @return The task
     */
    protected TaskProvider<NpmGlobalInstall> findInstallTask() {
        final pkg = findNpmPackage(project)
        if (!pkg) {
            throw new RuntimeException('Unable to find NPM package')
        }
        final task = npmPackageInstallTaskName(pkg)

        findInstallTask(project, task)
    }

    /**
     * Find the path to the entrypoint script.
     * <p>
     *     The returned path is used as the first argument to the Node.js
     *     executable.
     * </p>
     * <p>
     *     The default implementation tries to find the Node.js extension from
     *     this project, and parent projects recursively.  If another strategy
     *     for finding it is desired, subclass can override the
     *     {@link #findNodeJSExtension(Project)} method.
     * </p>
     * <p>
     *     The default implementation then tries to find the {@link NpmPackage}
     *     that provides the entrypoint script. It is searched from the
     *     {@link NpmGlobalExtension} of this project, and parent projects
     *     recursively.  If another strategy for finding it is desired, subclass
     *     can override the {@link #findNpmPackage(Project)} method.
     * </p>
     * <p>
     *     The subclass should implement the
     *     {@link #providesEntrypoint(NpmPackage)} method to select the correct
     *     package.  Typically the matching should be done based on the
     *     {@link NpmPackage#getPkg() NPM package name}.
     * </p>
     * <p>
     *     The default implementation then resolves the path to the local NPM
     *     module directory.  It is unlikely that a subclass should customize
     *     this, but overriding one or more of the {@link #moduleForNode},
     *     {@link #nodeModulesForNode}, and {@link #module} can accomplish that.
     * </p>
     * <p>
     *     Finally, given the resolved local NPM module directory, the
     *     {@link #resolveEntrypoint} is invoked.  A subclass should override it.
     * </p>
     * <p>
     *     The returned string is the {@link File#getCanonicalPath canonical path}
     *     to the {@link #resolveEntrypoint entrypoint}.
     * </p>
     * <p>
     *     If the extension points mentioned above do not fit, a subclass can
     *     override this method altogether.
     * </p>
     *
     * @return Path to the Node.js script that can be used as the entrypoint.
     */
    protected Provider<String> findEntrypoint() {
        final node = findNodeJSExtension(project)
        final pkg = findNpmPackage(project)
        if (!node) {
            throw new RuntimeException("Unable to find node")
        }
        if (!pkg) {
            throw new RuntimeException("Unable to find NPM package")
        }

        final Provider<File> nodejs = node.executable
        final Provider<String> localName = npmPackageLocalName(pkg)

        return moduleForNode(nodejs, localName)
                .map { resolveEntrypoint(it) }
                .map { it.toFile().canonicalPath }
    }

    /**
     * Tries to find the {@link NodeJSExtension} from the given project or its
     * parent projects, recursively.
     *
     * @param project A project
     * @return The first found {@link NodeJSExtension} or {@code null}
     */
    @Nullable
    protected NodeJSExtension findNodeJSExtension(@Nullable Project project) {
        if (!project)
            return null
        final node = project.extensions.findByType(NodeJSExtension)
        if (!node)
            return findNodeJSExtension(project.parent)
        return node
    }

    /**
     * Tries to find the {@link NpmPackage} from the given project or its parent
     * projects, recursively.
     * <p>
     *     The default implementation uses the {@link #providesEntrypoint}
     *     method to select the {@link NpmPackage}.
     * </p>
     *
     * @param project A project
     * @return The first found {@link NpmPackage} or {@code null}
     */
    @Nullable
    protected NpmPackage findNpmPackage(@Nullable Project project) {
        if (!project)
            return null
        final npm = project.extensions.findByType(NpmGlobalExtension)
        final pkg = npm?.packages?.find { providesEntrypoint(it) }
        if (!pkg)
            return findNpmPackage(project.parent)
        return pkg
    }

    /**
     * Tries to find the {@link NpmGlobalInstall} task from the given project or
     * its parent projects, recursively.
     *
     * @param project A project
     * @param taskName Name of the task that install the NPM package.
     * @throws UnknownTaskException if the task is not found
     * @return The first found task
     */
    protected TaskProvider<NpmGlobalInstall> findInstallTask(Project project,
                                                             String taskName) {
        try {
            return project.tasks.named(taskName, NpmGlobalInstall)
        } catch (UnknownTaskException notFound) {
            if (!project.parent)
                throw notFound
            return findInstallTask(project.parent, taskName)
        }
    }

    /**
     * Given a Node.js executable, {@code node}, and a local name for an NPM
     * module, resolves the local path of the NPM module directory.
     * <p>
     *     Uses {@link #nodeModulesForNode} to resolve the {@code node_modules}
     *     directory, and then {@link #module} to resolve the NPM module
     *     directory.
     * </p>
     *
     * @param node The node executable.
     * @param localName The local name of the NPM module.
     * @return The path to the NPM module directory.
     */
    protected Provider<Path> moduleForNode(Provider<File> node,
                                           Provider<String> localName) {
        module(nodeModulesForNode(node), localName)
    }

    /**
     * Given a Node.js executable, resolves the "global" {@code node_modules}
     * directory path for it.
     *
     * @param node The node executable.
     * @return The path to the "global" {@code node_modules} directory.
     */
    protected Provider<Path> nodeModulesForNode(Provider<File> node) {
        node.map(executable -> {
            executable.parentFile.parentFile
                      .toPath()
                      .resolve("lib")
                      .resolve("node_modules")
        })
    }

    /**
     * Given a {@code node_modules} directory and the local module name,
     * resolves the module directory.
     *
     * @param modules a path to {@code node_modules}.
     * @param localName Local module name.
     * @return Path to the module directory.
     */
    protected Provider<Path> module(Provider<Path> modules,
                                    Provider<String> localName) {
        modules.zip(localName, (nodeModules, module) -> {
            nodeModules.resolve(module)
        })
    }

}
