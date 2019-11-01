/*
 * Copyright (c) 2019 Leon Linhart
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
import com.github.gw2toolbelt.build.*
import com.github.gw2toolbelt.build.tasks.*
import org.gradle.api.publish.maven.MavenPom
import org.gradle.internal.jvm.*

plugins {
    `java-library`
    signing
    `maven-publish`
}

val artifactName = "gw2ml"
val nextVersion = "0.2.0"

group = "com.github.gw2toolbelt.gw2ml"
version = when (deployment.type) {
    com.github.gw2toolbelt.build.BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

val currentJVMAtLeast9 = Jvm.current().javaVersion!! >= JavaVersion.VERSION_1_9

java {
    /*
     * Source- and target-compatibility are set here so that an IDE can easily pick them up. They are, however,
     * overwritten by the compileJava task (as part of a workaround).
     */
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileJava {
        /* JDK 8 does not support the --release option */
        if (Jvm.current().javaVersion!! > JavaVersion.VERSION_1_8) {
            // Workaround for https://github.com/gradle/gradle/issues/2510
            options.compilerArgs.addAll(listOf("--release", "8"))
        }
    }

    val compileJava9 = create<JavaCompile>("compileJava9") {
        val ftSource = fileTree("src/main/java-jdk9")
        ftSource.include("**/*.java")
        options.sourcepath = files("src/main/java-jdk9")
        source = ftSource

        classpath = files()
        destinationDir = File(buildDir, "classes/java-jdk9/main")

        sourceCompatibility = "9"
        targetCompatibility = "9"

        // Workaround for https://github.com/gradle/gradle/issues/2510
        options.compilerArgs.addAll(listOf("--release", "9"))

        afterEvaluate {
            // module-path hack
            options.compilerArgs.add("--module-path")
            options.compilerArgs.add(compileJava.get().classpath.asPath)
        }

        /*
         * If the JVM used to invoke Gradle is JDK 9 or later, there is no reason to require a separate JDK 9 instance.
         */
        if (!currentJVMAtLeast9) {
            val jdk9Props = arrayOf(
                "JDK9_HOME",
                "JAVA9_HOME",
                "JDK_9"
            )

            val jdk9Home = jdk9Props.mapNotNull { System.getenv(it) }
                .map { File(it) }
                .firstOrNull(File::exists) ?: throw Error("Could not find valid JDK9 home")
            options.forkOptions.javaHome = jdk9Home
            options.isFork = true
        }
    }

    jar {
        dependsOn(compileJava9)

        archiveBaseName.set(artifactName)

        into("META-INF/versions/9") {
            from(compileJava9.outputs.files.filter(File::isDirectory)) {
                exclude("**/Stub.class")
            }

            includeEmptyDirs = false
        }

        manifest {
            attributes(mapOf(
                "Name" to project.name,
                "Specification-Version" to project.version,
                "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Multi-Release" to "true"
            ))
        }
    }

    create<Jar>("sourcesJar") {
        archiveBaseName.set(artifactName)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)

        into("META-INF/versions/9") {
            from(compileJava9.inputs.files.filter(File::isDirectory)) {
                exclude("**/Stub.java")
            }

            includeEmptyDirs = false
        }
    }

    javadoc {
        with (options as StandardJavadocDocletOptions) {
            tags = listOf(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }

    create<Jar>("javadocJar") {
        dependsOn(javadoc)

        archiveBaseName.set(artifactName)
        archiveClassifier.set("javadoc")
        from(javadoc.get().outputs)
    }

    val compileNative = create<Exec>("compileNative") {
        executable = "cl"
        workingDir = mkdir(File(buildDir, "compileNative/tmp"))

        args("/LD")
        args("/Wall")
        args("/O2")

        inputs.files(fileTree(file("src/main/c")) {
            include("*.c")
            include("*.h")
        })

        val output = File(buildDir, "compileNative/gw2ml.dll")
        outputs.files(output)

        doFirst {
            var jdk8Home = Jvm.current().javaHome
            if (jdk8Home === null || (Jvm.current().javaVersion!! !== JavaVersion.VERSION_1_8)) {
                val jdk8Props = arrayOf(
                    "JDK8_HOME",
                    "JAVA8_HOME",
                    "JDK_8"
                )

                jdk8Home = jdk8Props.mapNotNull { System.getenv(it) }
                    .map { File(it) }
                    .firstOrNull(File::exists) ?: throw Error("Could not find valid JDK8 home")
            }

            args("/I${jdk8Home}/include")
            args("/I${jdk8Home}/include/win32")
            args(inputs.files)
            args("/Fe:${output.absolutePath}")
        }
    }

    val generateNativeModuleInfo = create<GenerateOpenModuleInfo>("generateNativeModuleInfo") {
        moduleName = "com.github.gw2toolbelt.gw2ml.natives"
    }

    val compileNativeModuleInfo = create<JavaCompile>("compileNativeModuleInfo") {
        dependsOn(generateNativeModuleInfo)

        val ftSource = fileTree(generateNativeModuleInfo.outputFile.parentFile)
        ftSource.include("**/*.java")
        options.sourcepath = files(generateNativeModuleInfo.outputFile.parentFile)
        source = ftSource

        classpath = files()
        destinationDir = File(buildDir, "classes/compileNativeModuleInfo/main")

        sourceCompatibility = "9"
        targetCompatibility = "9"

        // Workaround for https://github.com/gradle/gradle/issues/2510
        options.compilerArgs.addAll(listOf("--release", "9"))

        /*
         * If the JVM used to invoke Gradle is JDK 9 or later, there is no reason to require a separate JDK 9 instance.
         */
        if (!currentJVMAtLeast9) {
            val jdk9Props = arrayOf(
                "JDK9_HOME",
                "JAVA9_HOME",
                "JDK_9"
            )

            val jdk9Home = jdk9Props.mapNotNull { System.getenv(it) }
                .map { File(it) }
                .firstOrNull(File::exists) ?: throw Error("Could not find valid JDK9 home")
            options.forkOptions.javaHome = jdk9Home
            options.isFork = true
        }
    }

    create<Jar>("nativeJar") {
        dependsOn(compileNative)
        dependsOn(compileNativeModuleInfo)

        archiveBaseName.set(artifactName)
        archiveClassifier.set("natives-windows")

        into("META-INF/versions/9") {
            from(compileJava9.outputs.files.filter(File::isDirectory)) {
                exclude("**/Stub.class")
            }

            includeEmptyDirs = false
        }

        from(compileNative.outputs) {
            into("windows/x64/com/github/gw2toolbelt/gw2ml")
        }

        manifest {
            attributes(mapOf(
                "Name" to project.name,
                "Specification-Version" to project.version,
                "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Multi-Release" to "true"
            ))
        }
    }

    build {
        dependsOn(compileNative)
        dependsOn(compileNativeModuleInfo)
    }
}

publishing {
    repositories {
        maven {
            url = uri(deployment.repo)

            credentials {
                username = deployment.user
                password = deployment.password
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            artifact(tasks["nativeJar"])

            artifactId = artifactName

            pom {
                name.set(project.name)
                description.set("A Java library for accessing data provided by a Guild Wars 2 game client via the MumbleLink mechanism.")
                packaging = "jar"
                url.set("https://github.com/GW2Toolbelt/GW2ML")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/GW2Toolbelt/GW2ML/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("TheMrMilchmann")
                        name.set("Leon Linhart")
                        email.set("themrmilchmann@gmail.com")
                        url.set("https://github.com/TheMrMilchmann")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/GW2Toolbelt/GW2ML.git")
                    developerConnection.set("scm:git:git://github.com/GW2Toolbelt/GW2ML.git")
                    url.set("https://github.com/GW2Toolbelt/GW2ML.git")
                }
            }
        }
    }
}

signing {
    isRequired = (deployment.type === com.github.gw2toolbelt.build.BuildType.RELEASE)
    sign(publishing.publications)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")
}