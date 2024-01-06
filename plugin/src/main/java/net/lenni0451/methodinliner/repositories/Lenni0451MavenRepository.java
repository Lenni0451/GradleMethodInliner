package net.lenni0451.methodinliner.repositories;

import net.lenni0451.methodinliner.MethodInlinerPlugin;
import org.gradle.api.Action;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.jetbrains.annotations.NotNull;

public class Lenni0451MavenRepository implements Action<MavenArtifactRepository> {

    @Override
    public void execute(@NotNull MavenArtifactRepository mavenArtifactRepository) {
        if (MethodInlinerPlugin.VERSION.endsWith("-SNAPSHOT")) {
            mavenArtifactRepository.setName("lenni0451-snapshots");
            mavenArtifactRepository.setUrl("https://maven.lenni0451.net/snapshots");
        } else {
            mavenArtifactRepository.setName("lenni0451-releases");
            mavenArtifactRepository.setUrl("https://maven.lenni0451.net/releases");
        }
    }

}
