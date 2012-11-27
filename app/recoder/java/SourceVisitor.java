// This file is part of the RECODER library and protected by the LGPL.

package recoder.java;

import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.AnnotationElementValuePair;
import recoder.java.declaration.AnnotationPropertyDeclaration;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.EnumConstantSpecification;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.Extends;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.Implements;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.Modifier;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.declaration.Throws;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.declaration.modifier.Abstract;
import recoder.java.declaration.modifier.Final;
import recoder.java.declaration.modifier.Native;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.Static;
import recoder.java.declaration.modifier.StrictFp;
import recoder.java.declaration.modifier.Synchronized;
import recoder.java.declaration.modifier.Transient;
import recoder.java.declaration.modifier.Volatile;
import recoder.java.expression.ArrayInitializer;
import recoder.java.expression.ElementValueArrayInitializer;
import recoder.java.expression.Literal;
import recoder.java.expression.Operator;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.literal.BooleanLiteral;
import recoder.java.expression.literal.CharLiteral;
import recoder.java.expression.literal.DoubleLiteral;
import recoder.java.expression.literal.FloatLiteral;
import recoder.java.expression.literal.IntLiteral;
import recoder.java.expression.literal.LongLiteral;
import recoder.java.expression.literal.NullLiteral;
import recoder.java.expression.literal.StringLiteral;
import recoder.java.expression.operator.BinaryAnd;
import recoder.java.expression.operator.BinaryAndAssignment;
import recoder.java.expression.operator.BinaryNot;
import recoder.java.expression.operator.BinaryOr;
import recoder.java.expression.operator.BinaryOrAssignment;
import recoder.java.expression.operator.BinaryXOr;
import recoder.java.expression.operator.BinaryXOrAssignment;
import recoder.java.expression.operator.Conditional;
import recoder.java.expression.operator.CopyAssignment;
import recoder.java.expression.operator.Divide;
import recoder.java.expression.operator.DivideAssignment;
import recoder.java.expression.operator.Equals;
import recoder.java.expression.operator.GreaterOrEquals;
import recoder.java.expression.operator.GreaterThan;
import recoder.java.expression.operator.Instanceof;
import recoder.java.expression.operator.LessOrEquals;
import recoder.java.expression.operator.LessThan;
import recoder.java.expression.operator.LogicalAnd;
import recoder.java.expression.operator.LogicalNot;
import recoder.java.expression.operator.LogicalOr;
import recoder.java.expression.operator.Minus;
import recoder.java.expression.operator.MinusAssignment;
import recoder.java.expression.operator.Modulo;
import recoder.java.expression.operator.ModuloAssignment;
import recoder.java.expression.operator.Negative;
import recoder.java.expression.operator.New;
import recoder.java.expression.operator.NewArray;
import recoder.java.expression.operator.NotEquals;
import recoder.java.expression.operator.Plus;
import recoder.java.expression.operator.PlusAssignment;
import recoder.java.expression.operator.Positive;
import recoder.java.expression.operator.PostDecrement;
import recoder.java.expression.operator.PostIncrement;
import recoder.java.expression.operator.PreDecrement;
import recoder.java.expression.operator.PreIncrement;
import recoder.java.expression.operator.ShiftLeft;
import recoder.java.expression.operator.ShiftLeftAssignment;
import recoder.java.expression.operator.ShiftRight;
import recoder.java.expression.operator.ShiftRightAssignment;
import recoder.java.expression.operator.Times;
import recoder.java.expression.operator.TimesAssignment;
import recoder.java.expression.operator.TypeCast;
import recoder.java.expression.operator.UnsignedShiftRight;
import recoder.java.expression.operator.UnsignedShiftRightAssignment;
import recoder.java.reference.AnnotationPropertyReference;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.EnumConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MetaClassReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.SuperConstructorReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisConstructorReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.reference.VariableReference;
import recoder.java.statement.Assert;
import recoder.java.statement.Break;
import recoder.java.statement.Case;
import recoder.java.statement.Catch;
import recoder.java.statement.Continue;
import recoder.java.statement.Default;
import recoder.java.statement.Do;
import recoder.java.statement.Else;
import recoder.java.statement.EmptyStatement;
import recoder.java.statement.EnhancedFor;
import recoder.java.statement.Finally;
import recoder.java.statement.For;
import recoder.java.statement.If;
import recoder.java.statement.LabeledStatement;
import recoder.java.statement.Return;
import recoder.java.statement.Switch;
import recoder.java.statement.SynchronizedBlock;
import recoder.java.statement.Then;
import recoder.java.statement.Throw;
import recoder.java.statement.Try;
import recoder.java.statement.While;

/**
 * A source visitor defines actions to be triggered while visiting source
 * elements. The {@link recoder.java.PrettyPrinter}is an instance of this
 * visitor.
 */
public abstract class SourceVisitor {

    /**
     * Visits the specified compilation unit. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitCompilationUnit(CompilationUnit x) {
        // defaults to nothing
   }

    /**
     * Visits the specified identifier. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitIdentifier(Identifier x) {
        // defaults to nothing
    }

    /**
     * Visits the specified import. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitImport(Import x) {
        // defaults to nothing
    }

    /**
     * Visits the specified package specification. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPackageSpecification(PackageSpecification x) {
        // defaults to nothing
    }

    /**
     * Visits the specified statement block. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitStatementBlock(StatementBlock x) {
        // defaults to nothing
   }

    /**
     * Visits the specified class declaration. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitClassDeclaration(ClassDeclaration x) {
        // defaults to nothing
    }
    
    public void visitAnnotationDeclaration(AnnotationDeclaration x) {
        // defaults to nothing
    }

    /**
     * Visits the specified class initializer. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitClassInitializer(ClassInitializer x) {
        // defaults to nothing
    }

    /**
     * Visits the specified constructor declaration. The default implementation
     * calls {@link #visitMethodDeclaration}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitConstructorDeclaration(ConstructorDeclaration x) {
        visitMethodDeclaration(x);
    }

    /**
     * Visits the specified extends. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitExtends(Extends x) {
        // defaults to nothing
    }

    /**
     * Visits the specified field declaration. The default implementation calls
     * {@link #visitVariableDeclaration}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitFieldDeclaration(FieldDeclaration x) {
        visitVariableDeclaration(x);
    }

    /**
     * Visits the specified implements clause. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitImplements(Implements x) {
        // defaults to nothing
    }

    /**
     * Visits the specified interface declaration. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitInterfaceDeclaration(InterfaceDeclaration x) {
        // defaults to nothing
    }

    /**
     * Visits the specified local variable declaration. The default
     * implementation calls {@link #visitVariableDeclaration}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLocalVariableDeclaration(LocalVariableDeclaration x) {
        visitVariableDeclaration(x);
    }

    /**
     * Visits the specified method declaration. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitMethodDeclaration(MethodDeclaration x) {
        // defaults to nothing
    }
    
    /**
     * Visits the specified annotation property declaration. 
     * Defaults to call visitMethodDeclaration.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitAnnotationPropertyDeclaration(AnnotationPropertyDeclaration x) {
        visitMethodDeclaration(x);
    }
    
    /**
     * Visit the specified AnnotationPropertyReference.
     * Defaults to call <code>x.getIdentifier().accept(this)</code>, if identifier is not null. 
     * @param x
     */
    public void visitAnnotationPropertyReference(AnnotationPropertyReference x) {
    	Identifier id = x.getIdentifier();
    	if (id != null)
    		id.accept(this);
    }


    /**
     * Visits the specified parameter declaration. The default implementation
     * calls {@link #visitVariableDeclaration}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitParameterDeclaration(ParameterDeclaration x) {
        visitVariableDeclaration(x);
    }

    /**
     * Visits the specified throws clause. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitThrows(Throws x) {
    	/* defaults to nothing */ 
    }

    /**
     * Visits the specified variable specification. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitVariableSpecification(VariableSpecification x) {
    	/* defaults to nothing */ 
    }

    /**
     * Visits the specified field specification. The default implementation
     * calls {@link #visitVariableSpecification}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitFieldSpecification(FieldSpecification x) {
        visitVariableSpecification(x);
    }

    /**
     * Visits the specified abstract modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitAbstract(Abstract x) {
        visitModifier(x);
    }

    /**
     * Visits the specified final modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitFinal(Final x) {
        visitModifier(x);
    }

    /**
     * Visits the specified native modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitNative(Native x) {
        visitModifier(x);
    }

    /**
     * Visits the specified private modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitPrivate(Private x) {
        visitModifier(x);
    }

    /**
     * Visits the specified protected modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitProtected(Protected x) {
        visitModifier(x);
    }

    /**
     * Visits the specified public modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitPublic(Public x) {
        visitModifier(x);
    }

    /**
     * Visits the specified static modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitStatic(Static x) {
        visitModifier(x);
    }

    /**
     * Visits the specified strictfp modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitStrictFp(StrictFp x) {
        visitModifier(x);
    }

    /**
     * Visits the specified synchronized modifier. The default implementation
     * calls {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitSynchronized(Synchronized x) {
        visitModifier(x);
    }

    /**
     * Visits the specified transient modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitTransient(Transient x) {
        visitModifier(x);
    }

    /**
     * Visits the specified volatile modifier. The default implementation calls
     * {@link #visitModifier}.
     * 
     * @param x
     *            the program element to final.
     */
    public void visitVolatile(Volatile x) {
        visitModifier(x);
    }
    
    /**
     * visits the specified annotation. The default implementation does nothing.
     * 
     * @param a
     */    
    public void visitElementValuePair(AnnotationElementValuePair x) {
    	/* defaults to nothing */ 
    }
    
    /**
     * visits the specified annotation. The default implementation calls
     * {@link #visitDeclarationSpecifier}.
     * 
     * @param a
     */
    public void visitAnnotationUse(AnnotationUseSpecification a) {
       visitDeclarationSpecifier(a); 
    }
    
    public void visitDeclarationSpecifier(DeclarationSpecifier x) {
    	/* defaults to nothing */ 
    }

    /**
     * Visits the specified array initializer. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitArrayInitializer(ArrayInitializer x) {
    	/* defaults to nothing */ 
    }
    
    /**
     * Visits the specified element value array initializer. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitElementValueArrayInitializer(ElementValueArrayInitializer x) {
    	/* defaults to nothing */ 
    }

    /**
     * Visits the specified parenthesized expression. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitParenthesizedExpression(ParenthesizedExpression x) {
    	/* defaults to nothing */ 
    }

    /**
     * Visits the specified boolean literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBooleanLiteral(BooleanLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified char literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitCharLiteral(CharLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified double literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitDoubleLiteral(DoubleLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified float literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitFloatLiteral(FloatLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified int literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitIntLiteral(IntLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified long literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLongLiteral(LongLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified null literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitNullLiteral(NullLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified string literal. The default implementation calls
     * {@link #visitLiteral}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitStringLiteral(StringLiteral x) {
        visitLiteral(x);
    }

    /**
     * Visits the specified binary-and operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryAnd(BinaryAnd x) {
        visitOperator(x);
    }

    /**
     * Visits the specified binary-and assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryAndAssignment(BinaryAndAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified binary-not operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryNot(BinaryNot x) {
        visitOperator(x);
    }

    /**
     * Visits the specified binary-or operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryOr(BinaryOr x) {
        visitOperator(x);
    }

    /**
     * Visits the specified binary-or assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryOrAssignment(BinaryOrAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified binary-xor operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryXOr(BinaryXOr x) {
        visitOperator(x);
    }

    /**
     * Visits the specified binary-xor assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBinaryXOrAssignment(BinaryXOrAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified conditional operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitConditional(Conditional x) {
        visitOperator(x);
    }

    /**
     * Visits the specified copy assignment operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitCopyAssignment(CopyAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified divide operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitDivide(Divide x) {
        visitOperator(x);
    }

    /**
     * Visits the specified divide assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitDivideAssignment(DivideAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified equals operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitEquals(Equals x) {
        visitOperator(x);
    }

    /**
     * Visits the specified greater-or-equals operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitGreaterOrEquals(GreaterOrEquals x) {
        visitOperator(x);
    }

    /**
     * Visits the specified greater-than operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitGreaterThan(GreaterThan x) {
        visitOperator(x);
    }

    /**
     * Visits the specified instanceof operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitInstanceof(Instanceof x) {
        visitOperator(x);
    }

    /**
     * Visits the specified less-or-equals operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLessOrEquals(LessOrEquals x) {
        visitOperator(x);
    }

    /**
     * Visits the specified less-than operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLessThan(LessThan x) {
        visitOperator(x);
    }

    /**
     * Visits the specified logical-and operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLogicalAnd(LogicalAnd x) {
        visitOperator(x);
    }

    /**
     * Visits the specified logical-not operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLogicalNot(LogicalNot x) {
        visitOperator(x);
    }

    /**
     * Visits the specified logical-or operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLogicalOr(LogicalOr x) {
        visitOperator(x);
    }

    /**
     * Visits the specified minus operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitMinus(Minus x) {
        visitOperator(x);
    }

    /**
     * Visits the specified minus assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitMinusAssignment(MinusAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified modulo operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitModulo(Modulo x) {
        visitOperator(x);
    }

    /**
     * Visits the specified modulo assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitModuloAssignment(ModuloAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified negative operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitNegative(Negative x) {
        visitOperator(x);
    }

    /**
     * Visits the specified new operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitNew(New x) {
        visitOperator(x);
    }

    /**
     * Visits the specified new-array operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitNewArray(NewArray x) {
        visitOperator(x);
    }

    /**
     * Visits the specified not-equals operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitNotEquals(NotEquals x) {
        visitOperator(x);
    }

    /**
     * Visits the specified plus operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPlus(Plus x) {
        visitOperator(x);
    }

    /**
     * Visits the specified plus assignment operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPlusAssignment(PlusAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified positive operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPositive(Positive x) {
        visitOperator(x);
    }

    /**
     * Visits the specified post-decrement operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPostDecrement(PostDecrement x) {
        visitOperator(x);
    }

    /**
     * Visits the specified post-increment operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPostIncrement(PostIncrement x) {
        visitOperator(x);
    }

    /**
     * Visits the specified pre-decrement operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPreDecrement(PreDecrement x) {
        visitOperator(x);
    }

    /**
     * Visits the specified pre-increment operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPreIncrement(PreIncrement x) {
        visitOperator(x);
    }

    /**
     * Visits the specified shift-left operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitShiftLeft(ShiftLeft x) {
        visitOperator(x);
    }

    /**
     * Visits the specified shift-left assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitShiftLeftAssignment(ShiftLeftAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified shift-right operator. The default implementation
     * calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitShiftRight(ShiftRight x) {
        visitOperator(x);
    }

    /**
     * Visits the specified shift-right assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitShiftRightAssignment(ShiftRightAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified times operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitTimes(Times x) {
        visitOperator(x);
    }

    /**
     * Visits the specified times assignment operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitTimesAssignment(TimesAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified type cast operator. The default implementation calls
     * {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitTypeCast(TypeCast x) {
        visitOperator(x);
    }

    /**
     * Visits the specified unsigned shift-right operator. The default
     * implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitUnsignedShiftRight(UnsignedShiftRight x) {
        visitOperator(x);
    }

    /**
     * Visits the specified unsigned shift-right assignment operator. The
     * default implementation calls {@link #visitOperator}.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitUnsignedShiftRightAssignment(UnsignedShiftRightAssignment x) {
        visitOperator(x);
    }

    /**
     * Visits the specified break statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitBreak(Break x) {
        // defaults to nothing
    }

    /**
     * Visits the specified case statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitCase(Case x) {
        // defaults to nothing
    }

    /**
     * Visits the specified catch branch. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitCatch(Catch x) {
        // defaults to nothing
    }

    /**
     * Visits the specified continue statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitContinue(Continue x) {
        // defaults to nothing
    }

    /**
     * Visits the specified default branch. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitDefault(Default x) {
        // defaults to nothing
    }

    /**
     * Visits the specified do statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitDo(Do x) {
        // defaults to nothing
    }

    /**
     * Visits the specified else branch. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitElse(Else x) {
        // defaults to nothing
    }

    /**
     * Visits the specified empty statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitEmptyStatement(EmptyStatement x) {
        // defaults to nothing
    }

    /**
     * Visits the specified finally branch. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitFinally(Finally x) {
        // defaults to nothing
    }

    /**
     * Visits the specified for statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitFor(For x) {
        // defaults to nothing
    }
    
    /**
     * Visits the specified enhanced for statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitEnhancedFor(EnhancedFor x) {
        // defaults to nothing
    }

    /**
     * Visits the specified assert statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitAssert(Assert x) {
        // defaults to nothing
    }

    /**
     * Visits the specified if statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitIf(If x) {
        // defaults to nothing
    }

    /**
     * Visits the specified labeled statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitLabeledStatement(LabeledStatement x) {
        // defaults to nothing
    }

    /**
     * Visits the specified return statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitReturn(Return x) {
        // defaults to nothing
    }

    /**
     * Visits the specified switch statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitSwitch(Switch x) {
        // defaults to nothing
    }

    /**
     * Visits the specified synchronized block. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitSynchronizedBlock(SynchronizedBlock x) {
        // defaults to nothing
    }

    /**
     * Visits the specified then branch. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitThen(Then x) {
        // defaults to nothing
    }

    /**
     * Visits the specified throw statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitThrow(Throw x) {
        // defaults to nothing
    }

    /**
     * Visits the specified try statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitTry(Try x) {
        // defaults to nothing
    }

    /**
     * Visits the specified while statement. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitWhile(While x) {
        // defaults to nothing
    }

    /**
     * Visits the specified array reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitArrayReference(ArrayReference x) {
        // defaults to nothing
    }


    /**
     * Visits the specified field reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitFieldReference(FieldReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified meta-class reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitMetaClassReference(MetaClassReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified method reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitMethodReference(MethodReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified package reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitPackageReference(PackageReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified super-constructor reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitSuperConstructorReference(SuperConstructorReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified super reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitSuperReference(SuperReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified this-constructor reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitThisConstructorReference(ThisConstructorReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified this reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitThisReference(ThisReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified type reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitTypeReference(TypeReference x) {
        // defaults to nothing
    }

    /**
     * Visits the specified uncollated reference qualifier. Defaults to do
     * nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitUncollatedReferenceQualifier(UncollatedReferenceQualifier x) {
        // defaults to nothing
    }

    /**
     * Visits the specified variable reference. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    public void visitVariableReference(VariableReference x) {
        // defaults to nothing
    }

    /**
     * Hook method that visits the specified modifier. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    protected void visitModifier(Modifier x) {
        // defaults to nothing
    }

    /**
     * Hook method that visits the specified literal. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    protected void visitLiteral(Literal x) {
        // defaults to nothing
    }

    /**
     * Hook method that visits the specified operator. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    protected void visitOperator(Operator x) {
        // defaults to nothing
    }

    /**
     * Visits the specified variable declaration. Defaults to do nothing.
     * 
     * @param x
     *            the program element to visit.
     */
    protected void visitVariableDeclaration(VariableDeclaration x) {
        // defaults to nothing
    }

    /**
     * Visits the specified single-line comment. The default implementation
     * calls {@link #visitComment}.
     * 
     * @param x
     *            the comment to visit.
     */
    public void visitSingleLineComment(SingleLineComment x) {
        visitComment(x);
    }

    /**
     * Visits the specified doc comment. The default implementation calls
     * {@link #visitComment}.
     * 
     * @param x
     *            the comment to visit.
     */
    public void visitDocComment(DocComment x) {
        visitComment(x);
    }

    /**
     * Visits the specified comment. Defaults to do nothing.
     * 
     * @param x
     *            the comment to visit.
     */
    public void visitComment(Comment x) {
        // defaults to nothing
    }
    
    /**
     * Visits the specified EnumConstructorReference,
     * which is part of an EnumConstantSpecification. 
     * Defaults to do nothing.
     * 
     * @param x
     *            the comment to visit.
     */
    public void visitEnumConstructorReference(EnumConstructorReference x) {
    	// default to nothing.
    }
    
    /**
     * Visits the specified EnumConstantDeclaration.
     * Defaults to do nothing.
     * 
     * @param x
     *            the EnumConstantDeclaration to visit.
     */
    public void visitEnumConstantDeclaration(EnumConstantDeclaration x) {
    	// default to nothing.
    }
    
    /**
     * Visits the specified EnumConstantSpecification.
     * Defaults to nothing
     * 
     * @param x
     *            the EnumConstantSpecification to visit.
     */
    public void visitEnumConstantSpecification(EnumConstantSpecification x) {
    	// defaults to nothing
    }
    
    /**
     * Visits the specified EnumDeclaration.
     * Defaults to do nothing.
     * 
     * @param x
     *            the comment to visit.
     */    
    public void visitEnumDeclaration(EnumDeclaration x) {
    	// defaults to nothing
    }
    
    /**
     * Visits the specified TypeArgument.
     * Defaults to do nothing.
     * 
     * @param x the TypeArgument to visit.
     */
    public void visitTypeArgument(TypeArgumentDeclaration x) {
    	// defaults to nothing
    }
    
    public void visitTypeParameter(TypeParameterDeclaration x) {
    	// defaults to nothing
    }

}