package net.lenni0451.methodinliner.tasks;

import net.lenni0451.classtransform.additionalclassprovider.LazyFileClassProvider;
import net.lenni0451.classtransform.additionalclassprovider.PathClassProvider;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.MethodInliner;
import net.lenni0451.classtransform.utils.annotations.AnnotationUtils;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.ClassTree;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class InlineTask extends DefaultTask {

    private static final String INLINE_ANNOTATION_DESC = "Lnet/lenni0451/methodinliner/InlineMethod;";

    @Input
    public abstract Property<SourceSet> getSourceSet();

    @TaskAction
    public void run() throws IOException, UncheckedIOException {
        for (File classesDir : this.getSourceSet().get().getOutput().getClassesDirs()) {
            if (!classesDir.isDirectory()) continue;
            Path root = classesDir.toPath();
            ClassTree classTree = new ClassTree();
            IClassProvider classProvider = new PathClassProvider(root, new LazyFileClassProvider(this.getSourceSet().get().getCompileClasspath().getFiles(), new BasicClassProvider()));

            try (Stream<Path> stream = Files.walk(root)) {
                stream.forEach(path -> {
                    String relative = root.relativize(path).toString();
                    if (!relative.endsWith(".class")) return;
                    try {
                        this.process(classTree, classProvider, path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
    }

    private void process(final ClassTree classTree, final IClassProvider classProvider, final Path path) throws IOException {
        byte[] bytecode = Files.readAllBytes(path);
        ClassNode classNode = ASMUtils.fromBytes(bytecode);
        classNode = this.transformClass(classNode);
        if (classNode != null) {
            bytecode = ASMUtils.toBytes(classNode, classTree, classProvider);
            Files.write(path, bytecode);
        }
    }

    private ClassNode transformClass(final ClassNode classNode) {
        boolean changed = false;
        for (MethodNode method : classNode.methods) {
            if (AnnotationUtils.hasAnnotation(method, INLINE_ANNOTATION_DESC)) {
                if (!Modifier.isPrivate(method.access)) {
                    throw new IllegalStateException("Method " + classNode.name + "." + method.name + method.desc + " is not private but has the @InlineMethod annotation");
                } else {
                    changed = true;
                    System.out.println("Inlining method " + classNode.name + "." + method.name + method.desc);
                    MethodInliner.wrappedInline(classNode, method, classNode.name);
                }
            }
        }
        if (changed) return classNode;
        else return null;
    }

}
