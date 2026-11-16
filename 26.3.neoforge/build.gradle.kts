@file:Suppress("UnstableApiUsage")

plugins {
	id("java-library")
	id("maven-publish")
	id("net.neoforged.moddev")
	id("io.github.p03w.machete")
}

val minecraftVersion = "26.3"
val neoforgeVersion = "26.3.0.103"

group = project.property("maven_group") as String
version = "${project.property("version")}+$minecraftVersion"
base.archivesName = project.property("archives_base_name").toString()

neoForge {
	version = neoforgeVersion

	accessTransformers {
		from(file("src/main/resources/META-INF/accesstransformer.cfg"))
	}

	runs {
		create("client") {
			client()
			systemProperty("neoforge.enabledGameTestNamespaces", "axolotlclient_rendering")
			// The in-game test scene lives in src/test; run with its classpath (it includes main).
			sourceSet = sourceSets.test.get()
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
	compileOnly("org.jetbrains:annotations:24.0.0")
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
	options.release.set(25)
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}
