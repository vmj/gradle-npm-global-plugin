package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.npm.NpmPackageSpecification

import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.npmPackageInstallTaskName

class NpmPackageTaskNameSpec extends NpmPackageSpecification {

    void 'it should produce the correct task name'() {
        given:
        final npmPackage = npmPackage name: 'foo'

        expect:
        npmPackageInstallTaskName(npmPackage) == 'installFoo'
    }
}
