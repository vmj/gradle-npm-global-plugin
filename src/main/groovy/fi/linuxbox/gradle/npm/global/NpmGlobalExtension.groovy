package fi.linuxbox.gradle.npm.global

import groovy.transform.CompileStatic
import groovy.transform.NamedParam
import groovy.transform.NamedVariant
import groovy.transform.PackageScope
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer

import javax.inject.Inject

/**
 * Provides the {@code npmGlobal} extension to project build file.
 */
@CompileStatic
abstract class NpmGlobalExtension {
    public static final String NAME = 'npmGlobal'

    abstract NamedDomainObjectContainer<NpmPackage> getPackages()

    private final Logger logger

    @Inject
    NpmGlobalExtension(Project project) {
        this(project.logger, project.tasks)
    }

    private NpmGlobalExtension(Logger logger, TaskContainer tasks) {
        this.logger = logger

        packages.whenObjectAdded((NpmPackage npmPackage) -> {
            registerNpmGlobalInstallTask(logger, tasks, npmPackage)
        })
    }

    /**
     * Registers an {@code installPkg} task, which executes
     * {@code npm install -g from} or
     * {@code npm install -n [alias@npm:][@scope/]pkg[@version]}, but only if
     * some other Gradle task depends on the {@code installPkg} task.
     *
     * @param pkg The NPM package name
     * @param from The URL from which to install the NPM package
     * @param alias The local name to use for the NPM package
     * @param scope The scope of the package.
     * @param version The version of the package.
     */
    @NamedVariant(coerce = true)
    void install(String pkg,
                 @NamedParam String from = null,
                 @NamedParam String alias = null,
                 @NamedParam String scope = null,
                 @NamedParam String version = null) {
        final name = domainObjectName(pkg)
        logger.debug "registering NpmPackage $name"

        // .create calls synchronously
        //   -> .whenObjectAdded, which calls tasks.register
        //   -> configuration closure
        packages.create(name, (NpmPackage npmPackage) -> {
            logger.debug "configuring NpmPackage $npmPackage.name"
            npmPackage.pkg.set(pkg)
            if (from != null) npmPackage.from.set(from)
            if (alias != null) npmPackage.alias.set(alias)
            if (scope != null) npmPackage.scope.set(scope)
            if (version != null) npmPackage.version.set(version)
            logger.debug "configured NpmPackage $npmPackage.name"
        })
        logger.debug "registered NpmPackage $name"
    }

    /**
     * Derives the Gradle compatible object name for an NPM package name.
     *
     * @param pkg NPM package name
     * @return Gradle domain object name in the npmGlobal extension object
     */
    @PackageScope
    static final String domainObjectName(String pkg) {
        pkg.replaceAll(/(?:[-_.])([a-z1-9])/, { List<String> groups ->
            groups[1].toUpperCase()
        })
    }

    /**
     * Registers a task of type {@link NpmGlobalInstall}, that installs the
     * given {@link NpmPackage}.
     *
     * @param logger Logger to use for debug logging
     * @param tasks The tasks contained into which the task is registered
     * @param npmPackage The package for which to register the task
     */
    @PackageScope
    static final void registerNpmGlobalInstallTask(
            Logger logger,
            TaskContainer tasks,
            NpmPackage npmPackage) {
        final taskName = npmPackageInstallTaskName(npmPackage)

        logger.debug("registering task $taskName")
        tasks.register(taskName, NpmGlobalInstall) {
            logger.debug("configuring task $taskName")
            it.location.set(npmPackageLocation(npmPackage))
            it.localName.set(npmPackageLocalName(npmPackage))
            logger.debug("configured task $taskName")
        }
        logger.debug("registered task $taskName")
    }

    /**
     * Given an {@link NpmPackage}, figures out the Gradle task name to install
     * it.
     *
     * @param pkg The package.
     * @return The task name.
     */
    @PackageScope
    static final String npmPackageInstallTaskName(NpmPackage pkg) {
        "install${pkg.name.capitalize()}"
    }

    /**
     * Given an {@link NpmPackage}, figures out the format the {@code npm}
     * command line argument that can be used to install it.
     *
     * @param pkg The package.
     * @return The location argument.
     */
    @PackageScope
    static final Provider<String> npmPackageLocation(NpmPackage pkg) {
        // npm install [<@scope>/]<name>
        // npm install [<@scope>/]<name>@<tag>
        // npm install [<@scope>/]<name>@<version>
        // npm install [<@scope>/]<name>@<version range>
        // npm install <alias>@npm:<name>
        final alias = pkg.alias.map { it + '@npm:' } orElse('')
        final scope = pkg.scope.map { '@' + it + '/' } orElse('')
        final name = pkg.pkg.orElse(pkg.name)
        final version = pkg.version.map { '@' + it } orElse('')

        pkg.from.orElse(alias.zip(scope, String::concat)
                             .zip(name, String::concat)
                             .zip(version, String::concat))
    }

    /**
     * Given an {@link NpmPackage}, figures out the name of the expected local
     * directory it will land in.
     *
     * @param pkg The package
     * @return The local name
     */
    @PackageScope
    static final Provider<String> npmPackageLocalName(NpmPackage pkg) {
        pkg.alias
           .orElse(pkg.pkg)
           .orElse(pkg.name)
    }

}
