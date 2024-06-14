package dev.twelveoclock.lang.crescent.manual

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.lexers.Lexer
import dev.twelveoclock.lang.crescent.parsers.Parser
//import dev.twelveoclock.lang.crescent.vm.CrescentToPTIR
import dev.twelveoclock.lang.crescent.vm.CrescentVM
//import tech.poder.ir.Machine
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.system.measureNanoTime

internal object Bench {

	const val DEFAULT_CYCLES = 10_000_000

	val filePath = Path.of("example.crescent")

	val lexerBenchmark = Benchmark("Lexer")

	val parserBenchmark = Benchmark("Parser")

	val vmBenchmark = Benchmark("VM")

	val originalSystemOut = System.out

	val originalSystemIn = System.`in`

	val nullSystemPrinter = PrintStream(PrintStream.nullOutputStream())

	private inline fun collectSystemOut(block: () -> Unit): String {

		val byteArrayOutputStream = ByteArrayOutputStream()
		val printStream = PrintStream(byteArrayOutputStream)

		System.setOut(printStream)
		block()
		System.setOut(originalSystemOut)

		return byteArrayOutputStream.toString()
	}

	private inline fun ignoreSystemOut(block: () -> Unit) {
		System.setOut(nullSystemPrinter)
		block()
		System.setOut(originalSystemOut)
	}

	private inline fun fakeUserInput(input: String, block: () -> Unit) {
		System.setIn(ByteArrayInputStream(input.toByteArray()))
		block()
		System.setIn(originalSystemIn)
	}


	@JvmStatic
	fun main(args: Array<String>) {

		benchCode("Hello Worlds", TestCode.helloWorlds)
		benchCode("If Statement", TestCode.ifStatement, listOf("true"))
		//benchCode("If Input Statement", TestCode.ifInputStatement)
		//benchCode("Calculator", TestCode.calculator)
		benchCode("Constants and Objects", TestCode.constantsAndObject)
		//benchCode("Impl", TestCode.impl)
		benchCode("Math", TestCode.math)
		//benchCode("Sealed", TestCode.sealed)
		//benchCode("Enum", TestCode.enum)
		//benchCode("Comments", TestCode.comments)
		//benchCode("Imports", TestCode.imports)


		/*
		benchVM("Hello Worlds", 	"""
			fun main {
                print("Hello World")
                print("Hello World")
                print("Hello World")
            }
		""")*/
		//benchVM("Triangles", TestCode.triangleRecursion)

	}

	fun benchCode(name: String, code: String, args: List<String> = emptyList()) {

		lexerBenchmark.apply {
			bench(name) {
				Lexer.invoke(code)
			}
		}

		val tokens = Lexer.invoke(code)

		parserBenchmark.apply {
			bench(name) {
				Parser.invoke(filePath, tokens)
			}
		}

		benchVM(name, code, args)

		println()
	}

	fun benchVM(name: String, code: String, args: List<String> = emptyList()) {

		val tokens = Lexer.invoke(code)
		val parsed = Parser.invoke(Path("example.moo"), tokens)
		//val methods = CrescentToPTIR.craft(parsed)

		val vm = CrescentVM(listOf(parsed), parsed)

		vmBenchmark.bench("Kat:$name") {
			ignoreSystemOut {
				vm.invoke(args)
			}
		}

		/*
		Machine.loadCode(*methods.toTypedArray())

		vmBenchmark.bench("Moo:$name") {
			collectSystemOut {
				Machine.execute("static.main")
			}
		}
		*/
	}


	class Benchmark(val name: String) {

		inline fun bench(
			subName: String,
			warmUpCycles: Int = DEFAULT_CYCLES,
			benchCycles: Int = DEFAULT_CYCLES,
			block: () -> Unit
		) {
			println(measureNS(subName, State.WARMUP, warmUpCycles, block))
			println(measureNS(subName, State.BENCH, benchCycles, block))
		}

		inline fun measureNS(subName: String, state: State, cycles: Int, block: () -> Unit): Result {

			var totalTimeNS = 0L

			repeat(cycles) {
				totalTimeNS += measureNanoTime(block)
			}

			return Result(name, subName, state, totalTimeNS, totalTimeNS / cycles)
		}


		data class Result(
			val name: String,
			val subName: String,
			val state: State,
			val totalNS: Long,
			val averageNS: Long
		) {

			override fun toString(): String {
				return (
					"""
                        |$name - $subName - $state Average: ${averageNS}ns/op
                    """.trimMargin()
				)
			}

		}

		enum class State {
			WARMUP,
			BENCH
		}

	}

}