package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.npm.NpmPackageSpecification

import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.npmPackageLocation

class NpmPackageLocationSpec extends NpmPackageSpecification {

    void 'it should produce the correct location'() {
        given:
        final npmPackage = npmPackage props

        expect:
        npmPackageLocation(npmPackage).get() == location

        where:
        props                                                       || location
        [from: 'f', alias: 'a', scope: 's', pkg: 'p', version: 'v'] || 'f'
        [from: 'f']                                                 || 'f'
        [:]                                                         || 'n'
        [pkg: 'p']                                                  || 'p'
        [pkg: 'p', version: 'v']                                    || 'p@v'
        [alias: 'a', pkg: 'p', version: 'v']                        || 'a@npm:p@v'
        [scope: 's', pkg: 'p', version: 'v']                        || '@s/p@v'
        [alias: 'a', scope: 's', pkg: 'p', version: 'v']            || 'a@npm:@s/p@v'
    }
}
