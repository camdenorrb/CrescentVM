package dev.twelveoclock.lang.crescent

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.*
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive.Number.I16
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive.Number.I8
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Statement.When
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Operator.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Visibility
import dev.twelveoclock.lang.crescent.lexers.Lexer
import dev.twelveoclock.lang.crescent.parsers.Parser
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

internal class CrescentParserTests {

    @Test
    fun helloWorld() {

        val tokens = Lexer.invoke(TestCode.helloWorlds)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                IdentifierCall("println", listOf(String("Hello World"))),
                IdentifierCall("println", listOf(String("Hello World"))),
                IdentifierCall("println", listOf(String("Hello World"))),
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun argsHelloWorld() {

    }

    @Test
    fun funThing() {

    }

    @Test
    fun ifStatement() {

        val tokens = Lexer.invoke(TestCode.ifStatement)
        val parsed = Parser.invoke(Path.of("example.crescent"), tokens)

        val mainFunction = assertNotNull(
            parsed.mainFunction,
            "No main function found"
        )


        assertContentEquals(
            listOf(
                Statement.If(
                    predicate = Expression(listOf(
                        GetCall("args", listOf(I8(0))), String("true"), EQUALS_COMPARE
                    )),
                    block = Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Meow")))
                    )),
                    elseBlock = Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Hiss")))
                    )),
                ),
            ),
            parsed.functions["test1"]!!.innerCode.nodes,
        )

        assertContentEquals(
            listOf(
                Statement.If(
                    predicate = Expression(listOf(
                        GetCall("args", listOf(I8(0))), String("true"), EQUALS_COMPARE
                    )),
                    block = Statement.Block(listOf(
                        Return(String("Meow"))
                    )),
                    elseBlock = Statement.Block(listOf(
                        Return(String("Hiss"))
                    )),
                ),
                IdentifierCall("println", listOf(String("This shouldn't be printed")))
            ),
            parsed.functions["test2"]!!.innerCode.nodes,
        )


        assertContentEquals(
            listOf(Parameter.Basic("args", Type.Array(Type.Basic("String")))),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                IdentifierCall("test1", listOf(Identifier("args"))),
                IdentifierCall("println", listOf(IdentifierCall("test2", listOf(Identifier("args")))))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun ifInputStatement() {

        val tokens = Lexer.invoke(TestCode.ifInputStatement)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                Variable.Basic(
                    "input", Type.Implicit, IdentifierCall(
                        "readBoolean",
                        listOf(String("Enter a boolean value [true/false]"))
                    ), true, Visibility.PUBLIC
                ),
                Statement.If(
                    Identifier("input"),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Meow")))
                    )),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Hiss")))
                    )),
                ),
            ),
            mainFunction.innerCode.nodes,
        )
    }


    @Test
    fun stringInterpolation() {

        val tokens = Lexer.invoke(TestCode.stringInterpolation)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                Variable.Basic("x", Type.Implicit, I8(0), true, Visibility.PUBLIC),
                Variable.Basic("y", Type.Implicit, I8(0), true, Visibility.PUBLIC),
                Variable.Basic("z", Type.Implicit, I8(0), true, Visibility.PUBLIC),

                IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, String(" Hello "), ADD, Identifier("y"), ADD, String(" Hello "), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),

                IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, String("Hello"), ADD, Identifier("y"), ADD, String("Hello"), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),

                IdentifierCall("println", listOf(Expression(listOf(String(""), Char('$'), ADD)))),
                IdentifierCall("println", listOf(String("\$x"))),

                IdentifierCall("println", listOf(String("$ x"))),
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun forLoop1() {

        val tokens = Lexer.invoke(TestCode.forLoop1)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(

                Variable.Basic("x", Type.Implicit, I8(0), true, Visibility.PUBLIC),
                Variable.Basic("y", Type.Implicit, I8(0), true, Visibility.PUBLIC),
                Variable.Basic("z", Type.Implicit, I8(0), true, Visibility.PUBLIC),

                // TODO: Simplify expression into the above list since it's the only expression
                IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),

                Statement.For(
                    listOf(Identifier("x")),
                    listOf(Statement.Range(I8(0), I8(9))),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD)))),
                    ))
                ),

                Statement.For(
                    listOf(Identifier("x"), Identifier("y"), Identifier("z")),
                    listOf(Statement.Range(I8(0), I8(9))),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                    ))
                ),
                Statement.For(
                    listOf(Identifier("x"), Identifier("y"), Identifier("z")),
                    listOf(Statement.Range(I8(0), I8(9)), Statement.Range(I8(0), I8(9)), Statement.Range(I8(0), I8(9))),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                    ))
                ),
                IdentifierCall("println", listOf(String("Hello World")))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun whileLoop() {

        val tokens = Lexer.invoke(TestCode.whileLoop)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                Variable.Basic("x", Type.Implicit, I8(1), false, Visibility.PUBLIC),
                Statement.While(
                    Expression(listOf(
                        Identifier("x"), I8(10), LESSER_EQUALS_COMPARE
                    )),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Identifier("x"))),
                        Expression(listOf(Identifier("x"), I8(1), ADD_ASSIGN)),
                    ))
                )

            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun calculator() {

        val tokens = Lexer.invoke(TestCode.calculator)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                Variable.Basic(
                    "input1",
                    Type.Implicit,
                    IdentifierCall("readDouble", listOf(String("Enter your first number"))),
                    true,
                    Visibility.PUBLIC
                ),
                Variable.Basic(
                    "input2",
                    Type.Implicit,
                    IdentifierCall("readDouble", listOf(String("Enter your second number"))),
                    true,
                    Visibility.PUBLIC
                ),
                Variable.Basic(
                    "operation",
                    Type.Implicit,
                    IdentifierCall("readLine", listOf(String("Enter an operation [+, -, *, /]"))),
                    true,
                    Visibility.PUBLIC
                ),
                Variable.Basic(
                    "result", Type.Implicit,
                    When(
                        Identifier("operation"),
                        listOf(
                            When.Clause(
                                Char('+'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), ADD
                                ))))
                            ),
                            When.Clause(
                                Char('-'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), SUB
                                ))))
                            ),
                            When.Clause(
                                Char('*'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), MUL
                                ))))
                            ),
                            When.Clause(
                                Char('/'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), DIV
                                ))))
                            )
                        )
                    ),
                    true,
                    Visibility.PUBLIC,
                ),
                IdentifierCall("println", listOf(Identifier("result")))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun constantsAndObject() {

        val tokens = Lexer.invoke(TestCode.constantsAndObject)
        val crescentFile = Parser.invoke(Path.of("example.crescent"), tokens)

        assertContentEquals(
            listOf(Variable.Constant("thing1", Type.Implicit, String("Mew"), Visibility.PUBLIC)),
            crescentFile.constants.values,
            "Variables not as expected"
        )

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        val constantsObject = assertNotNull(
            crescentFile.objects["Constants"],
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Variable.Constant("thing2", Type.Implicit, String("Meow"), Visibility.PUBLIC),
            ),
            constantsObject.constants.values,
        )

        assertContentEquals(
            listOf(
                IdentifierCall("println", listOf(Identifier("thing1"))),
                IdentifierCall("println", listOf(Identifier("thing2")))
            ),
            constantsObject.functions["printThings"]!!.innerCode.nodes
        )
        assertContentEquals(
            listOf(
                DotChain(listOf(Identifier("Constants"), IdentifierCall("printThings"))),
                IdentifierCall("println", listOf(Identifier("thing1"))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("Constants"), Identifier("thing2"))))),
            ),
            mainFunction.innerCode.nodes
        )
    }

    @Test
    fun impl() {

        val tokens = Lexer.invoke(TestCode.impl)
        val parsed = Parser.invoke(Path.of("example.crescent"), tokens)
        val mainFunction = assertNotNull(parsed.mainFunction, "No main function found")

        assertContentEquals(
            listOf(
                Struct("Example", listOf(
                    Variable.Basic("aNumber", Type.Basic("I32"), Expression(emptyList()), true, Visibility.PUBLIC),
                    Variable.Basic("aValue1", Type.Implicit, String(""), true, Visibility.PUBLIC),
                    Variable.Basic("aValue2", Type.Implicit, String(""), true, Visibility.PUBLIC),
                ))
            ),
            parsed.structs.values,
        )

        assertContentEquals(
            listOf(
                Variable.Basic(
                    "example",
                    Type.Implicit,
                    IdentifierCall("Example", listOf(I8(1), String("Meow"), String("Mew"))),
                    true,
                    Visibility.PUBLIC
                ),
                DotChain(listOf(Identifier("example"), IdentifierCall("printValues"))),
                IdentifierCall("println"),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("example"), Identifier("aNumber"))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("example"), Identifier("aValue1"))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("example"), Identifier("aValue2"))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("Example"), IdentifierCall("add", listOf(I8(1), I8(2))))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("Example"), IdentifierCall("sub", listOf(I8(1), I8(2))))))),
            ),
            mainFunction.innerCode.nodes,
        )

        assertContentEquals(
            listOf(
                Impl(
                    type = Type.Basic("Example"),
                    modifiers = emptyList(),
                    functions = listOf(
                        Function(
                            name = "printValues",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = emptyList(),
                            returnType = Type.unit,
                            innerCode = Statement.Block(listOf(
                                IdentifierCall("println", listOf(Identifier("aNumber"))),
                                IdentifierCall("println", listOf(Identifier("aValue1"))),
                                IdentifierCall("println", listOf(Identifier("aValue2"))),
                            ))
                        ),
                    ),
                    extends = emptyList(),
                ),
            ),
            parsed.impls.values,
        )

        assertContentEquals(
            listOf(
                Impl(
                    type = Type.Basic("Example"),
                    modifiers = listOf(CrescentToken.Modifier.STATIC),
                    functions = listOf(
                        Function(
                            name = "add",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("I32")), Parameter.Basic("value2", Type.Basic("I32"))),
                            returnType = Type.Basic("I32"),
                            innerCode = Statement.Block(listOf(
                                Return(Expression(listOf(Identifier("value1"), Identifier("value2"), ADD)))
                            ))
                        ),
                        Function(
                            name = "sub",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("I32")), Parameter.Basic("value2", Type.Basic("I32"))),
                            returnType = Type.Basic("I32"),
                            innerCode = Statement.Block(listOf(
                                Return(Expression(listOf(Identifier("value1"), Identifier("value2"), SUB)))
                            ))
                        ),
                    ),
                    extends = emptyList(),
                )
            ),
            parsed.staticImpls.values,
        )
    }



    @Test
    fun math() {

        val tokens = Lexer.invoke(TestCode.math)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                IdentifierCall("println", listOf(Expression(listOf(
                    I8(1), I8(1), ADD,
                    I8(1), I8(10), DIV, ADD, I16(1000), I8(10), MUL, I8(11), I8(10), POW, DIV, ADD
                )))),
                IdentifierCall("println", listOf(Expression(listOf(
                    I8(4), I8(3), MUL, I8(1), ADD
                )))),
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun sealed() {

        val tokens = Lexer.invoke(TestCode.sealed)
        val crescentFile = Parser.invoke(Path.of("example.crescent"), tokens)

        val sealedExample = assertNotNull(
            crescentFile.sealeds["Example"],
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Struct("Thing1", listOf(Variable.Basic(
                    "name",
                    Type.Basic("String"),
                    Expression(emptyList()),
                    true,
                    Visibility.PUBLIC
                ))),
                Struct("Thing2", listOf(Variable.Basic(
                    "id",
                    Type.Basic("i32"),
                    Expression(emptyList()),
                    true,
                    Visibility.PUBLIC
                ))),
            ),
            sealedExample.structs,
        )

        assertContentEquals(
            listOf(
                Object("Thing3", emptyMap(), emptyMap(), emptyMap())
            ),
            sealedExample.objects,
        )
    }


    @Test
    fun enum() {

        val tokens = Lexer.invoke(TestCode.enum)
        val crescentFile = Parser.invoke(Path.of("example.crescent"), tokens)

        val mainFunction = assertNotNull(
            Parser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                Enum(
                    name = "Color",
                    parameters = listOf(Parameter.Basic("name", Type.Basic("String"))),
                    structs = listOf(
                        EnumEntry("RED", listOf(String("Red"))),
                        EnumEntry("GREEN", listOf(String("Green"))),
                        EnumEntry("BLUE", listOf(String("Blue"))),
                    ),
                ),
            ), crescentFile.enums.values,
        )

        assertContentEquals(
            listOf(
                Variable.Basic(
                    "color",
                    Type.Implicit,
                    DotChain(listOf(Identifier("Color"), IdentifierCall("random", emptyList()))),
                    true,
                    Visibility.PUBLIC
                ),
                When(
                    Identifier("color"),
                    listOf(
                        When.Clause(
                            When.EnumShortHand("RED"),
                            Statement.Block(listOf(
                                IdentifierCall("println", listOf(String("Meow")))
                            ))
                        ),
                        When.Clause(
                            When.EnumShortHand("GREEN"),
                            Statement.Block(emptyList())
                        ),
                        When.Clause(
                            null,
                            Statement.Block(emptyList())
                        ),
                    )),
                // TODO: Encode name into a Variable instead of a bunch of tokens
                When(Expression(listOf(Identifier("name"), DotChain(listOf(Identifier("color"), Identifier("name"))), ASSIGN)), listOf(
                    When.Clause(
                        String("Red"),
                        Statement.Block(listOf(
                            IdentifierCall("println", listOf(Identifier("name"))),
                        ))
                    ),
                    When.Clause(
                        String("Green"),
                        Statement.Block(emptyList())
                    ),
                    When.Clause(
                        null,
                        Statement.Block(emptyList())
                    ),
                )),
            ),
            mainFunction.innerCode.nodes,
        )

    }

    @Test
    fun comments() {

        val tokens = Lexer.invoke(TestCode.comments)
        val crescentFile = Parser.invoke(Path.of("example.crescent"), tokens)

        val mainFunction = assertNotNull(
            crescentFile.mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                Identifier("println"),
                String("#meow"),
                Expression(listOf(I8(1), I8(1), ADD, I8(1), I8(1), DIV, I8(1), MUL, SUB, ASSIGN)),
            ),
            mainFunction.innerCode.nodes,
        )

    }

    @Test
    fun imports() {

        val tokens = Lexer.invoke(TestCode.imports)
        val crescentFile = Parser.invoke(Path.of("example.crescent"), tokens)

        assertContentEquals(
            listOf(
                Import("crescent.examples", "Thing"),
                Import("crescent.examples", "Thing2", "Thing3"),
                Import("crescent.examples", "*"),

                //Import("crescent.examples", "", "examples"),
                Import("", "Thing"),
                Import("", "Thing2", "Thing3"),
            ),
            crescentFile.imports
        )
    }

    @Test
    fun nateTriangle() {

        val tokens = Lexer.invoke(TestCode.nateTriangle)
        val crescentFile = Parser.invoke(Path.of("example.crescent"), tokens)

        assertContentEquals(
            listOf(
                Statement.If(
                    predicate = Expression(listOf(Identifier("n"), I8(0), GREATER_EQUALS_COMPARE)),
                    block = Statement.Block(listOf(

                        IdentifierCall("triangle", listOf(
                            Expression(listOf(Identifier("n"), I8(1), SUB)),
                            Expression(listOf(Identifier("k"), I8(1), ADD)),
                        )),

                        Variable.Basic("x", Type.Basic("I32"), I8(0), false, Visibility.PUBLIC),
                        Variable.Basic("y", Type.Basic("I32"), I8(0), false, Visibility.PUBLIC),

                        Statement.While(
                            predicate = Expression(listOf(Identifier("x"), Identifier("k"), LESSER_COMPARE)),
                            block = Statement.Block(listOf(
                                IdentifierCall("print", listOf(String(" "))),
                                Expression(listOf(Identifier("x"), Identifier("x"), I8(1), ADD, ASSIGN))
                            ))
                        ),

                        Statement.While(
                            predicate = Expression(listOf(Identifier("y"), Identifier("n"), LESSER_COMPARE)),
                            block = Statement.Block(listOf(
                                IdentifierCall("print", listOf(String("* "))),
                                Expression(listOf(Identifier("y"), Identifier("y"), I8(1), ADD, ASSIGN))
                            ))
                        ),

                        IdentifierCall("println", emptyList())
                    )),
                    elseBlock = null
                )
            ),
            crescentFile.functions["triangle"]!!.innerCode.nodes
        )

    }

}