@file:Suppress("UnstableApiUsage")

plugins {
	id("java-library")
	id("maven-publish")
	id("net.neoforged.moddev.legacyforge")
	id("io.github.p03w.machete")
}

val mcVersion = "1.20.1"
val forgeVer = "47.3.0"

group = project.property("maven_group") as String
version = "${project.property("version")}+$mcVersion"
base.archivesName = project.property("archives_base_name").toString()

legacyForge {
	setVersion("$mcVersion-$forgeVer")

	parchment {
		minecraftVersion = "1.20.1"
		mappingsVersion = "2023.09.03"
	}

	accessTransformers {
		from(file("src/main/resources/META-INF/accesstransformer.cfg"))
	}

	runs {
		create("client") {
			client()
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
	compileOnly("org.jetbrains:annotations:24.0.1")
	compileOnly("io.github.llamalad7:mixinextras-common:0.4.1")
	annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")
	jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")
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

	filesMatching("META-INF/mods.toml") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
		options.release.set(17)
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
}
