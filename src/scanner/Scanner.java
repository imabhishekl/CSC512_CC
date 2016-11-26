package scanner;

/* Import the packages */
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by abhishek on 9/1/16.
 * Scanner class to get all the token from the file
 * It identifies all the token present in the file defined as per grammar
 */
public class Scanner
{
    /* Inner class for file detail management */
    private class InputFileDetail
    {
        String file_path;
        String file_name;
        String extension;
        String only_name;
        boolean isSet = false;
    }

    /* Class variable */
    private Grammar grammar;
    private InputFileDetail ifd;
    private BufferedReader input_reader;
    private Token current_token;
    private StringBuffer token_string;
    private char extra_char_read; /* Extra character storage if read in current token not belonging to it*/
    private Token default_Error_Token;
    private String in_file_name = null;
    char buffer[] = null;
    int buffer_index = 0;
    int buffer_length = -1;

    /* initialize the grammar definiation and other temproary variable */
    private void init()throws FileNotFoundException
    {
        this.grammar = new Grammar();
        this.token_string = new StringBuffer();
        this.extra_char_read = '\0';
        this.ifd = new InputFileDetail();
        this.default_Error_Token = new Token(TokenType.ERROR,CSC512_Constants.EMPTY);
    }

    /* open the file to read line by line */
    private void openInStream()throws FileNotFoundException
    {
        Charset encode = Charset.defaultCharset();
        File in = new File(this.in_file_name);
        InputStream in_stream = new FileInputStream(in);
        this.input_reader = new BufferedReader(new InputStreamReader(in_stream));
    }

    /* Constructor for the class to open file and initialize the class */
    public Scanner(String in_file_name)throws FileNotFoundException
    {
        this.in_file_name = in_file_name;
        ifd = new InputFileDetail();
        init();
        setFileDetail(in_file_name);
        openInStream();
    }

    /* Set the file details like it extension,file and full file name */
    private void setFileDetail(String file_name)
    {
        if(ifd == null)
            return;

        Path p = Paths.get(file_name);
        ifd.file_path = p.getParent().toString();
        ifd.file_name = p.getFileName().toString();
        ifd.only_name = ifd.file_name.split("\\.")[0];
        ifd.extension = ifd.file_name.split("\\.")[1];
        ifd.isSet = true;
    }

    /* Routine to get the target file name */
    public String getTargetFileName()
    {
        if(ifd.isSet)
        {
            return ifd.file_path + "/" + ifd.only_name + CSC512_Constants.GENERATE_TAG + CSC512_Constants.DOT + ifd.extension;
        }
        else
            return null;
    }

    /* Iterator routine which checks for next token in the file and load it in variable */
    public boolean hasMoreToken()
    {
        this.current_token = readNextTokenFromFile();

        /* If the token is not present of eof file reached return false */
        if (this.current_token != null && this.current_token.getToken_type() == TokenType.EOF)
        {
            /* No More Token Left */
            return false;
        }
        return true;
    }

    /* Get the next token which was last loaded */
    public Token getNextToken()
    {
        return this.current_token;
    }

    /* routine which trims the white space and then searched for next token */
    private Token readNextTokenFromFile()
    {
        int c;
        if(extra_char_read == '\0')
            c = getNextCharFromFile();
        else
            c = extra_char_read;
        extra_char_read = '\0';

        while(c != -1 && Character.isWhitespace(c))
        {
            c = getNextCharFromFile();
        }
        if(c == -1)
            return new Token(TokenType.EOF,CSC512_Constants.EMPTY);
        return generateToken((char)c);
    }

    /* Routine which returns the next token from the file by reading set of character from the file */
    private Token generateToken(char c)
    {
        Token res_token = null;
        token_string.delete(0,token_string.length());
        token_string.append(c);

        /* Identify the possible token */
        if(isMetaCharacter(c)) /* Identify meta token */
        {
            res_token = formMetaToken();
        }
        else if(Character.isDigit(c)) /* Identify digit */
        {
            res_token = formNumberToken();
        }
        else if(Character.isLetter(c)) /* Identify letter */
        {
            res_token = formLetterToken();
        }
        else if(isArithimaticOperator(c) || isBracket(c) || isSemiColonComma(c)) /* Identify single character symbol*/
        {
            res_token = formSpecialSingleSymbolToken();
        }
        else if (isEqualityOrAssignment(c)) /* Identify equality or comparison operator */
        {
            res_token = formSpecialMultiSymbolToken();
        }
        else if(isLogical_AND_OR(c)) /* Identify the logical operator defines in the grammar */
        {
            res_token = formLogicalOpToken();
        }
        else if(isOpeningQuote(c)) /* Identify the string token */
        {
            res_token = formStringToken();
        }
        if(res_token == null) /* if nothing matched return the default token */
        {
            default_Error_Token.setToken_name(token_string.toString());
            res_token = default_Error_Token;
        }
        return res_token;
    }

    /* Metacharacter */
    private boolean isMetaCharacter(char c)
    {
        if (c == '#')
            return true;
        else if (c == '/')
        {
            c = (char) getNextCharFromFile();
            if (c == '/')
            {
                token_string.append((char)c);
                return true;
            }
            else
                extra_char_read = c;
        }
        return false;
    }

    /* Logical AND and OR */
    private boolean isLogical_AND_OR(char c)
    {
        if (c == '&' || c == '|')
            return true;
        else
            return false;
    }

    /* opening quote */
    private boolean isOpeningQuote(char c)
    {
        if(c == '"')
            return true;
        else
            return false;
    }

    /* Equality or assignment operator */
    private boolean isEqualityOrAssignment(char c)
    {
        if (c == '=' || c == '<' || c == '>' || c == '!')
            return true;
        else
            return false;
    }

    /* Bracket character */
    private boolean isBracket(char c)
    {
        return grammar.isBracket(c);
    }

    /* Arthimatic operator */
    private boolean isArithimaticOperator(char c)
    {
        return grammar.isArthOp(c);
    }

    /* Language reserved keyword */
    private boolean isReservedWord(String keyword)
    {
        if(keyword != null && grammar.isReservedKeyWord(keyword))
            return true;
        else
            return false;
    }

    /* semi colon or comma character */
    private boolean isSemiColonComma(char c)
    {
        if (c == ';' || c == ',')
            return true;
        else
            return false;
    }

    /***************************Token Formation routine definition as per Grammar ******************************/
    private Token formNumberToken() /*Digit token creation*/
    {
        int c;
        while ((c = getNextCharFromFile()) != -1)
        {
            if(Character.isDigit(c))
            {
                token_string.append((char)c);
            }
            else
            {
                extra_char_read = (char)c;
                break;
            }
        }
        return new Token(TokenType.NUMBER,token_string.toString());
    }

    private Token formLetterToken()/*Letter or Identifier token creation*/
    {
        int c;
        while ((c = getNextCharFromFile()) != -1)
        {
            if(Character.isDigit(c) || Character.isLetter(c) || c == '_')
            {
                token_string.append((char)c);
            }
            else
            {
                extra_char_read = (char)c;
                break;
            }
        }

        /* Check for reserved keyword or identifier */
        if(isReservedWord(token_string.toString()))
        {
            return new Token(TokenType.RESERVED_WORD,token_string.toString());
        }
        else
        {
            return new Token(TokenType.IDENTIFIER,token_string.toString());
        }
    }

    private Token formStringToken()/*String token creation*/
    {
        int c;
        while ((c = getNextCharFromFile()) != -1)
        {
            if(c == '\n')
            {
                default_Error_Token.setToken_name("\\n in encountered in string");
                return default_Error_Token;
            }
            else if(c != '"')
            {
                token_string.append((char) c);
            }
            else
            {
                token_string.append((char) c);
                break;
            }
        }
        return new Token(TokenType.STRING,token_string.toString());
    }

    private Token formSpecialSingleSymbolToken()/*Special symbol of single length token creation*/
    {
        return new Token(TokenType.SYMBOL,token_string.toString());
    }

    /* Not sure if this contains single or multiple character
       Check for ==,!=,>=,>,<=,<,= */
    private Token formSpecialMultiSymbolToken()/*Special symbol of multiple length token creation*/
    {
        int c = getNextCharFromFile();

        if(c != -1 && (char)c == '=')
            token_string.append((char) c);
        else
            extra_char_read = (char)c;
        return new Token(TokenType.SYMBOL,token_string.toString());
    }

    /* Check for logical operator like and or. It also handles the derefencing operator of the C */
    private Token formLogicalOpToken()/*Logical operator token creation*/
    {
        int c = getNextCharFromFile();
        if(c != -1 && ((char)c == '&' || (char)c == '|'))
            token_string.append((char)c);
        else
            extra_char_read = (char)c;

        return new Token(TokenType.SYMBOL,token_string.toString());
    }

    private Token formMetaToken()/*Meta token creation*/
    {
        int c;
        if(extra_char_read != '\0')
            token_string.append(extra_char_read);
        extra_char_read = '\0';

        while ((c = getNextCharFromFile()) != -1)
        {
            if ((char)c == '\n')
                break;
            token_string.append((char)c);
        }
            return new Token(TokenType.META_CHAR,token_string.toString());
    }

    /***********************************************************************************************************/

    /* Read from the buffer of line character by character */
    private int getNextCharFromFile()
    {
        if(buffer == null || buffer_length == buffer_index)/* if buffer empty load new line for read */
        {
            buffer = loadNextLineFromFile();
            buffer_length = -1;
            buffer_index = 0;
        }

        if (buffer == null) /* if EOF then return -1 */
            return -1;
        else
        {
            if(buffer_length == -1)
                buffer_length=buffer.length;
            return (int)buffer[buffer_index++];
        }
    }

    /* Load the new line in the buffer from the file once the older line is consumped */
    private char[] loadNextLineFromFile()
    {
        String bf = null;
        try
        {
            do
            {
                bf = input_reader.readLine();
                if (bf == null) /*Everything consumed*/
                {
                    input_reader.close();
                    return null;
                }
            }while(bf.length() == 0); /* if empty line read next line */
        }
        catch (IOException ex)
        {
            System.out.println("Unexpected Error in reading the file...");
            return null;
        }
        bf += "\n"; /* Since read line does not count line return add it manually */
        return bf.toCharArray();
    }

    public boolean isPreNewLine(Token token)
    {
        return false;
    }

    /* If meta token or semicolon add a new line */
    public boolean isPostNewLine(Token token)
    {
        return (token.getToken_value().equals(";") || token.getToken_value().equals("{") ||
                token.getToken_value().equals("}") || token.getToken_type() == TokenType.META_CHAR);
    }

    /* Main routine */
    public static void main(String arg[])
    {
        if (arg.length != 1)/* Argument check for file input */
        {
            System.out.println("Please run the program as ");
            System.out.println("Scanner <input_C_file>");
            System.exit(CSC512_Constants.SUCCESS);
        }

        PrintWriter op = null; /* Output file handler */
        Logger LOGGER = Logger.getLogger(Scanner.class.getCanonicalName()); /* Logger class for result print */

        try
        {
            /* Set the input file for the scanner to work on */
            Scanner scan = new Scanner(arg[0]);
            Token token = null;
            Token previous_token = null;

            /* Create a output file stream to write the resultant program */
            String op_file = scan.getTargetFileName();
            op = new PrintWriter(new File(op_file));

            /* Process next token in the file */
            while (scan.hasMoreToken())
            {
                token = scan.getNextToken();
                if (token == null)
                {
                    LOGGER.severe("Fatal Error in scanning the code.");
                    System.exit(CSC512_Constants.SUCCESS);
                }
                else if(token.getToken_type() == TokenType.ERROR)
                {
                    LOGGER.warning("Error in scanning the file for token:" + token.getToken_value());
                    System.exit(CSC512_Constants.SUCCESS);
                }
                else
                {
                    if(token.getToken_type() == TokenType.IDENTIFIER && !token.getToken_value().equals(CSC512_Constants.MAIN))
                        op.write(CSC512_Constants.CS512 + token.getToken_value() + CSC512_Constants.SPACE);
                    else if (scan.isPreNewLine(token))/* Token with new line required at start */
                        op.write(CSC512_Constants.NEW_LINE + token.getToken_value() + CSC512_Constants.SPACE);
                    else if (scan.isPostNewLine(token))/* Token with new line required at end */
                        op.write(token.getToken_value() + CSC512_Constants.NEW_LINE);
                    else /* Normal token with no new line required */
                        op.write(token.getToken_value() + CSC512_Constants.SPACE);
                }

                if(previous_token != null && previous_token.getToken_type() == TokenType.META_CHAR)
                    op.println();

                previous_token = token;
                //System.out.println("Token Type : " + token.getToken_type() + "|Token value : " + token.getToken_value());
            }
        }
        catch(FileNotFoundException ex)
        {
            LOGGER.warning(ex.getMessage());;
        }
        finally
        {
            if (op != null)
                op.close();
        }
        LOGGER.info("Successfully scanned the input file " + arg[0] + " for tokens.");
    }
}