/*
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.rule.convention

import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractAstVisitor
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codenarc.util.AstUtil

/**
 * Checks for if statements where both the if and else blocks contain only a single return statement with a value
 *
 * @author Chris Mair
 */
class IfStatementCouldBeTernaryRule extends AbstractAstVisitorRule {

    String name = 'IfStatementCouldBeTernary'
    int priority = 2
    Class astVisitorClass = IfStatementCouldBeTernaryAstVisitor
}

class IfStatementCouldBeTernaryAstVisitor extends AbstractAstVisitor {

    @Override
    void visitIfElse(IfStatement ifElse) {
        if (isOnlyReturnStatement(ifElse.ifBlock) && isOnlyReturnStatement(ifElse.elseBlock)) {
            def ternaryExpression = "return ${ifElse.booleanExpression.text} ? ${getReturnValue(ifElse.ifBlock)} : ${getReturnValue(ifElse.elseBlock)}"
            addViolation(ifElse, "The if statement in class $currentClassName can be rewritten using the ternary operator: $ternaryExpression")
        }
        super.visitIfElse(ifElse)
    }

    private boolean isOnlyReturnStatement(ASTNode node) {
        isBlockWithSingleReturnStatement(node) || isReturnStatementWithValue(node)
    }

    private boolean isBlockWithSingleReturnStatement(ASTNode node) {
        return node instanceof BlockStatement &&
            node.statements.size() == 1 &&
            isReturnStatementWithValue(node.statements[0])
    }

    private boolean isReturnStatementWithValue(ASTNode node) {
        return node instanceof ReturnStatement &&
            !isNullValue(node.expression)
    }

    private boolean isNullValue(expression) {
        expression instanceof ConstantExpression && expression.isNullExpression()
    }

    private String getReturnValue(ASTNode node) {
        def expr = node instanceof BlockStatement ? node.statements[0].expression : node.expression
        return AstUtil.createPrettyExpression(expr)
    }

}