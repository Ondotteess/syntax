package lexer.TokenGenerators;

import syspro.tm.lexer.BadToken;
import syspro.tm.lexer.Token;

public class BadTokenGenerator extends TokenGenerator {

    public BadTokenGenerator(String sequence,
                             StringBuilder buffer,
                             int start,
                             int leadingTrivialLen,
                             int trailingTrivialLen) {

        super(sequence, buffer, start, leadingTrivialLen,trailingTrivialLen);
    }

    @Override
    protected void getBuffer() {

    }

    @Override
    public Token getToken() {
        return
                new BadToken(start,
                             computeEnd(),
                             leadingTrivialLen,
                             trailingTrivialLen);
    }

}
