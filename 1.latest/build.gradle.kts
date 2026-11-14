@file:Suppress("UnstableApiUsage")

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder


plugins {
	id("net.fabricmc.fabric-loom")
	id("io.github.p03w.machete")
}

val minecraftVersion = "26.3-pre-2"
val minecraftFriendly = "26.3"
val fapiVersion = "0.159.4"

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

repositories {
	mavenCentral()
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")

	implementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	implementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion+$minecraftFriendly")

	// compileOnly("com.terraformersmc:modmenu:18.0.0-alpha.5")

	implementation(include(project(path = ":common", configuration = "shadow"))!!)
	include(implementation("com.github.weisj:jsvg:1.6.1")!!)
}

configurations.configureEach {
	exclude("xml-apis", "xml-apis")
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType(JavaCompile::class).configureEach {
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
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
	//environment("LD_PRELOAD" to "/usr/lib/librenderdoc.so")
}

tasks.runServer {
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
			val repository = if (project.version.toString().contains("beta") || project.version.toString()
					.contains("alpha")
			) "snapshots" else "releases"
			url = uri("https://maven.axolotlclient.com/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

tasks.modrinth {
	dependsOn(tasks.getByName("optimizeOutputsOfJar"))
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
	uploadFile = tasks.jar.get()
	gameVersions.set(listOf(minecraftVersion))
	loaders.set(listOf("quilt", "fabric"))
	additionalFiles.set(listOf(tasks.sourcesJar))
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
