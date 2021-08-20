package me.camdenorrb.crescentvm.vm

import java.nio.file.Path


// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt
class CrescentAST {

    sealed class Node {

        data class Number(
            val number: kotlin.Number,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$number"
            }

        }

        data class Boolean(
            val data: kotlin.Boolean,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$data"
            }

        }

        data class String(
            val data: kotlin.String,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "\"$data\""
            }

        }

        data class Char(
            val data: kotlin.Char,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "'$data'"
            }

        }


        data class Argument(
            val value: Expression,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$value"
            }

        }

        // TODO: Get rid of Expression and just take in nodes instead
        data class Expression(
            val nodes: List<Node>,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$nodes"
            }

        }

        // TODO: Make a class called MathExpression and just store a list of tokens, or just retrieve the list of tokens in the parser and do shunting yard
        data class Operation(
            val first: Node,
            val operator: CrescentToken.Operator,
            val second: Node,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$first ${operator.literal} $second"
            }

        }

        data class InstanceOf(
            val expression: Expression,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "is $expression"
            }

        }

        data class Return(
            val expression: Expression,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "-> $expression"
            }

        }

        data class Import(
            val path: kotlin.String,
            val typeName: kotlin.String,
            val typeAlias: kotlin.String? = null,
        ) : Node()

        data class Struct(
            val name: kotlin.String,
            val variables: List<Variable>,
        ) : Node()

        data class Sealed(
            val name: kotlin.String,
            val structs: List<Struct>
        )

        data class Trait(
            val name: kotlin.String,
            val functionTraits: List<FunctionTrait>,
        ) : Node()

        data class Object(
            val name: kotlin.String,
            val variables: List<Variable>,
            val functions: List<Function>,
            val constants: List<Constant>,
        ) : Node()

        data class Impl(
            val type: Type,
            val functions: List<Function>,
            val extends: List<Type>,
        ) : Node()

        data class Enum(
            val name: kotlin.String,
            val parameters: List<Parameter>,
            val structs: List<EnumEntry>,
        ) : Node()

        data class EnumEntry(
            val name: kotlin.String,
            val arguments: List<Argument>,
        ) : Node()

        data class FunctionTrait(
            val name: kotlin.String,
            val params: List<Parameter>,
            val returnType: Type,
        ) : Node()

        data class FunctionCall(
            val name: kotlin.String,
            val arguments: List<Argument>,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$name(${arguments.joinToString { it.value.nodes.joinToString() }})"
            }

        }

        data class VariableCall(
            val name: kotlin.String,
        ) : Node() {

            override fun toString(): kotlin.String {
                return name
            }

        }

        data class ArrayCall(
            val name: kotlin.String,
            val index: Int,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$name[$index]"
            }

        }

        data class Constant(
            val name: kotlin.String,
            val visibility: CrescentToken.Visibility,
            val type: Type,
            val value: Node,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "const $name: ${type::class.simpleName} = $value"
            }

        }

        data class Variable(
            val name: kotlin.String,
            val isFinal: kotlin.Boolean,
            val visibility: CrescentToken.Visibility,
            val type: Type,
            val value: Node,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "${if (isFinal) "val" else "var"} $name: ${type::class.simpleName} = $value"
            }

        }

        data class Function(
            val name: kotlin.String,
            val modifiers: List<CrescentToken.Modifier>,
            val visibility: CrescentToken.Visibility,
            val params: List<Parameter>,
            val returnType: Type,
            val innerCode: Statement.Block,
        ) : Node()

        // TODO: Make a better toString
        data class File(
            val path: Path,
            val imports: List<Import>,
            val structs: List<Struct>,
            val sealeds: List<Sealed>,
            val impls: List<Impl>,
            val traits: List<Trait>,
            val objects: List<Object>,
            val enums: List<Enum>,
            val variables: List<Variable>,
            val constants: List<Constant>,
            val functions: List<Function>,
            val mainFunction: Function?,
        ) : Node()


        sealed class Parameter : Node() {

            abstract val name: kotlin.String


            data class Basic(
                override val name: kotlin.String,
                val type: Type,
            ) : Parameter()

            data class WithDefault(
                override val name: kotlin.String,
                val defaultValue: Expression,
            ) : Parameter()

        }

        // TODO: Add toStrings
        sealed class Type : Node() {

            // Should only be used for variables
            object Implicit : Type()

            // No return type
            object Unit : Type()


            // Should only be used for function return types
            data class Result(val type: Type) : Type()

            data class Basic(val name: kotlin.String) : Type()

            data class Array(val type: Type) : Type()


            data class Generic(
                val type: Basic,
                val parameters: List<Type>,
            ) : Type()

        }


        sealed class Statement : Node() {

            data class When(
                val argument: Argument,
                val predicateToBlock: List<Clause>,
            ) : Statement() {

                override fun toString(): kotlin.String {
                    return "when (${argument.value.nodes.joinToString()}) ${predicateToBlock.joinToString(prefix = "{ ", postfix = " }")}"
                }

                data class Clause(val ifExpression: Expression?, val thenBlock: Block) : Statement() {

                    override fun toString(): kotlin.String {
                        return "$ifExpression $thenBlock"
                    }

                }

            }

            data class Else(
                val block: Block,
            ) : Statement()

            data class If(
                val predicate: Expression,
                val block: Block,
            ) : Statement()

            data class While(
                val predicate: Expression,
                val block: Expression,
            ) : Statement()

            data class For(
                val variable: Variable,
                val predicate: Expression,
                val block: Expression,
            ) : Statement()

            data class Block(
                val expressions: List<Expression>,
            ) : Statement() {

                override fun toString(): kotlin.String {
                    return "{ ${expressions.joinToString { it.nodes.joinToString() }} }"
                }

            }

        }

    }

}