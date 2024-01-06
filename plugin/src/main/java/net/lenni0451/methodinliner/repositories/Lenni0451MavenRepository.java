package net.lenni0451.methodinliner.repositories;

import org.gradle.api.Action;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class Lenni0451MavenRepository implements Action<MavenArtifactRepository> {

    @Override
    public void execute(MavenArtifactRepository mavenArtifactRepository) {
        mavenArtifactRepository.setName("lenni0451-releases");
        mavenArtifactRepository.setUrl("https://maven.lenni0451.net/releases");
    }

}
