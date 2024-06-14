package dev.twelveoclock.lang.crescent.language.syntax

import dev.twelveoclock.lang.crescent.language.ast.Block
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.language.token.Token

// This can be used to convert Tokens -> AST and AST -> Token
object CrescentSyntax {

	val functionBlockStatements = listOf(
		CrescentToken.Statement.
	)

	fun initTokens() {
		register(Token.Operator, CrescentToken.Statement)
		"+" to CrescentToken.Operator.ADD
	}
	fun initAST() {

		push<CrescentAST.Enum> {
			CrescentAST.Enum(expect(Token.Name).string)
		}

		pull<CrescentAST.Enum> {
			+Token.Name(it.name)
		}

		CrescentAST.Enum::class {

			expect(CrescentToken.Type.ENUM)
			expect(Token.Name).bind(CrescentAST.Enum::name)

			expectExpression(wrappedBy = CrescentToken.Parenthesis)
			expectBlock(wrappedBy = CrescentToken.Brace) {
				Block
			}

		}

	}
}