plugins {
	id("fabric-loom")
	id("ploceus")
	id("io.github.p03w.machete")
}

val minecraftVersion = "1.8.9"
val feather = "1.8.9+build.27"
val osl = "0.16.3"
val lwjglVersion = "3.3.6"

group = project.property("maven_group")!!
version = "${project.property("version")}+$minecraftVersion"

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
	maven { url = uri("https://jitpack.io") }
}
dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings("net.ornithemc:feather:$feather")

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	ploceus.dependOsl(osl)

	modImplementation("com.terraformersmc:modmenu:0.3.1+mc1.8.9")

	implementation(include(project(path = ":common", configuration = "shadow"))!!)

	modImplementation("io.github.moehreag:legacy-lwjgl3:${project.property("legacy_lwgjl3")}")

	include(implementation("org.lwjgl:lwjgl-freetype:$lwjglVersion")!!)

	listOf("linux", "windows", "macos", "windows-arm64", "macos-arm64", "linux-arm64").forEach { platform ->
		include(runtimeOnly("org.lwjgl:lwjgl-freetype:$lwjglVersion:natives-$platform")!!)
	}

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
	include("org.apache.xmlgraphics:batik-svggen:1.19")
	include("org.apache.xmlgraphics:batik-awt-util:1.19")
	include("org.apache.xmlgraphics:batik-codec:1.19")
	include("xml-apis:xml-apis-ext:1.3.04")
}

configurations.configureEach {
	exclude("org.apache.xmlgraphics:batik-svggen:1.19")
	exclude("xml-apis", "xml-apis")
}

configurations.configureEach {
	exclude("org.lwjgl.lwjgl")
	resolutionStrategy {
		dependencySubstitution {
			substitute(module("io.netty:netty-all:4.0.23.Final")).using(module("io.netty:netty-all:4.1.9.Final"))
		}
		force("io.netty:netty-all:4.1.9.Final")
	}
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.runClient {
	if (project.property("native_glfw") == "true") {
		val glfwPath = project.properties.getOrDefault("native_glfw_path", "/usr/lib/libglfw.so")
		jvmArgs("-Dorg.lwjgl.glfw.libname=$glfwPath")
	}
	classpath(sourceSets.getByName("test").runtimeClasspath)
	jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
}

tasks.withType(JavaCompile::class).configureEach {
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
		options.release = 17
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
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
	loaders.set(listOf("ornithe"))
	additionalFiles.set(listOf(tasks.remapSourcesJar))
	dependencies {
		required.project("osl")
		required.project("moehreag-legacy-lwjgl3")
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
			tasks.modrinth.configure { isEnabled = false }
		}
	}
}
