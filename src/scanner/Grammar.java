package scanner;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by abhishek on 9/4/16.
 */
public class Grammar
{
    private Set<Character> brackets;
    private Set<Character> arithimatic_operators;
    private Set<String> reserved_keyword_list;

    public Grammar()
    {
        brackets = new HashSet<>();
        reserved_keyword_list = new HashSet<>();
        arithimatic_operators = new HashSet<>();
        init();
    }

    private void init()
    {
        /* Load Reserved Keyword */
        for(ReservedKeywords rkw : ReservedKeywords.values())
        {
            reserved_keyword_list.add(rkw.getKeyword_value());
        }

        for(Brackets b : Brackets.values())
        {
            brackets.add(b.getKeyword_value());
        }

        for(Op op : Op.values())
        {
            arithimatic_operators.add(op.getKeyword_value());
        }
    }

    public boolean isReservedKeyWord(String kw)
    {
        return reserved_keyword_list.contains(kw);
    }

    public boolean isBracket(char b)
    {
        return brackets.contains(b);
    }

    public boolean isArthOp(char op)
    {
        return arithimatic_operators.contains(op);
    }
}

enum ReservedKeywords
{
    INT("int"),
    VOID("void"),
    IF("if"),
    WHILE("while"),
    RETURN("return"),
    READ("read"),
    WRITE("write"),
    PRINT("print"),
    CONTINUE("continue"),
    BREAK("break"),
    BINARY("binary"),
    DECIMAL("decimal");

    private String keyword_value;

    ReservedKeywords(String val)
    {
        keyword_value = val;
    }

    public String getKeyword_value()
    {
        return keyword_value;
    }
}

enum Op
{
    PLUS('+'),
    MINUS('-'),
    MULT('*'),
    DIV('/');

    private char op;

    Op(char val)
    {
        op = val;
    }

    public char getKeyword_value()
    {
        return op;
    }
}

enum Brackets
{
    OC('{'),
    CC('}'),
    OS('['),
    CS(']'),
    OR('('),
    CR(')');

    private char bracket;

    Brackets(char val)
    {
        bracket = val;
    }

    public char getKeyword_value()
    {
        return bracket;
    }
}