package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.GradleSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ReadmeSpec extends GradleSpecification {

    void 'usage'() {
        given:
        buildScript << """
            // tag::readme-usage-build-gradle[]
            plugins {
                id 'fi.linuxbox.npm-global' version '<version>'
            }

            npmGlobal {
                install 'yarn', version: '1.22.19'
                install 'cdk', version: '2.41.0'
            }
            // end::readme-usage-build-gradle[]
        """

        when:
        final build = gradle(latestGradleVersion, 'installCdk').build()

        then:
        build.task(':installCdk').outcome == SUCCESS

        and:
        build.task(':installYarn') == null
    }

    void 'alternate usage'() {
        given:
        buildScript << """
            // tag::readme-alternate-usage-build-gradle[]
            plugins {
                id 'fi.linuxbox.npm-global' version '<version>'
            }

            npmGlobal {
                packages {
                    yarn {
                        version '1.22.0'
                    }
                    cdk {
                        version '2.41.0'
                    }
                }
            }
            // end::readme-alternate-usage-build-gradle[]
        """

        when:
        final build = gradle(latestGradleVersion, 'installCdk').build()

        then:
        build.task(':installCdk').outcome == SUCCESS

        and:
        build.task(':installYarn') == null
    }

    void 'example'() {
        given:
        projectFile("js-app-1/package.json") << """
            {
              "private": "true",
              "dependencies": {
                "object-keys": "^1.1.1"
              }
            }
        """

        and:
        projectFile("js-app-2/package.json") << """
            {
              "private": "true",
              "dependencies": {
                "flat": "^5.0.2"
              }
            }
        """

        and:
        settingsScript << """
            // tag::readme-example-settings-gradle[]
            include 'js-app-1'
            include 'js-app-2'
            include 'backend'
            include 'infra'
            // end::readme-example-settings-gradle[]
        """

        and:
        buildScript << """
            // tag::readme-example-build-gradle[]
            plugins {
                id 'fi.linuxbox.npm-global' version '<version>'
            }

            nodejs {
                useSystemPath() // <1>
            }

            npmGlobal {
                install 'yarn', version: '1.22.19'
            }
            // end::readme-example-build-gradle[]
        """

        and:
        (1..2).each {
            projectFile("js-app-$it/build.gradle") << """
                // tag::readme-example-js-app-build-gradle[]
                plugins {
                    id 'org.ysb33r.nodejs.npm' // <1>
                }

                nodejs {
                    useSystemPath() // <2>
                }

                tasks.register('yarnInstall', org.ysb33r.gradle.nodejs.tasks.NpxCmdlineTask) {
                    dependsOn rootProject.tasks.named('installYarn') // <3>
                    args = 'yarn install'.split() // <4>
                }
                // end::readme-example-js-app-build-gradle[]
            """
        }

        when:
        final build = gradle(latestGradleVersion, 'yarnInstall').build()

        then:
        build.task(':installYarn').outcome == SUCCESS
        build.task(':js-app-1:yarnInstall').outcome == SUCCESS
        build.task(':js-app-2:yarnInstall').outcome == SUCCESS
    }
}
