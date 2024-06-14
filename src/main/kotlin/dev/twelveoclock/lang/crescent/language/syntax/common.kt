package dev.twelveoclock.lang.crescent.language.syntax


interface Syntax {

	val
}
abstract class Syntax {

	protected fun expect()
	protected fun expectBlock() : Block
	protected fun expectExpression() : Expression


	protected data class Block

	protected data class Expression


}