@file:Suppress("UnstableApiUsage")

plugins {
	id("java-library")
	id("maven-publish")
	id("net.neoforged.moddev.legacyforge")
	id("io.github.p03w.machete")
}

val mcVersion = "1.16.5"
val forgeVer = "36.2.39"

group = project.property("maven_group") as String
version = "${project.property("version")}+$mcVersion"
base.archivesName = project.property("archives_base_name").toString()

legacyForge {
	setVersion("$mcVersion-$forgeVer")

	parchment {
		minecraftVersion = "1.16.5"
		mappingsVersion = "2022.03.06"
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
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register("printCP") {
	doLast {
		configurations.getByName("compileClasspath").files.forEach { println("CP: " + it) }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
}
