package org.checkerframework.checker.dividebyzero;

import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.dataflow.cfg.node.*;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;

import java.util.Set;

import org.checkerframework.checker.dividebyzero.qual.*;

public class DivByZeroTransfer extends CFTransfer {

    enum Comparison {
        /** == */ EQ,
        /** != */ NE,
        /** <  */ LT,
        /** <= */ LE,
        /** >  */ GT,
        /** >= */ GE
    }

    enum BinaryOperator {
        /** + */ PLUS,
        /** - */ MINUS,
        /** * */ TIMES,
        /** / */ DIVIDE,
        /** % */ MOD
    }

    // ========================================================================
    // Transfer functions to implement

    /**
     * Assuming that a simple comparison (lhs `op` rhs) returns true, this
     * function should refine what we know about the left-hand side (lhs). (The
     * input value "lhs" is always a legal return value, but not a very useful
     * one.)
     *
     * <p>For example, given the code
     * <pre>
     * if (y != 0) { x = 1 / y; }
     * </pre>
     * the comparison "y != 0" causes us to learn the fact that "y is not zero"
     * inside the body of the if-statement. This function would be called with
     * "NE", "top", and "zero", and should return "not zero" (or the appropriate
     * result for your lattice).
     *
     * <p>Note that the returned value should always be lower in the lattice
     * than the given point for lhs. The "glb" helper function below will
     * probably be useful here.
     *
     * @param operator   a comparison operator
     * @param lhs        the lattice point for the left-hand side of the comparison expression
     * @param rhs        the lattice point for the right-hand side of the comparison expression
     * @return a refined type for lhs
     */
    private AnnotationMirror refineLhsOfComparison(
            Comparison operator,
            AnnotationMirror lhs,
            AnnotationMirror rhs) {
        AnnotationMirror ltZero = reflect(LtZero.class);
        AnnotationMirror leZero = reflect(LeZero.class);
        AnnotationMirror gtZero = reflect(GtZero.class);
        AnnotationMirror geZero = reflect(GeZero.class);
        AnnotationMirror neZero = reflect(NeZero.class);
        AnnotationMirror eqZero = reflect(EqZero.class);

        switch(operator) {
            case EQ:
                return glb(lhs, rhs);
            case NE: {
                if (equal(rhs, eqZero)) {
                    if (equal(lhs, top())) {
                        return neZero;
                    } else if (equal(lhs, leZero)) {
                        return ltZero;
                    } else if (equal(lhs, eqZero)) {
                        return bottom();
                    } else if (equal(lhs, geZero)) {
                        return gtZero;
                    }
                }
                
                return lhs;
            }
            case LT: {
                if (equal(rhs, ltZero) || equal(rhs, leZero) || equal(rhs, eqZero)) {
                    if (equal(lhs, top()) || equal(lhs, ltZero) ||
                        equal(lhs, leZero) || equal(lhs, neZero)) {
                        return ltZero;
                    } else {
                        return bottom();
                    }
                }

                return lhs;
            }
            case LE: {
                if (equal(rhs, ltZero)) {
                    if (equal(lhs, top()) || equal(lhs, ltZero) ||
                        equal(lhs, leZero) || equal(lhs, neZero)) {
                        return ltZero;
                    } else {
                        return bottom();
                    }
                } else if (equal(rhs, leZero) || equal(rhs, eqZero)) {
                    if (equal(lhs, top()) || equal(lhs, leZero)) {
                        return leZero;
                    } else if (equal(lhs, ltZero) || equal(lhs, neZero)) {
                        return ltZero;
                    } else if (equal(lhs, eqZero) || equal(lhs, geZero)) {
                        return eqZero;
                    } else {
                        return bottom();
                    }
                }

                return lhs;
            }
            case GT: {
                AnnotationMirror am = refineLhsOfComparison(
                    Comparison.LT, flipAlongVerticalCenter(lhs), flipAlongVerticalCenter(rhs));

                return flipAlongVerticalCenter(am);
            }
            case GE: {
                AnnotationMirror am = refineLhsOfComparison(
                    Comparison.LE, flipAlongVerticalCenter(lhs), flipAlongVerticalCenter(rhs));

                return flipAlongVerticalCenter(am);
            }
        }

        throw new IllegalStateException();
    }

    /**
     * Flips the annotation around a line drawn through the center of the lattice,
     * where < and <= are on one side of the lattice, > and >= are on another,
     * and 0 and !0 are in the center of the lattice.
     */
    private AnnotationMirror flipAlongVerticalCenter(AnnotationMirror x) {
        if (equal(x, reflect(LeZero.class))) {
            return reflect(GeZero.class);
        } else if (equal(x, reflect(LtZero.class))) {
            return reflect(GtZero.class);
        } else if (equal(x, reflect(GeZero.class))) {
            return reflect(LeZero.class);
        } else if (equal(x, reflect(GtZero.class))) {
            return reflect(LtZero.class);
        } else {
            // No flipping required
            return x;
        }
    }

    /**
     * For an arithmetic expression (lhs `op` rhs), compute the point in the
     * lattice for the result of evaluating the expression. ("Top" is always a
     * legal return value, but not a very useful one.)
     *
     * <p>For example,
     * <pre>x = 1 + 0</pre>
     * should cause us to conclude that "x is not zero".
     *
     * @param operator   a binary operator
     * @param lhs        the lattice point for the left-hand side of the expression
     * @param rhs        the lattice point for the right-hand side of the expression
     * @return the lattice point for the result of the expression
     */
    private AnnotationMirror arithmeticTransfer(
            BinaryOperator operator,
            AnnotationMirror lhs,
            AnnotationMirror rhs) {
        AnnotationMirror ltZero = reflect(LtZero.class);
        AnnotationMirror leZero = reflect(LeZero.class);
        AnnotationMirror gtZero = reflect(GtZero.class);
        AnnotationMirror geZero = reflect(GeZero.class);
        AnnotationMirror neZero = reflect(NeZero.class);
        AnnotationMirror eqZero = reflect(EqZero.class);

        switch(operator) {
            case PLUS: {
                if (equal(lhs, bottom()) || equal(rhs, bottom())) {
                    return bottom();
                } else if (equal(lhs, top()) || equal(rhs, top())) {
                    return top();
                } else if (equal(lhs, neZero) || equal(rhs, neZero)) {
                    if (equal(lhs, eqZero) || equal(rhs, eqZero)) {
                        return neZero;
                    } else {
                        return top();
                    }
                } else if (equal(lhs, rhs)) {
                    return lhs;
                } else if (equal(lhs, eqZero)) {
                    return rhs;
                } else if (equal(rhs, eqZero)) {
                    return lhs;
                } else if ((equal(lhs, ltZero) && equal(rhs, leZero)) ||
                           (equal(rhs, ltZero) && equal(lhs, leZero))) {
                    return ltZero;
                } else if ((equal(lhs, gtZero) && equal(rhs, geZero)) ||
                           (equal(rhs, gtZero) && equal(lhs, geZero))) {
                    return gtZero;
                } else {
                    return top();
                }
            }
            case MINUS: {
                // Flip rhs and then call arithmeticTransfer
                if (equal(rhs, ltZero)) {
                    rhs = gtZero;
                } else if (equal(rhs, leZero)) {
                    rhs = geZero;
                } else if (equal(rhs, gtZero)) {
                    rhs = ltZero;
                } else if (equal(rhs, geZero)) {
                    rhs = leZero;
                }

                return arithmeticTransfer(BinaryOperator.PLUS, lhs, rhs);
            }
            case TIMES: {
                if (equal(lhs, bottom()) || equal(rhs, bottom())) {
                    return bottom();
                } else if (equal(lhs, top()) || equal(rhs, top())) {
                    return top();
                } else if (equal(lhs, eqZero) || equal(rhs, eqZero)) {
                    return eqZero;
                } else if (equal(lhs, neZero) || equal(rhs, neZero)) {
                    if (equal(lhs, leZero) || equal(lhs, geZero) ||
                        equal(rhs, leZero) || equal(rhs, geZero)) {
                        return top();
                    } else {
                        return neZero;
                    }
                } else if (equal(lhs, ltZero) || equal(rhs, ltZero)) {
                    if (equal(lhs, ltZero) && equal(rhs, ltZero)) {
                        return gtZero;
                    } else if (equal(lhs, leZero) || equal(rhs, leZero)) {
                        return geZero;
                    } else if (equal(lhs, geZero) || equal(rhs, geZero)) {
                        return leZero;
                    } else {  // equal(lhs, gtZero) || equal(rhs, gtZero)
                        return ltZero;
                    }
                } else if (equal(lhs, leZero) || equal(rhs, leZero)) {
                    if (equal(lhs, leZero) && equal(rhs, leZero)) {
                        return geZero;
                    } else {
                        return leZero;
                    }
                } else if (equal(lhs, geZero) || equal(rhs, geZero)) {
                    return geZero;
                } else {
                    return gtZero;
                }
            }
            case DIVIDE: {
                if (equal(rhs, top()) || equal(rhs, leZero) ||
                    equal(rhs, eqZero) || equal(rhs, geZero)) {
                    // Handle cases where rhs possibly zero
                    return bottom();
                } else {
                    // Change operator to MULTIPLY and call recursively
                    // since transfer fn for DIVIDE and MULTIPLY are identical
                    // aside from checking for divide by zero
                    return arithmeticTransfer(BinaryOperator.TIMES, lhs, rhs);
                }
            }
            case MOD: {
                if (equal(rhs, top()) || equal(rhs, leZero) || equal(rhs, eqZero) || equal(rhs, geZero)) {
                    // Cases where rhs possibly zero
                    return bottom();
                } else if (equal(lhs, bottom()) || equal(rhs, bottom())) {
                    return bottom();
                } else if (equal(lhs, top())) {
                    return top();
                } else if (equal(lhs, eqZero)) {
                    return eqZero;
                } else if (equal(lhs, neZero) || equal(rhs, neZero)) {
                    return top();
                } else if (equal(rhs, ltZero)) {
                    if (equal(lhs, ltZero) || equal(lhs, leZero)) {
                        return geZero;
                    } else {
                        return leZero;
                    }
                } else {  // equal(rhs, gtZero)
                    if (equal(lhs, ltZero) || equal(lhs, leZero)) {
                        return leZero;
                    } else {
                        return geZero;
                    }
                }
            }
        }

        throw new IllegalStateException();
    }

    // ========================================================================
    // Useful helpers

    /** Get the top of the lattice */
    private AnnotationMirror top() {
        return analysis.getTypeFactory().getQualifierHierarchy().getTopAnnotations().iterator().next();
    }

    /** Get the bottom of the lattice */
    private AnnotationMirror bottom() {
        return analysis.getTypeFactory().getQualifierHierarchy().getBottomAnnotations().iterator().next();
    }

    /** Compute the least-upper-bound of two points in the lattice */
    private AnnotationMirror lub(AnnotationMirror x, AnnotationMirror y) {
        return analysis.getTypeFactory().getQualifierHierarchy().leastUpperBound(x, y);
    }

    /** Compute the greatest-lower-bound of two points in the lattice */
    private AnnotationMirror glb(AnnotationMirror x, AnnotationMirror y) {
        return analysis.getTypeFactory().getQualifierHierarchy().greatestLowerBound(x, y);
    }

    /** Convert a "Class" object (e.g. "Top.class") to a point in the lattice */
    private AnnotationMirror reflect(Class<? extends Annotation> qualifier) {
        return AnnotationBuilder.fromClass(
            analysis.getTypeFactory().getProcessingEnv().getElementUtils(),
            qualifier);
    }

    /** Determine whether two AnnotationMirrors are the same point in the lattice */
    private boolean equal(AnnotationMirror x, AnnotationMirror y) {
        return AnnotationUtils.areSame(x, y);
    }

    /** `x op y` == `y flip(op) x` */
    private Comparison flip(Comparison op) {
        switch (op) {
            case EQ: return Comparison.EQ;
            case NE: return Comparison.NE;
            case LT: return Comparison.GT;
            case LE: return Comparison.GE;
            case GT: return Comparison.LT;
            case GE: return Comparison.LE;
            default: throw new IllegalArgumentException(op.toString());
        }
    }

    /** `x op y` == `!(x negate(op) y)` */
    private Comparison negate(Comparison op) {
        switch (op) {
            case EQ: return Comparison.NE;
            case NE: return Comparison.EQ;
            case LT: return Comparison.GE;
            case LE: return Comparison.GT;
            case GT: return Comparison.LE;
            case GE: return Comparison.LT;
            default: throw new IllegalArgumentException(op.toString());
        }
    }

    // ========================================================================
    // Checker Framework plumbing

    public DivByZeroTransfer(CFAnalysis analysis) {
        super(analysis);
    }

    private TransferResult<CFValue, CFStore> implementComparison(Comparison op, BinaryOperationNode n, TransferResult<CFValue, CFStore> out) {
        QualifierHierarchy hierarchy = analysis.getTypeFactory().getQualifierHierarchy();
        AnnotationMirror l = findAnnotation(analysis.getValue(n.getLeftOperand()).getAnnotations(), hierarchy);
        AnnotationMirror r = findAnnotation(analysis.getValue(n.getRightOperand()).getAnnotations(), hierarchy);

        if (l == null || r == null) {
            // this can happen for generic types
            return out;
        }

        CFStore thenStore = out.getThenStore().copy();
        CFStore elseStore = out.getElseStore().copy();

        thenStore.insertValue(
                JavaExpression.fromNode(n.getLeftOperand()),
            refineLhsOfComparison(op, l, r));

        thenStore.insertValue(
            JavaExpression.fromNode(n.getRightOperand()),
            refineLhsOfComparison(flip(op), r, l));

        elseStore.insertValue(
            JavaExpression.fromNode(n.getLeftOperand()),
            refineLhsOfComparison(negate(op), l, r));

        elseStore.insertValue(
            JavaExpression.fromNode(n.getRightOperand()),
            refineLhsOfComparison(flip(negate(op)), r, l));

        return new ConditionalTransferResult<>(out.getResultValue(), thenStore, elseStore);
    }

    private TransferResult<CFValue, CFStore> implementOperator(BinaryOperator op, BinaryOperationNode n, TransferResult<CFValue, CFStore> out) {
        QualifierHierarchy hierarchy = analysis.getTypeFactory().getQualifierHierarchy();
        AnnotationMirror l = findAnnotation(analysis.getValue(n.getLeftOperand()).getAnnotations(), hierarchy);
        AnnotationMirror r = findAnnotation(analysis.getValue(n.getRightOperand()).getAnnotations(), hierarchy);

        if (l == null || r == null) {
            // this can happen for generic types
            return out;
        }

        AnnotationMirror res = arithmeticTransfer(op, l, r);
        CFValue newResultValue = analysis.createSingleAnnotationValue(res, out.getResultValue().getUnderlyingType());
        return new RegularTransferResult<>(newResultValue, out.getRegularStore());
    }

    @Override
    public TransferResult<CFValue, CFStore> visitEqualTo(EqualToNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.EQ, n, super.visitEqualTo(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNotEqual(NotEqualNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.NE, n, super.visitNotEqual(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThan(GreaterThanNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.GT, n, super.visitGreaterThan(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThanOrEqual(GreaterThanOrEqualNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.GE, n, super.visitGreaterThanOrEqual(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitLessThan(LessThanNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.LT, n, super.visitLessThan(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitLessThanOrEqual(LessThanOrEqualNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.LE, n, super.visitLessThanOrEqual(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitIntegerDivision(IntegerDivisionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.DIVIDE, n, super.visitIntegerDivision(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitIntegerRemainder(IntegerRemainderNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.MOD, n, super.visitIntegerRemainder(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitFloatingDivision(FloatingDivisionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.DIVIDE, n, super.visitFloatingDivision(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitFloatingRemainder(FloatingRemainderNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.MOD, n, super.visitFloatingRemainder(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalMultiplication(NumericalMultiplicationNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.TIMES, n, super.visitNumericalMultiplication(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalAddition(NumericalAdditionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.PLUS, n, super.visitNumericalAddition(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalSubtraction(NumericalSubtractionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.MINUS, n, super.visitNumericalSubtraction(n, p));
    }

    private static AnnotationMirror findAnnotation(
            Set<AnnotationMirror> set, QualifierHierarchy hierarchy) {
        if (set.size() == 0) {
            return null;
        }
        Set<? extends AnnotationMirror> tops = hierarchy.getTopAnnotations();
        return hierarchy.findAnnotationInSameHierarchy(set, tops.iterator().next());
    }

}
