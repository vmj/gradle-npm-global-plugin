package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.GradleSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class NpmGlobalPluginSpec extends GradleSpecification {

    void 'it should apply cleanly (Gradle #gradleVersion)'(String gradleVersion) {
        given:
        buildScript << '''
        plugins {
            id 'fi.linuxbox.npm-global'
        }
        '''

        when:
        final result = gradle(gradleVersion, 'help').build()

        then:
        result.task(':help').outcome == SUCCESS

        where:
        gradleVersion << gradleVersions
    }

    void 'it should allow minimal configuration (Gradle #gradleVersion)'(String gradleVersion) {
        given:
        buildScript << '''
        plugins {
            id 'fi.linuxbox.npm-global'
        }
        
        npmGlobal {
            install 'foo'
            
            packages {
                bar
            }
        }
        '''

        when:
        final result = gradle(gradleVersion, 'tasks').build()

        then:
        result.output.contains('installFoo - Install and cache the NPM package')
        result.output.contains('installBar - Install and cache the NPM package')

        where:
        gradleVersion << [oldestGradleVersion, latestGradleVersion]
    }

    void 'it should allow aliasing (Gradle #gradleVersion)'(String gradleVersion) {
        given:
        buildScript << '''
        plugins {
            id 'fi.linuxbox.npm-global'
        }
        
        npmGlobal {
            install 'foo', alias: 'foo1'
            
            packages {
                bar {
                    alias = 'bar1'
                }
            }
        }
        '''

        when:
        final result = gradle(gradleVersion, 'tasks').build()

        then:
        result.output.contains('installFoo - Install and cache the NPM package')
        result.output.contains('installBar - Install and cache the NPM package')

        where:
        gradleVersion << [oldestGradleVersion, latestGradleVersion]
    }

    void 'it should be cacheable'() {
        given:
        buildScript << '''
        plugins {
            id 'fi.linuxbox.npm-global'
        }
        
        npmGlobal {
            install 'object-keys', version: '1.1.1'
        }
        '''

        when:
        final result = gradle(latestGradleVersion, 'installObjectKeys').build()

        then:
        result.task(':installObjectKeys').outcome == SUCCESS

        when:
        final result2 = gradle(latestGradleVersion, 'installObjectKeys').build()

        then:
        result2.task(':installObjectKeys').outcome == UP_TO_DATE
    }

}
