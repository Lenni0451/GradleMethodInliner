package net.lenni0451.methodinliner.model;

import org.objectweb.asm.tree.MethodNode;

public class InlinedMethod {

    private final String owner;
    private final MethodNode methodNode;

    public InlinedMethod(final String owner, final MethodNode methodNode) {
        this.owner = owner;
        this.methodNode = methodNode;
    }

    public String getOwner() {
        return this.owner;
    }

    public MethodNode getMethodNode() {
        return this.methodNode;
    }

}
