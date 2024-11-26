package lexer.TokenGenerators;

import syspro.tm.lexer.BooleanLiteralToken;
import syspro.tm.lexer.Token;

public class BooleanGenerator extends TokenGenerator {

    public BooleanGenerator(String sequence,
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
        if (this.buffer.toString().equals("true")) {
            return new BooleanLiteralToken(start,
                                            computeEnd(),
                                            leadingTrivialLen,
                                            trailingTrivialLen,
                                       true);
        } else {
            return new BooleanLiteralToken(start,
                                            computeEnd(),
                                            leadingTrivialLen,
                                            trailingTrivialLen,
                                       false);
        }
    }

}
