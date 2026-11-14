@file:Suppress("UnstableApiUsage")

plugins {
	id("java-library")
	id("maven-publish")
	id("net.neoforged.moddev")
	id("io.github.p03w.machete")
}

val minecraftVersion = "1.21.1"
val neoforgeVersion = "21.1.84"

group = project.property("maven_group") as String
version = "${project.property("version")}+$minecraftVersion"
base.archivesName = project.property("archives_base_name").toString()

neoForge {
	version = neoforgeVersion

	parchment {
		minecraftVersion = "1.21.1"
		mappingsVersion = "2024.11.17"
	}

	runs {
		create("client") {
			client()
			systemProperty("neoforge.enabledGameTestNamespaces", "axolotlclient_rendering")
		}
	}

	mods {
		create("axolotlclient_rendering") {
			sourceSet(sourceSets.main.get())
		}
		create("axolotlclient_rendering_test") {
			sourceSet(sourceSets.test.get())
		}
	}
}

dependencies {
	implementation("com.github.weisj:jsvg:1.6.1")
	jarJar("com.github.weisj:jsvg:1.6.1")
	testImplementation(sourceSets.main.get().output)
}

sourceSets {
	main {
		java.srcDir("../common/src/main/java")
		resources.srcDir("../common/src/main/resources")
	}
}

configurations {
	testCompileClasspath {
		extendsFrom(configurations.compileClasspath.get())
	}
	testRuntimeClasspath {
		extendsFrom(configurations.runtimeClasspath.get())
	}
}

tasks.processResources {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	inputs.property("version", version)

	filesMatching("META-INF/neoforge.mods.toml") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(21)
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
}
