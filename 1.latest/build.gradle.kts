@file:Suppress("UnstableApiUsage")

plugins {
	id("fabric-loom")
	id("io.github.p03w.machete")
}

val minecraftVersion = "1.21.11-pre1"
val minecraftFriendly = "1.21.11"
val fapiVersion = "0.139.1"

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
	mappings(loom.layered {
		officialMojangMappings {
			nameSyntheticMembers = true
		}
		parchment("org.parchmentmc.data:parchment-1.21.10:2025.10.12@zip")
	})

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion+$minecraftFriendly")

	modCompileOnly("com.terraformersmc:modmenu:17.0.0-alpha.1")

	implementation(include(project(path = ":common", configuration = "shadow"))!!)

	//include(implementation("org.lwjgl:lwjgl-harfbuzz:3.3.3")!!)
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
			url = uri("https://moehreag.duckdns.org/maven/$repository")
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
	projectId = ""
	versionNumber = "${project.version}"
	versionType = if (project.version.toString().contains("beta")) {
		"beta"
	} else if (project.version.toString().contains("alpha")) {
		"alpha"
	} else {
		"release"
	}
	uploadFile = tasks.remapJar.get()
	gameVersions.set(listOf(minecraftVersion))
	loaders.set(listOf("quilt", "fabric"))
	additionalFiles.set(listOf(tasks.remapSourcesJar))
	dependencies {
		required.project("fabric-api")
	}

	// Changelog fetching: Credit LambdAurora.
	// https://github.com/LambdAurora/LambDynamicLights/blob/1ef85f486084873b5d97b8a08df72f57859a3295/build.gradle#L145
	// License: MIT
	val changelogText = file("../CHANGELOG.md").readText()
	val regexVersion =
		((project.version) as String).split("+")[0].replace("\\.".toRegex(), "\\.").replace("\\+".toRegex(), "+")
	val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
	val matcher = changelogRegex.find(changelogText)

	if (matcher != null) {
		var changelogContent = matcher.groups[1]?.value

		val changelogLines = changelogText.split("\n")
		val linkRefRegex = "^\\[([A-z0-9 _\\-/+.]+)]: ".toRegex()
		for (line in changelogLines.reversed()) {
			if ((linkRefRegex.matches(line)))
				changelogContent += "\n" + line
			else break
		}
		changelog = changelogContent
	} else {
		afterEvaluate {
			tasks.modrinth.configure { enabled = false }
		}
	}
}
