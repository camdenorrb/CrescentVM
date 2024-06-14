package dev.twelveoclock.lang.crescent.parsers

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import java.nio.file.Path

// TODO: Maybe support comments
// TODO: Unwrap expressions of size 1, and make everything take in a node rather than an expression
// Example: Variable("x", false, Visibility.PUBLIC, Type.Implicit, Expression(listOf(Number(1))))

// TODO: This can be converted to a generic parser when done
object Parser {

	// TODO: Loop through all the AST and generate parsers lazily List<Lazy<Parser>>



	class Expectations(
		// Essentially a list of possible tokens at each step for it to be considered a match
		tokens: List<List<CrescentToken>>,
	)

	class Block(
		val parsers: Map<Expectations, (List<CrescentToken>) ->
	)

	fun invoke(filePath: Path, tokens: List<CrescentToken>): CrescentAST.Node.File {

	}

	// Generate a parser based on the AST definition
	fun Scope.parserFor(node: CrescentAST.Node): Parser {

	}

}
	/*

	fun invoke(filePath: Path, tokens: List<CrescentToken>): CrescentAST.Node.File {

		val imports = mutableListOf<CrescentAST.Node.Import>()

		val impls = mutableMapOf<String, CrescentAST.Node.Impl>()
		val enums = mutableMapOf<String, CrescentAST.Node.Enum>()
		val traits = mutableMapOf<String, CrescentAST.Node.Trait>()
		val staticImpls = mutableMapOf<String, CrescentAST.Node.Impl>()

		val sealeds = mutableMapOf<String, CrescentAST.Node.Sealed>()
		val structs = mutableMapOf<String, CrescentAST.Node.Struct>()
		val objects = mutableMapOf<String, CrescentAST.Node.Object>()

		val functions = mutableMapOf<String, CrescentAST.Node.Function>()
		val variables = mutableMapOf<String, CrescentAST.Node.Variable.Basic>()
		val constants = mutableMapOf<String, CrescentAST.Node.Variable.Constant>()

		var mainFunction: CrescentAST.Node.Function? = null
		val tokenIterator = PeekingTokenIterator(tokens.filter { it !is CrescentToken.Data.Comment })

		var visibility = CrescentToken.Visibility.PUBLIC
		val modifiers = mutableListOf<CrescentToken.Modifier>()

		while (tokenIterator.hasNext()) {

			val token = tokenIterator.next()

			// Should read top level tokens
			when (token) {

				is CrescentToken.Data.Comment -> {
					/*NOOP*/
				}

				is CrescentToken.Visibility -> {
					visibility = token
					continue
				}

				is CrescentToken.Modifier -> {
					modifiers += token
					continue
				}

				CrescentToken.Type.STRUCT -> {
					readStruct(tokenIterator).let {
						structs[it.name] = it
					}
				}

				CrescentToken.Type.IMPL -> {
					readImpl(tokenIterator).let {
						if (CrescentToken.Modifier.STATIC in it.modifiers) {
							staticImpls["${it.type}"] = it
						} else {
							impls["${it.type}"] = it
						}
					}
				}

				CrescentToken.Variable.CONST -> {
					readConstant(tokenIterator, visibility).let {
						constants[it.name] = it
					}
				}

				CrescentToken.Variable.VAL, CrescentToken.Variable.VAR -> {
					readVariables(tokenIterator, visibility, token == CrescentToken.Variable.VAL).forEach {
						variables[it.name] = it
					}
				}

				CrescentToken.Type.TRAIT -> {

					val name = (tokenIterator.next() as CrescentToken.Key).string
					val functionTraits = mutableListOf<CrescentAST.Node.FunctionTrait>()

					readNextUntilClosed(tokenIterator) { innerTokens ->

						when (innerTokens) {

							CrescentToken.Bracket.OPEN,
							CrescentToken.Parenthesis.OPEN,
							CrescentToken.Parenthesis.CLOSE -> {
								/*NOOP*/
							}

							CrescentToken.Statement.FUN -> {
								functionTraits += readFunctionTrait(tokenIterator)
							}

							else -> error("Unexpected token $innerTokens")
						}

					}

					traits[name] = CrescentAST.Node.Trait(name, functionTraits)
				}

				CrescentToken.Type.OBJECT -> {
					readObject(tokenIterator).let {
						objects[it.name] = it
					}
				}

				CrescentToken.Type.ENUM -> {

					val name = (tokenIterator.next() as CrescentToken.Key).string
					val parameters = readParameters(tokenIterator)
					val entries = mutableListOf<CrescentAST.Node.EnumEntry>()

					readNextUntilClosed(tokenIterator) { innerToken ->

						if (innerToken == CrescentToken.Bracket.OPEN) {
							return@readNextUntilClosed
						}

						val entryName = (innerToken as CrescentToken.Key).string
						val entryArgs = readArguments(tokenIterator)

						entries += CrescentAST.Node.EnumEntry(entryName, entryArgs)
					}

					enums[name] = CrescentAST.Node.Enum(name, parameters, entries)
				}

				CrescentToken.Type.SEALED -> {

					val name = (tokenIterator.next() as CrescentToken.Key).string
					val innerStructs = mutableListOf<CrescentAST.Node.Struct>()
					val innerObjects = mutableListOf<CrescentAST.Node.Object>()

					readNextUntilClosed(tokenIterator) { innerToken ->

						when (innerToken) {
							CrescentToken.Type.STRUCT -> {
								innerStructs += readStruct(tokenIterator)
							}
							CrescentToken.Type.OBJECT -> {
								innerObjects += readObject(tokenIterator)
							}
							else -> {}
						}
					}

					sealeds[name] = CrescentAST.Node.Sealed(name, innerStructs, innerObjects)
				}

				CrescentToken.Statement.FUN -> {

					val function = readFunction(tokenIterator, visibility, modifiers)

					if (function.name == "main") {

						check(mainFunction == null) {
							"More than one main function???"
						}

						mainFunction = function
					}

					functions[function.name] = function
				}

				CrescentToken.Statement.IMPORT -> {

					// If it's a local import
					if (tokenIterator.peekNext() == CrescentToken.Operator.IMPORT_SEPARATOR) {

						// Skip import separator
						tokenIterator.next()

						val typeName = (tokenIterator.next() as CrescentToken.Key).string

						val typeAlias = if (tokenIterator.peekNext() == CrescentToken.Operator.AS) {
							checkEquals(CrescentToken.Operator.AS, tokenIterator.next())
							(tokenIterator.next() as CrescentToken.Key).string
						} else {
							null
						}

						imports += CrescentAST.Node.Import(
							path = "",
							typeName,
							typeAlias
						)

						continue
					}

					val path = buildString {

						append((tokenIterator.next() as CrescentToken.Key).string)

						while (tokenIterator.peekNext() == CrescentToken.Operator.DOT) {

							// Skip the dot
							tokenIterator.next()

							append('.').append((tokenIterator.next() as CrescentToken.Key).string)
						}
					}

					check(tokenIterator.next() == CrescentToken.Operator.IMPORT_SEPARATOR) {
						"Incorrect import format expected `{path}::{type}`"
					}

					val typeName = when (val next = tokenIterator.next()) {

						is CrescentToken.Key -> next.string
						CrescentToken.Operator.MUL -> "*"

						else -> error("Unexpected typeName: $next")
					}

					val typeAlias = if (tokenIterator.peekNext() == CrescentToken.Operator.AS) {
						checkEquals(CrescentToken.Operator.AS, tokenIterator.next())
						(tokenIterator.next() as CrescentToken.Key).string
					} else {
						null
					}

					imports += CrescentAST.Node.Import(path, typeName, typeAlias)
				}

				else -> error("Unexpected token: $token")
			}

			// Reset visibility and modifiers
			visibility = CrescentToken.Visibility.PUBLIC
			modifiers.clear()
		}

		return CrescentAST.Node.File(
			path = filePath,
			imports,
			structs,
			sealeds,
			impls,
			staticImpls,
			traits,
			objects,
			enums,
			variables,
			constants,
			functions,
			mainFunction,
		)
	}


	private fun readObject(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Object {

		val name = (tokenIterator.next() as CrescentToken.Key).string

		val objectVariables = mutableMapOf<String, CrescentAST.Node.Variable.Basic>()
		val objectFunctions = mutableMapOf<String, CrescentAST.Node.Function>()
		val objectConstants = mutableMapOf<String, CrescentAST.Node.Variable.Constant>()

		var innerVisibility = CrescentToken.Visibility.PUBLIC
		val innerModifiers = mutableListOf<CrescentToken.Modifier>()

		readNextUntilClosed(tokenIterator) { innerToken ->

			when (innerToken) {

				CrescentToken.Bracket.OPEN, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
					/*NOOP*/
				}

				is CrescentToken.Modifier -> {
					innerModifiers += innerToken
					return@readNextUntilClosed
				}

				CrescentToken.Variable.VAL -> {
					readVariable(tokenIterator, innerVisibility, isFinal = true).also {
						objectVariables[it.name] = it
					}
				}

				CrescentToken.Variable.CONST -> {
					readConstant(tokenIterator, innerVisibility).also {
						objectConstants[it.name] = it
					}
				}

				CrescentToken.Statement.FUN -> {
					readFunction(tokenIterator, innerVisibility, innerModifiers).also {
						objectFunctions[it.name] = it
					}
				}

				is CrescentToken.Visibility -> {
					innerVisibility = innerToken
					return@readNextUntilClosed
				}

				else -> error("Unexpected token $innerToken")
			}

			innerVisibility = CrescentToken.Visibility.PUBLIC
			innerModifiers.clear()
		}

		return CrescentAST.Node.Object(name, objectVariables, objectConstants, objectFunctions)
	}

	private fun readStruct(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Struct {

		val name = (tokenIterator.next() as CrescentToken.Key).string
		val variables = mutableListOf<CrescentAST.Node.Variable.Basic>()

		// Skip open bracket
		checkEquals(CrescentToken.Parenthesis.OPEN, tokenIterator.next())

		var variableVisibility = CrescentToken.Visibility.PUBLIC

		while (tokenIterator.hasNext()) {

			when (val nextToken = tokenIterator.next()) {

				CrescentToken.Parenthesis.CLOSE -> break

				is CrescentToken.Data.Comment -> continue

				CrescentToken.Operator.COMMA -> {
					// NOOP
				}

				is CrescentToken.Visibility -> {
					variableVisibility = nextToken
					continue
				}

				is CrescentToken.Variable -> {
					variables.addAll(
						readVariables(
							tokenIterator,
							variableVisibility,
							nextToken == CrescentToken.Variable.VAL
						)
					)
				}

				else -> error("Unexpected token $nextToken")
			}

			// Reset visibility and modifiers
			variableVisibility = CrescentToken.Visibility.PUBLIC
		}

		return CrescentAST.Node.Struct(name, variables)
	}

	private fun readImpl(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Impl {

		lateinit var type: CrescentAST.Node.Type

		val functions = mutableListOf<CrescentAST.Node.Function>()
		val readModifiers = mutableListOf<CrescentToken.Modifier>()
		val implModifiers = mutableListOf<CrescentToken.Modifier>()

		var readVisibility = CrescentToken.Visibility.PUBLIC

		readNextUntilClosed(tokenIterator) { token ->

			when (token) {

				CrescentToken.Bracket.OPEN, is CrescentToken.Data.Comment, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
					/*NOOP*/
				}

				is CrescentToken.Key, CrescentToken.SquareBracket.OPEN -> {
					tokenIterator.back() // UnSkip type start
					type = readType(tokenIterator)
					implModifiers.addAll(readModifiers)
				}

				is CrescentToken.Modifier -> {
					readModifiers += token
					return@readNextUntilClosed
				}

				is CrescentToken.Visibility -> {
					readVisibility = token
					return@readNextUntilClosed
				}

				CrescentToken.Statement.FUN -> {
					functions += readFunction(tokenIterator, readVisibility, readModifiers)
				}

				else -> error("Unexpected token $token")
			}

			readModifiers.clear()
			readVisibility = CrescentToken.Visibility.PUBLIC
		}

		return CrescentAST.Node.Impl(type, implModifiers, emptyList(), functions)
	}


	private fun readFunction(
		tokenIterator: PeekingTokenIterator,
		visibility: CrescentToken.Visibility,
		modifiers: List<CrescentToken.Modifier>
	): CrescentAST.Node.Function {

		val name = (tokenIterator.next() as CrescentToken.Key).string
		val parameters = readParameters(tokenIterator)

		val type =
			if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
				tokenIterator.next()
				readType(tokenIterator)
			} else {
				CrescentAST.Node.Type.unit
			}

		val expressions = readBlock(tokenIterator)

		return CrescentAST.Node.Function(
			name,
			modifiers,
			visibility,
			parameters,
			type,
			expressions,
		)
	}

	private fun readBlock(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Statement.Block {

		val expressionNodes = mutableListOf<CrescentAST.Node>()

		checkEquals(CrescentToken.Bracket.OPEN, tokenIterator.next())

		while (tokenIterator.hasNext() && tokenIterator.peekNext() != CrescentToken.Bracket.CLOSE) {

			val peekNext = tokenIterator.peekNext()

			if (peekNext == CrescentToken.Variable.VAL || peekNext == CrescentToken.Variable.VAR) {

				tokenIterator.next()

				readVariables(
					tokenIterator,
					CrescentToken.Visibility.PUBLIC,
					peekNext == CrescentToken.Variable.VAL
				).forEach {
					expressionNodes += it
				}
			}
			// If assign after identifier || If assign after get
			// TODO: FIGURE THIS OUT
			else if (tokenIterator.peekNext(2) is CrescentToken.Operator) { //|| tokenIterator.peekNext(5) is CrescentToken.Operator) {
				expressionNodes += readExpression(tokenIterator)
			} else {

				val expressionNode = when (tokenIterator.peekNext()) {

					CrescentToken.Statement.WHILE -> {
						readWhile(tokenIterator)
					}

					CrescentToken.Statement.FOR -> {
						readFor(tokenIterator)
					}

					else -> {
						readExpressionNode(tokenIterator)
					}
				}


				if (expressionNode != null) {
					expressionNodes += expressionNode
				}
			}
		}

		if (tokenIterator.hasNext()) {
			checkEquals(CrescentToken.Bracket.CLOSE, tokenIterator.next())
		}

		return CrescentAST.Node.Statement.Block(expressionNodes)
	}

	private fun readFunctionTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionTrait {

		val name = (tokenIterator.next() as CrescentToken.Key).string
		val parameters = readParameters(tokenIterator)

		val type =
			if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
				tokenIterator.next()
				readType(tokenIterator)
			} else {
				CrescentAST.Node.Type.unit
			}

		return CrescentAST.Node.FunctionTrait(name, parameters, type)
	}

	private fun readConstant(
		tokenIterator: PeekingTokenIterator,
		visibility: CrescentToken.Visibility,
	): CrescentAST.Node.Variable.Constant {

		val name = (tokenIterator.next() as CrescentToken.Key).string

		val type =
			if (tokenIterator.peekNext() == CrescentToken.Operator.TYPE_PREFIX) {
				tokenIterator.next() // Skip
				readType(tokenIterator)
			} else {
				CrescentAST.Node.Type.Implicit
			}

		val expression =
			if (tokenIterator.peekNext() == CrescentToken.Operator.ASSIGN) {
				tokenIterator.next() // Skip
				readExpression(tokenIterator)
			} else {
				CrescentAST.Node.Expression(emptyList())
			}


		return CrescentAST.Node.Variable.Constant(name, type, expression, visibility)
	}

	fun readVariable(
		tokenIterator: PeekingTokenIterator,
		visibility: CrescentToken.Visibility,
		isFinal: Boolean,
	): CrescentAST.Node.Variable.Basic {

		val name = (tokenIterator.next() as CrescentToken.Key).string

		val type =
			if (tokenIterator.peekNext() == CrescentToken.Operator.TYPE_PREFIX) {
				tokenIterator.next()
				readType(tokenIterator)
			} else {
				CrescentAST.Node.Type.Implicit
			}

		val expression =
			if (tokenIterator.peekNext() == CrescentToken.Operator.ASSIGN) {
				checkEquals(CrescentToken.Operator.ASSIGN, tokenIterator.next())
				readExpression(tokenIterator)
			} else {
				CrescentAST.Node.Expression(emptyList())
			}

		return CrescentAST.Node.Variable.Basic(name, type, expression, isFinal, visibility)
	}

	fun readVariables(
		tokenIterator: PeekingTokenIterator,
		visibility: CrescentToken.Visibility,
		isFinal: Boolean,
	): List<CrescentAST.Node.Variable.Basic> {

		val names =
			tokenIterator.nextUntil { it !is CrescentToken.Key && it != CrescentToken.Operator.COMMA }.mapNotNull {
				if (it == CrescentToken.Operator.COMMA) {
					null
				} else {
					(it as CrescentToken.Key).string
				}
			}

		check(names.isNotEmpty()) {
			"Variable has no names?"
		}

		val type =
			if (tokenIterator.peekNext() == CrescentToken.Operator.TYPE_PREFIX) {
				tokenIterator.next()
				readType(tokenIterator)
			} else {
				CrescentAST.Node.Type.Implicit
			}

		val expression =
			if (tokenIterator.peekNext() == CrescentToken.Operator.ASSIGN) {
				checkEquals(CrescentToken.Operator.ASSIGN, tokenIterator.next())
				readExpression(tokenIterator)
			} else {
				CrescentAST.Node.Expression(emptyList())
			}

		return names.map { name ->
			CrescentAST.Node.Variable.Basic(name, type, expression, isFinal, visibility)
		}
	}

	fun readWhile(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Statement.While {

		checkEquals(CrescentToken.Statement.WHILE, tokenIterator.next())
		checkEquals(CrescentToken.Parenthesis.OPEN, tokenIterator.next())

		val predicate = readExpression(tokenIterator)
		checkEquals(CrescentToken.Parenthesis.CLOSE, tokenIterator.next())
		val block = readBlock(tokenIterator)

		return CrescentAST.Node.Statement.While(predicate, block)
	}

	fun readFor(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Statement.For {

		checkEquals(CrescentToken.Statement.FOR, tokenIterator.next())

		val identifiers = mutableListOf<CrescentAST.Node.Identifier>()

		while (tokenIterator.hasNext()) {

			identifiers += readExpressionNode(tokenIterator) as CrescentAST.Node.Identifier

			if (tokenIterator.peekNext() != CrescentToken.Operator.COMMA) {
				break
			}

			checkEquals(CrescentToken.Operator.COMMA, tokenIterator.next())
		}

		checkEquals(CrescentToken.Operator.CONTAINS, tokenIterator.next())

		val ranges = mutableListOf<CrescentAST.Node.Statement.Range>()

		while (tokenIterator.hasNext()) {

			ranges += readExpressionNode(tokenIterator) as CrescentAST.Node.Statement.Range

			if (tokenIterator.peekNext() != CrescentToken.Operator.COMMA) {
				break
			}

			checkEquals(CrescentToken.Operator.COMMA, tokenIterator.next())
		}

		val block = readBlock(tokenIterator)

		return CrescentAST.Node.Statement.For(identifiers, ranges, block)
	}

	fun readParameters(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Parameter> {

		if (tokenIterator.peekNext() != CrescentToken.Parenthesis.OPEN) {
			return emptyList()
		}

		checkEquals(CrescentToken.Parenthesis.OPEN, tokenIterator.next())
		val parameters = mutableListOf<CrescentAST.Node.Parameter>()

		// TODO: Count opens and closes
		// TODO: Support default values
		while (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {

			val names = tokenIterator.nextUntil { it !is CrescentToken.Key }.map {
				(it as CrescentToken.Key).string
			}

			checkEquals(CrescentToken.Operator.TYPE_PREFIX, tokenIterator.next())
			val type = readType(tokenIterator)

			names.forEach { name ->
				parameters += CrescentAST.Node.Parameter.Basic(name, type)
			}

			if (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
				checkEquals(CrescentToken.Operator.COMMA, tokenIterator.next())
			}
		}

		checkEquals(CrescentToken.Parenthesis.CLOSE, tokenIterator.next())

		return parameters
	}

	fun readArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node> {

		if (tokenIterator.peekNext() != CrescentToken.Parenthesis.OPEN) {
			return emptyList()
		}

		checkEquals(CrescentToken.Parenthesis.OPEN, tokenIterator.next())
		val arguments = mutableListOf<CrescentAST.Node>()

		while (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {

			arguments += readExpression(tokenIterator)

			if (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
				checkEquals(CrescentToken.Operator.COMMA, tokenIterator.next())
			}
		}

		checkEquals(CrescentToken.Parenthesis.CLOSE, tokenIterator.next())

		return arguments
	}

	// TODO: Merge with readArguments
	fun readGetArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node> {

		if (tokenIterator.peekNext() != CrescentToken.SquareBracket.OPEN) {
			return emptyList()
		}

		checkEquals(CrescentToken.SquareBracket.OPEN, tokenIterator.next())
		val arguments = mutableListOf<CrescentAST.Node>()

		while (tokenIterator.peekNext() != CrescentToken.SquareBracket.CLOSE) {

			arguments += readExpression(tokenIterator)

			if (tokenIterator.peekNext() != CrescentToken.SquareBracket.CLOSE) {
				checkEquals(CrescentToken.Operator.COMMA, tokenIterator.next())
			}
		}

		checkEquals(CrescentToken.SquareBracket.CLOSE, tokenIterator.next())

		return arguments
	}

	fun readWhen(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Statement.When {

		val clauses = mutableListOf<CrescentAST.Node.Statement.When.Clause>()

		val argument =
			if (tokenIterator.peekNext() == CrescentToken.Parenthesis.OPEN) {
				checkEquals(CrescentToken.Parenthesis.OPEN, tokenIterator.next())
				readExpression(tokenIterator)
			} else {
				CrescentAST.Node.Expression(emptyList())
			}

		checkEquals(CrescentToken.Parenthesis.CLOSE, tokenIterator.next())
		checkEquals(CrescentToken.Bracket.OPEN, tokenIterator.next())

		readNextUntilClosed(tokenIterator) {

			// Unskip the read token from readNextUntilClosed(tokenIterator)
			tokenIterator.back()

			val ifExpressionNode = when (tokenIterator.peekNext()) {

				// TODO: Add contains operator here

				CrescentToken.Operator.DOT -> {

					checkEquals(CrescentToken.Operator.DOT, tokenIterator.next())
					val identifier = readExpressionNode(tokenIterator) as CrescentAST.Node.Identifier
					//checkEquals(tokenIterator.next(), CrescentToken.Operator.RETURN)

					CrescentAST.Node.Statement.When.EnumShortHand(identifier.name)
				}

				CrescentToken.Statement.ELSE -> {

					checkEquals(CrescentToken.Statement.ELSE, tokenIterator.next())
					checkEquals(CrescentToken.Operator.RETURN, tokenIterator.next())

					CrescentAST.Node.Statement.When.Else(readBlock(tokenIterator))
				}

				else -> {
					readExpressionNode(tokenIterator) ?: return@readNextUntilClosed
				}

			}


			// Usually happens if there is a comment before last closing }

			//println(ifExpressionNode)

			// If is else statement, no ifExpression
			if (ifExpressionNode is CrescentAST.Node.Statement.When.Else) {
				clauses += CrescentAST.Node.Statement.When.Clause(null, ifExpressionNode.thenBlock)
				return@readNextUntilClosed
			}

			checkEquals(CrescentToken.Operator.RETURN, tokenIterator.next())

			val thenExpressions =
				if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
					readBlock(tokenIterator)
				} else {
					CrescentAST.Node.Statement.Block(listOfNotNull(readExpression(tokenIterator)))
				}

			clauses += CrescentAST.Node.Statement.When.Clause(ifExpressionNode, thenExpressions)
		}

		return CrescentAST.Node.Statement.When(argument, clauses)
	}

	fun readType(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Type {

		var type: CrescentAST.Node.Type

		when (val peekNext = tokenIterator.peekNext()) {

			CrescentToken.SquareBracket.OPEN -> {

				tokenIterator.next()
				type =
					CrescentAST.Node.Type.Array(CrescentAST.Node.Type.Basic((tokenIterator.next() as CrescentToken.Key).string))

				// TODO: Maybe support array of results

				// Skip Array close
				checkEquals(CrescentToken.SquareBracket.CLOSE, tokenIterator.next())
			}

			is CrescentToken.Key -> {
				tokenIterator.next()
				type = CrescentAST.Node.Type.Basic(peekNext.string)
			}

			else -> {
				type = CrescentAST.Node.Type.Implicit
			}

		}

		if (tokenIterator.peekNext() == CrescentToken.Operator.RESULT) {
			tokenIterator.next()
			type = CrescentAST.Node.Type.Result(type)
		}

		return type
	}

	// Returns null if the node should be skipped in the expression
	// TODO: Find a way to remove recursion?
	fun readExpressionNode(tokenIterator: PeekingTokenIterator, isInDotChain: Boolean = false): CrescentAST.Node? {

		return when (val next = tokenIterator.next()) {

			is CrescentToken.Data.Comment -> {
				null
			}

			CrescentToken.SquareBracket.OPEN -> {

				val nodes = mutableListOf<CrescentAST.Node>()

				while (
					tokenIterator.hasNext() &&
					tokenIterator.peekNext() != CrescentToken.SquareBracket.CLOSE
				) {

					nodes += readExpressionNode(tokenIterator)!!

					if (tokenIterator.peekNext() != CrescentToken.SquareBracket.CLOSE) {
						checkEquals(CrescentToken.Operator.COMMA, tokenIterator.next())
					}
				}

				checkEquals(CrescentToken.SquareBracket.CLOSE, tokenIterator.next())
				CrescentAST.Node.Array(nodes.toTypedArray())
			}

			CrescentToken.Operator.RETURN -> {
				CrescentAST.Node.Return(readExpression(tokenIterator))
			}

			/*
			CrescentToken.Operator.INSTANCE_OF -> {
				CrescentAST.Node.InstanceOf(readExpression(tokenIterator))
			}
			*/

			is CrescentToken.Operator -> {
				next
			}

			CrescentToken.Statement.WHEN -> {
				readWhen(tokenIterator)
			}

			CrescentToken.Parenthesis.OPEN -> {
				readExpression(tokenIterator).also {
					checkEquals(CrescentToken.Parenthesis.CLOSE, tokenIterator.next())
				}
			}

			CrescentToken.Statement.IF -> {

				checkEquals(CrescentToken.Parenthesis.OPEN, tokenIterator.next())

				val argument = readExpression(tokenIterator).also {
					checkEquals(CrescentToken.Parenthesis.CLOSE, tokenIterator.next())
				}

				val ifBlock = readBlock(tokenIterator)

				val elseBlock =
					if (tokenIterator.peekNext() == CrescentToken.Statement.ELSE) {
						tokenIterator.next()
						readBlock(tokenIterator)
					} else {
						null
					}

				CrescentAST.Node.Statement.If(argument, ifBlock, elseBlock)
			}

			is CrescentToken.Data -> {
				when (next) {

					is CrescentToken.Data.String -> {

						// If no string interpolation, keep it simple
						if ('$' !in next.kotlinString) {
							return CrescentAST.Node.Primitive.String(next.kotlinString)
						}


						// String interpolation
						val nodes = mutableListOf<CrescentAST.Node>()
						val builder = StringBuilder()
						val iterator = PeekingCharIterator(next.kotlinString)

						while (iterator.hasNext()) {

							val nextChar = iterator.next()

							if (nextChar == '\\' && iterator.peekNext() == '$') {
								checkEquals(iterator.next(), '$')
								builder.append('$')
								continue
							}

							if (nextChar != '$' || (!iterator.peekNext().isLetter() && iterator.peekNext() != '{')) {
								builder.append(nextChar)
								continue
							}

							if (builder.isNotEmpty() || (builder.isEmpty() && nodes.isEmpty())) {
								nodes += CrescentAST.Node.Primitive.String(builder.toString())
								builder.clear()
								nodes += CrescentToken.Operator.ADD
							}

							if (iterator.peekNext() == '{') {
								nodes += readExpression(
									PeekingTokenIterator(
										CrescentLexer.invoke(
											iterator.nextUntilAndSkip(
												'}'
											)
										)
									)
								)
							} else {
								nodes += CrescentAST.Node.Identifier(iterator.nextUntil { it == ' ' || it == '$' || !it.isLetterOrDigit() })
							}

							if (iterator.hasNext()) {
								nodes += CrescentToken.Operator.ADD
							}
						}

						if (builder.isNotEmpty()) {
							nodes += CrescentAST.Node.Primitive.String(builder.toString())
						}

						return if (nodes.size == 1) {
							nodes[0]
						} else {
							CrescentAST.Node.Expression(ShuntingYard.invoke(nodes))
						}
					}

					is CrescentToken.Data.Char -> {
						if (tokenIterator.peekNext() == CrescentToken.Operator.RANGE_TO) {
							checkEquals(CrescentToken.Operator.RANGE_TO, tokenIterator.next())
							CrescentAST.Node.Statement.Range(
								CrescentAST.Node.Primitive.Char(next.kotlinChar),
								readExpressionNode(tokenIterator)!!
							)
						} else {
							CrescentAST.Node.Primitive.Char(next.kotlinChar)
						}
					}

					is CrescentToken.Data.Boolean -> {
						CrescentAST.Node.Primitive.Boolean(next.kotlinBoolean)
					}

					is CrescentToken.Data.Number -> {
						if (tokenIterator.peekNext() == CrescentToken.Operator.RANGE_TO) {
							checkEquals(CrescentToken.Operator.RANGE_TO, tokenIterator.next())
							CrescentAST.Node.Statement.Range(
								CrescentAST.Node.Primitive.Number.from(next.number),
								readExpressionNode(tokenIterator)!!
							)
						} else {
							CrescentAST.Node.Primitive.Number.from(next.number)
						}
					}

					else -> error("Unknown data token: $next")
				}
			}

			is CrescentToken.Key -> {

				val identifier = next.string

				val keyNode = when (tokenIterator.peekNext()) {

					CrescentToken.Parenthesis.OPEN -> {
						CrescentAST.Node.IdentifierCall(identifier, readArguments(tokenIterator))
					}

					CrescentToken.SquareBracket.OPEN -> {
						CrescentAST.Node.GetCall(identifier, readGetArguments(tokenIterator))
					}

					else -> {
						CrescentAST.Node.Identifier(identifier)
					}

				}

				if (tokenIterator.peekNext() == CrescentToken.Operator.DOT && !isInDotChain) {

					val nodes = mutableListOf(keyNode)

					while (tokenIterator.peekNext() == CrescentToken.Operator.DOT) {
						tokenIterator.next()
						nodes += readExpressionNode(tokenIterator, true)!!
					}

					return CrescentAST.Node.DotChain(nodes)
				}

				return keyNode
			}

			else -> {
				error("Unexpected token: $next")
			}

		}

	}

	fun readExpression(tokenIterator: PeekingTokenIterator): CrescentAST.Node {

		val nodes = mutableListOf<CrescentAST.Node>()

		if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
			tokenIterator.next()
		}

		while (tokenIterator.hasNext()) {
			nodes += when (val peekNext = tokenIterator.peekNext()) {

				CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.CLOSE, CrescentToken.SquareBracket.CLOSE -> {
					break
				}

				CrescentToken.Operator.COMMA -> {
					break
				}

				else -> {

					if (nodes.isNotEmpty()) {
						if (peekNext is CrescentToken.Operator) {
							if (peekNext == CrescentToken.Operator.RETURN) {
								break
							}
						} else if (nodes.lastOrNull() !is CrescentToken.Operator) {
							break
						}
					}

					readExpressionNode(tokenIterator) ?: continue
				}
			}
		}

		// If only one node unwrap from expression
		return if (nodes.size == 1) {
			nodes[0]
		}
		// If there is operations, sort them
		else if (nodes.any { it is CrescentToken.Operator }) {
			CrescentAST.Node.Expression(ShuntingYard.invoke(nodes))
		}
		// Return as is
		else {
			CrescentAST.Node.Expression(nodes)
		}
	}

	inline fun readNextUntilClosed(tokenIterator: PeekingTokenIterator, block: (token: CrescentToken) -> Unit) {

		var count = 0

		while (tokenIterator.hasNext()) {

			val token = tokenIterator.next()

			when (token) {
				CrescentToken.Bracket.OPEN -> {
					count++
				}
				CrescentToken.Bracket.CLOSE -> {

					count--

					if (count <= 0) {
						break
					}
				}
			}

			block(token)
		}
	}

}
*/