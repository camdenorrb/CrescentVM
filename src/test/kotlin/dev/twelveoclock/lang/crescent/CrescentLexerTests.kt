package dev.twelveoclock.lang.crescent

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Operator.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Statement.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Type.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Variable.*
import dev.twelveoclock.lang.crescent.lexers.Lexer
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class CrescentLexerTests {

    @Test
    fun helloWorld() {

        val tokens = Lexer.invoke(TestCode.helloWorlds)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hello World"), Parenthesis.CLOSE,
                Identifier("println"), *Array(2) { Parenthesis.OPEN }, Data.String("Hello World"), *Array(2) { Parenthesis.CLOSE },
                Identifier("println"), *Array(10) { Parenthesis.OPEN }, Data.String("Hello World"), *Array(10) { Parenthesis.CLOSE },
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun argsHelloWorld() {

        //val tokens = CrescentLexer.invoke(TestCode.helloWorlds)

    }

    @Test
    fun funThing() {

    }

    @Test
    fun ifStatement() {

        val tokens = Lexer.invoke(TestCode.ifStatement)

        assertContentEquals(
            listOf(

                FUN, Identifier("test1"), Parenthesis.OPEN, Identifier("args"), TYPE_PREFIX, SquareBracket.OPEN, Identifier("String"), SquareBracket.CLOSE, Parenthesis.CLOSE, Bracket.OPEN,
                IF, Parenthesis.OPEN, Identifier("args"), SquareBracket.OPEN, Data.Number(0.toByte()), SquareBracket.CLOSE, EQUALS_COMPARE, Data.String("true"), Parenthesis.CLOSE, Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                ELSE, Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hiss"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,

                FUN, Identifier("test2"), Parenthesis.OPEN, Identifier("args"), TYPE_PREFIX, SquareBracket.OPEN, Identifier("String"), SquareBracket.CLOSE, Parenthesis.CLOSE, RETURN, Identifier("String"), Bracket.OPEN,
                IF, Parenthesis.OPEN, Identifier("args"), SquareBracket.OPEN, Data.Number(0.toByte()), SquareBracket.CLOSE, EQUALS_COMPARE, Data.String("true"), Parenthesis.CLOSE, Bracket.OPEN,
                RETURN, Data.String("Meow"),
                Bracket.CLOSE,
                ELSE, Bracket.OPEN,
                RETURN, Data.String("Hiss"),
                Bracket.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Data.String("This shouldn't be printed"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                FUN, Identifier("main"), Parenthesis.OPEN, Identifier("args"), TYPE_PREFIX, SquareBracket.OPEN, Identifier("String"), SquareBracket.CLOSE, Parenthesis.CLOSE, Bracket.OPEN,
                Identifier("test1"), Parenthesis.OPEN, Identifier("args"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("test2"), Parenthesis.OPEN, Identifier("args"), Parenthesis.CLOSE, Parenthesis.CLOSE,
                Bracket.CLOSE
            ),
            tokens
        )
    }

    @Test
    fun ifInputStatement() {

        val tokens = Lexer.invoke(TestCode.ifInputStatement)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,
                VAL, Identifier("input"), ASSIGN, Identifier("readBoolean"), Parenthesis.OPEN, Data.String("Enter a boolean value [true/false]"), Parenthesis.CLOSE,
                IF, Parenthesis.OPEN, Identifier("input"), Parenthesis.CLOSE, Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                ELSE, Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hiss"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun stringInterpolation() {

        val tokens = Lexer.invoke(TestCode.stringInterpolation)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,

                VAL, Identifier("x"), COMMA, Identifier("y"), COMMA, Identifier("z"), ASSIGN, Data.Number(0.toByte()),

                Identifier("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hello \$x\$y\$z Hello"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hello \$x Hello \$y Hello \$z Hello"), Parenthesis.CLOSE,

                Identifier("println"), Parenthesis.OPEN, Data.String("\${x}\${y}\${z}"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hello \${x}\${y}\${z} Hello"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Data.String("Hello \${x}Hello\${y}Hello\${z} Hello"), Parenthesis.CLOSE,

                Data.Comment("Should printout a dollar sign"),
                Identifier("println"), Parenthesis.OPEN, Data.String("\${'$'}"), Parenthesis.CLOSE,

                Data.Comment("Should println dollar sign next to the letter x"),
                Identifier("println"), Parenthesis.OPEN, Data.String("\\\$x"), Parenthesis.CLOSE,

                Identifier("println"), Parenthesis.OPEN, Data.String("$ x"), Parenthesis.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun forLoop1() {

        val tokens = Lexer.invoke(TestCode.forLoop1)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,

                VAL, Identifier("x"), COMMA, Identifier("y"), COMMA, Identifier("z"), ASSIGN, Data.Number(0.toByte()),
                Identifier("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,

                FOR, Identifier("x"), CONTAINS, Data.Number(0.toByte()), RANGE_TO, Data.Number(9.toByte()), Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("\$x"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                FOR, Identifier("x"), COMMA, Identifier("y"), COMMA, Identifier("z"), CONTAINS, Data.Number(0.toByte()), RANGE_TO, Data.Number(9.toByte()), Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                FOR, Identifier("x"), COMMA, Identifier("y"), COMMA, Identifier("z"), CONTAINS, Data.Number(0.toByte()), RANGE_TO, Data.Number(9.toByte()), COMMA, Data.Number(0.toByte()), RANGE_TO, Data.Number(9.toByte()), COMMA, Data.Number(0.toByte()), RANGE_TO, Data.Number(9.toByte()), Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                Identifier("println"), Parenthesis.OPEN, Data.String("Hello World"), Parenthesis.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun whileLoop() {

        val tokens = Lexer.invoke(TestCode.whileLoop)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,

                VAR, Identifier("x"), ASSIGN, Data.Number(1.toByte()),

                WHILE, Parenthesis.OPEN, Identifier("x"), LESSER_EQUALS_COMPARE, Data.Number(10.toByte()), Parenthesis.CLOSE, Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Identifier("x"), Parenthesis.CLOSE,
                Identifier("x"), ADD_ASSIGN, Data.Number(1.toByte()),
                Bracket.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun calculator() {

        val tokens = Lexer.invoke(TestCode.calculator)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,
                VAL, Identifier("input1"), ASSIGN, Identifier("readDouble"), Parenthesis.OPEN, Data.String("Enter your first number"), Parenthesis.CLOSE,
                VAL, Identifier("input2"), ASSIGN, Identifier("readDouble"), Parenthesis.OPEN, Data.String("Enter your second number"), Parenthesis.CLOSE,
                VAL, Identifier("operation"), ASSIGN, Identifier("readLine"), Parenthesis.OPEN, Data.String("Enter an operation [+, -, *, /]"), Parenthesis.CLOSE,
                VAL, Identifier("result"), ASSIGN, WHEN, Parenthesis.OPEN, Identifier("operation"), Parenthesis.CLOSE, Bracket.OPEN,
                Data.Char('+'), RETURN, Identifier("input1"), ADD, Identifier("input2"),
                Data.Char('-'), RETURN, Identifier("input1"), SUB, Identifier("input2"),
                Data.Char('*'), RETURN, Identifier("input1"), MUL, Identifier("input2"),
                Data.Char('/'), RETURN, Identifier("input1"), DIV, Identifier("input2"),
                Bracket.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("result"), Parenthesis.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun constantsAndObject() {

        val tokens = Lexer.invoke(TestCode.constantsAndObject)

        assertContentEquals(
            listOf(

                CONST, Identifier("thing1"), ASSIGN, Data.String("Mew"),

                OBJECT, Identifier("Constants"), Bracket.OPEN,
                CONST, Identifier("thing2"), ASSIGN, Data.String("Meow"),
                FUN, Identifier("printThings"), Parenthesis.OPEN, Parenthesis.CLOSE, Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Identifier("thing1"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("thing2"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,

                FUN, Identifier("main"), Bracket.OPEN,
                Identifier("Constants"), DOT, Identifier("printThings"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("thing1"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("Constants"), DOT, Identifier("thing2"), Parenthesis.CLOSE,
                Bracket.CLOSE
            ),
            tokens
        )
    }

    @Test
    fun impl() {

        val tokens = Lexer.invoke(TestCode.impl)

        assertContentEquals(
            listOf(

                STRUCT, Identifier("Example"), Parenthesis.OPEN,
                VAL, Identifier("aNumber"), TYPE_PREFIX, Identifier("I32"), Data.Comment("New lines makes commas redundant"),
                VAL, Identifier("aValue1"), Identifier("aValue2"), ASSIGN, Data.String(""), Data.Comment("Multi declaration of same type, can all be set to one or multiple default values"),
                Parenthesis.CLOSE,

                IMPL, Identifier("Example"), Bracket.OPEN,
                Data.Comment("All implementation methods"),
                FUN, Identifier("printValues"), Bracket.OPEN,
                Identifier("println"), Parenthesis.OPEN, Identifier("aNumber"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("aValue1"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("aValue2"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,

                Data.Comment("Can't use self in static syntax"),
                IMPL, Modifier.STATIC, Identifier("Example"), Bracket.OPEN,
                FUN, Identifier("add"), Parenthesis.OPEN, Identifier("value1"), Identifier("value2"), TYPE_PREFIX, Identifier("I32"), Parenthesis.CLOSE, RETURN, Identifier("I32"), Bracket.OPEN,
                RETURN, Identifier("value1"), ADD, Identifier("value2"),
                Bracket.CLOSE,
                FUN, Identifier("sub"), Parenthesis.OPEN, Identifier("value1"), Identifier("value2"), TYPE_PREFIX, Identifier("I32"), Parenthesis.CLOSE, RETURN, Identifier("I32"), Bracket.OPEN,
                RETURN, Identifier("value1"), SUB, Identifier("value2"),
                Bracket.CLOSE,
                Bracket.CLOSE,

                FUN, Identifier("main"), Bracket.OPEN,
                VAL, Identifier("example"), ASSIGN, Identifier("Example"), Parenthesis.OPEN, Data.Number(1.toByte()), COMMA, Data.String("Meow"), COMMA, Data.String("Mew"), Parenthesis.CLOSE,
                Identifier("example"), DOT, Identifier("printValues"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("example"), DOT, Identifier("aNumber"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("example"), DOT, Identifier("aValue1"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("example"), DOT, Identifier("aValue2"), Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("Example"), DOT, Identifier("add"), Parenthesis.OPEN, Data.Number(1.toByte()), COMMA, Data.Number(2.toByte()), Parenthesis.CLOSE, Parenthesis.CLOSE,
                Identifier("println"), Parenthesis.OPEN, Identifier("Example"), DOT, Identifier("sub"), Parenthesis.OPEN, Data.Number(1.toByte()), COMMA, Data.Number(2.toByte()), Parenthesis.CLOSE, Parenthesis.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }


    @Test
    fun math() {

        val tokens = Lexer.invoke(TestCode.math)

        assertContentEquals(
            listOf(
                FUN, Identifier("main"), Bracket.OPEN,

                Identifier("println"), Parenthesis.OPEN,
                Parenthesis.OPEN, Data.Number(1.0.toInt().toByte()), ADD, Data.Number(1.toByte()), Parenthesis.CLOSE,
                ADD, Data.Number(1.0.toInt().toByte()), DIV, Data.Number(10.0.toInt().toByte()), ADD, Data.Number(1000.0.toInt().toShort()), MUL, Data.Number(10.0.toInt().toByte()),
                DIV, Data.Number(11.0.toInt().toByte()), POW, Data.Number(10.0.toInt().toByte()),
                Parenthesis.CLOSE,

                Identifier("println"), Parenthesis.OPEN,
                Data.Number(4.toByte()), MUL, Parenthesis.OPEN, Data.Number(3.toByte()), Parenthesis.CLOSE, ADD, Data.Number(1.toByte()),
                Parenthesis.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun sealed() {

        val tokens = Lexer.invoke(TestCode.sealed)

        assertContentEquals(
            listOf(
                SEALED, Identifier("Example"), Bracket.OPEN,
                STRUCT, Identifier("Thing1"), Parenthesis.OPEN, VAL, Identifier("name"), TYPE_PREFIX, Identifier("String"), Parenthesis.CLOSE,
                STRUCT, Identifier("Thing2"), Parenthesis.OPEN, VAL, Identifier("id"), TYPE_PREFIX, Identifier("i32"), Parenthesis.CLOSE,
                OBJECT, Identifier("Thing3"),
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun enum() {

        val tokens = Lexer.invoke(TestCode.enum)

        assertContentEquals(
            listOf(

                ENUM, Identifier("Color"), Parenthesis.OPEN, Identifier("name"), TYPE_PREFIX, Identifier("String"), Parenthesis.CLOSE, Bracket.OPEN,
                Identifier("RED"), Parenthesis.OPEN, Data.String("Red"), Parenthesis.CLOSE,
                Identifier("GREEN"), Parenthesis.OPEN, Data.String("Green"), Parenthesis.CLOSE,
                Identifier("BLUE"), Parenthesis.OPEN, Data.String("Blue"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                FUN, Identifier("main"), Bracket.OPEN,

                Data.Comment(".random() will be built into the Enum type implementation"),
                VAL, Identifier("color"), ASSIGN, Identifier("Color"), DOT, Identifier("random"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Data.Comment("Shows off cool Enum shorthand for when statements"),

                WHEN, Parenthesis.OPEN, Identifier("color"), Parenthesis.CLOSE, Bracket.OPEN,
                DOT, Identifier("RED"), RETURN, Bracket.OPEN, Identifier("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE, Bracket.CLOSE,
                DOT, Identifier("GREEN"), RETURN, Bracket.OPEN, Bracket.CLOSE,
                ELSE, RETURN, Bracket.OPEN, Bracket.CLOSE,
                Bracket.CLOSE,

                WHEN, Parenthesis.OPEN, Identifier("name"), ASSIGN, Identifier("color"), DOT, Identifier("name"), Parenthesis.CLOSE, Bracket.OPEN,
                Data.String("Red"), RETURN, Identifier("println"), Parenthesis.OPEN, Identifier("name"), Parenthesis.CLOSE,
                Data.String("Green"), RETURN, Bracket.OPEN, Bracket.CLOSE,
                ELSE, RETURN, Bracket.OPEN, Bracket.CLOSE,
                Bracket.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )

    }

    @Test
    fun comments() {

        val tokens = Lexer.invoke(TestCode.comments)

        assertContentEquals(
            listOf(
                Data.Comment("Project level comment"),
                FUN, Identifier("main"), Bracket.OPEN,
                Identifier("println"), Data.Comment("(\"Meow\")"),
                Data.Comment("Meow"),
                Data.Comment("Meow"),
                Data.String("#meow"),
                Data.Number(1.toByte()), ADD, Data.Comment("Meow"),
                Data.Number(1.toByte()), SUB, Data.Comment("Meow"),
                Data.Number(1.toByte()), DIV, Data.Comment("Meow"),
                Data.Number(1.toByte()), MUL, Data.Comment("Meow"),
                Data.Number(1.toByte()), ASSIGN, Data.Comment("Meow"),
                Data.Comment("}")
            ),
            tokens
        )
    }

    @Test
    fun imports() {

        val tokens = Lexer.invoke(TestCode.imports)

        assertContentEquals(
            listOf(
                Data.Comment("Current idea, Package -> Type"),
                IMPORT, Identifier("crescent"), DOT, Identifier("examples"), IMPORT_SEPARATOR, Identifier("Thing"),
                IMPORT, Identifier("crescent"), DOT, Identifier("examples"), IMPORT_SEPARATOR, Identifier("Thing2"), AS, Identifier("Thing3"),
                IMPORT, Identifier("crescent"), DOT, Identifier("examples"), IMPORT_SEPARATOR, MUL,

                Data.Comment("import crescent.examples as examples"),//IMPORT, Key("crescent"), DOT, Key("examples"), AS, Key("examples"),

                Data.Comment("Short hand method (If in same package)"),
                IMPORT, IMPORT_SEPARATOR, Identifier("Thing"),
                IMPORT, IMPORT_SEPARATOR, Identifier("Thing2"), AS, Identifier("Thing3")
            ),
            tokens
        )
    }


    @Test
    fun nateTriangle() {

        val tokens = Lexer.invoke(TestCode.nateTriangle)

        println(tokens)

        assertContentEquals(
            listOf(
                FUN, Identifier("triangle"), Parenthesis.OPEN, Identifier("n"), TYPE_PREFIX, Identifier("Any"), COMMA, Identifier("k"), TYPE_PREFIX, Identifier("Any"), Parenthesis.CLOSE, Bracket.OPEN,

                    IF, Parenthesis.OPEN, Identifier("n"), GREATER_EQUALS_COMPARE, Data.Number(0.toByte()), Parenthesis.CLOSE, Bracket.OPEN,

                        Identifier("triangle"), Parenthesis.OPEN, Identifier("n"), SUB, Data.Number((1).toByte()), COMMA, Identifier("k"), ADD, Data.Number(1.toByte()), Parenthesis.CLOSE,

                        VAR, Identifier("x"), TYPE_PREFIX, Identifier("I32"), ASSIGN, Data.Number(0.toByte()),
                        VAR, Identifier("y"), TYPE_PREFIX, Identifier("I32"), ASSIGN, Data.Number(0.toByte()),

                        WHILE, Parenthesis.OPEN, Identifier("x"), LESSER_COMPARE, Identifier("k"), Parenthesis.CLOSE, Bracket.OPEN,
                            Identifier("print"), Parenthesis.OPEN, Data.String(" "), Parenthesis.CLOSE,
                            Identifier("x"), ASSIGN, Identifier("x"), ADD, Data.Number(1.toByte()),
                        Bracket.CLOSE,

                        WHILE, Parenthesis.OPEN, Identifier("y"), LESSER_COMPARE, Identifier("n"), Parenthesis.CLOSE, Bracket.OPEN,
                            Identifier("print"), Parenthesis.OPEN, Data.String("* "), Parenthesis.CLOSE,
                            Identifier("y"), ASSIGN, Identifier("y"), ADD, Data.Number(1.toByte()),
                        Bracket.CLOSE,

                        Identifier("println"), Parenthesis.OPEN, Parenthesis.CLOSE,

                    Bracket.CLOSE,

                Bracket.CLOSE,

                FUN, Identifier("main"), Parenthesis.OPEN, Parenthesis.CLOSE, Bracket.OPEN,
                    Identifier("triangle"), Parenthesis.OPEN, Data.Number(5.toByte()), COMMA, Data.Number(0.toByte()), Parenthesis.CLOSE,
                Bracket.CLOSE
            ),
            tokens
        )
    }

}