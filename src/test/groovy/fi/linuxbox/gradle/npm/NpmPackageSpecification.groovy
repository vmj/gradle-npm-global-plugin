package fi.linuxbox.gradle.npm

import fi.linuxbox.gradle.npm.global.NpmPackage
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class NpmPackageSpecification extends Specification {

    NpmPackage npmPackage(Map<String, String> props = [:]) {
        final project = ProjectBuilder.builder().build()
        final doc = project.objects.domainObjectContainer(NpmPackage)
        final npmPackage = doc.create(props.get('name', 'n'))
        if (props.containsKey('from'))
            npmPackage.from.set(props.from)
        if (props.containsKey('alias'))
            npmPackage.alias.set(props.alias)
        if (props.containsKey('scope'))
            npmPackage.scope.set(props.scope)
        if (props.containsKey('pkg'))
            npmPackage.pkg.set(props.pkg)
        if (props.containsKey('version'))
            npmPackage.version.set(props.version)
        npmPackage
    }

}
