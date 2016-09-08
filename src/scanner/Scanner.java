package scanner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by abhishek on 9/1/16.
 */
public class Scanner
{

    private class InputFileDetail
    {
        String file_path;
        String file_name;
        String extension;
        String only_name;
        boolean isSet = false;
    }

    private Grammar grammar;
    private InputFileDetail ifd;
    private Reader input_reader;
    private Token current_token;
    private StringBuffer token_string;
    private char extra_char_read;
    private Token default_Error_Token;

    private void init()throws FileNotFoundException
    {
        this.grammar = new Grammar();
        this.token_string = new StringBuffer();
        this.extra_char_read = '\0';
        this.ifd = new InputFileDetail();
        this.default_Error_Token = new Token(TokenType.ERROR,CSC512_Constants.EMPTY);
    }

    private void openInStream()throws FileNotFoundException
    {
        Charset encode = Charset.defaultCharset();
        File in = new File(ifd.file_path);
        InputStream in_stream = new FileInputStream(in);
        this.input_reader = new BufferedReader(new InputStreamReader(in_stream));
    }

    public Scanner(String in_file_name)throws FileNotFoundException
    {
        ifd = new InputFileDetail();
        init();
        setFileDetail(in_file_name);
        openInStream();
    }

    private void setFileDetail(String file_name)
    {
        if(ifd == null)
            return;

        Path p = Paths.get(file_name);
        ifd.file_path = file_name;
        ifd.file_name = p.getFileName().toString();
        ifd.only_name = ifd.file_name.split("\\.")[0];
        ifd.extension = ifd.file_name.split("\\.")[1];
        ifd.isSet = true;
    }

    public String getTargetFileName()
    {
        if(ifd.isSet)
        {
            return ifd.only_name + CSC512_Constants.GENERATE_TAG + CSC512_Constants.DOT + ifd.extension;
        }
        else
            return null;
    }

    public boolean hasMoreToken()
    {
        this.current_token = readNextTokenFromFile();

        if (this.current_token != null && this.current_token.getToken_type() == TokenType.EOF)
        {
            /* No More Token Left */
            return false;
        }
        return true;
    }

    public Token getNextToken()
    {
        return this.current_token;
    }

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

    private Token generateToken(char c)
    {
        Token res_token = null;
        token_string.delete(0,token_string.length());
        token_string.append(c);

        /* Identify the possible token */
        if(isMetaCharacter(c))
        {
            res_token = formMetaToken();
        }
        else if(Character.isDigit(c))
        {
            res_token = formNumberToken();
        }
        else if(Character.isLetter(c))
        {
            res_token = formLetterToken();
        }
        else if(isArithimaticOperator(c) || isBracket(c) || isSemiColonComma(c))
        {
            res_token = formSpecialSingleSymbolToken();
        }
        else if (isEqualityOrAssignment(c))
        {
            res_token = formSpecialMultiSymbolToken();
        }
        else if(isLogical_AND_OR(c))
        {
            res_token = formLogicalOpToken();
        }
        else if(isOpeningQuote(c))
        {
            res_token = formStringToken();
        }
        if(res_token == null)
        {
            default_Error_Token.setToken_name(token_string.toString());
            res_token = default_Error_Token;
        }
        return res_token;
    }

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

    private boolean isLogical_AND_OR(char c)
    {
        if (c == '&' || c == '|')
            return true;
        else
            return false;
    }

    private boolean isOpeningQuote(char c)
    {
        if(c == '"')
            return true;
        else
            return false;
    }

    private boolean isEqualityOrAssignment(char c)
    {
        if (c == '=' || c == '<' || c == '>' || c == '!')
            return true;
        else
            return false;
    }

    private boolean isBracket(char c)
    {
        return grammar.isBracket(c);
    }

    private boolean isArithimaticOperator(char c)
    {
        return grammar.isArthOp(c);
    }

    private boolean isReservedWord(String keyword)
    {
        if(keyword != null && grammar.isReservedKeyWord(keyword))
            return true;
        else
            return false;
    }

    private boolean isSemiColonComma(char c)
    {
        if (c == ';' || c == ',')
            return true;
        else
            return false;
    }

    /***************************Token Formation routine definition as per Grammar ******************************/
    private Token formNumberToken()
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

    private Token formLetterToken()
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
        if(isReservedWord(token_string.toString()))
        {
            return new Token(TokenType.RESERVED_WORD,token_string.toString());
        }
        else
        {
            return new Token(TokenType.IDENTIFIER,token_string.toString());
        }
    }

    private Token formStringToken()
    {
        int c;
        while ((c = getNextCharFromFile()) != -1)
        {
            if(c != '"')
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

    private Token formSpecialSingleSymbolToken()
    {
        return new Token(TokenType.SYMBOL,token_string.toString());
    }

    /* Not sure if this contains single or multiple character
       Check for ==,!=,>=,>,<=,<,= */
    private Token formSpecialMultiSymbolToken()
    {
        int c = getNextCharFromFile();

        if(c != -1 && (char)c == '=')
            token_string.append((char) c);
        else
            extra_char_read = (char)c;
        return new Token(TokenType.SYMBOL,token_string.toString());
    }

    /* Check for logical operator like and or. It also handles the derefencing operator of the C */
    private Token formLogicalOpToken()
    {
        int c = getNextCharFromFile();
        if(c != -1 && ((char)c == '&' || (char)c == '|'))
            token_string.append((char)c);
        else
            extra_char_read = (char)c;

        return new Token(TokenType.SYMBOL,token_string.toString());
    }

    /* Meta statement */
    private Token formMetaToken()
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

    private int getNextCharFromFile()
    {
        int read_char;
        try
        {
            read_char = input_reader.read();
            if (read_char == -1)
                input_reader.close();
        }
        catch (IOException ex)
        {
            System.out.print("Unexpected Error in reading the file...");
            return -1;
        }
        return read_char;
    }

    public boolean isPreNewLine(Token token)
    {
        return false;
    }

    public boolean isPostNewLine(Token token)
    {
        return (token.getToken_value().equals(";") || token.getToken_value().equals("{") ||
                token.getToken_value().equals("}") || token.getToken_type() == TokenType.META_CHAR);
    }

    public static void main(String arg[])
    {
        if (arg.length != 1)
        {
            System.out.println("Please run the program as ");
            System.out.println("Scanner <input_C_file>");
            System.exit(CSC512_Constants.SUCCESS);
        }

        PrintWriter op = null;
        Logger LOGGER = Logger.getLogger(Scanner.class.getCanonicalName());

        try
        {
            /* Set the input file for the scanner to work on */
            Scanner scan = new Scanner(arg[0]);
            Token token = null;
            Token previous_token = null;

            /* Create a output file stream to write the resultant program */
            String op_file = scan.getTargetFileName();
            op = new PrintWriter(new File(op_file));

            /* Process all the token in the file */
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
                        op.write(CSC512_Constants.CSC512 + token.getToken_value() + CSC512_Constants.SPACE);
                    else if (scan.isPreNewLine(token))
                        op.write(CSC512_Constants.NEW_LINE + token.getToken_value() + CSC512_Constants.SPACE);
                    else if (scan.isPostNewLine(token))
                        op.write(token.getToken_value() + CSC512_Constants.NEW_LINE);
                    else
                        op.write(token.getToken_value() + CSC512_Constants.SPACE);
                }

                if(previous_token != null && previous_token.getToken_type() == TokenType.META_CHAR)
                    op.println();

                previous_token = token;
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