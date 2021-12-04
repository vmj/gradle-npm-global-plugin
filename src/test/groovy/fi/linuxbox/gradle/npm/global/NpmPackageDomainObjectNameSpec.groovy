package fi.linuxbox.gradle.npm.global

import spock.lang.Specification

import static fi.linuxbox.gradle.npm.global.NpmGlobalExtension.domainObjectName

class NpmPackageDomainObjectNameSpec extends Specification {

    void '#npm -> #don'() {
        expect:
        don == domainObjectName(npm)

        where:
        npm                          || don
        'foo'                        || 'foo'
        'aws-cdk'                    || 'awsCdk'
        'string.prototype.trimstart' || 'stringPrototypeTrimstart'
    }
}
