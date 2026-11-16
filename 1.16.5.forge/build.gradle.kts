@file:Suppress("UnstableApiUsage")

import java.util.Properties

// Standalone build (see settings.gradle.kts): Essential Loom gives a real SRG remap and mixin support for
// Forge 1.16.5. The previous moddev.legacyforge setup produced a jar with mixed MCP/SRG names and no mixins.
// The sources are the 1.16.5 Fabric module's (official Mojang names); only the loader glue
// (Rendering, Platform, the test mod) is Forge-specific.
plugins {
	id("gg.essential.loom") version "1.15.52"
	id("maven-publish")
	id("io.freefair.lombok") version "9.2.0"
	id("com.gradleup.shadow") version "9.6.1"
}

val rootProps = Properties().apply { file("../gradle.properties").reader().use { load(it) } }

val mcVersion = "1.16.5"
val forgeVer = "1.16.5-36.2.39"

group = rootProps.getProperty("maven_group")
version = "${rootProps.getProperty("version")}+$mcVersion-forge"
base.archivesName = rootProps.getProperty("archives_base_name")

java {
	toolchain.languageVersion = JavaLanguageVersion.of(17)
}

loom {
	accessWidenerPath.set(file("src/main/resources/axolotlclient-rendering.accesswidener"))
	forge {
		mixinConfig("axolotlclient-rendering.mixins.json")
		convertAccessWideners.set(true)
		extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
	}
	mods {
		create("axolotlclient_rendering") {
			sourceSet(sourceSets.main.get())
		}
		create("axolotlclient_rendering_test") {
			sourceSet(sourceSets.test.get())
		}
	}
	runConfigs {
		named("client") {
			isIdeConfigGenerated = true
		}
		remove(findByName("server"))
	}
}

repositories {
	// Forge's maven has dropped or breaks much of the 1.16.5 toolchain, so a project-local repo pins what is gone:
	//  - mcp_config: identical to the original except config.json, whose tools repo (the dead
	//    files.minecraftforge.net) is replaced with maven.neoforged.net, which still hosts mergetool & co.
	//  - forge 36.2.39: the -sources classifier Loom needs is no longer served (404), and the rest of the
	//    module times out often; installer/userdev/universal were fetched from maven.minecraftforge.net as-is.
	exclusiveContent {
		forRepository {
			maven(file("patched-maven")) {
				metadataSources {
					mavenPom()
					artifact()
				}
			}
		}
		filter {
			includeModule("de.oceanlabs.mcp", "mcp_config")
			includeModule("net.minecraftforge", "forge")
		}
	}
	maven("https://repo.essential.gg/repository/maven-public/")
	// Other 1.16.5-era Forge artifacts (binarypatcher, cpw.mods:grossjava9hacks, ...) are mirrored by NeoForged.
	maven("https://maven.neoforged.net/releases/")
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
	mappings(loom.officialMojangMappings())
	forge("net.minecraftforge:forge:$forgeVer")

	// Forge 1.16.5 has no jar-in-jar (JarJar arrived in 1.18), so JSVG is shaded + relocated.
	shade("com.github.weisj:jsvg:1.6.1")
	compileOnly("org.jetbrains:annotations:24.0.1")

	testImplementation(sourceSets.main.get().output)
}

tasks.shadowJar {
	configurations = listOf(shade)
	archiveClassifier = "dev-shadow"
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
	filesMatching("META-INF/mods.toml") {
		expand("version" to version)
	}
}

tasks.processTestResources {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	inputs.property("version", version)
	filesMatching("META-INF/mods.toml") {
		expand("version" to version)
	}
}

// src/test holds the in-game test mod (used by runClient), not unit tests.
tasks.test {
	enabled = false
}
