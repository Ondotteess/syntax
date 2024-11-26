package lexer;

import lexer.TokenGenerators.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private String currentAnalyzedSequence;
    private int currentPosition;
    private final StringBuilder buffer;
    private final HashSet<Integer> digits;
    private final HashSet<Integer> symbols;
    private final HashSet<String> keywords;
    private Context context;
    private boolean endOfFile;

    public Tokenizer(Context context) {
        this.currentAnalyzedSequence = null;
        this.currentPosition = -1;
        this.buffer = new StringBuilder();
        this.digits = initializeDigitSet();
        this.symbols = intializeSymbolSet();
        this.keywords = initializeKeywordSet();
        this.context = context;
        this.endOfFile = false;
    }

    private HashSet<String> initializeKeywordSet() {
        HashSet<String> symbols = new HashSet<>();

        symbols.add("this"); symbols.add("super"); symbols.add("is"); symbols.add("if");
        symbols.add("else"); symbols.add("for"); symbols.add("in"); symbols.add("while");
        symbols.add("def"); symbols.add("var"); symbols.add("val"); symbols.add("return");
        symbols.add("break"); symbols.add("continue"); symbols.add("abstract"); symbols.add("virtual");
        symbols.add("override"); symbols.add("native");

        return symbols;
    }

    private HashSet<Integer> intializeSymbolSet(){
        HashSet<Integer> symbols = new HashSet<>();

        symbols.add((int) '.'); symbols.add((int) ':'); symbols.add((int) ',');
        symbols.add((int) '+'); symbols.add((int) '-'); symbols.add((int) '*');
        symbols.add((int) '/'); symbols.add((int) '%'); symbols.add((int) '!');
        symbols.add((int) '~'); symbols.add((int) '&'); symbols.add((int) '|');
        symbols.add((int) '^'); symbols.add((int) '<'); symbols.add((int) '>');
        symbols.add((int) '['); symbols.add((int) ']'); symbols.add((int) '(');
        symbols.add((int) ')'); symbols.add((int) '='); symbols.add((int)'?');

        return symbols;
    }

    private HashSet<Integer> initializeDigitSet(){
        HashSet<Integer> digits = new HashSet<>();
        digits.add((int) '1');
        digits.add((int) '2');
        digits.add((int) '3');
        digits.add((int) '4');
        digits.add((int) '5');
        digits.add((int) '6');
        digits.add((int) '7');
        digits.add((int) '8');
        digits.add((int) '9');
        digits.add((int) '0');
        return digits;
    }

    private boolean isSymbol(int codePoint) {
        return symbols.contains(codePoint);
    }

    private boolean isDigit(int codePoint) {
        return digits.contains(codePoint);
    }

    private boolean isKey(String s) {
        return keywords.contains(s);
    }

    private boolean isBooleanLiteral(String s) {
        return s.equals("true") || s.equals("false");
    }

    private boolean isTrivial(int codePoint) {
        return Character.isWhitespace(codePoint) || codePoint == '#'
                || codePoint == '\n' || codePoint == '\r';
    }

    private int getCode() {
        int codePoint = currentAnalyzedSequence.codePointAt(currentPosition);
        currentPosition += Character.charCount((int) codePoint);
        return codePoint;
    }

    private boolean isValidIdentifier(String buffer) {
        return buffer.matches("[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{Nd}\\p{Pc}\\p{Mn}\\p{Mc}\\p{Cf}]*");
    }

    private ArrayList<String> splitTrivial(SequenceInfo tokenInfo) {
        ArrayList<String> output = new ArrayList<>();

        String token = tokenInfo.getToken();
        int leadingTriviaLength = tokenInfo.getLeadingTriviaLength();
        int trailingTriviaLength = tokenInfo.getTrailingTriviaLength();

        if (leadingTriviaLength > 0) {
            String leadingTrivia = token.substring(0, leadingTriviaLength);
            output.add(leadingTrivia);
        }

        int tokenStart = leadingTriviaLength;
        int tokenEnd = token.length() - trailingTriviaLength;
        if (tokenEnd > tokenStart) {
            String mainToken = token.substring(tokenStart, tokenEnd);
            output.add(mainToken);
        }

        if (trailingTriviaLength > 0) {
            String trailingTrivia = token.substring(tokenEnd);
            output.add(trailingTrivia);
        }

        return output;
    }


    public static boolean hasIndent(String input) {

        String withoutComments = input.replaceAll("#.*", "");
        Pattern pattern = Pattern.compile("\r?\n[ \t]+");
        Matcher matcher = pattern.matcher(withoutComments);

        return matcher.find() || withoutComments.matches("(\r?\n)+");
    }

    private static boolean isRune(String seq) {
        Pattern RUNE_PATTERN = Pattern.compile(
                "^'" +
                        "(" +
                        "[^'\\r\\n]" +
                        "|" +
                        "\\\\[0abrtv'\"\\\\]" +
                        "|" +
                        "\\\\U\\+[0-9A-F]{4,5}" +
                        ")" +
                        "'$"
        );
        if (seq == null || seq.length() < 3) {
            return false;
        }
        return RUNE_PATTERN.matcher(seq).matches();
    }

    private boolean isIdStart(int codePoint){
        Pattern pattern = Pattern.compile("[\\p{L}\\p{Nl}_]");
        String firstChar = new String(Character.toChars(codePoint));
        return pattern.matcher(firstChar).matches();
    }


    private TokenGenerator nextToken() {

        if (currentAnalyzedSequence.isEmpty()) return null;

        TokenGenerator generator = null;

        int codePoint = getCode();
        this.buffer.appendCodePoint(codePoint);

        if (isRune(currentAnalyzedSequence)) {
            buffer.append(currentAnalyzedSequence);
            generator = new RuneGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
        } else if (isTrivial (codePoint)) {
            generator = new IndentationGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength, context.currentIndentLength, context.currentIndentLevel, endOfFile);
            context.currentIndentLevel = generator.getIndentLevel();
            context.currentIndentLength = generator.getIndentLenght();
        } else if (isIdStart(codePoint)) { // KEY || BOOL || IDENTIFIER

            int length = this.currentAnalyzedSequence.length();

            while (this.currentPosition < length + 1) {

                if (length == this.currentPosition) {
                    generator = new IdentifierGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
                    break;
                }

                this.buffer.appendCodePoint(getCode());
                String currentBuffer = this.buffer.toString();

                if (isKey(currentBuffer) && (currentPosition == currentAnalyzedSequence.length())) {
                    generator = new KeywordGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
                    break;
                }
                else if (isBooleanLiteral(currentBuffer) && currentPosition == currentAnalyzedSequence.length()) {
                    generator = new BooleanGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
                    break;
                } else if (!isValidIdentifier(currentBuffer)) {
                    this.buffer.deleteCharAt(this.buffer.length() - 1);
                    if (isKey(this.buffer.toString())) {
                        generator = new KeywordGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
                    } else {
                        generator = new IdentifierGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
                    }
                    break;
                }

            }
        } else if (isSymbol(codePoint)) {
            generator = new SymbolGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
        } else if (isDigit(codePoint)) {
            generator = new IntegerGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
        } else if (codePoint == '\"') {
            generator = new StringGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
        } else {
            generator = new BadTokenGenerator(this.currentAnalyzedSequence, this.buffer, context.start, context.leadingTriviaLength, context.trailingTriviaLength);
        }

        assert generator != null;

        context.end = generator.computeEnd();

        if ((generator instanceof IndentationGenerator) && (((IndentationGenerator) generator).getDifference() == 0)) {

        }
        else {
            context.start = context.end + 1;
            context.leadingTriviaLength = 0;
        }
        this.buffer.setLength(0);
        this.currentPosition = 0;

        if (isRune(currentAnalyzedSequence) || generator instanceof StringGenerator){
            this.currentAnalyzedSequence = "";
        } else {
            this.currentAnalyzedSequence = currentAnalyzedSequence.substring(generator.buffer.length());
        }

        return generator;
    }

    public ArrayList<TokenGenerator> scan(SequenceInfo sequenceInfo) {

        ArrayList<TokenGenerator> output = new ArrayList<>();
        ArrayList<String> splitedTrivialSequence = splitTrivial(sequenceInfo);

        for (int i = 0; i < splitedTrivialSequence.size(); i++) {

            if (sequenceInfo.lastSequence && (i == splitedTrivialSequence.size() - 1)) {
                endOfFile = true;
            }
            String sequence = splitedTrivialSequence.get(i);

            if (isTrivial(sequence.codePointAt(0)) && !hasIndent(sequence)) {
                context.leadingTriviaLength += sequence.length();
                continue;
            }

            this.currentAnalyzedSequence = sequence;
            this.currentPosition = 0;

            TokenGenerator generator = nextToken();

            while (generator != null) {
                if (generator instanceof IndentationGenerator) {
                    int differenceIndent = ((IndentationGenerator) generator).getDifference();
                    sequence = sequence.substring(generator.tokenLen());

                    if (differenceIndent == 0) {
                        context.start = generator.start;
                        context.leadingTriviaLength += generator.tokenLen();
                    } else {
                        IndentationGenerator initialGenerator = (IndentationGenerator) generator;
                        initialGenerator.setDifference(differenceIndent > 0 ? 1 : -1);
                        output.add(initialGenerator);

                        for (int j = 0; j < Math.abs(differenceIndent) - 1; j++) {
                            IndentationGenerator additionalGenerator = new IndentationGenerator(initialGenerator);
                            additionalGenerator.setDifference(differenceIndent > 0 ? 1 : -1);
                            additionalGenerator.trailingTrivialLen = 0;
                            additionalGenerator.leadingTrivialLen = 0;
                            additionalGenerator.start += initialGenerator.leadingTrivialLen;
                            additionalGenerator.buffer.setLength(initialGenerator.buffer.indexOf("\r") != 0 ? 1 : 2);
                            output.add(additionalGenerator);
                        }
                        this.currentAnalyzedSequence = sequence;
                    }
                    generator = nextToken();
                    continue;
                }



                output.add(generator);
                generator = nextToken();
            }
        }


        return output;

    }


}
