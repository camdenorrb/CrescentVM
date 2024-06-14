package dev.twelveoclock.lang.crescent.compiler

import dev.twelveoclock.lang.crescent.iterator.PeekingNodeIterator
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.*
import dev.twelveoclock.lang.crescent.language.ir.CrescentIR
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.lexers.Lexer
import dev.twelveoclock.lang.crescent.parsers.Parser
import dev.twelveoclock.lang.crescent.project.extensions.minimize
import dev.twelveoclock.lang.crescent.vm.CrescentIRVM
import kotlin.io.path.Path

object CrescentIRCompiler {

	@JvmStatic
	fun main(args: Array<String>) {

		val code =
			"""
				fun thing(mew1 mew2: String) {
					println(mew1)
					println(mew2)
					println(mew1 + mew2)
				}
				
				fun main {
					println("Meow" + 1)
					println(1 + 2 * 4 / 2)
					println("Meow" + 1)
				    println("Meow" + 1)
					thing("Meow1", "Meow2")
				}
			""".trimIndent()

		val crescentIR = invoke(Parser.invoke(Path("meow.crescent"), Lexer.invoke(code)))
		crescentIR.commands.forEach { println(it) }
		println()
		CrescentIRVM(crescentIR).invoke()
	}

	// TODO: Maybe have multiple files of CrescentIR?
	fun invoke(file: File): CrescentIR {

		val commandsOutput = mutableListOf<CrescentIR.Command>()

		file.functions.forEach { (name, function) ->

			commandsOutput.add(CrescentIR.Command.Fun(name))

			function.params.forEach {
				commandsOutput.add(CrescentIR.Command.Push(it.name))
				commandsOutput.add(CrescentIR.Command.Assign)
			}

			function.innerCode.nodes.forEach {
				compileNode(it, commandsOutput)
			}
		}

		return CrescentIR(commandsOutput)
	}


	// TODO: Take in named values in a reassignable way
	private fun compileNode(node: CrescentAST.Node, commandsOutput: MutableList<CrescentIR.Command>) {
		when (node) {

			is Primitive.Boolean -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Char -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.String -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.I8 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.I16 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.I32 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.I64 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.U8 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.U16 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.U32 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.U64 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.F32 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.F64 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))

			is Identifier -> commandsOutput.add(CrescentIR.Command.PushNamedValue(node.name))
			is Expression -> compileExpression(node, commandsOutput)

			is Variable -> {
				node as Variable.Basic
				commandsOutput.add(CrescentIR.Command.Push(Identifier(node.name)))
				compileNode(node.value, commandsOutput)
				commandsOutput.add(CrescentIR.Command.Assign)
			}

			is Statement.For -> {

				node.block.nodes.forEach {
					compileNode(it, commandsOutput)
				}

				TODO("Complete")
			}

			is Statement.While -> {

				val start = commandsOutput.size
				compileNode(node.predicate, commandsOutput)
				val afterPredicate = commandsOutput.size

				node.block.nodes.forEach {
					compileNode(it, commandsOutput)
				}

				commandsOutput.add(CrescentIR.Command.Jump(start))
				commandsOutput.add(afterPredicate, CrescentIR.Command.JumpIfFalse(commandsOutput.size + 1))
			}

			is Statement.If -> {

				compileNode(node.predicate, commandsOutput)

				val startIndex = commandsOutput.size

				node.block.nodes.forEach {
					compileNode(it, commandsOutput)
				}

				commandsOutput.add(startIndex, CrescentIR.Command.JumpIfFalse(commandsOutput.size - startIndex))

				// TODO: Add support for else if
				node.elseBlock?.nodes?.forEach {
					compileNode(it, commandsOutput)
				}
			}

			is IdentifierCall -> {

				node.arguments.asReversed().forEach {
					compileNode(it, commandsOutput)
				}

				commandsOutput.add(CrescentIR.Command.Invoke(node.identifier))
			}

			is Return -> {
				compileNode(node.expression, commandsOutput)
			}

			else -> error("Unexpected node: \n${node::class} \n$node")
		}
	}

	private fun compileExpression(expression: Expression, commandsOutput: MutableList<CrescentIR.Command>) {

		val nodeIterator = PeekingNodeIterator(expression.nodes)

		while (nodeIterator.hasNext()) {
			when (val node = nodeIterator.next()) {

				is CrescentToken.Operator -> {
					compileOperator(node, commandsOutput)
				}

				is Primitive.String,
				is Primitive.Number,
				is Primitive.Char,
				is Primitive.Boolean,
				is CrescentAST.Node.Array,
				is Identifier,
				is GetCall,
				is Statement.If,
				is IdentifierCall,
				-> {
					compileNode(node, commandsOutput)
				}

				else -> error("Unexpected node: \n${node::class} \n$node")
			}
		}
	}

	private fun compileOperator(operator: CrescentToken.Operator, commandsOutput: MutableList<CrescentIR.Command>) {
		when (operator) {

			CrescentToken.Operator.NOT -> TODO()
			CrescentToken.Operator.ADD -> commandsOutput.add(CrescentIR.Command.Add)
			CrescentToken.Operator.SUB -> commandsOutput.add(CrescentIR.Command.Sub)
			CrescentToken.Operator.MUL -> commandsOutput.add(CrescentIR.Command.Mul)
			CrescentToken.Operator.DIV -> commandsOutput.add(CrescentIR.Command.Div)

			CrescentToken.Operator.BIT_SHIFT_RIGHT -> commandsOutput.add(CrescentIR.Command.ShiftRight)
			CrescentToken.Operator.BIT_SHIFT_LEFT -> commandsOutput.add(CrescentIR.Command.ShiftLeft)
			CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> commandsOutput.add(CrescentIR.Command.UnsignedShiftRight)
			CrescentToken.Operator.BIT_OR -> commandsOutput.add(CrescentIR.Command.Or)
			CrescentToken.Operator.BIT_AND -> commandsOutput.add(CrescentIR.Command.And)
			CrescentToken.Operator.BIT_XOR -> commandsOutput.add(CrescentIR.Command.Xor)


			CrescentToken.Operator.POW -> {
				TODO("Mooo ADD POW?!?!@?! D:")
			}
			CrescentToken.Operator.REM -> {
				TODO("Mooo ADD remainder?!?!@?! D:")
			}

			CrescentToken.Operator.ASSIGN -> {

				//println(commandsOutput)
				commandsOutput.add(CrescentIR.Command.Assign)
				//codeBuilder.setField()
				//TODO("Figure out")
			}

			CrescentToken.Operator.ADD_ASSIGN -> {

				commandsOutput.add(CrescentIR.Command.AddAssign)
				//println(commandsOutput)
				//TODO("Figure out")
			}

			CrescentToken.Operator.SUB_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.MUL_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.DIV_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.REM_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.POW_ASSIGN -> TODO("Figure out")

			CrescentToken.Operator.OR_COMPARE -> TODO()
			CrescentToken.Operator.AND_COMPARE -> commandsOutput.add(CrescentIR.Command.AndCompare)
			CrescentToken.Operator.EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.LESSER_EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.GREATER_EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.LESSER_COMPARE -> commandsOutput.add(CrescentIR.Command.IsLesser)
			CrescentToken.Operator.GREATER_COMPARE -> commandsOutput.add(CrescentIR.Command.IsGreater)
			CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()
			CrescentToken.Operator.NOT_EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
			CrescentToken.Operator.CONTAINS -> TODO()
			CrescentToken.Operator.RANGE_TO -> TODO()
			CrescentToken.Operator.TYPE_PREFIX -> TODO()
			CrescentToken.Operator.RETURN -> TODO()
			CrescentToken.Operator.RESULT -> TODO()
			CrescentToken.Operator.INSTANCE_OF -> TODO()
			CrescentToken.Operator.NOT_INSTANCE_OF -> TODO()

			CrescentToken.Operator.AS -> {
				/* Ignore */
			}

			else -> error("Unexpected operator: $operator")
		}
	}

}