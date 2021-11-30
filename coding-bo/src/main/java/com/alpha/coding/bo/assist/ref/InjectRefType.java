package com.alpha.coding.bo.assist.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * InjectRefType
 *
 * @version 1.0
 * Date: 2020/4/14
 */
@Getter
@AllArgsConstructor
public enum InjectRefType {

    INTERFACE("interface", new ContextVisitor(new DefaultRefContext())),
    ANNOTATION("annotation", new ContextVisitor(new AnnotatedRefContext())),
    GLOBAL("global", new ContextVisitor(new DefaultRefContext()));

    private final String name;
    private final ContextVisitor contextVisitor;

    public static final class ContextVisitor {

        private final RefContext context;

        private ContextVisitor(RefContext context) {
            this.context = context;
        }

        public RefContext visit() {
            return context;
        }
    }

}
