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

}
