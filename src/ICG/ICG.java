package ICG;

/* Import the packages */
import org.intellij.lang.annotations.Flow;
import scanner.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * Created by abhishek on 9/26/16.
 * ICG class implementation to parse the file against a grammar
 * It scanner functionality to scan the file for token and pass it to
 * the parser which uses the grammar scanned in recursive decent fashion
 *
 */
public class ICG
{
    private Logger LOGGER = null; // Loggin of data
    private int variableCount = 0;
    private int functionCount = 0;
    private int statementCount = 0;
    private BasicGrammar bg = null;
    private String file_name = null;
    private Scanner scan = null;
    private Token EOF_TOKEN = new Token(TokenType.EOF,CSC512_Constants.EMPTY);
    private Token look_ahead_token = null;
    private Token last_read_var = null;
    private Token last_read_res_kw = null;
    private VariableMap vm = null;
    private boolean consumed = true;
    private StringBuffer final_code = null;
    private StringBuffer func_code_line = null;
    private StringBuffer func_code = null;
    private StringBuffer parameter_assign_code = null;
    private ArrayList<Token> token_list = new ArrayList<>();
    private Stack<FlowLabels> flow_labels = new Stack<>();
    Stack<FlowLabels> while_labels = new Stack<>();
    private FlowLabels temp_flow = null;
    private int array_size;
    private int flow_index;
    private boolean isGlobalAArray;
    private Stack<String> operations = null;
    private Stack<String> values = null;
    private int local_var_count;

    /* Constructor to load the file which will be parsed */
    public ICG(String file_name) throws FileNotFoundException
    {
        this.file_name = file_name;
        scan = new Scanner(this.file_name);
        bg = new BasicGrammar();
        vm = new VariableMap();
        final_code = new StringBuffer();
        func_code = new StringBuffer();
        func_code_line = new StringBuffer();
        flow_index = 0;
        local_var_count = 0;
        isGlobalAArray = false;
        array_size = 0;
        operations = new Stack<>();
        values = new Stack<>();
        parameter_assign_code = new StringBuffer();
        LOGGER = Logger.getLogger(ICG.class.getCanonicalName());
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

    /* Flow control mechanism to add correct labels */
    private void addNewFlow(boolean isLoop)
    {
        temp_flow = new FlowLabels();
        temp_flow.start_label = CSC512_Constants.START + flow_index;
        temp_flow.execute_label = CSC512_Constants.EXEC + flow_index;
        temp_flow.exit_label= CSC512_Constants.EXIT + flow_index;
        temp_flow.isLoop = isLoop;
        flow_labels.push(temp_flow);
        if(isLoop)
            while_labels.push(temp_flow);
        flow_index++;
    }

    /* Start the grammar parsing from here */
    public boolean isParsable()
    {
        /* Skip the meta statement */
        look_ahead_token = getNextTokenWrapper();
        while(look_ahead_token.getToken_type() == TokenType.META_CHAR)
        {
            func_code_line.append(look_ahead_token.getToken_value() + "\n");
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
        }
        return program();
    }

    /* Add Start flow labels */
    public void addStartGoToLabel()
    {
        if(flow_labels.isEmpty())
            return;
        func_code_line.append(CSC512_Constants.GOTO + CSC512_Constants.SPACE + flow_labels.peek().execute_label + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
        func_code_line.append(CSC512_Constants.GOTO + CSC512_Constants.SPACE + flow_labels.peek().exit_label + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
        func_code_line.append(flow_labels.peek().execute_label + CSC512_Constants.COLON + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
        loadLine();
    }

    /* Function Call */
    public String getFuncToken(ArrayList<Token> func_call_token)
    {
        StringBuffer result_expr = new StringBuffer();
        for(Token t : func_call_token)
        {
            result_expr.append(t.getToken_value() + CSC512_Constants.SPACE);
            if(t.getToken_value().equals(CSC512_Constants.SC) || t.getToken_value().equals(CSC512_Constants.LBRC))
                result_expr.append(CSC512_Constants.NEW_LINE);
        }
        return result_expr.toString();
    }

    /* Add End Label */
    public void addEndLabel()
    {
        if(flow_labels.isEmpty())
            return;
        func_code_line.append(flow_labels.peek().exit_label + CSC512_Constants.COLON + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
        loadLine();
    }

    public void addLoopBackGoto()
    {
        if(flow_labels.isEmpty())
            return;
        func_code_line.append(CSC512_Constants.GOTO + CSC512_Constants.SPACE + flow_labels.peek().start_label + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
        loadLine();
    }

    public void removeLast()
    {
        if(flow_labels.isEmpty())
            return;
        if(flow_labels.peek().isLoop && !while_labels.isEmpty())
            while_labels.pop();
        flow_labels.pop();
    }

    /**
     *  Clear the map for new function
     */
    public void clearMap(boolean isGlobal)
    {
        System.out.println("Clear Map : " + func_code + isGlobal);
        if(!isGlobal)
            func_code = new StringBuffer(func_code.toString().replaceFirst("(local)\\[\\d+\\]","local[" + local_var_count + "]")); // Correct the ariable length
        final_code.append(func_code);
        func_code.setLength(0);
        vm.printMap(false);
        vm.clearMap(false);
    }

    /*
     * Add the codebase to output
     */
    private void addDeclVarToCode(boolean isGlobal)
    {
        if(isGlobal)
        {
            System.out.println("Declare Global : " + array_size);
            func_code_line.append("int global[" + (array_size > 0 ? array_size : vm.getMapSize(isGlobal) ) + "];" + CSC512_Constants.NEW_LINE);
            if(array_size > 0)
                isGlobalAArray = true;
        }
        else
        {
            func_code_line.append("int local[" + (vm.getMapSize(isGlobal) + 1) + "];" + CSC512_Constants.NEW_LINE);
            local_var_count = vm.getMapSize(isGlobal);
        }
        if(parameter_assign_code.length() != 0)
          func_code_line.append(parameter_assign_code);
        //array_size = 0;
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
            checkLastVarORFunc(look_ahead_token);
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
        checkLastVarORFunc(temp_token);
        return temp_token;
    }

    /* Last read token was identifier or not */
    public void checkLastVarORFunc(Token token)
    {
        if(token.getToken_type() == TokenType.RESERVED_WORD)
            last_read_res_kw = token;
        if(token.getToken_type() == TokenType.IDENTIFIER)
            last_read_var = token;
    }

    /* Add to the global variable map */
    public void addToGlobal(ArrayList<Token> list)
    {
        for(Token t : list)
        {
            System.out.println(t.getToken_type() + ":" + t.getToken_value());
            if(t.getToken_type() == TokenType.IDENTIFIER)
                vm.addToMap(true,t.getToken_value());
        }
        System.out.println(vm.global_var_map.size());
    }

    /* */
    public void addVaribaleToCode(StringBuffer code,boolean isGlobal,Token t) {
        if (vm.isPresent(false,t.getToken_value()))
        {
            code.append("local[" + vm.getValue(false, t.getToken_value()) + "]");
        }
        else if(vm.isPresent(true,t.getToken_value()))
        {
            if (!isGlobalAArray)
                code.append("global[" + vm.getValue(true, t.getToken_value()) + "]");
            else
                code.append("global");
        }
        else
            code.append(t.getToken_value() + CSC512_Constants.SPACE);
    }

    public void populateParameterAssign()
    {
        parameter_assign_code.setLength(0);

        for(Map.Entry<String,Integer> ent : vm.local_var_map.entrySet())
        {
            parameter_assign_code.append("local[" + ent.getValue() +"] = " + ent.getKey() + ";" + CSC512_Constants.NEW_LINE);
        }
    }

    public String compute(ArrayList<Token> expr_token_list)
    {
        if(expr_token_list.size() == 0)
                return "";
        StringBuffer result_expr = new StringBuffer();
        for(Token t : expr_token_list)
        {
            result_expr.append(t.getToken_value() + CSC512_Constants.SPACE);
            if(t.getToken_value().equals(CSC512_Constants.SC) || t.getToken_value().equals(CSC512_Constants.LBRC))
                result_expr.append(CSC512_Constants.NEW_LINE);
        }

        if(expr_token_list.size() == 2 || expr_token_list.size() == 1) // For single token or signed token like -1 etc
            return result_expr.toString();
        values.clear();operations.clear();

        for(Token t : expr_token_list)
        {
            System.out.println("Compute : " + t.getToken_value());
            if(t.getToken_value().equals(CSC512_Constants.LP))
                operations.push(t.getToken_value());
            else if(t.getToken_value().equals(CSC512_Constants.RP))
            {
                while (values.size() >= 2 && !operations.peek().equals(CSC512_Constants.LP))
                    values.push(generate(operations.pop(),values.pop(),values.pop()));
                if(!operations.isEmpty())
                    operations.pop();
            }
            else if(t.getToken_value().equals("+") || t.getToken_value().equals("-") || t.getToken_value().equals("*") || t.getToken_value().equals("/"))
            {
                while(!operations.isEmpty() && precendence(t.getToken_value(),operations.peek()))
                    values.push(generate(operations.pop(),values.pop(),values.pop()));
                operations.push(t.getToken_value());
            }
            else
                values.push(t.getToken_value());
        }

        while (!operations.isEmpty() && values.size() >= 2)
            values.push(generate(operations.pop(),values.pop(),values.pop()));
        //if(values.size() > 1 || operations.size() != 0 || !values.peek().contains("local") || !values.contains("global"))
        //{
          //  func_code.append("local[" + local_var_count + "] = " + result_expr.toString() + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
            //return "local[" + local_var_count++ + "]";
        //}
        System.out.println("Evaluated : " + values.peek());
        return values.pop();
        //return result_expr;
    }

    public boolean precendence(String incoming,String top_of_stack)
    {
        if(top_of_stack.equals("(") || top_of_stack.equals(")"))
            return false;
        else if((incoming.equals("*") || incoming.equals("/")) && (top_of_stack.equals("+") || top_of_stack.equals("-")))
            return false;
        else
            return true;
    }

    public String generate(String op,String val1,String val2)
    {
        System.out.println("Got : " + op + ":" + val1 + ":" + val2);
        func_code.append("local[" + local_var_count + "] = " + val2 + op + val1 + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
        System.out.println(local_var_count + 1);
        return "local[" + local_var_count++ + "]";
    }

    public void loadLine()
    {
        System.out.println("loadLine : " + func_code_line);
        func_code.append(func_code_line);
        func_code_line.setLength(0);
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
        if(type_name(false))
        {
            token_list.add(look_ahead_token);
            look_ahead_token = getNextTokenWrapper();
            if(look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                token_list.add(look_ahead_token);
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
            for (Token t : token_list) /* Add to the final code */
                    func_code_line.append(t.getToken_value() + CSC512_Constants.SPACE);
            token_list.clear();
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            return func_list1();
        }
        else if(data_decls1(true))
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

        if (type_name(false))
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
                {
                    func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
        if (type_name(true))
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
            {
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
                {
                    func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                    consumed = true;
                    if (func() && func_list())
                        return true;
                }
            }
            return false;
        }
        else
        {
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            return true;
        }
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
            loadLine();
            consumed = true;
            clearMap(false);
            return true;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.LBRC))
        {
            functionCount++;
            func_code_line.append(CSC512_Constants.NEW_LINE + look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
            consumed = true;
            if (data_decls() && statements())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RBRC))
                {
                    func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                    loadLine();
                    consumed = true;
                    clearMap(false);
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
        System.out.println("Got the function : " + last_read_var.getToken_value());
        if (parameter_list())
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
            {
                func_code_line.append(look_ahead_token.getToken_value());
                populateParameterAssign();
                consumed = true;
                return true;
            }
        }
        return false;
    }

    //<type name> --> int | void | binary | decimal
    private boolean type_name(boolean isWritable)
    {
        System.out.println("type_name");
        look_ahead_token = getNextTokenWrapper();
        if(bg.isTypeName(look_ahead_token.getToken_value()))
        {
            if(isWritable)
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
            func_code_line.append(look_ahead_token.getToken_value());
            consumed = true;
            return true;
        }
        else if (non_empty_list())
            return true;
        else
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
                func_code_line.append(look_ahead_token.getToken_value());
                vm.addToMap(false,last_read_var.getToken_value());
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            if (type_name(true))
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_type() == TokenType.IDENTIFIER)
                {
                    func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                    vm.addToMap(false,last_read_var.getToken_value());
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
     *                  |<id list1> semicolon <data decls2>
     */
    private boolean data_decls1(boolean isGlobal)
    {
        System.out.println("data_decls1");
        if(id_list1(isGlobal))
        {
            vm.addToMap(isGlobal,last_read_var.getToken_value());
            variableCount++;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                consumed = true;
                if (data_decls2(isGlobal))
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
    private boolean data_decls2(boolean isGlobal)
    {
        look_ahead_token = getNextTokenWrapper();
        token_list.add(look_ahead_token);
        if (type_name(false))
        {
            look_ahead_token = getNextTokenWrapper();
            if (isID(look_ahead_token))
            {
                token_list.add(look_ahead_token);
                consumed = true;
                if (data_or_func_check(isGlobal))
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
    private boolean data_or_func_check(boolean isGlobal)
    {
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            if(token_list.size() != 0)
            {
                if(array_size == 0)
                    addToGlobal(token_list);
                addDeclVarToCode(true);
                loadLine();
            }
            //for (Token t : token_list) /* Add to the final code */
            func_code_line.append(token_list.get(token_list.size() - 2).getToken_value() + CSC512_Constants.SPACE);
            func_code_line.append(token_list.get(token_list.size() - 1).getToken_value() + CSC512_Constants.SPACE);
            token_list.clear();
            if (func_list2())
                return true;
            return false;
        }

        else if (data_decls1(isGlobal))
        {
            return true;
        }
        return false;
    }

    /**
     * <data decls> --> empty | <type name> <id list> semicolon <data decls>
     */
    private boolean data_decls()
    {
        System.out.println("data_decls");
        if (type_name(false))
        {
            if (id_list())
            {
                vm.addToMap(false,last_read_var.getToken_value());
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
        addDeclVarToCode(false);
        loadLine();
        System.out.println("Got total of " + vm.local_var_map.size());
        return true;
    }

    /**
     *<id list1> --> <id'><id list'>
     */
    private boolean id_list1(boolean isGlobal)
    {
        System.out.println("id_list1");
        if (id_dash(isGlobal) && id_list_dash())
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
            vm.addToMap(false,last_read_var.getToken_value());
            variableCount++;
            //func_code_line.append(look_ahead_token.getToken_value());
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
            if (id_dash(false))
                return true;
        }
        return false;
    }

    /**
     * <id'> --> empty | left_bracket <expression> right_bracket
     */
    private boolean id_dash(boolean isDecl)
    {
        ArrayList<Token> arr_len = new ArrayList<>();
        System.out.println("id_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LB))
        {
            if(!isDecl)
                func_code_line.append(look_ahead_token.getToken_value());
            consumed = true;
            if (expression(arr_len))
            {
                if(isDecl && arr_len.size() > 0 && arr_len.get(0).getToken_value().matches("\\d+"))
                {
                    array_size += Integer.parseInt(arr_len.get(0).getToken_value().trim());
                    System.out.println("Global Array Size : " + array_size);
                }
                else
                {
                    func_code_line.append(compute(arr_len));
                }
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RB))
                {
                    if(!isDecl)
                        func_code_line.append(look_ahead_token.getToken_value());
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
    private boolean block_statement(boolean isLoop)
    {
        System.out.println("block_statement");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LBRC))
        {
            addStartGoToLabel();
            consumed = true;
            if (statements())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RBRC))
                {
                    if(isLoop)
                        addLoopBackGoto();
                    addEndLabel();
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
        ArrayList<Token> expr_code = new ArrayList<>();
        System.out.println("statement");
        statementCount++;
        look_ahead_token = getNextTokenWrapper();
        if (isID(look_ahead_token))
        {
            addVaribaleToCode(func_code_line,false,look_ahead_token);
            consumed = true;
            if (statementZ())
            {
                return true;
            }
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.IF))
        {
            addNewFlow(false);
            func_code_line.append(look_ahead_token.getToken_value());
            consumed = true;
            if (if_statement())
            {
                removeLast();
                return true;
            }
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.WHILE))
        {
            addNewFlow(true);
            func_code_line.append(flow_labels.peek().start_label + CSC512_Constants.COLON + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
            func_code_line.append(CSC512_Constants.IF);
            consumed = true;
            if (while_statement())
            {
                removeLast();
                return true;
            }
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.RETURN))
        {
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            if (return_statementZ())
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.BREAK) || look_ahead_token.getToken_value().equals(CSC512_Constants.CONTINUE))
        {
            if(while_labels.isEmpty())
            {
                System.out.println(look_ahead_token.getToken_value() + " without a loop.");
                return false;
            }
            if(look_ahead_token.getToken_value().equals(CSC512_Constants.BREAK))
                func_code_line.append("goto " + while_labels.peek().exit_label);
            else
                func_code_line.append("goto " + while_labels.peek().start_label);
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                loadLine();
                consumed = true;
                return true;
            }
            return false;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.READ))
        {
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
            {
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                if (isID(look_ahead_token))
                {
                    addVaribaleToCode(func_code_line,false,look_ahead_token);
                    consumed = true;
                    look_ahead_token = getNextTokenWrapper();
                    func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                    {
                        consumed = true;
                        look_ahead_token = getNextTokenWrapper();
                        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                        {
                            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                            loadLine();
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
            {
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                consumed = true;
                if (expression(expr_code))
                {
                    func_code_line.append(compute(expr_code));
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                    {
                        func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                        consumed = true;
                        look_ahead_token = getNextTokenWrapper();
                        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                        {
                            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                            loadLine();
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
            {
                consumed = true;
                look_ahead_token = getNextTokenWrapper();
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                if (look_ahead_token.getToken_type() == TokenType.STRING)
                {
                    consumed = true;
                    look_ahead_token = getNextTokenWrapper();
                    func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                    {
                        consumed = true;
                        look_ahead_token = getNextTokenWrapper();
                        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                        {
                            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                            loadLine();
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
        ArrayList<Token> expr_code = new ArrayList<>();
        System.out.println("assignment");
        if (id_dash(false))
        {
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.EQUALS))
            {
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                consumed = true;
                if (expression(expr_code))
                {
                    func_code_line.append(compute(expr_code));
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                    {
                        func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                        loadLine();
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
        ArrayList<Token> expr_code = new ArrayList<>();
        System.out.println("func_call : " + last_read_var.getToken_value());
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            //expr_code.add(last_read_var);
            expr_code.add(look_ahead_token);
            //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            if (expr_list(expr_code))
            {
                //func_code_line.append(compute(expr_code));
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    expr_code.add(look_ahead_token);
                    //expr_code.add(new Token(TokenType.IDENTIFIER,"local[" + local_var_count++ + "]"));
                    func_code_line.append(getFuncToken(expr_code));
                    //expr_code.add(new Token(TokenType.IDENTIFIER,"local[" + local_var_count++ + "]"));
                    //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                    consumed = true;
                    look_ahead_token = getNextTokenWrapper();
                    if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
                    {
                        func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                        loadLine();
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
    private boolean expr_list(ArrayList<Token> expr_code)
    {
        System.out.println("expr_list");
        look_ahead_token = getNextTokenWrapper();
        if (isID(look_ahead_token) || look_ahead_token.getToken_type() == TokenType.NUMBER ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            if (non_empty_expr_list(expr_code))
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
    private boolean non_empty_expr_list(ArrayList<Token> expr_code)
    {
        System.out.println("non_expr_list");
        if (expression(expr_code))
        {
            //func_code_line.append(compute(expr_code));
            //expr_code.clear();
            if(non_empty_expr_list_dash(expr_code))
                return true;
        }
        return false;
    }

    /**
     *<non-empty expr list'> --> empty | comma <expression> <non-empty expr list'>
     */
    private boolean non_empty_expr_list_dash(ArrayList<Token> expr_code)
    {
        System.out.println("non_empty_expr_list_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.COMMA))
        {
            expr_code.add(look_ahead_token);
            consumed = true;
            if (expression(expr_code))
            {
                //func_code_line.append(compute(expr_code));
                //expr_code.clear();
                if(non_empty_expr_list_dash(expr_code))
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
            func_code_line.append(look_ahead_token.getToken_value());
            consumed = true;
            if (condition_expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    func_code_line.append(look_ahead_token.getToken_value());
                    consumed = true;
                    if (block_statement(false))
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
            func_code_line.append(CSC512_Constants.SPACE + look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
        ArrayList<Token> expr_code = new ArrayList<>();
        System.out.println("condition");
        if (expression(expr_code))
        {
            func_code_line.append(compute(expr_code));
            if(comparison_op())
            {
                expr_code.clear();
                if(expression(expr_code))
                {
                    func_code_line.append(compute(expr_code));
                    return true;
                }
            }
        }
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
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
            func_code_line.append(look_ahead_token.getToken_value());
            consumed = true;
            if (condition_expression())
            {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    func_code_line.append(look_ahead_token.getToken_value());
                    consumed = true;
                    if (block_statement(true))
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
        ArrayList<Token> expr_code = new ArrayList<>();
        System.out.println("return_statementZ");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
        {
            func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
            loadLine();
            consumed = true;
            return true;
        }
        else if (expression(expr_code))
        {
            func_code_line.append(compute(expr_code));
            look_ahead_token = getNextTokenWrapper();
            if (look_ahead_token.getToken_value().equals(CSC512_Constants.SC))
            {
                func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.NEW_LINE);
                loadLine();
                consumed = true;
                return true;
            }
        }
        return false;
    }

    /**
     *<expression> --> <term> <expression'>
     */
    private boolean expression(ArrayList<Token> expr_code)
    {
        System.out.println("expression");
        if (term(expr_code) && expression_dash(expr_code))
        {
            return true;
        }
        else
            return false;
    }

    /**
     *<expression'> --> empty | <addop> <term> <expression'>
     *     first(addop)={plus_sign,minus_sign}
     */
    private boolean expression_dash(ArrayList<Token> expr_code)
    {
        System.out.println("expression_dash");
        look_ahead_token = getNextTokenWrapper();

        if (look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN) ||
            look_ahead_token.getToken_value().equals(CSC512_Constants.PLUS_SIGN))
        {
            if(addop(expr_code) && term(expr_code) && expression_dash(expr_code))
                    return true;
            return false;
        }
        else
            return true;
    }

    /**
     *<addop> --> plus_sign | minus_sign
     */
    private boolean addop(ArrayList<Token> expr_code)
    {
        System.out.println("addop");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.PLUS_SIGN))
        {
            expr_code.add(look_ahead_token);
            //func_code_line.append(look_ahead_token.getToken_value());
            consumed = true;
            return true;
        }
        return false;
    }

    /**
     *<term> --> <factor> <term'>
     */
    private boolean term(ArrayList<Token> expr_code)
    {
        System.out.println("term");
        if (factor(expr_code) && term_dash(expr_code))
            return true;
        else
            return false;
    }

    /**
     *<term'> --> empty | <mulop> <factor> <term'>
     *     first(mulop)={star_sign,forward_sign}
     */
    private boolean term_dash(ArrayList<Token> expr_code)
    {
        System.out.println("term_dash");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.STAR_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.FORWARD_SIGN))
        {
            if (mulop(expr_code) && factor(expr_code) && term_dash(expr_code))
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
    private boolean mulop(ArrayList<Token> expr_code)
    {
        System.out.println("mulop");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.STAR_SIGN) ||
                look_ahead_token.getToken_value().equals(CSC512_Constants.FORWARD_SIGN))
        {
            //func_code_line.append(look_ahead_token.getToken_value());
            expr_code.add(look_ahead_token);
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
    private boolean factor(ArrayList<Token> expr_code)
    {
        System.out.println("factor");
        StringBuffer token_string = new StringBuffer();
        look_ahead_token = getNextTokenWrapper();
        if (isID(look_ahead_token))
        {
            addVaribaleToCode(token_string,false,look_ahead_token);
            expr_code.add(new Token(TokenType.IDENTIFIER,token_string.toString()));
            consumed = true;
            if(factorZ(expr_code))
                return true;
            else
                return false;
        }
        else if (look_ahead_token.getToken_type() == TokenType.NUMBER)
        {
            expr_code.add(look_ahead_token);
            //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            return true;
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.MINUS_SIGN))
        {
            expr_code.add(look_ahead_token);
            //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            look_ahead_token = getNextTokenWrapper();
            if(look_ahead_token.getToken_type() == TokenType.NUMBER)
            {
                expr_code.add(look_ahead_token);
                //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
                consumed = true;
                return true;
            }
        }
        else if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP))
        {
            expr_code.add(look_ahead_token);
            //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            if (expression(expr_code))
            {
                //func_code_line.append(compute(expr_code));
                //expr_code.clear();
                //func_code_line.append(expr_code);
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP))
                {
                    expr_code.add(look_ahead_token);
                    //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
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
    private boolean factorZ(ArrayList<Token> expr_code) {
        ArrayList<Token> func_call_token = new ArrayList<>();
        System.out.println("factorZ");
        look_ahead_token = getNextTokenWrapper();
        if (look_ahead_token.getToken_value().equals(CSC512_Constants.LP)) {
            func_call_token.add(last_read_var);
            expr_code.remove(expr_code.size() - 1);
            //func_code_line.append(last_read_var.getToken_value() + CSC512_Constants.SPACE);
            //func_code_line.append(look_ahead_token.getToken_value() + CSC512_Constants.SPACE);
            func_call_token.add(look_ahead_token);
            consumed = true;
            if (expr_list(func_call_token)) {
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RP)) {
                    func_call_token.add(look_ahead_token);
                    func_code.append("local[" + local_var_count + "] = " + getFuncToken(func_call_token) + CSC512_Constants.SC + CSC512_Constants.NEW_LINE);
                    expr_code.add(new Token(TokenType.IDENTIFIER,"local[" + local_var_count++ + "]"));
                    consumed = true;
                    return true;
                }
            }
            return false;
        } else if (look_ahead_token.getToken_value().equals(CSC512_Constants.LB)) {
            //addVaribaleToCode(expr_code,false, last_read_var);
            expr_code.add(look_ahead_token);
            func_code_line.append(getFuncToken(expr_code));
            expr_code.clear();
            //func_code_line.append(last_read_var.getToken_value() + CSC512_Constants.SPACE);
            consumed = true;
            if (expression(expr_code)) {
                //func_code_line.append(compute(expr_code));
                func_code_line.append(getFuncToken(expr_code));
                //func_code_line.append(expr_code);
                expr_code.clear();
                look_ahead_token = getNextTokenWrapper();
                if (look_ahead_token.getToken_value().equals(CSC512_Constants.RB)) {
                    expr_code.add(look_ahead_token);
                    func_code_line.append(getFuncToken(expr_code));
                    expr_code.clear();
                    consumed = true;
                    return true;
                }
            }
            return false;
        }
        else
        {
            //addVaribaleToCode(expr_code,false,last_read_var);
            //consumed = true;
            return true;
        }
    }
    /*******************************************End of grammar implementation *****************************************/


    public static void main(String[] arg)
    {
        //if (arg.length != 1)/* Argument check for file input */
        //{
          //  System.out.println("Please run the program as ");
            //System.out.println("ICG <input_C_file>");
            //System.exit(CSC512_Constants.SUCCESS);
        //}
        java.util.Scanner cin = new java.util.Scanner(System.in);
        String file_name = cin.next();
        ICG parser = null;
        try
        {
            parser = new ICG(file_name);
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
        System.out.println(parser.final_code.toString());
    }
}