/*
 * Copyright (c) 2019-2022 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
plugins {
    id("com.gw2tb.maven-publish-conventions")
    `java-library`
}

val artifactName = "gw2ml"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.compilerArgs.add("--enable-preview")
        options.release = 21
    }

    javadoc {
        with (options as StandardJavadocDocletOptions) {
            tags = listOf(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )

            addStringOption("-release", "21")
        }
    }

    withType<Jar>().configureEach {
        archiveBaseName = "gw2ml"

        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            artifact(tasks["nativeWinJar"])

            artifactId = artifactName
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jspecify)
}