/*
 * Copyright (c) 2019-2020 Leon Linhart
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
@file:Suppress("UnstableApiUsage")
import com.gw2tb.gw2ml.build.*
import com.gw2tb.gw2ml.build.BuildType
import com.gw2tb.gw2ml.build.tasks.*
import org.gradle.internal.jvm.*

plugins {
    `java-library`
    signing
    `maven-publish`
}

val artifactName = "gw2ml"
val nextVersion = "2.0.1"

group = "com.gw2tb.gw2ml"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val currentJVM = Jvm.current() ?: error("Failed to detect current JVM.")
val currentJVMVersion = currentJVM.javaVersion ?: error("Failed to detect version of the current JVM.")

val String.toJDKHome get() = (findProperty(this) ?: System.getenv(this))?.let {
    File(it.toString()).also(Jvm::forHome)
} ?: error("Failed to locate JDK: $this")

val jdk8Home by lazy {
    if (currentJVMVersion.isJava8 && currentJVM.javaHome !== null) {
        currentJVM.javaHome!!
    } else {
        "JDK_8".toJDKHome
    }
}
val jdk9Home by lazy {
    if (currentJVMVersion.isJava9 && currentJVM.javaHome !== null) {
        currentJVM.javaHome!!
    } else {
        "JDK_9".toJDKHome
    }
}
val jdk14Home by lazy {
    if (currentJVMVersion == JavaVersion.VERSION_14 && currentJVM.javaHome !== null) {
        currentJVM.javaHome!!
    } else {
        "JDK_14".toJDKHome
    }
}

tasks {
    compileJava {
        /*
         * The main target of this library is Java 8. Thus, if we want to compile it in production mode, either the
         * current JVM needs to support compilation to Java 8 or it must be a JDK 8 installation.
         * The former is achieved by passing the "--release 8" parameter to the compiler. [1]
         * However, there is a bug in JDK 9 preventing usage of the "--release" flag from the JDK Tools API. [2] Thus
         * Gradle cannot invoke the compiler using that API and instead needs to use the command line.
         *
         * [1] https://github.com/gradle/gradle/issues/2510
         * [2] https://bugs.openjdk.java.net/browse/JDK-8139607
         */
        if (currentJVMVersion > JavaVersion.VERSION_1_8) {
            options.compilerArgs.addAll(listOf("--release", "8"))

            if  (currentJVMVersion.isJava9) {
                options.isFork = true
                options.forkOptions.javaHome = jdk8Home
            }
        }
    }

    val compileJava9 = create<JavaCompile>("compileJava9") {
        /*
         * To make the library a fully functional module for Java 9 and later, we make use of multi-release JARs. To be
         * precise: The module descriptor (module-info.class) is placed in /META-INF/versions/9 to be available on
         * Java 9 and later only.
         *
         * (Additional Java 9 specific functionality may also be used and is handled by this task.)
         *
         * Usually we want to pass the "--release 9" parameter to the compiler to specify the target version. [1]
         * Keep in mind however, that there is a bug in JDK 9 preventing usage of the "--release" flag from the JDK
         * Tools API. [2] Since we want to compile using JDK 9 there is no reason to pass the flag when we are running
         * on JDK 9 though.
         * Also there is a bug in javac that causes modular MRJARs to be recognized as automatic modules. This is a
         * warning when javac is invoke via CLI, but seems to be an error when it is invoked via Tools API. Thus Gradle
         * cannot invoke the compiler using that API and needs to use the command line. (This bug only surfaces with an
         * according dependency.)
         *
         * [1] https://github.com/gradle/gradle/issues/2510
         * [2] https://bugs.openjdk.java.net/browse/JDK-8139607
         */
        destinationDir = File(buildDir, "classes/java-jdk9/main")

        val java9Source = fileTree("src/main/java-jdk9") {
            include("**/*.java")
        }

        source = java9Source
        options.sourcepath = files(sourceSets["main"].java.srcDirs) + files(java9Source.dir)

        classpath = files()
        options.release.set(9)

        afterEvaluate {
            options.compilerArgs.add("--module-path")
            options.compilerArgs.add(compileJava.get().classpath.asPath)
        }

        options.forkOptions.javaHome = jdk9Home
        options.isFork = true
    }

    classes {
        dependsOn(compileJava9)
    }

    jar {
        archiveBaseName.set(artifactName)

        into("META-INF/versions/9") {
            from(compileJava9.outputs.files.filter(File::isDirectory))
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
            from(compileJava9.inputs.files.filter(File::isDirectory))
            includeEmptyDirs = false
        }
    }

    javadoc {
        doFirst {
            executable = Jvm.forHome(jdk14Home).javadocExecutable.absolutePath
        }

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

    // TODO the compileNative task still needs a major revamp
    val compileNative = create<Exec>("compileNative") {
        executable = "cl"
        workingDir = mkdir(File(buildDir, "compileNative/tmp"))

        standardOutput = System.out
        errorOutput = System.err

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
            args("/I${jdk8Home}/include")
            args("/I${jdk8Home}/include/win32")
            args(inputs.files)
            args("/Fe:${output.absolutePath}")
        }
    }

    val generateNativeModuleInfo = create<GenerateOpenModuleInfo>("generateNativeModuleInfo") {
        moduleName = "com.gw2tb.gw2ml.natives"
        body = "requires transitive com.gw2tb.gw2ml;"
    }

    val compileNativeModuleInfo = create<JavaCompile>("compileNativeModuleInfo") {
        dependsOn(generateNativeModuleInfo)
        dependsOn(jar)

        destinationDir = File(buildDir, "classes/compileNativeModuleInfo/main")

        val nativeModuleInfoSource = fileTree(generateNativeModuleInfo.outputFile.parentFile) {
            include("**/*.java")
        }

        source = nativeModuleInfoSource
        options.sourcepath = files(nativeModuleInfoSource.dir)

        classpath = files()

        sourceCompatibility = "9"
        targetCompatibility = "9"
        if (!currentJVMVersion.isJava9) options.compilerArgs.addAll(listOf("--release", "9"))

        afterEvaluate {
            options.compilerArgs.add("--module-path")
            options.compilerArgs.add(compileJava.get().classpath.asPath + ";" + jar.get().outputs.files.asPath)
        }

        options.forkOptions.javaHome = jdk9Home
        options.isFork = true
    }

    create<Jar>("nativeJar") {
        dependsOn(compileNative)
        dependsOn(compileNativeModuleInfo)

        archiveBaseName.set(artifactName)
        archiveClassifier.set("natives-windows")

        into("META-INF/versions/9") {
            from(compileNativeModuleInfo.outputs.files.filter(File::isDirectory))
            includeEmptyDirs = false
        }

        from(compileNative.outputs) {
            into("windows/x64/com/gw2tb/gw2ml")
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

    create("buildNativeWindows") {
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
    isRequired = (deployment.type === BuildType.RELEASE)
    sign(publishing.publications)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")
}