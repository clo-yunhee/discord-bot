package nukescript.parser

import nukescript.token.Token
import nukescript.token.Token.*
import nukescript.token.removeToken
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.properties.Delegates.notNull

// TODO:
class Pointer

class Parser(private val input: Queue<Int>) {

    companion object {
        val NO_NAME = 0

        val ADD_SYMBOLS = setOf(MINUS, OR, PLUS)
        val BLOCK_SYMBOLS = setOf(BEGIN, CONST, PROCEDURE, TYPE, VAR)
        val CONSTANT_SYMBOLS = setOf(NAME, NUMERAL)
        val EXPRESSION_SYMBOLS = setOf(LEFT_PARENTHESIS, MINUS, NAME, NOT, NUMERAL, PLUS)
        val FACTOR_SYMBOLS = setOf(LEFT_PARENTHESIS, NAME, NOT, NUMERAL)
        val LONG_SYMBOLS = setOf(NAME, NUMERAL)
        val MULTIPLY_SYMBOLS = setOf(AND, ASTERISK, DIV, MOD)
        val PARAMETER_SYMBOLS = setOf(NAME, VAR)
        val RELATION_SYMBOLS = setOf(EQUAL, GREATER, LESS, NOT_EQUAL, NOT_GREATER, NOT_LESS)
        val SELECTOR_SYMBOLS = setOf(LEFT_BRACKET, PERIOD)
        val SIGN_SYMBOLS = setOf(MINUS, PLUS)
        val STATEMENT_SYMBOLS = setOf(BEGIN, IF, NAME, WHILE)
        val TERM_SYMBOLS = FACTOR_SYMBOLS
        val SIMPLE_EXPR_SYMBOLS = SIGN_SYMBOLS + TERM_SYMBOLS
    }

    private val _errors: StringWriter = StringWriter()
    private val errors: PrintWriter = PrintWriter(_errors)

    private var symbol: Token by notNull()
    private var argument: Int by notNull()

    init {

    }

    fun execute(): String {
        nextToken()
        Program(setOf(END_TEXT))

        _errors.flush()
        return _errors.toString()
    }

    private fun nextToken() {
        symbol = input.removeToken()
        if (symbol in LONG_SYMBOLS) {
            argument = input.remove()
        }
    }

    /* internal methods */

    private fun syntaxCheck(stop: Set<Token>) {
        if (symbol !in stop) syntaxError(stop)
    }

    private fun syntaxError(stop: Set<Token>) {
        errors.println("Invalid syntax on symbol $symbol")
        while (symbol !in stop) nextToken()
    }

    internal fun expect(expected: Token, stop: Set<Token>) {
        if (symbol == expected) nextToken()
        else syntaxError(stop)
        syntaxCheck(stop)
    }

    internal fun expectName(stop: Set<Token>): Int {
        val name: Int
        if (NAME == symbol) {
            name = argument
            nextToken()
        } else {
            name = NO_NAME
            syntaxError(stop)
        }
        syntaxCheck(stop)
        return name
    }


    /* TypeName = Name */
    private fun TypeName(stop: Set<Token>) {
        expect(NAME, stop)
    }

    /* Constant = Numeral | ConstantName */
    private fun Constant(stop: Set<Token>) {
        /*val value: Int
        val type: Pointer*/
        if (NUMERAL == symbol) {
            /*value = argument
            type = Pointer.TYPE_INTEGER*/
            expect(NUMERAL, stop)
        } else if (NAME == symbol) {
            /*val object = find(argument)
            if (object.kind == Pointer.CONSTANT) {
                value = object.constValue
                type = object.constType
            }
            else {
                kindError(object)
                value = 0
                type = Pointer.TYPE_UNIVERSAL
            }*/
            expect(NAME, stop)
        } else {
            syntaxError(stop)
            //value = 0
        }
        //return value
    }

    /* ConstantDefinition = ConstantName "=" Constant ";" */
    private fun ConstantDefinition(stop: Set<Token>) {
        expect(NAME, setOf(EQUAL, SEMICOLON) + CONSTANT_SYMBOLS + stop)
        expect(EQUAL, CONSTANT_SYMBOLS + setOf(SEMICOLON) + stop)
        Constant(setOf(SEMICOLON) + stop)
        expect(SEMICOLON, stop)
    }

    /* ConstantDefinitionPart = ConstantDefinition { ConstantDefinition } */
    private fun ConstantDefinitionPart(stop: Set<Token>) {
        val stop2 = setOf(NAME) + stop
        expect(CONST, stop2)
        ConstantDefinition(stop2)
        while (NAME == symbol)
            ConstantDefinition(stop2)
    }

    /* NewArrayType = "array" "[" IndexRange "]" "of" TypeName */
    /* IndexRange = Constant ".." Constant */
    private fun NewArrayType(stop: Set<Token>) {
        expect(ARRAY, setOf(LEFT_BRACKET, RIGHT_BRACKET, OF, NAME) + CONSTANT_SYMBOLS + stop)
        expect(LEFT_BRACKET, setOf(RIGHT_BRACKET, OF, NAME) + CONSTANT_SYMBOLS + stop)
        Constant(setOf(DOUBLE_DOT, RIGHT_BRACKET, OF, NAME) + CONSTANT_SYMBOLS + stop)
        expect(DOUBLE_DOT, setOf(RIGHT_BRACKET, OF, NAME) + CONSTANT_SYMBOLS + stop)
        Constant(setOf(RIGHT_BRACKET, OF, NAME) + stop)
        expect(RIGHT_BRACKET, setOf(OF, NAME) + stop)
        expect(OF, setOf(NAME) + stop)
        TypeName(stop)
    }

    /* RecordSection = FieldName SectionTail */
    /* SectionTail = "," RecordSection | ":" TypeName */
    private fun RecordSection(stop: Set<Token>) {
        expect(NAME, setOf(COMMA, COLON) + stop)
        if (COMMA == symbol) {
            expect(COMMA, setOf(NAME) + stop)
            RecordSection(stop)
        } else {
            expect(COLON, setOf(NAME) + stop)
            TypeName(stop)
        }
    }

    /* FieldList = RecordSection { ";" RecordSection } */
    private fun FieldList(stop: Set<Token>) {
        val stop2 = setOf(SEMICOLON) + stop
        RecordSection(stop2)
        while (SEMICOLON == symbol) {
            expect(SEMICOLON, setOf(NAME) + stop)
            RecordSection(stop2)
        }
    }

    /* NewRecordType = "record" FieldList "end" */
    private fun NewRecordType(stop: Set<Token>) {
        expect(RECORD, setOf(NAME, END) + stop)
        FieldList(setOf(END) + stop)
        expect(END, stop)
    }

    /* TypeDefinition = TypeName "=" NewType ";" */
    /* NewType = NewArrayType | NewRecordType */
    private fun TypeDefinition(stop: Set<Token>) {
        val stop2 = setOf(SEMICOLON) + stop
        expect(NAME, setOf(EQUAL, ARRAY, RECORD) + stop)
        if (ARRAY == symbol) {
            NewArrayType(stop2)
        } else if (RECORD == symbol) {
            NewRecordType(stop2)
        } else {
            syntaxError(stop2)
        }
        expect(SEMICOLON, stop)
    }

    /* TypeDefinitionPart = "type" TypeDefinition { TypeDefinition } */
    private fun TypeDefinitionPart(stop: Set<Token>) {
        val stop2 = setOf(NAME) + stop
        expect(TYPE, stop2)
        TypeDefinition(stop2)
        while (NAME == symbol) {
            TypeDefinition(stop2)
        }
    }

    /* VariableGroup = VariableName GroupTail */
    /* GroupTail = "," VariableGroup | ":" TypeName } */
    private fun VariableGroup(stop: Set<Token>) {
        expect(NAME, setOf(COMMA, COLON) + stop)
        if (COMMA == symbol) {
            expect(COMMA, setOf(NAME) + stop)
            VariableGroup(stop)
        } else {
            expect(COLON, setOf(NAME) + stop)
            TypeName(stop)
        }
    }

    /* VariableDefinition = VariableGroup ";" */
    private fun VariableDefinition(stop: Set<Token>) {
        VariableGroup(setOf(SEMICOLON) + stop)
        expect(SEMICOLON, stop)
    }

    /* VariableDefinitionPart = "var" VariableDefinition { VariableDefinition } */
    private fun VariableDefinitionPart(stop: Set<Token>) {
        val stop2 = setOf(NAME) + stop
        expect(VAR, stop2)
        VariableDefinition(stop2)
        while (NAME == symbol) {
            VariableDefinition(stop2)
        }
    }

    /* ParameterDefinition = [ "var" ] VariableGroup */
    private fun ParameterDefinition(stop: Set<Token>) {
        syntaxCheck(setOf(VAR, NAME) + stop)
        if (VAR == symbol) {
            expect(VAR, setOf(NAME) + stop)
            VariableGroup(stop)
        } else {
            VariableGroup(stop)
        }
    }

    /* FormalParameterList = ParameterDefinition { ";" ParameterDefinition } */
    private fun FormalParameterList(stop: Set<Token>) {
        val stop2 = setOf(SEMICOLON) + stop
        ParameterDefinition(stop2)
        while (SEMICOLON == symbol) {
            expect(SEMICOLON, PARAMETER_SYMBOLS + stop)
            ParameterDefinition(stop2)
        }
    }

    /* ProcedureDefinition = "procedure" ProcedureName ProcedureBlock ";" */
    /* ProcedureBlock = [ "(" FormatParameterList ")" ] ";" BlockBody */
    private fun ProcedureDefinition(stop: Set<Token>) {
        expect(PROCEDURE, setOf(NAME, LEFT_PARENTHESIS, SEMICOLON) + BLOCK_SYMBOLS + stop)
        expect(NAME, setOf(LEFT_PARENTHESIS, SEMICOLON) + BLOCK_SYMBOLS + stop)
        if (LEFT_PARENTHESIS == symbol) {
            expect(LEFT_PARENTHESIS, PARAMETER_SYMBOLS + setOf(RIGHT_PARENTHESIS, SEMICOLON) + BLOCK_SYMBOLS + stop)
            FormalParameterList(setOf(RIGHT_PARENTHESIS, SEMICOLON) + BLOCK_SYMBOLS + stop)
            expect(RIGHT_PARENTHESIS, setOf(SEMICOLON) + BLOCK_SYMBOLS + stop)
        }
        expect(SEMICOLON, setOf(SEMICOLON) + BLOCK_SYMBOLS + stop)
        BlockBody(setOf(SEMICOLON) + stop)
        expect(SEMICOLON, stop)
    }

    /* IndexedSelector = "[" Expression "]" */
    private fun IndexedSelector(stop: Set<Token>) {
        expect(LEFT_BRACKET, EXPRESSION_SYMBOLS + setOf(RIGHT_BRACKET) + stop)
        Expression(setOf(RIGHT_BRACKET) + stop)
        expect(RIGHT_BRACKET, stop)
    }

    /* FieldSelector = "." FieldName */
    private fun FieldSelector(stop: Set<Token>) {
        expect(PERIOD, setOf(NAME) + stop)
        if (NAME == symbol) {
            expect(NAME, stop)
        } else {
            syntaxError(stop)
        }
    }

    /* VariableAccess = VariableName { Selector } */
    /* Selector = IndexedSelector | FieldSelector */
    private fun VariableAccess(stop: Set<Token>) {
        if (NAME == symbol) {
            val stop2 = SELECTOR_SYMBOLS + stop
            expect(NAME, stop2)
            while (symbol in SELECTOR_SYMBOLS) {
                if (LEFT_BRACKET == symbol) {
                    IndexedSelector(stop2)
                } else {
                    FieldSelector(stop2)
                }
            }
        } else {
            syntaxError(stop)
        }
    }

    /* Factor = Constant | VariableAccess | "(" Expression ")" | "not" Factor */
    private fun Factor(stop: Set<Token>) {
        if (NUMERAL == symbol) {
            Constant(stop)
        } else if (NAME == symbol) {
            // TODO:
            expect(NAME, stop)
        } else if (LEFT_PARENTHESIS == symbol) {
            expect(LEFT_PARENTHESIS, EXPRESSION_SYMBOLS + setOf(RIGHT_PARENTHESIS) + stop)
            Expression(setOf(RIGHT_PARENTHESIS) + stop)
            expect(RIGHT_PARENTHESIS, stop)
        } else if (NOT == symbol) {
            expect(NOT, FACTOR_SYMBOLS + stop)
            Factor(stop)
        } else {
            syntaxError(stop)
        }
    }

    /* Term = Factor { MultiplyingOperator Factor } */
    /* MultiplyingOperator = "*" | "div" | "mod" | "and" */
    private fun Term(stop: Set<Token>) {
        val stop2 = MULTIPLY_SYMBOLS + stop
        Factor(stop2)
        while (symbol in MULTIPLY_SYMBOLS) {
            expect(symbol, FACTOR_SYMBOLS + stop2)
            Factor(stop2)
        }
    }

    /* SimpleExpression = [ SignOperator ] Term { AddingOperator Term } */
    /* SignOperator = "+" | "-" */
    /* AddingOperator = "+" | "-" | "or" */
    private fun SimpleExpression(stop: Set<Token>) {
        val stop2 = ADD_SYMBOLS + stop
        syntaxCheck(SIGN_SYMBOLS + TERM_SYMBOLS + stop2)
        if (symbol in SIGN_SYMBOLS) {
            expect(symbol, TERM_SYMBOLS + stop2)
            Term(stop2)
        } else {
            Term(stop2)
        }
        while (symbol in ADD_SYMBOLS) {
            expect(symbol, TERM_SYMBOLS + stop2)
            Term(stop2)
        }
    }

    /* Expression = SimpleExpresion [ RelationalOperator SimpleExpression ] */
    /* RelationOperator = "<" | "=" | ">" | "<=" | "<>" | ">=" */
    private fun Expression(stop: Set<Token>) {
        SimpleExpression(RELATION_SYMBOLS + stop)
        if (symbol in RELATION_SYMBOLS) {
            expect(symbol, SIMPLE_EXPR_SYMBOLS + stop)
            SimpleExpression(stop)
        }
    }

    /* IOStatement = "Read" "(" VariableAccess ")" | "Write" "(" Expression ")" */
    private fun IOStatement(stop: Set<Token>) {
        val stop2 = setOf(RIGHT_PARENTHESIS) + stop
        val name = argument
        expect(NAME, EXPRESSION_SYMBOLS + stop2)
        expect(LEFT_PARENTHESIS, EXPRESSION_SYMBOLS + stop2)
        if (/*READ*/4 == name) {
            VariableAccess(stop2)
        } else {
            Expression(stop2)
        }
        expect(RIGHT_PARENTHESIS, stop)
    }

    /* ActualParameterList = [ ActualParameterList "," ] ActualParameter */
    /* ActualParameter = Expression | VariableAccess */
    private fun ActualParameterList(stop: Set<Token>) {
        // TODO:
    }

    /* ProcedureStatement = IOStatement | ProcedureName [ "(" ActualParameterList ")" ] */
    private fun ProcedureStatement(stop: Set<Token>) {
    }

    /* AssignmentStatement = VariableAccess ":=" Expression */
    private fun AssignmentStatement(stop: Set<Token>) {
        VariableAccess(setOf(BECOMES) + EXPRESSION_SYMBOLS + stop)
        expect(BECOMES, EXPRESSION_SYMBOLS + stop)
        Expression(stop)
    }

    /* IfStatement = "if" Expression "then" Statement [ "else" Statement ] */
    private fun IfStatement(stop: Set<Token>) {
        expect(IF, EXPRESSION_SYMBOLS + setOf(THEN, ELSE) + STATEMENT_SYMBOLS + stop)
        Expression(setOf(THEN, ELSE) + STATEMENT_SYMBOLS + stop)
        expect(THEN, STATEMENT_SYMBOLS + setOf(ELSE) + stop)
        Statement(setOf(ELSE) + stop)
        if (ELSE == symbol) {
            expect(ELSE, STATEMENT_SYMBOLS + stop)
            Statement(stop)
        }
    }

    /* WhileStatement = "while" Expression "do" Statement */
    private fun WhileStatement(stop: Set<Token>) {
        expect(WHILE, EXPRESSION_SYMBOLS + setOf(DO) + STATEMENT_SYMBOLS + stop)
        Expression(setOf(DO) + STATEMENT_SYMBOLS + stop)
        expect(DO, STATEMENT_SYMBOLS + stop)
        Statement(stop)
    }

    /* Statement = AssignmentStatement | ProcedureStatement |
                   IfStatement | WhileStatement |
                   CompoundStatement | Empty */
    private fun Statement(stop: Set<Token>) {
        if (NAME == symbol) {
            // TODO:
            expect(NAME, stop)
        } else if (IF == symbol) {
            IfStatement(stop)
        } else if (WHILE == symbol) {
            WhileStatement(stop)
        } else if (BEGIN == symbol) {
            CompoundStatement(stop)
        } else {
            syntaxCheck(stop)
        }
    }

    /* CompoundStatement = "begin" Statement { ";" Statement } "end" */
    private fun CompoundStatement(stop: Set<Token>) {
        expect(BEGIN, STATEMENT_SYMBOLS + setOf(SEMICOLON, END) + stop)
        Statement(setOf(SEMICOLON, END) + stop)
        while (SEMICOLON == symbol) {
            expect(SEMICOLON, STATEMENT_SYMBOLS)
        }
    }

    /* BlockBody = [ ConstantDefinitionPart ] [ TypeDefinitionPart ]
                   [ VariableDefinitionPart ] { ProcedureDefinition }
                   CompoundStatement */
    private fun BlockBody(stop: Set<Token>) {
        syntaxCheck(BLOCK_SYMBOLS + stop)
        if (CONST == symbol) {
            ConstantDefinitionPart(setOf(TYPE, VAR, PROCEDURE, BEGIN) + stop)
        }
        if (TYPE == symbol) {
            TypeDefinitionPart(setOf(VAR, PROCEDURE, BEGIN) + stop)
        }
        if (VAR == symbol) {
            VariableDefinitionPart(setOf(PROCEDURE, BEGIN) + stop)
        }
        while (PROCEDURE == symbol) ProcedureDefinition(setOf(PROCEDURE, BEGIN) + stop)
        CompoundStatement(stop)
    }

    /* Program = "program" ProgramName ";" BlockBody "." */
    private fun Program(stop: Set<Token>) {
        expect(PROGRAM, setOf(NAME, SEMICOLON, PERIOD) + BLOCK_SYMBOLS + stop)
        expect(NAME, setOf(SEMICOLON, PERIOD) + BLOCK_SYMBOLS + stop)
        expect(SEMICOLON, setOf(PERIOD) + BLOCK_SYMBOLS + stop)
        BlockBody(setOf(PERIOD) + stop)
        expect(PERIOD, stop)
    }


}






