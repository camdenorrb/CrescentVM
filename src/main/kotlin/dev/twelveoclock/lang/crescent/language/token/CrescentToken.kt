package dev.twelveoclock.lang.crescent.language.token

// TODO: Make a way to reconstruct the code through .toString
// TODO: Add ++ and --
// TODO: Add Operator keyword
// TODO: Error keyword
// TODO: Maybe remove majority of these and let the parser determine it, EX: Import, while
// TODO: Store line numbers, start/end char positions
object CrescentToken {

	enum class Variable : Token {
		VAL,
		VAR,
		CONST,
	}

	enum class Type : Token {
		STRUCT,
		IMPL,
		TRAIT,
		OBJECT,
		ENUM,
		SEALED,
	}

	enum class Statement : Token {
		IMPORT,
		WHILE,
		WHEN,
		FOR,
		IF,
		FUN,
		ELSE,
	}

	enum class Visibility : Token {
		PRIVATE,
		INTERNAL,
		PUBLIC,
	}

	enum class Modifier : Token {
		ASYNC,
		OVERRIDE,
		OPERATOR,
		INLINE,
		STATIC,
		INFIX,
	}


	enum class Keyword(val literal: String) : Token {
		SELF("self"),
		BREAK("break"),
		CONTINUE("continue"),
	}

	enum class Parenthesis : Token {

		LEFT,
		RIGHT,
		;

		override fun toString(): String {
			return if (this == LEFT) "(" else ")"
		}
	}

	enum class Brace : Token {

		LEFT,
		RIGHT,
		;

		override fun toString(): String {
			return if (this == LEFT) "{" else "}"
		}
	}

	enum class Bracket : Token {

		LEFT,
		RIGHT,
		;

		override fun toString(): String {
			return if (this == LEFT) "[" else "]"
		}
	}

	// TODO: Precedence
	enum class Operator(val literal: String) : Token.Operator {
		//NEW_LINE("\n"),
		NOT("!"),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		POW("^"),
		REM("%"),
		ASSIGN("="),
		ADD_ASSIGN("+="),
		SUB_ASSIGN("-="),
		MUL_ASSIGN("*="),
		DIV_ASSIGN("/="),
		REM_ASSIGN("%="),
		POW_ASSIGN("%="),
		OR_COMPARE("||"),
		AND_COMPARE("&&"),
		EQUALS_COMPARE("=="),
		LESSER_COMPARE("<"),
		GREATER_COMPARE(">"),
		LESSER_EQUALS_COMPARE("<="),
		GREATER_EQUALS_COMPARE(">="),
		BIT_SHIFT_RIGHT("shr"),
		BIT_SHIFT_LEFT("shl"),
		UNSIGNED_BIT_SHIFT_RIGHT("ushr"), // Left isn't needed
		BIT_OR("or"),
		BIT_XOR("xor"),
		BIT_AND("and"),
		EQUALS_REFERENCE_COMPARE("==="),
		NOT_EQUALS_COMPARE("!="),
		NOT_EQUALS_REFERENCE_COMPARE("!=="),
		CONTAINS("in"),
		NOT_CONTAINS("!in"),
		RANGE_TO(".."),
		TYPE_PREFIX(":"),
		RETURN("->"),
		RESULT("?"),
		COMMA(","),
		DOT("."),
		AS("as"),
		IMPORT_SEPARATOR("::"),
		INSTANCE_OF("is"),
		//NOT_INSTANCE_OF("!is"),
	}


}