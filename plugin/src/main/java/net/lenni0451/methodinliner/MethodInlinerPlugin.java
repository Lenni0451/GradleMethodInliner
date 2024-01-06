package net.lenni0451.methodinliner;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MethodInlinerPlugin implements Plugin<Project> {

    public void apply(Project project) {
        // Register a task
        project.getTasks().register("greeting", task -> {
            task.doLast(s -> System.out.println("Hello from plugin 'net.lenni0451.methodinliner.greeting'"));
        });
    }

}
