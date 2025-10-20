package org.dotspace.oofp.support.expression.transform;

/**
 * Represents a mapping between a reader expression and a writer expression.
 * This is used in the context of transforming data from one format to another.
 */
public record TransformMapping(String readerExpr, String writerExpr) {
}
