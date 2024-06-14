package dev.twelveoclock.lang.crescent.language.token


//region Types

interface Token

interface Operator : Token

interface Statement : Token

interface Primitive : Token

//endregion


// Used only by the token iterator
object Unknown : Token


@JvmInline
value class Name(val string: String) : Token

@JvmInline
value class Comment(val string: String) : Token


//region Primitive

// TODO: Take in expressions
@JvmInline
value class String(val kotlinString: kotlin.String) : Primitive

@JvmInline
value class Boolean(val kotlinBoolean: kotlin.Boolean) : Primitive

@JvmInline
value class Char(val kotlinChar: kotlin.Char) : Primitive

@JvmInline
value class Number(val number: kotlin.Number) : Primitive {

	override fun toString(): kotlin.String {
		return "Number(${number} ${number::class.simpleName})"
	}

}

//endregion


