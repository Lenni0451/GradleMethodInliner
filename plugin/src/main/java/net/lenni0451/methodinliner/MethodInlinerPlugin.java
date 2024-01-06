package net.lenni0451.methodinliner;

import net.lenni0451.methodinliner.repositories.Lenni0451MavenRepository;
import net.lenni0451.methodinliner.tasks.InlineTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;

public class MethodInlinerPlugin implements Plugin<Project> {

    public static final String VERSION = "${version}";

    @Override
    public void apply(@NotNull final Project project) {
        this.addAnnotationDependency(project);
        this.registerTask(project);
    }

    private void addAnnotationDependency(final Project project) {
        project.getRepositories().add(project.getRepositories().maven(new Lenni0451MavenRepository()));
        project.getDependencies().add("compileOnly", "net.lenni0451.method-inliner:runtime:" + VERSION);
    }

    private void registerTask(final Project project) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        sourceSets.all(set -> {
            TaskProvider<InlineTask> inlineTask = project.getTasks().register(set.getTaskName("inline", "methods"), InlineTask.class, task -> task.getSourceSet().set(set));
            inlineTask.get().dependsOn(set.getClassesTaskName());
            project.getTasks().getByName(set.getClassesTaskName()).finalizedBy(inlineTask);
        });
    }

}
