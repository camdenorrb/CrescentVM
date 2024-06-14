package dev.twelveoclock.lang.crescent.math

import dev.twelveoclock.lang.crescent.iterator.PeekingNodeIterator
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import java.util.*

object ShuntingYard {

	// TODO: Make this work on common ast alone
	//       This can be done via annotations in AST
	fun invoke(input: List<CrescentAST.Node>): List<CrescentAST.Node> {

		// Queue -> Add/Remove
		// Stack -> Push/Pop

		val outputQueue = LinkedList<CrescentAST.Node>()
		val operatorStack = LinkedList<CrescentToken.Operator>()

		val nodeIterator = PeekingNodeIterator(input)

		while (nodeIterator.hasNext()) {
			when (val next = nodeIterator.next()) {

				is CrescentAST.Node.Primitive.Number,
				is CrescentAST.Node.Identifier,
				-> {
					outputQueue.add(next)
				}

				is CrescentAST.Node.Expression -> {
					// Don't need to invoke again due to how the parser works
					outputQueue.addAll(next.nodes)
				}

				is CrescentToken.Operator -> {

					while (operatorStack.isNotEmpty() && precedence(operatorStack.first) <= precedence(next)) {
						outputQueue.add(operatorStack.pop())
					}

					operatorStack.push(next)
				}

				else -> outputQueue.add(next)
			}
		}

		while (operatorStack.isNotEmpty()) {
			outputQueue.add(operatorStack.pop())
		}

		return outputQueue
	}

	// Lower is higher precedence (Runs operations top to bottom)
	fun precedence(operator: CrescentToken.Operator) = when (operator) {

		CrescentToken.Operator.POW -> 1

		CrescentToken.Operator.MUL,
		CrescentToken.Operator.DIV,
		CrescentToken.Operator.REM,
		-> 2

		CrescentToken.Operator.ADD,
		CrescentToken.Operator.SUB
		-> 3

		CrescentToken.Operator.BIT_SHIFT_LEFT,
		CrescentToken.Operator.BIT_SHIFT_RIGHT,
		CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT,
		-> 4

		CrescentToken.Operator.BIT_AND -> 5
		CrescentToken.Operator.BIT_XOR -> 6
		CrescentToken.Operator.BIT_OR -> 7

		CrescentToken.Operator.INSTANCE_OF -> 10

		CrescentToken.Operator.LESSER_EQUALS_COMPARE,
		CrescentToken.Operator.LESSER_COMPARE,
		CrescentToken.Operator.GREATER_COMPARE,
		CrescentToken.Operator.EQUALS_COMPARE,
		CrescentToken.Operator.NOT_EQUALS_COMPARE,
		CrescentToken.Operator.EQUALS_REFERENCE_COMPARE,
		CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE,
		-> 11

		CrescentToken.Operator.AND_COMPARE -> 12
		CrescentToken.Operator.OR_COMPARE -> 13

		// Should always be higher than any operators, for stuff like assignment
		else -> 14
	}

}