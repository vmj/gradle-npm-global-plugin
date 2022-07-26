package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.GradleSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class NpmGlobalPluginSpec extends GradleSpecification {
    private static final Set<String> gradleVersions = [
            // https://gradle.org/releases/
            '7.5',   // Jul 14, 2022
            '7.4.2', // Mar 31, 2022
            '7.3.3', // Dec 22, 2021
            '7.2',   // Aug 17, 2021
            '7.1.1', // Jun 14, 2021
//            '7.0.2', // May 14, 2021
//            '6.9.2', // Dec 21, 2021
//            '6.8.3', // Feb 22, 2021
//            '6.7.1', // Nov 16, 2020
//            '6.6.1', // Aug 25, 2020
//            '6.5.1', // Jun 30, 2020
//            '6.4.1', // May 15, 2020
//            '6.3',   // Mar 24, 2020
//            '6.2.2', // Mar 04, 2020
//            '6.1.1', // Jan 24, 2020
//            '6.0.1', // Nov 18, 2019
//            '5.6.4', // Nov 01, 2019
//            '5.5.1', // Jul 10, 2019
//            '5.4.1', // Apr 26, 2019
//            '5.3.1', // Mar 28, 2019
//            '5.2.1', // Feb 08, 2019
    ]

    void 'it should apply cleanly (Gradle #gradleVersion)'() {
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

    void 'it should allow minimal configuration (Gradle #gradleVersion)'() {
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
        gradleVersion << [gradleVersions.first(), gradleVersions.last()]
    }

    void 'it should allow aliasing (Gradle #gradleVersion)'() {
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
        gradleVersion << [gradleVersions.first(), gradleVersions.last()]
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
        final result = gradle(gradleVersions.first(), 'installObjectKeys').build()

        then:
        result.task(':installObjectKeys').outcome == SUCCESS

        when:
        final result2 = gradle(gradleVersions.first(), 'installObjectKeys').build()

        then:
        result2.task(':installObjectKeys').outcome == UP_TO_DATE
    }

}
