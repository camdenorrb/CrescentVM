package dev.twelveoclock.lang.crescent

import dev.twelveoclock.lang.crescent.compiler.CrescentIRCompiler
import dev.twelveoclock.lang.crescent.lexers.Lexer
import dev.twelveoclock.lang.crescent.parsers.Parser
import dev.twelveoclock.lang.crescent.vm.CrescentIRVM
import dev.twelveoclock.lang.crescent.vm.CrescentVM
import kotlin.io.path.Path


object Main {

	// TODO: Replace all checkEquals in project with check and a custom message
	@JvmStatic
	fun main(args: Array<String>) {

		for (x in 0..9) {
			for (y in 0..9) {
				for (z in 0..9) {
					for (t in 0..9) {
						println("$x-$y-$z-$t")
					}
				}
			}
		}

		//testCodeVM2()
	}


	private fun testCodeVM() {

		val code =
			"""
                fun main {
                    
                    var i = 0
                    
                    while (i < 2) {
                        println("${'$'}meow")
                        i += 1
                    }
                }
            """.trimIndent()

		println("Here")
		val tokens = Lexer.invoke(code)
		println(tokens)
		val file = Parser.invoke(Path(""), tokens)
		println(file.mainFunction?.innerCode)
		CrescentVM(listOf(file), file).invoke()
	}

	private fun testCodeVM2() {

		val code =
			"""
                fun triangle(n: Any, k: Any){
                    if (n < 0){
                        return
                    }
                    
                    triangle(n-1, k+1);
                
                    var x;
                    var y;
                    while (x < k){
                        print(" ")
                        x = x + 1
                    }
                    while (y < n){
                        print("* ")
                        y = y+1
                    }
                    println()
                    
                }
                
                fun main(){
                    triangle(5, 0)
                }
            """.trimIndent()

		println("Here")
		val tokens = Lexer.invoke(code)
		println(tokens)
		val file = Parser.invoke(Path(""), tokens)
		println(file.mainFunction?.innerCode)
		CrescentVM(listOf(file), file).invoke()
	}

	private fun testCodeIR() {

		val code =
			"""
                fun main {
                    
                    var i = 0
                    
                    while (i < 2) {
                        println("Meow")
                        i += 1
                    }
                }
            """.trimIndent()

		val file = Parser.invoke(Path(""), Lexer.invoke(code))
		CrescentIRVM(CrescentIRCompiler.invoke(file).also { println(it) }).invoke()
	}
}


/*
@JvmStatic
fun main(args: Array<String>) {

    val file =
        if (args.isNotEmpty()) {
            Paths.get(args[0]).toAbsolutePath()
        }
        else {
            this::class.java.getResource("/crescent/examples/math.moon")?.toURI()?.toPath()?.toAbsolutePath()
        }

    check(file != null && file.exists()) {
        "File '$file' does not exist"
    }

    val files =
        if (file.isDirectory()) {
            Files.walk(file)
                .filter { !it.isDirectory() && it.extension == "moon" }
                .collect(Collectors.toList())
        }
        else {
            listOf(file)
        }

    val assemblies = mutableSetOf<CrescentAST.Node.File>()
    val vm = CrescentVM(VM.JVM_BYTECODE)

    files.forEach { path ->
        val tokens = vm.lex(path.readText())
        println(tokens)
        assemblies.addAll(vm.parse(path, tokens))
    }

    vm.invoke(assemblies)
}*/
