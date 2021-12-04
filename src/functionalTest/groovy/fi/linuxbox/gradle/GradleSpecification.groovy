package fi.linuxbox.gradle

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

abstract class GradleSpecification extends Specification {
    @TempDir
    Path testProjectDir

    GradleRunner gradle(String gradleVersion, String... args) {
        GradleRunner
                .create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(args)
    }

    File getProjectDir() {
        projectFile(null)
    }

    File getBuildScript() {
        projectFile('build.gradle')
    }

    File projectFile(String path) {
        if (path == null)
            return testProjectDir.toFile()

        path.split(/\//)
            .inject(testProjectDir, (Path a, String b) -> {
                a.resolve(b)
            })
            .toFile()
    }

}
