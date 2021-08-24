package me.camdenorrb.crescentvm.vm

import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.instructions.simple.CodeBuilder

object CrescentToPTIR {

    fun invoke(astFile: CrescentAST.Node.File): List<Method> {
        val methods = mutableListOf<Method>()
        astFile.functions.forEach { (_, u) ->
            methods.add(Method.create(u.name, u.params.size.toUByte(), u.returnType != CrescentAST.Node.Type.Unit) {
                u.innerCode.nodes.forEach { node ->
                    nodeToCode(it, node)
                }
            })
        }
        return methods
    }

    private fun nodeToCode(builder: CodeBuilder, node: CrescentAST.Node) {
        when (node) {
            is CrescentAST.Node.Return -> {
                builder.return_()
            }
            is CrescentAST.Node.Statement.Block -> {
                node.nodes.forEach {
                    nodeToCode(builder, it)
                }
            }
            is CrescentAST.Node.Operator -> {
                when (node.operator) {
                    CrescentToken.Operator.EQUALS_COMPARE -> {
                        builder.compare()
                    }
                    else -> error("Unknown operator: ${node.operator}")
                }
            }
            is CrescentAST.Node.Primitive.String -> {
                builder.push(node.data)
            }
            is CrescentAST.Node.Primitive.Boolean -> {
                builder.push(node.data)
            }
            is CrescentAST.Node.Expression -> {
                val opOrdering = mutableListOf<CrescentAST.Node>()
                var i = 0
                var index = 0
                var tmpOp: CrescentAST.Node? = null
                while (index < node.nodes.size) {
                    val tmp = node.nodes[index++]
                    if (tmp is CrescentAST.Node.Operator) {
                        tmpOp = tmp
                    } else {
                        opOrdering.add(tmp)
                        i++
                    }
                    if (i >= 2 && tmpOp != null) {
                        opOrdering.add(tmpOp)
                        tmpOp = null
                        i = 0
                    }
                }

                if (tmpOp != null) {
                    opOrdering.add(tmpOp)
                }
                opOrdering.forEach {
                    nodeToCode(builder, it)
                }
            }
            is CrescentAST.Node.GetCall -> {
                when (node.identifier) {
                    "args" -> {
                        //todo pull from stack if not next, args may need to be placed in an array var for this language
                    }
                    else -> {
                        println(node.identifier)
                    }
                }
            }
            is CrescentAST.Node.FunctionCall -> {
                node.arguments.reversed().forEach { arg ->
                    nodeToCode(builder, arg)
                }
                when (node.identifier) {
                    "println" -> {
                        builder.sysCall(SpecialCalls.PRINTLN)
                    }
                    else -> {
                        builder.invoke("static." + node.identifier, node.arguments.size)
                    }
                }
            }
            is CrescentAST.Node.Statement.If -> {
                val afterLabel =
                    if (node.elseBlock == null) {
                        null
                    } else {
                        builder.newLabel()
                    }
                val elseLabel = builder.newLabel()
                nodeToCode(builder, node.predicate)
                builder.push(0)
                builder.ifEquals(elseLabel)
                nodeToCode(builder, node.block)
                if (afterLabel != null) {
                    builder.jmp(afterLabel)
                }
                builder.placeLabel(elseLabel)
                if (afterLabel != null) {
                    nodeToCode(builder, node.elseBlock!!)
                    builder.placeLabel(afterLabel)
                }
            }

            else -> error("Unknown Node: ${node::class.java}")
        }
    }
}