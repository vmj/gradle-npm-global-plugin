package fi.linuxbox.gradle

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

abstract class GradleSpecification extends Specification {
    static final Set<String> gradleVersions = [
            // https://gradle.org/releases/
            // tag::readme-gradle-versions[]
            '7.5.1', // Aug 05, 2022
            '7.4.2', // Mar 31, 2022
            '7.3.3', // Dec 22, 2021
            '7.2',   // Aug 17, 2021
            '7.1.1', // Jun 14, 2021
            // end::readme-gradle-versions[]
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

    static final String latestGradleVersion = gradleVersions.first()
    static final String oldestGradleVersion = gradleVersions.last()

    @TempDir
    Path testProjectDir
    @TempDir
    File testKitDir

    GradleRunner gradle(String gradleVersion, String... args) {
        GradleRunner
                .create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectDir)
                .withTestKitDir(testKitDir)
                .withPluginClasspath()
                .withArguments(args)
    }

    File getProjectDir() {
        projectFile(null)
    }

    File getBuildScript() {
        projectFile('build.gradle')
    }

    File getSettingsScript() {
        projectFile('settings.gradle')
    }

    File projectFile(String path) {
        if (path == null)
            return testProjectDir.toFile()

        final file = path.split(/\//)
            .inject(testProjectDir, (Path a, String b) -> {
                a.resolve(b)
            })
            .toFile()
        file.parentFile.mkdirs()
        file
    }

}
