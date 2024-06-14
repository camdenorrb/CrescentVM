package dev.twelveoclock.lang.crescent.iterator

import dev.twelveoclock.lang.crescent.language.token.CrescentToken

// TODO: Make a peeking iterator interface
class PeekingTokenIterator(val input: List<CrescentToken>) : Iterator<CrescentToken> {

	@PublishedApi
	internal var index = 0


	override fun hasNext(): Boolean {
		return index < input.size
	}

	override fun next(): CrescentToken {
		return input[index++]
	}


	fun back(): CrescentToken {
		return input[--index]
	}


	fun peekNext(amount: Int = 1): CrescentToken {
		return input.getOrElse(index + (amount - 1)) { CrescentToken.Empty }
	}

	fun peekBack(amount: Int = 1): CrescentToken {
		return input.getOrElse(index - amount) { CrescentToken.Empty }
	}


	inline fun nextUntil(predicate: (CrescentToken) -> Boolean): List<CrescentToken> {

		val tokens = mutableListOf<CrescentToken>()

		while (index < input.size && !predicate(input[index])) {
			tokens += input[index]
			index++
		}

		return tokens
	}

	inline fun peekBackUntil(predicate: (CrescentToken) -> Boolean): List<CrescentToken> {

		var currentIndex = index - 1
		val result = mutableListOf<CrescentToken>()

		while (currentIndex > 0) {

			val token = input[currentIndex--]

			if (predicate(token)) {
				break
			}

			result += token
		}

		return result
	}

}