package parser;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by abhishek on 9/27/16.
 */
public class BasicGrammar
{
    private Set<String> type_name_sets;
    private Set<String> comparison_op;

    BasicGrammar()
    {
        type_name_sets = new HashSet<>();
        comparison_op = new HashSet<>();
        init();
    }

    private void init()
    {
        /* Load Reserved Keyword */
        for(TypeName typeName : TypeName.values())
        {
            type_name_sets.add(typeName.getKeyword_value());
        }

        for (Comparison_Op cop : Comparison_Op.values())
        {
            comparison_op.add(cop.getComparision_op());
        }
    }

    public boolean isTypeName(String kw)
    {
        return type_name_sets.contains(kw);
    }

    public boolean isCompOp(String comp_op) { return comparison_op.contains(comp_op);}
}

enum Comparison_Op
{
    EQUALITY("=="),
    NOT_EQUAL("!="),
    GREATER(">"),
    GREATER_EQUAL(">="),
    LESS("<"),
    LESS_EQUAL("<=");

    private String comparision_op;

    Comparison_Op(String comp_op){ comparision_op = comp_op;}

    public String getComparision_op() {return comparision_op;}
}

enum TypeName
{
    INT("int"),
    VOID("void"),
    BINARY("binary"),
    DECIMAL("decimal");

    private String keyword_value;

    TypeName(String val)
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