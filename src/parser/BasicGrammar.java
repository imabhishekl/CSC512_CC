package parser;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by abhishek on 9/27/16.
 * This class load some of length comparison
 * in the if condition to sets so that the
 * if condition are short
 */
public class BasicGrammar
{
    private Set<String> type_name_sets; /* Set for type name production */
    private Set<String> comparison_op; /* Set for comparison operator */

    BasicGrammar()
    {
        type_name_sets = new HashSet<>();
        comparison_op = new HashSet<>();
        init();
    }

    private void init()
    {
        /* Load Type name Keyword */
        for(TypeName typeName : TypeName.values())
        {
            type_name_sets.add(typeName.getKeyword_value());
        }

        /* Load the operator keyword */
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

/* Valid Value for comparison operator*/
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

/* Valid Value for typemame keyword*/
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