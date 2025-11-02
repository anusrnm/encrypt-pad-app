This folder should contain the Gradle wrapper JAR: gradle-wrapper.jar

To create the wrapper locally (recommended):

1. Install Gradle locally (or use SDK packages).
2. Run from the project root:

   gradle wrapper --gradle-version 8.1.1

This will populate `gradle/wrapper/gradle-wrapper.jar` and ensure `gradlew` works.

If you cannot run Gradle locally, you may download `gradle-wrapper.jar` from a trusted source or regenerate the wrapper in CI.
