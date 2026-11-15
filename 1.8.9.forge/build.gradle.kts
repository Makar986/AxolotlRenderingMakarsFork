@file:Suppress("UnstableApiUsage")

plugins {
	id("java-library")
	id("maven-publish")
	id("net.neoforged.moddev.legacyforge")
	id("io.github.p03w.machete")
}

val mcVersion = "1.8.9"
val forgeVer = "11.15.1.2318-1.8.9"

group = project.property("maven_group") as String
version = "${project.property("version")}+$mcVersion"
base.archivesName = project.property("archives_base_name").toString()

legacyForge {
	setVersion("$mcVersion-$forgeVer")

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

val forgeJar = "/home/makar/.gradle/caches/essential-loom/minecraftMaven/net/minecraft/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-generated-intermediate/1.8.9-de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-generated-intermediate-1.8.9-de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9.jar"

sourceSets {
	main {
		java.srcDir("../common/src/main/java")
		resources.srcDir("../common/src/main/resources")
	}
}

dependencies {
	compileOnly(files(forgeJar))
	testImplementation(files(forgeJar))

	compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.3")
	testImplementation("org.lwjgl.lwjgl:lwjgl:2.9.3")
	compileOnly("org.lwjgl:lwjgl-freetype:3.4.3")
	testImplementation("org.lwjgl:lwjgl-freetype:3.4.3")

	compileOnly("org.slf4j:slf4j-api:1.7.36")
	testImplementation("org.slf4j:slf4j-api:1.7.36")
	compileOnly("org.apache.commons:commons-lang3:3.12.0")
	testImplementation("org.apache.commons:commons-lang3:3.12.0")

	compileOnly("it.unimi.dsi:fastutil:8.5.9")
	testImplementation("it.unimi.dsi:fastutil:8.5.9")
	compileOnly("com.google.guava:guava:17.0")
	testImplementation("com.google.guava:guava:17.0")
	compileOnly("com.google.code.gson:gson:2.10")
	testImplementation("com.google.code.gson:gson:2.10")
	compileOnly("org.apache.logging.log4j:log4j-api:2.15.0")
	testImplementation("org.apache.logging.log4j:log4j-api:2.15.0")

	implementation("com.github.weisj:jsvg:1.6.1")
	jarJar("com.github.weisj:jsvg:1.6.1")
	compileOnly("org.jetbrains:annotations:24.0.1")
	testImplementation(sourceSets.main.get().output)
}

configurations {
	all {
		exclude(group = "net.minecraftforge", module = "forge")
	}
	testCompileClasspath {
		extendsFrom(configurations.compileClasspath.get())
	}
	testRuntimeClasspath {
		extendsFrom(configurations.runtimeClasspath.get())
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.named("createMinecraftArtifacts") {
	onlyIf { false }
}

tasks.named("reobfJar") {
	onlyIf { false }
}

tasks.processResources {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	inputs.property("version", version)

	filesMatching("mcmod.info") {
		expand("version" to version)
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
}
