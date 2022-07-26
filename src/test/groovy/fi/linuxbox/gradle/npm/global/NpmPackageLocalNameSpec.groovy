package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.npm.NpmPackageSpecification

import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.npmPackageLocalName

class NpmPackageLocalNameSpec extends NpmPackageSpecification {

    void 'it should produce the correct local name'() {
        given:
        final npmPackage = npmPackage props

        expect:
        npmPackageLocalName(npmPackage).get() == localName

        where:
        props                  || localName
        [:]                    || 'n'
        [alias: 'a', pkg: 'p'] || 'a'
        [alias: 'a']           || 'a'
        [pkg: 'p']             || 'p'
    }

    void 'it should update the local name if alias is changed later'() {
        given:
        final npmPackage = npmPackage alias: 'a'
        final localName = npmPackageLocalName npmPackage

        when:
        npmPackage.alias.set('local-alias')

        then:
        localName.get() == 'local-alias'
    }

    void 'it should update the local name if the package name is changed later'() {
        given:
        final npmPackage = npmPackage pkg: 'p'
        final localName = npmPackageLocalName npmPackage

        when:
        npmPackage.pkg.set('some-package')

        then:
        localName.get() == 'some-package'
    }

    void 'it should update the local name if the alias is added later'() {
        given:
        final npmPackage = npmPackage pkg: 'p'
        final localName = npmPackageLocalName npmPackage

        when:
        npmPackage.alias.set('local-alias')

        then:
        localName.get() == 'local-alias'
    }
}
