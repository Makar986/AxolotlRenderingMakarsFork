@file:Suppress("UnstableApiUsage")

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder


plugins {
	id("fabric-loom")
	id("io.github.p03w.machete")
}

val minecraftVersion = "1.21.1"
val minecraftFriendly = "1.21.1"
val fapiVersion = "0.116.4"

group = project.property("maven_group") as String
version = "${project.property("version")}+$minecraftFriendly"
base.archivesName = project.property("archives_base_name").toString()

loom {
	accessWidenerPath.set(file("src/main/resources/axolotlclient-rendering.accesswidener"))
	mods {
		create("axolotlclient-rendering") {
			sourceSet("main")
		}
		create("axolotlclient-rendering-test") {
			sourceSet("test")
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${minecraftVersion}")
	mappings(loom.layered {
		officialMojangMappings {
			nameSyntheticMembers = true
		}
		parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
	})

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${fapiVersion}+${minecraftFriendly}")

	modCompileOnlyApi("com.terraformersmc:modmenu:8.0.0") {
		exclude(group = "net.fabricmc")
	}

	implementation(include(project(path = ":common", configuration = "shadow"))!!)
	include(implementation("org.apache.xmlgraphics:batik-transcoder:1.19")!!)
	include("org.apache.xmlgraphics:batik-anim:1.19")
	include("org.apache.xmlgraphics:batik-shared-resources:1.19")
	include("org.apache.xmlgraphics:batik-util:1.19")
	include("org.apache.xmlgraphics:batik-constants:1.19")
	include("org.apache.xmlgraphics:batik-i18n:1.19")
	include("org.apache.xmlgraphics:xmlgraphics-commons:2.11")
	include("org.apache.xmlgraphics:batik-css:1.19")
	include("org.apache.xmlgraphics:batik-dom:1.19")
	include("org.apache.xmlgraphics:batik-ext:1.19")
	include("org.apache.xmlgraphics:batik-xml:1.19")
	include("org.apache.xmlgraphics:batik-parser:1.19")
	include("org.apache.xmlgraphics:batik-svg-dom:1.19")
	include("org.apache.xmlgraphics:batik-bridge:1.19")
	include("org.apache.xmlgraphics:batik-gvt:1.19")
	include("org.apache.xmlgraphics:batik-script:1.19")
	include("org.apache.xmlgraphics:batik-awt-util:1.19")
	include("xml-apis:xml-apis-ext:1.3.04")
}

configurations.configureEach {
	exclude("org.apache.xmlgraphics:batik-svggen:1.19")
	exclude("xml-apis", "xml-apis")
}


tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType(JavaCompile::class).configureEach {
	// Ensure that the encoding is set to UTF-8, no matter what the system default is
	// this fixes some edge cases with special characters not displaying correctly
	// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_22)) {
		options.release = 21
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.runClient {
	classpath(sourceSets.getByName("test").runtimeClasspath)
	jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "owlMaven"
			val repository = if (project.version.toString().contains("beta") || project.version.toString().contains("alpha")) "snapshots" else "releases"
			url = uri("https://maven.axolotlclient.com/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

tasks.modrinth {
	dependsOn(tasks.getByName("optimizeOutputsOfRemapJar"))
}

modrinth {
	token = System.getenv("MODRINTH_TOKEN")
	projectId = "2qX7WO3l"
	versionNumber = "${project.version}"
	versionType = if (project.version.toString().contains("beta")) {
		"beta"
	} else if (project.version.toString().contains("alpha")) {
		"alpha"
	} else {
		"release"
	}
	uploadFile = tasks.remapJar.get()
	gameVersions.set(listOf("1.21", "1.21.1"))
	loaders.set(listOf("quilt", "fabric"))
	additionalFiles.set(listOf(tasks.remapSourcesJar))
	dependencies {
		required.project("fabric-api")
	}

	var git = Git(FileRepositoryBuilder().setGitDir(project.rootDir.resolve(".git")).build())
	val id = git.log().setMaxCount(1)().first().id
	val abbrev = git.repository.newObjectReader()
		.abbreviate(id, 8).name()
	changelog = "The changelog is available at https://codeberg.org/AxolotlClient/AxolotlClient-rendering/graph <br>" +
		"Commit: [$abbrev](https://codeberg.org/AxolotlClient/AxolotlClient-rendering/commit/${id.name})"
}
