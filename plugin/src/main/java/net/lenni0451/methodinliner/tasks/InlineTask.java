package net.lenni0451.methodinliner.tasks;

import net.lenni0451.classtransform.additionalclassprovider.LazyFileClassProvider;
import net.lenni0451.classtransform.additionalclassprovider.PathClassProvider;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.MethodInliner;
import net.lenni0451.classtransform.utils.annotations.AnnotationParser;
import net.lenni0451.classtransform.utils.annotations.AnnotationUtils;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.ClassTree;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import net.lenni0451.methodinliner.InlineMethod;
import net.lenni0451.methodinliner.model.InlinedMethod;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class InlineTask extends DefaultTask {

    @Input
    public abstract Property<SourceSet> getSourceSet();

    @TaskAction
    public void run() throws IOException, UncheckedIOException {
        for (File classesDir : this.getSourceSet().get().getOutput().getClassesDirs()) {
            if (!classesDir.isDirectory()) continue;
            Path root = classesDir.toPath();
            List<InlinedMethod> inlinedMethods = new ArrayList<>();
            ClassTree classTree = new ClassTree();
            IClassProvider classProvider = new PathClassProvider(root, new LazyFileClassProvider(this.getSourceSet().get().getCompileClasspath().getFiles(), new BasicClassProvider()));

            try (Stream<Path> stream = Files.walk(root)) {
                stream.forEach(path -> {
                    String relative = root.relativize(path).toString();
                    if (!relative.endsWith(".class")) return;
                    try {
                        this.process(inlinedMethods, true, classTree, classProvider, path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
            if (!inlinedMethods.isEmpty()) {
                try (Stream<Path> stream = Files.walk(root)) {
                    System.out.println("Inlining " + inlinedMethods.size() + " public methods");
                    stream.forEach(path -> {
                        String relative = root.relativize(path).toString();
                        if (!relative.endsWith(".class")) return;
                        try {
                            this.process(inlinedMethods, false, classTree, classProvider, path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                }
            }
        }
    }

    private void process(final List<InlinedMethod> inlinedMethods, final boolean inlinePrivate, final ClassTree classTree, final IClassProvider classProvider, final Path path) throws IOException {
        byte[] bytecode = Files.readAllBytes(path);
        ClassNode classNode = ASMUtils.fromBytes(bytecode);
        if (inlinePrivate) classNode = this.inlinePrivate(inlinedMethods, classTree, classProvider, classNode);
        else this.inlinePublic(inlinedMethods, classNode);
        if (classNode != null) {
            bytecode = ASMUtils.toBytes(classNode, classTree, classProvider);
            Files.write(path, bytecode);
        }
    }

    private ClassNode inlinePrivate(final List<InlinedMethod> inlinedMethods, final ClassTree classTree, final IClassProvider classProvider, final ClassNode classNode) {
        boolean changed = false;
        for (MethodNode method : classNode.methods.toArray(new MethodNode[0])) {
            Optional<AnnotationNode> rawAnnotation = AnnotationUtils.findAnnotation(method, InlineMethod.class);
            if (rawAnnotation.isPresent()) {
                InlineMethod inlineMethod = AnnotationParser.parse(InlineMethod.class, classTree, classProvider, AnnotationUtils.listToMap(rawAnnotation.get().values));

                if (!inlineMethod.keep() && !Modifier.isPrivate(method.access)) {
                    throw new IllegalStateException("Method " + classNode.name + "." + method.name + method.desc + " is not private but has the @InlineMethod annotation");
                } else {
                    changed = true;
                    System.out.println("Inlining method " + classNode.name + "." + method.name + method.desc);
                    MethodInliner.wrappedInline(classNode, method, classNode.name);
                    if (inlineMethod.keep()) {
                        classNode.methods.add(method);
                        if (Modifier.isStatic(method.access)) {
                            inlinedMethods.add(new InlinedMethod(classNode.name, method));
                        } else {
                            System.err.println("Method " + classNode.name + "." + method.name + method.desc + " is not static but has the @InlineMethod(keep = true) annotation. It will only be inlined inside the containing class.");
                        }
                    }
                }
            }
        }
        if (changed) return classNode;
        else return null;
    }

    private void inlinePublic(final List<InlinedMethod> inlinedMethods, final ClassNode classNode) {
        for (InlinedMethod inlinedMethod : inlinedMethods) {
            MethodInliner.wrappedInline(classNode, inlinedMethod.getMethodNode(), inlinedMethod.getOwner());
        }
    }

}
