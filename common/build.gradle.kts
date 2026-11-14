plugins {
	id("java")
	id("com.gradleup.shadow")
}

group = project.property("maven_group").toString()+"."+project.property("archives_base_name").toString()
base.archivesName.set(project.property("archives_base_name").toString()+"-common")

dependencies {
	compileOnly("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")
	compileOnly("org.jetbrains:annotations:24.0.0")

	compileOnly("com.google.code.gson:gson:2.10")

	compileOnly("org.slf4j:slf4j-api:1.7.36")

	implementation("com.github.weisj:jsvg:1.6.1")
	shadow("com.github.weisj:jsvg:1.6.1")
}

tasks.jar {
	enabled = false
}

tasks.build {
	dependsOn(tasks.shadowJar)
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType(JavaCompile::class).configureEach {
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
		options.release = 17
	}
}

tasks.withType(AbstractArchiveTask::class).configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

tasks.shadowJar {
	archiveClassifier.set("")
	exclude("fabric.mod.json")
	mergeServiceFiles()

	append("../LICENSE")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

publishing {
	publications {
		create("shadow", MavenPublication::class) {
			artifactId = base.archivesName.get()
			from(components["shadow"])
			artifact(tasks.sourcesJar)
		}
	}
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/Makar986/AxolotlRenderingMakarsFork")
			credentials {
				username = System.getenv("GITHUB_ACTOR") ?: "Makar986"
				password = System.getenv("GITHUB_TOKEN") ?: ""
			}
		}
		mavenLocal()
	}
}
