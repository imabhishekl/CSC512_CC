***********************************************************************************************************
LL(1) Grammar
<program> -->  empty
              |<type name> ID <programZ>
<programZ> --> <data decls1> <programZZ>
              |left_parenthesis <func list1>
<programZZ> --> empty
               |<type name> ID left_parenthesis <func list1>
<func list> --> empty 
               |<type name> ID left_parenthesis <func> <func list>
<func_list2> --> left_parenthesis <func> <func list>
<func list1> --> <func> <func list>
<func> --> <func decl> <funcZ>
<funcZ> --> semicolon | left_brace <data decls> <statements> right_brace 
<func decl> --> <parameter list> right_parenthesis 
<type name> --> int | void | binary | decimal 
<type name1> --> int | binary | decimal 
<parameter list> --> empty | void | <non-empty list>
<non-empty list> --> <type name1> ID <non-empty list'> 
<non-empty list'> --> empty | comma <type name> ID <non-empty list'> 
<data decls1> --> empty
                 |<id list1> semicolon <data decls2>
<data decls2> --> <type name> ID <data or func check> 
                 | empty
<data or func check> --> <data decls1>
                        |<func_list2>
<data decls> --> empty | <type name> <id list> semicolon <data decls>
<id list1> --> <id'><id list'>
<id list> --> <id> <id list'>
<id list'> --> empty | comma <id> <id list'>   
<id> --> ID <id'>
<id'> --> empty | left_bracket <expression> right_bracket
<block statements> --> left_brace <statements> right_brace 
<statements> --> empty | <statement> <statements> 
<statement> --> ID <statementZ>
               |if <if statement>
               |while <while statement> 
               |return <return statementZ> 
               |break semicolon  
               |continue semicolon
               |read left_parenthesis  ID right_parenthesis semicolon 
               |write left_parenthesis <expression>right_parenthesis semicolon 
               |print left_parenthesis  STRING right_parenthesis semicolon 
<statementZ> --> <assignment>
                |<func call>
<assignment> --> <id'> equal_sign <expression> semicolon 
<func call> --> left_parenthesis <expr list> right_parenthesis semicolon 
<expr list> --> empty | <non-empty expr list> 
<non-empty expr list> --> <expression> <non-empty expr list'>
<non-empty expr list'> --> empty | comma <expression> <non-empty expr list'>
<if statement> --> left_parenthesis <condition expression> right_parenthesis <block statements> 
<condition expression> --> <condition> <condition expressionZ>
<condition expressionZ> --> empty |<condition op> <condition>
<condition op> --> double_and_sign | double_or_sign 
<condition> --> <expression> <comparison op> <expression> 
<comparison op> --> == | != | > | >= | < | <=
<while statement> --> left_parenthesis <condition expression> right_parenthesis <block statements> 
<return statementZ>  --> <expression> semicolon
                        |semicolon
<expression> --> <term> <expression'>  
<expression'> --> empty | <addop> <term> <expression'>
<addop> --> plus_sign | minus_sign
<term> --> <factor> <term'> 
<term'> --> empty | <mulop> <factor> <term'>
<mulop> --> star_sign | forward_slash 
<factor> --> ID <factorZ> 
            |NUMBER | minus_sign NUMBER | left_parenthesis <expression> right_parenthesis
<factorZ> --> empty
             |left_parenthesis <expr list> right_parenthesis
             |left_bracket <expression> right_bracket
***********************************************************************************************************
LL(1) Grammar First+ set
1.  factorZ
    first+:{left_parenthesis,left_bracket,star_sign,forward_slash,plus_sign,minus_sign,right_bracket,semicolon,==,!=,>,>=,<,<=,right_parenthesis,comma}
2.  term'
    first+:{star_sign,forward_slash,plus_sign,minus_sign,right_bracket,semicolon,==,!=,>,>=,<,<=,right_parenthesis,comma}
3.  term
    first+:{ID,NUMBER,minus_sign,left_parenthesis}
4.  factor
    first+:{ID,NUMBER,minus_sign,left_parenthesis}
5.  mulop
    first+:{star_sign,forward_slash}
6.  addop
    first+:{plus_sign,minus_sign}
7.  expression'
    first+:{plus_sign,minus_sign,right_bracket,semicolon,==,!=,>,>=,<,<=,right_parenthesis,comma}
8.  expression
    first+:{ID,NUMBER,minus_sign,left_parenthesis}
9.  return statementZ
    first+:{ID,NUMBER,minus_sign,left_parenthesis,semicolon}
10. while statement
    first+:{left_parenthesis}
11. comparison op
    first+:{==,!=,>,>=,<,<=}
12. condition
    first+:{ID,NUMBER,minus_sign,left_parenthesis}
13. condition op
    first+:{double_and_sign,double_or_sign}
15. condition expressionZ
    first+:{double_and_sign,double_or_sign,right_parenthesis}
16. condition expression
    first+:{ID,NUMBER,minus_sign,left_parenthesis}
17. if statement
    first+:{left_parenthesis}
18. non-empty expr list'
    first+:{right_parenthesis,comma}
19. non-empty expr list
    first+:{ID,NUMBER,minus_sign,left_parenthesis}
20. expr list
    first+:{right_parenthesis,ID,NUMBER,minus_sign,left_parenthesis}
21. func call
    first+:{left_parenthesis}
22. assignment
    first+:{right_brace,ID,if,while,return,break,continue,read,write,print,left_bracket,equal_sign}
23. statementZ
    first+:{right_brace,ID,if,while,return,break,continue,read,write,print,left_bracket,equal_sign,left_parenthesis}
24. statement
    first+:{ID,if,while,return,break,continue,read,write,print}
25. statements
    first+:{right_brace,ID,if,while,return,break,continue,read,write,print}
26. block statements
    first+:{left_brace}
27. id'
    first+:{comma,semicolon,equal_sign,left_bracket}
28. id
    first+:{ID}
29. id list'
    first+:{semicolon,comma}
30. id list
    first+:{ID}
31. id list1
    first+:{semicolon,left_bracket,comma}
32. data decls
    first+:{ID,if,while,return,break,continue,read,write,print,right_brace,$,int,void,binary,decimal}
33. data decls1
    first+:{void,int,binary,decimal,$,left_bracket,comma,semicolon}
33. data decls2
    first+:{int,void,binary,decimal}
34. non-empty list'
    first+:{right_parenthesis,comma}
35. non-empty list
    first+:{int,binary,decimal}
36. parameter list
    first+:{right_parenthesis,void,int,binary,decimal}
37. type name
    first+:{int,void,binary,decimal}
39. type name1
    first+:{int,binary,decimal}
38. func decl
    first+:{semicolon,left_brace,void,int,binary,decimal,right_parenthesis}
39. funcZ
    first+:{semicolon,left_brace}
40. func
    first+:{void,int,binary,decimal,$,int,binary,decimal,right_parenthesis,semicolon,left_brace}
41. data or func check
    first+:{void,int,binary,decimal,$,left_bracket,comma,semicolon,left_parenthesis}
42. func list2
    first+:{left_parenthesis}
43. func list1
    first+:{$,void,int,binary,decimal,right_parenthesis,semicolon,left_brace}
44. func list
    first+:{$,void,int,binary,decimal}
45. programZZ
    first+:{$,void,int,binary,decimal}
46. programZ
    first+:{$,left_bracket,comma,void,int,binary,decimal,left_parenthesis,semicolon}
47. program
    first+:{$,void,int,binary,decimal}

The above LL(1) grammar contains 47 non terminal symbols and 95 production.
***********************************************************************************************************
How to Compile the Program?
-> use the below command to compile the program.
   javac *.java

How to Run the program?
-> Please use the below command to run the program.
   java Parser <input file name>
   output of the program will be PASS with variable, count and function count
   if the program fails in parsing or scanning then the output of the progam will
   be Error in parsing the <file name>  
   or 
   Error in scanning the <file name> for token:token value.
***********************************************************************************************************
Functionality/Implementation
-> Scanner.java
   This file read the input file to be scanned. It scans the program for input tokens.
   It has 2 methods used by parser i.e. hasMoreToken and nextToken.
-> The scanner skips all the meta statement and comment.
-> Token which are detected to be invalid scanner send a token with tokentype as invalid.
-> Parser uses the scanner program to read all the token in order and pass the token to the 
   grammar in the program. The grammar is validated using recursice decent parser.
-> If the entire call stack for the grammar with the start symbol was succesful then the 
   parsing result will be true else the parsing will pass down failure this capturing 
   result accurately. 
-> Stack in function call is major data structure used. For some larger
   terminal macthing Set data structure has been used.
-> BasicGrammar.java contains set for long comparison like operator and brackets to make
   the program short and clean.
-> Scanning error are thrown as error ins scanning token:token_value.
-> Parser error are thrown as error in parsing the file.
-> Parser works by comparing the first of each production inorder to decide which production to 
   call.
-> Every lookahead token is read is not consumed until its matching token is found from the file
   being parsed.
-> If the token is not consumed getNextTokenWrapper will not read the next token and hence any call
   to this function without consumption of the token will not result in next token read.
-> If there is a epsiolon/empty transition in the production then first it is check for matching
   productiion first if none of them first matches then it returns true. If first matches but when
   calling the function for that matched production returns failure that is no full match then
   the production rule will return as false.
-> Statement count is added in the statement production. If none of the statement matches then it 
   is decremented as this was a valid statement.
-> Variable count is added in the data decls/data decls1/id_list_dash production production which are
   responsible for addition of new identifier.
-> function counter is added in the funcZ production.
-> It also contains the unit_test.sh script which runs all the file in the given input folder against
   the parser.
