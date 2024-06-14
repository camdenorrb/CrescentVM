package dev.twelveoclock.lang.crescent.lexers

import dev.twelveoclock.lang.crescent.iterator.PeekingCharIterator
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.language.token.Token
import dev.twelveoclock.lang.crescent.project.checkEquals
import dev.twelveoclock.lang.crescent.project.extensions.minimize

// TODO: Support negative numbers
// TODO: Make this generic
object Lexer {

	// TODO: Remake to act like how my filter works, finds all matches and eliminates as it continues to read
	fun invoke(input: String): List<Token> {

		val tokens = mutableListOf<Token>()
		val charIterator = PeekingCharIterator(input)

		while (charIterator.hasNext()) {

			// MicroOptimization to avoid toDoubleOrNull
			var isANumber = false

			// Skip to next key
			charIterator.nextUntil {
				!it.isWhitespace()
			}

			if (!charIterator.hasNext()) {
				break
			}

			val key = when (val peekNext = charIterator.peekNext()) {

				':' -> {
					if (charIterator.peekNext(2) == ':') {
						"${charIterator.next()}${charIterator.next()}"
					} else {
						"${charIterator.next()}"
					}
				}

				'!', '+', '-', '/', '%', '^', '*', '=', '<', '>' -> {

					val next = charIterator.next()
					val peek = charIterator.peekNext()

					// If is negative number
					/*
					if (!charIterator.peekBack(2).isDigit() && next == '-' && (peek.isDigit() || (peek == '.' && charIterator.peekNext(2).isDigit()))) {
						isANumber = true
						"-${readNumber(charIterator)}"
					}
					else*/if (peek == '=' || next == '-' && peek == '>') {
						"$next${charIterator.next()}"
					} else {
						"$next"
					}
				}

				// Only take in one of these at a time
				'(', ')', '{', '}', '[', ']', '\'', '"', '#' -> {
					charIterator.next().toString()
				}

				// Is symbol
				else -> {
					when {

						peekNext.isDigit() || (peekNext == '.' && charIterator.peekNext(2).isDigit()) -> {

							isANumber = true

							// Select number, stop if rangeTo (..) is found
							readNumber(charIterator)
						}


						peekNext.isLetter() -> {
							charIterator.nextUntil { !it.isLetterOrDigit() }
						}

						else -> {
							charIterator.nextUntil { it.isLetterOrDigit() || it.isWhitespace() }
						}

					}
				}

			}

			if (isANumber) {

				tokens +=
					if ('.' in key) {
						Token.Primitive.Number(key.toDouble().minimize())
					} else {
						Token.Primitive.Number(key.toLong().minimize())
					}

				continue
			}

			tokens += when (key) {

				// Semicolons
				";" -> continue

				// Parenthesis
				"(" -> CrescentToken.Parenthesis.LEFT
				")" -> CrescentToken.Parenthesis.RIGHT

				// Bracket
				"{" -> CrescentToken.Brace.LEFT
				"}" -> CrescentToken.Brace.RIGHT

				// Array declaration
				"[" -> CrescentToken.Bracket.LEFT
				"]" -> CrescentToken.Bracket.RIGHT

				// Infix Operators
				"in" -> CrescentToken.Operator.CONTAINS
				".." -> CrescentToken.Operator.RANGE_TO
				"as" -> CrescentToken.Operator.AS

				// Variables
				"var" -> CrescentToken.Variable.VAR
				"val" -> CrescentToken.Variable.VAL
				"const" -> CrescentToken.Variable.CONST

				// Types
				"struct" -> CrescentToken.Type.STRUCT
				"impl" -> CrescentToken.Type.IMPL
				"trait" -> CrescentToken.Type.TRAIT
				"object" -> CrescentToken.Type.OBJECT
				"enum" -> CrescentToken.Type.ENUM
				"sealed" -> CrescentToken.Type.SEALED

				// Statements
				"else" -> CrescentToken.Statement.ELSE
				"import" -> CrescentToken.Statement.IMPORT
				"if" -> CrescentToken.Statement.IF
				"when" -> CrescentToken.Statement.WHEN
				"while" -> CrescentToken.Statement.WHILE
				"for" -> CrescentToken.Statement.FOR
				"fun" -> CrescentToken.Statement.FUN

				// Modifiers
				"async" -> CrescentToken.Modifier.ASYNC
				"override" -> CrescentToken.Modifier.OVERRIDE
				"operator" -> CrescentToken.Modifier.OPERATOR
				"inline" -> CrescentToken.Modifier.INLINE
				"static" -> CrescentToken.Modifier.STATIC

				// Visibility
				"public" -> CrescentToken.Visibility.PUBLIC
				"internal" -> CrescentToken.Visibility.INTERNAL
				"private" -> CrescentToken.Visibility.PRIVATE

				// Arithmetic
				"!" -> CrescentToken.Operator.NOT
				"+" -> CrescentToken.Operator.ADD
				"-" -> CrescentToken.Operator.SUB
				"*" -> CrescentToken.Operator.MUL
				"/" -> CrescentToken.Operator.DIV
				"%" -> CrescentToken.Operator.REM
				"^" -> CrescentToken.Operator.POW

				// Bit
				"shr" -> CrescentToken.Operator.BIT_SHIFT_RIGHT
				"shl" -> CrescentToken.Operator.BIT_SHIFT_LEFT
				"ushr" -> CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT
				"and" -> CrescentToken.Operator.BIT_AND
				"or" -> CrescentToken.Operator.BIT_OR
				"xor" -> CrescentToken.Operator.BIT_XOR

				// Assign
				"=" -> CrescentToken.Operator.ASSIGN
				"+=" -> CrescentToken.Operator.ADD_ASSIGN
				"-=" -> CrescentToken.Operator.SUB_ASSIGN
				"*=" -> CrescentToken.Operator.MUL_ASSIGN
				"/=" -> CrescentToken.Operator.DIV_ASSIGN
				"%=" -> CrescentToken.Operator.REM_ASSIGN
				"^=" -> CrescentToken.Operator.POW_ASSIGN

				// Compare
				"||" -> CrescentToken.Operator.OR_COMPARE
				"&&" -> CrescentToken.Operator.AND_COMPARE
				"<" -> CrescentToken.Operator.LESSER_COMPARE
				">" -> CrescentToken.Operator.GREATER_COMPARE
				"<=" -> CrescentToken.Operator.LESSER_EQUALS_COMPARE
				">=" -> CrescentToken.Operator.GREATER_EQUALS_COMPARE
				"==" -> CrescentToken.Operator.EQUALS_COMPARE
				"!=" -> CrescentToken.Operator.NOT_EQUALS_COMPARE

				// Compare references
				"===" -> CrescentToken.Operator.EQUALS_REFERENCE_COMPARE
				"!==" -> CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE

				// Type prefix
				":" -> CrescentToken.Operator.TYPE_PREFIX

				// String
				// TODO: Add support for ${} - No this will be done in the parser
				"\"" -> Token.Primitive.String(charIterator.nextUntilAndSkip('"'))
				"'" -> {
					val data = charIterator.nextUntilAndSkip('\'')
					checkEquals(1, data.length)
					Token.Primitive.Char(data[0])
				}

				// Comment
				"#" -> Token.Comment(charIterator.nextUntil('\n').trim())

				// Keywords
				"break" -> CrescentToken.Keyword.BREAK
				"continue" -> CrescentToken.Keyword.CONTINUE


				//"\n" -> CrescentToken.Operator.NEW_LINE
				"is" -> CrescentToken.Operator.INSTANCE_OF
				"->" -> CrescentToken.Operator.RETURN
				"?" -> CrescentToken.Operator.RESULT
				"," -> CrescentToken.Operator.COMMA
				"." -> CrescentToken.Operator.DOT
				"::" -> CrescentToken.Operator.IMPORT_SEPARATOR

				"true" -> Token.Primitive.Boolean(true)
				"false" -> Token.Primitive.Boolean(false)

				else -> Token.Name(key)
			}
		}

		return tokens
	}

	fun readNumber(charIterator: PeekingCharIterator): String {
		return charIterator.nextUntil {
			!it.isDigit() && it != '.'
			/*
			if (it == '.' && charIterator.peekNext(2) != '.') {
				false
			} else {
				!it.isDigit()
			}
			*/
		}
	}

}