package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.npm.NpmPackageSpecification

import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.npmPackageLocation

class NpmPackageLocationSpec extends NpmPackageSpecification {

    void 'it should produce the correct location'(Map<String, String> props, String location) {
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

    void 'it should update the location if from is updated later'() {
        given:
        final npmPackage = npmPackage from: 'f'
        final location = npmPackageLocation npmPackage

        when:
        npmPackage.from.set('https://example.com/my-package')

        then:
        location.get() == 'https://example.com/my-package'
    }

    void 'it should update the location if alias is updated later'() {
        given:
        final npmPackage = npmPackage alias: 'a'
        final location = npmPackageLocation npmPackage

        when:
        npmPackage.alias.set('local-alias')

        then:
        location.get() == 'local-alias@npm:n'
    }

    void 'it should update the location if scope is updated later'() {
        given:
        final npmPackage = npmPackage scope: 's'
        final location = npmPackageLocation npmPackage

        when:
        npmPackage.scope.set('types')

        then:
        location.get() == '@types/n'
    }

    void 'it should update the location if package name is updated later'() {
        given:
        final npmPackage = npmPackage pkg: 'p'
        final location = npmPackageLocation npmPackage

        when:
        npmPackage.pkg.set('my-package')

        then:
        location.get() == 'my-package'
    }

    void 'it should update the location if version is updated later'() {
        given:
        final npmPackage = npmPackage version: 'v'
        final location = npmPackageLocation npmPackage

        when:
        npmPackage.version.set('1.0')

        then:
        location.get() == 'n@1.0'
    }

    void 'it should update the location if from is added later'() {
        given:
        final npmPackage = npmPackage pkg: 'p'
        final location = npmPackageLocation npmPackage

        when:
        npmPackage.from.set('https://example.com/my-package')

        then:
        location.get() == 'https://example.com/my-package'
    }
}
