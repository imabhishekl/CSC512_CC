package parser;

/* Import the packages */
import scanner.CSC512_Constants;
import scanner.Scanner;
import scanner.Token;
import scanner.TokenType;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

/**
 * Created by abhishek on 9/26/16.
 * Parser class implementation to parse the file against a grammar
 * It scanner functionality to scan the file for token and pass it to
 * the parser which uses the grammar scanned in recursive decent fashion
 *
 */
public class Parser
{
    private Logger LOGGER = null; // Loggin of data
    private int variableCount = 0;
    private int functionCount = 0;
    private int statementCount = 0;
    BasicGrammar bg = null;
    private String file_name = null;
    private Scanner scan = null;
    private Token EOF_TOKEN = new Token(TokenType.EOF,CSC512_Constants.EMPTY);
    private Token look_ahead_token = null;
    private boolean consumed = true;

    /* Constructor to load the file which will be parsed */
    public Parser(String file_name) throws FileNotFoundException
    {
        this.file_name = file_name;
        scan = new Scanner(this.file_name);
        bg = new BasicGrammar();
        LOGGER = Logger.getLogger(Parser.class.getCanonicalName());
    }

    public int getVariableCount() {
        return variableCount;
    } /* Get the total variable counted */

    public int getFunctionCount() {
        return functionCount;
    } /* Get the total function counted */

    public int getStatementCount() {
        return statementCount;
    } /* Get the total statement counted */

    /**
     *Helper Method to detect if its an identifier or not
     */
    private boolean isID(Token token)
    {
        if (token.getToken_type() == TokenType.IDENTIFIER)
            return true;
        else
            return false;
    }

    /* Start the grammar parsing from here */
    public boolean isParsable()
    {
        /* Skip the meta statement */
        look_ahead_token = getNextTokenWrapper();
        while(look_ahead_token.getToken_type() == TokenType.META_CHAR)
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
        }
        return program();
    }

    /* Get the next token from the scanner.
    *  It will read the next token only if current token is consumed
    *  else it returns the last read token
     */
    private Token getNextTokenWrapper()
    {
        if (!consumed)/* Check if consumed or not */
        {
            System.out.println("Token read:" + look_ahead_token.getToken_value());
            return look_ahead_token;
        }
        Token temp_token;
        if(scan.hasMoreToken())
        {
            temp_token = scan.getNextToken();
            if (temp_token == null)
            {
                LOGGER.severe("Fatal Error in scanning the code.");
                System.exit(CSC512_Constants.SUCCESS);
            }
            else if(temp_token.getToken_type() == TokenType.ERROR)
            {
                LOGGER.warning("Error in scanning the file for token:" + temp_token.getToken_value());
                System.exit(CSC512_Constants.SUCCESS);
            }
        }
        else
            temp_token = EOF_TOKEN;
        consumed = false;
        System.out.println("Token read:" + temp_token.getToken_value());
        return temp_token;
    }

    /***************************************Grammar implementation for each production **********************************/

    /**
     * <program> -->  empty
     *               |<type name> ID <programZ>
     */
    private boolean program()
    {
        System.out.println("program");
        look_ahead_token = getNextTokenWrapper();
        /* Check for empty token */
        if(type_name())
        {
            look_ahead_token = getNextTokenWrapper();
            if(look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                consumed = true;
                return programZ();
            }
            return false;
        }
        else
            return true;
    }

    /**
     *<programZ> --> <data decls1> <programZZ>
     *              |left_parenthesis <func list1>
     */
    private boolean programZ()
    {
        System.out.println("programZ");
        look_ahead_token = getNextTokenWrapper();
        if(look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            return func_list1();
        }

        else if(data_decls1())
        {
            return programZZ();
        }
        else
            return false;
    }

    /**
     *<programZZ> --> empty
     *               |<type name> ID left_parenthesis <func list1>
     */
    private boolean programZZ()
    {
        System.out.println("programZZ");
        look_ahead_token = getNextTokenWrapper();

        if (type_name())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
                {
                    consumed = true;
                    return func_list1();
                }
            }
            return false;
        }
        else
            return true;
    }

    /**
     *<func list> --> empty
     *               |<type name> ID left_parenthesis <func> <func list>
     */
    private boolean func_list()
    {
        System.out.println("func_list");
        look_ahead_token = getNextTokenWrapper();
        if (type_name())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
                {
                    consumed = true;
                    if (func() && func_list())
                        return true;
                }
            }
            return false;
        }
        else
            return true;
    }

    /**
     *<func list1> --> <func> <func list>
     */
    private boolean func_list1()
    {
        System.out.println("func_list1");
        if (func() && func_list())
            return true;
        else
            return false;
    }

    /**
     *<func_list2> --> left_parenthesis <func> <func list>
     */
    private boolean func_list2()
    {
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            if (func() && func_list())
                return true;
        }
        return false;
    }

    /**
     *<func> --> <func decl> <funcZ>
     */
    private boolean func()
    {
        System.out.println("func");
        if (func_decl() && funcZ())
            return true;
        else
            return false;
    }

    /**
     *<funcZ> --> semicolon | left_brace <data decls> <statements> right_brace
     */
    private boolean funcZ()
    {
        System.out.println("funcZ");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
        {
            consumed = true;
            return true;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.LBRC))
        {
            functionCount++;
            consumed = true;
            if (data_decls() && statements())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RBRC))
                {
                    consumed = true;
                    return true;
                }
            }
        }
        return false;
    }

    //<func decl> --> <parameter list> right_parenthesis
    private boolean func_decl()
    {
        System.out.println("func_decl");
        if (parameter_list())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
            {
                consumed = true;
                return true;
            }
        }
        return false;
    }

    //<type name> --> int | void | binary | decimal
    private boolean type_name()
    {
        System.out.println("type_name");
        look_ahead_token = getNextTokenWrapper();
        if(bg.isTypeName(look_ahead_token.getToken_value()))
        {
            consumed = true;
            return true;
        }
        else
            return false;
    }

    //<type name1> --> int | binary | decimal
    private boolean type_name1()
    {
        System.out.println("type_name1");
        look_ahead_token = getNextTokenWrapper();
        if (!look_ahead_token.getToken_value().equals(TypeName.VOID) && bg.isTypeName(look_ahead_token.getToken_value()))
        {
            consumed = true;
            return true;
        }
        else
            return false;
    }

    //<parameter list> --> empty | void | <non-empty list>
    private boolean parameter_list()
    {
        System.out.println("parameter_list");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(TypeName.VOID))
        {
            consumed = true;
            return true;
        }
        else if (non_empty_list())
            return true;
        return true;
    }

    /**
     *<non-empty list> --> <type name1> ID <non-empty list'>
     */
    private boolean non_empty_list()
    {
        System.out.println("non_empty_list");
        if (type_name1())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                consumed = true;
                if (non_empty_list_dash())
                    return true;
            }
        }
        return false;
    }

    /**
     *<non-empty list'> --> empty | comma <type name> ID <non-empty list'>
     */
    private boolean non_empty_list_dash()
    {
        System.out.println("non_empty_list_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.COMMA))
        {
            consumed = true;
            if (type_name())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
                {
                    consumed = true;
                    if (non_empty_list_dash())
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * <data decls1> --> empty
     *                  |<id list1> semicolon <data decls>
     */
    private boolean data_decls1()
    {
        System.out.println("data_decls1");
        if(id_list1())
        {
            variableCount++;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                consumed = true;
                if (data_decls2())
                    return true;
            }
            return false;
        }
        return true;
    }

    /**
     * <data decls2> --> <type name> ID <data or func check>
     *                  | empty
     */
    private boolean data_decls2()
    {
        if (type_name())
        {
            look_ahead_token = getNextTokenWrapper();
            if (isID(look_ahead_token))
            {
                consumed = true;
                if (data_or_func_check())
                    return true;
            }
            return false;
        }
        else
            return true;
    }

    /**
     * <data or func check> --> <data decls1>
     *                          |<func_list2>
     */
    private boolean data_or_func_check()
    {
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            if (func_list2())
                return true;
            return false;
        }
        else if (data_decls1())
            return true;
        return false;
    }

    /**
     * <data decls> --> empty | <type name> <id list> semicolon <data decls>
     */
    private boolean data_decls()
    {
        System.out.println("data_decls");
        if (type_name())
        {
            if (id_list())
            {

                variableCount++;
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                {
                    consumed = true;
                    if (data_decls())
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     *<id list1> --> <id'><id list'>
     */
    private boolean id_list1()
    {
        System.out.println("id_list1");
        if (id_dash() && id_list_dash())
            return true;
        return false;
    }

    /**
     *<id list> --> <id> <id list'>
     */
    private boolean id_list()
    {
        System.out.println("id_list");
        if (id() && id_list_dash())
            return true;
        return false;
    }

    /**
     *<id list'> --> empty | comma <id> <id list'>
     */
    private boolean id_list_dash()
    {
        System.out.println("id_list_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.COMMA))
        {
            variableCount++;
            consumed = true;
            if (id())
                if (id_list_dash())
                    return true;
            return false;
        }
        else
            return true;
    }

    /**
     *<id> --> ID <id'>
     */
    private boolean id()
    {
        System.out.println("id");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
        {
            consumed = true;
            if (id_dash())
                return true;
        }
        return false;
    }

    /**
     * <id'> --> empty | left_bracket <expression> right_bracket
     */
    private boolean id_dash()
    {
        System.out.println("id_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LB))
        {
            consumed = true;
            if (expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RB))
                {
                    consumed = true;
                    return true;
                }
                return false;
            }
            else
                return false;
        }
        else
            return true;
    }

    /**
     *<block statements> --> left_brace <statements> right_brace
     */
    private boolean block_statement()
    {
        System.out.println("block_statement");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LBRC))
        {
            consumed = true;
            if (statements())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RBRC))
                {
                    consumed = true;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *<statements> --> empty | <statement> <statements>
     */
    private boolean statements()
    {
        System.out.println("statements");
        if (statement())
        {
            if (statements())
                return true;
            return false;
        }
        else
            return true;
    }

    /**
     *<statement> --> ID <statementZ>
     *               |if <if statement>
     *               |while <while statement>
     *               |return <return statementZ>
     *               |break semicolon
     *               |continue semicolon
     *               |read left_parenthesis  ID right_parenthesis semicolon
     *               |write left_parenthesis <expression>right_parenthesis semicolon
     *               |print left_parenthesis  STRING right_parenthesis semicolon
     */
    private boolean statement()
    {
        System.out.println("statement");
        statementCount++;
        look_ahead_token = getNextTokenWrapper();
        if (isID(look_ahead_token))
        {
            consumed = true;
            if (statementZ())
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.IF))
        {
            consumed = true;
            if (if_statement())
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.WHILE))
        {
            consumed = true;
            if (while_statement())
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.RETURN))
        {
            consumed = true;
            if (return_statementZ())
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.BREAK))
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                consumed = true;
                return true;
            }
            return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.CONTINUE))
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                consumed = true;
                return true;
            }
            return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.READ))
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
            {
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (isID(look_ahead_token))
                {
                    consumed = true;
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                    {
                        consumed = true;
                        look_ahead_token = getNextTokenWrapper();
                        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                        {
                            consumed = true;
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.WRITE))
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
            {
                consumed = true;
                if (expression())
                {
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                    {
                        consumed = true;
                        look_ahead_token = getNextTokenWrapper();
                        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                        {
                            consumed = true;
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.PRINT))
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
            {
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_type() == TokenType.STRING)
                {
                    consumed = true;
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                    {
                        consumed = true;
                        look_ahead_token = getNextTokenWrapper();
                        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                        {
                            consumed = true;
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        statementCount--;
        return false;
    }

    /**
     * <statementZ> --> <assignment>
     *                 |<func call>
     */
    private boolean statementZ()
    {
        System.out.println("statementZ");
        if (assignment())
            return true;
        else if (func_call())
            return true;
        return false;
    }

    /**
     *<assignment> --> <id'> equal_sign <expression> semicolon
     */
    private boolean assignment()
    {
        System.out.println("assignment");
        if (id_dash())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.EQUALS))
            {
                consumed = true;
                if (expression())
                {
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                    {
                        consumed = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *<func call> --> left_parenthesis <expr list> right_parenthesis semicolon
     */
    private boolean func_call()
    {
        System.out.println("func_call");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            if (expr_list())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    consumed = true;
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                    {
                        consumed = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *<expr list> --> empty | <non-empty expr list>
     *     first(non-empty expr list)=ID,NUMBER,minus_sign,left_parenthesis
     */
    private boolean expr_list()
    {
        System.out.println("expr_list");
        look_ahead_token = getNextTokenWrapper();
        if (isID(look_ahead_token) || look_ahead_token.getToken_type() == TokenType.NUMBER ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            if (non_empty_expr_list())
                return true;
            else
                return false;
        }
        else
            return true;

    }

    /**
     *<non-empty expr list> --> <expression> <non-empty expr list'>
     */
    private boolean non_empty_expr_list()
    {
        System.out.println("non_expr_list");
        if (expression() && non_empty_expr_list_dash())
            return true;
        else
            return false;
    }

    /**
     *<non-empty expr list'> --> empty | comma <expression> <non-empty expr list'>
     */
    private boolean non_empty_expr_list_dash()
    {
        System.out.println("non_empty_expr_list_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.COMMA))
        {
            consumed = true;
            if (expression() && non_empty_expr_list_dash())
            {
                return true;
            }
            return false;
        }
        else
            return true;
    }

    /**
     *<if statement> --> left_parenthesis <condition expression> right_parenthesis <block statements>
     */
    private boolean if_statement()
    {
        System.out.println("if_statement");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            if (condition_expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    consumed = true;
                    if (block_statement())
                        return true;
                }
            }
        }
        return false;
    }

    /**
     *<condition expression> --> <condition> <condition expressionZ>
     */
    private boolean condition_expression()
    {
        System.out.println("condition_expression");
        if (condition() && condition_expressionZ())
            return true;
        else
            return false;
    }

    /**
     *<condition expressionZ> --> empty |<condition op> <condition>
     *first(condition op) = double_and_sign,double_or_sign
     */
    private boolean condition_expressionZ()
    {
        System.out.println("condition_expressionZ");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.DOUBLE_AND_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.DOUBLE_OR_SIGN))
        {
            if (condition_op())
            {
                if (condition())
                    return true;
            }
            return false;
        }
        else
            return true;
    }

    /**
     *<condition op> --> double_and_sign | double_or_sign
     */
    private boolean condition_op()
    {
        System.out.println("condition_op");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.DOUBLE_AND_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.DOUBLE_OR_SIGN))
        {
            consumed = true;
            return true;
        }
        return false;
    }

    /**
     *<condition> --> <expression> <comparison op> <expression>
     */
    private boolean condition()
    {
        System.out.println("condition");
        if (expression() && comparison_op() && expression())
            return true;
        else
            return false;
    }

    /**
     *<comparison op> --> == | != | > | >= | < | <=
     */
    private boolean comparison_op()
    {
        System.out.println("comparison_op");
        look_ahead_token = getNextTokenWrapper();
        if (bg.isCompOp(look_ahead_token.getToken_value()))
        {
            consumed = true;
            return true;
        }
        else
            return false;
    }

    /**
     *<while statement> --> left_parenthesis <condition expression> right_parenthesis <block statements>
     */
    public boolean while_statement()
    {
        System.out.println("while_statement");
        look_ahead_token = getNextTokenWrapper();

        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            if (condition_expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    consumed = true;
                    if (block_statement())
                        return true;
                }
            }
        }
        return false;
    }

    /**
     *<return statementZ>  --> <expression> semicolon
     *                        |semicolon
     **/
    private boolean return_statementZ()
    {
        System.out.println("return_statementZ");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
        {
            consumed = true;
            return true;
        }
        else if (expression())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                consumed = true;
                return true;
            }
        }
        return false;
    }

    /**
     *<expression> --> <term> <expression'>
     */
    private boolean expression()
    {
        System.out.println("expression");
        if (term() && expression_dash())
            return true;
        else
            return false;
    }

    /**
     *<expression'> --> empty | <addop> <term> <expression'>
     *     first(addop)={plus_sign,minus_sign}
     */
    private boolean expression_dash()
    {
        System.out.println("expression_dash");
        look_ahead_token = getNextTokenWrapper();

        if (look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN) ||
            look_ahead_token.getToken_value().equals(CSC512_Constants.PLUS_SIGN))
        {
            if(addop() && term() && expression_dash())
                    return true;
            return false;
        }
        else
            return true;
    }

    /**
     *<addop> --> plus_sign | minus_sign
     */
    private boolean addop()
    {
        System.out.println("addop");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.PLUS_SIGN))
        {
            consumed = true;
            return true;
        }
        return false;
    }

    /**
     *<term> --> <factor> <term'>
     */
    private boolean term()
    {
        System.out.println("term");
        if (factor() && term_dash())
            return true;
        else
            return false;
    }

    /**
     *<term'> --> empty | <mulop> <factor> <term'>
     *     first(mulop)={star_sign,forward_sign}
     */
    private boolean term_dash()
    {
        System.out.println("term_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.STAR_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.FORWARD_SIGN))
        {
            if (mulop() && factor() && term_dash())
                return true;
            else
                return false;
        }
        else
            return true;
    }

    /**
     *<mulop> --> star_sign | forward_slash
     */
    private boolean mulop()
    {
        System.out.println("mulop");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.STAR_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.FORWARD_SIGN))
        {
            consumed = true;
            return true;
        }
        else
            return false;
    }

    /**
     *<factor> --> ID <factorZ>
     *            |NUMBER | minus_sign NUMBER | left_parenthesis <expression> right_parenthesis
     */
    private boolean factor()
    {
        System.out.println("factor");
        look_ahead_token = getNextTokenWrapper();
        if (isID(look_ahead_token))
        {
            consumed = true;
            if(factorZ())
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_type() == TokenType.NUMBER)
        {
            consumed = true;
            return true;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN))
        {
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if(look_ahead_token.getToken_type() == TokenType.NUMBER)
            {
                consumed = true;
                return true;
            }
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            if (expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    consumed = true;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *<factorZ> --> empty
     *              |left_parenthesis <expr list> right_parenthesis
     *              |left_bracket <expression> right_bracket
     */
    private boolean factorZ()
    {
        System.out.println("factorZ");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            consumed = true;
            if (expr_list())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    consumed=true;
                    return true;
                }
            }
            return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.LB))
        {
            consumed = true;
            if (expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RB))
                {
                    consumed = true;
                    return true;
                }
            }
            return false;
        }
        else
            return true;
    }
    /*******************************************End of grammar implementation *****************************************/


    public static void main(String[] arg)
    {
        //if (arg.length != 1)/* Argument check for file input */
        //{
          //  System.out.println("Please run the program as ");
            //System.out.println("Parser <input_C_file>");
            //System.exit(CSC512_Constants.SUCCESS);
        //}
        java.util.Scanner cin = new java.util.Scanner(System.in);
        String file_name = cin.next();
        Parser parser = null;
        try
        {
            parser = new Parser(file_name);
            if (parser.isParsable())
            {
                System.out.println(CSC512_Constants.PASS + " variable " + parser.getVariableCount() + " function " +
                        parser.getFunctionCount() + " statement " + parser.getStatementCount());
            }
            else
            {
                System.out.println(CSC512_Constants.ERROR + " parsing the file " + file_name);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}