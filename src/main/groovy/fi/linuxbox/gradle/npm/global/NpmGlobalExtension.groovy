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

@CompileStatic
abstract class NpmGlobalExtension {
    public static final String NAME = 'npmGlobal'

    abstract NamedDomainObjectContainer<NpmPackage> getPackages()

    private final Logger logger

    @Inject
    NpmGlobalExtension(Project project) {
        logger = project.logger

        packages.whenObjectAdded((NpmPackage npmPackage) -> {
            registerNpmGlobalInstallTask(logger,
                                         project.tasks,
                                         npmPackage)
        })
    }

    /**
     * Registers an {@code installPkg} task, which executes
     * {@code npm install -g from} or
     * {@code npm install -n [alias@npm:][@scope/]pkg[@version]}, but only if
     * some other Gradle task depends on the {@code installPkg} task.
     *
     * @param pkg The NPM package name
     * @param from The URL
     * @param alias
     * @param scope
     * @param version
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

    @PackageScope
    static final void registerNpmGlobalInstallTask(
            Logger logger,
            TaskContainer tasks,
            //NodeJSExtension nodejs,
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

    @PackageScope
    static final String npmPackageInstallTaskName(NpmPackage pkg) {
        "install${pkg.name.capitalize()}"
    }

    /**
     *
     * @param pkg
     * @return
     */
    @PackageScope
    static final Provider<String> npmPackageLocation(NpmPackage pkg) {
        final from = pkg.from
        if (from.present) {
            return from
        }

        // npm install [<@scope>/]<name>
        // npm install [<@scope>/]<name>@<tag>
        // npm install [<@scope>/]<name>@<version>
        // npm install [<@scope>/]<name>@<version range>
        // npm install <alias>@npm:<name>
        final alias = pkg.alias.map { it + '@npm:' }.orElse('')
        final scope = pkg.scope.map { '@' + it + '/' }.orElse('')
        final name = pkg.pkg.orElse(pkg.name)
        final version = pkg.version.map { '@' + it }.orElse('')

        alias.zip(scope, NpmGlobalExtension::concat)
             .zip(name, NpmGlobalExtension::concat)
             .zip(version, NpmGlobalExtension::concat)
    }

    @PackageScope
    static final Provider<String> npmPackageLocalName(NpmPackage pkg) {
        pkg.alias
           .orElse(pkg.pkg)
           .orElse(pkg.name)
    }

    private static String concat(String a, String b) {
        a + b
    }

}
