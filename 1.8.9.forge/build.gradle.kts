@file:Suppress("UnstableApiUsage")

import java.util.Properties

plugins {
	id("gg.essential.loom") version "1.15.52"
	id("dev.architectury.architectury-pack200") version "0.1.3"
	id("maven-publish")
	id("io.freefair.lombok") version "9.2.0"
	id("com.gradleup.shadow") version "9.6.1"
}

val rootProps = Properties().apply { file("../gradle.properties").reader().use { load(it) } }

val mcVersion = "1.8.9"
val forgeVer = "1.8.9-11.15.1.2318-1.8.9"

group = rootProps.getProperty("maven_group")
version = "${rootProps.getProperty("version")}+$mcVersion-forge"
base.archivesName = rootProps.getProperty("archives_base_name")

java {
	toolchain.languageVersion = JavaLanguageVersion.of(8)
}

loom {
	forge {
		pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
	}
	runConfigs {
		named("client") {
			isIdeConfigGenerated = true
		}
		remove(findByName("server"))
	}
}

repositories {
	maven("https://repo.essential.gg/repository/maven-public/")
	maven("https://maven.minecraftforge.net/")
	mavenCentral()
}

val shade: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(shade)

sourceSets {
	main {
		java.srcDir("../common/src/main/java")
		resources.srcDir("../common/src/main/resources")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:$mcVersion")
	mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
	forge("net.minecraftforge:forge:$forgeVer")

	// Forge 1.8.9 has no jar-in-jar and does not ship these, so they are shaded + relocated.
	shade("it.unimi.dsi:fastutil:8.5.9")
	shade("com.github.weisj:jsvg:1.6.1")
	compileOnly("org.jetbrains:annotations:24.0.1")

	testImplementation(sourceSets.main.get().output)
}

tasks.shadowJar {
	configurations = listOf(shade)
	archiveClassifier = "dev-shadow"
	relocate("it.unimi.dsi.fastutil", "io.github.axolotlclient.rendering.shadow.fastutil")
	relocate("com.github.weisj.jsvg", "io.github.axolotlclient.rendering.shadow.jsvg")
	minimize()
	exclude("META-INF/versions/**", "module-info.class")
}

tasks.remapJar {
	inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
	archiveClassifier = ""
}

tasks.jar {
	archiveClassifier = "dev"
}

tasks.runClient {
	classpath(sourceSets.test.get().runtimeClasspath)
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.processResources {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	inputs.property("version", version)
	filesMatching("mcmod.info") {
		expand("version" to version)
	}
}

// src/test holds the in-game test mod (used by runClient), not unit tests.
tasks.test {
	enabled = false
}

// FML 1.8.9 builds a mod's resource pack from its classes directory, so in dev
// the resources must sit next to the classes or assets (shaders, svg) are not found.
sourceSets.configureEach {
	output.setResourcesDir(java.classesDirectory)
}
