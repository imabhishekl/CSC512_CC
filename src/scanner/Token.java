package scanner;

/**
 * Created by abhishek on 9/5/16.
 */
public class Token
{
    private TokenType token_type;
    private TokenSubType TokenSubType;
    private String token_value;

    Token(TokenType token_type,String token_value)
    {
        this.token_type = token_type;
        this.token_value = token_value;
    }

    public TokenType getToken_type() {
        return token_type;
    }

    public void setToken_type(TokenType token__type) {
        this.token_type = token__type;
    }

    public String getToken_value() {
        return token_value;
    }

    public void setToken_name(String token_value) {
        this.token_value = token_value;
    }

    public scanner.TokenSubType getTokenSubType() {
        return TokenSubType;
    }

    public void setTokenSubType(scanner.TokenSubType tokenSubType) {
        TokenSubType = tokenSubType;
    }
}
