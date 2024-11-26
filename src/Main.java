import lexer.MyLexer;
import syntax.MyParser;
import syspro.tm.Tasks;
import syspro.tm.WebServer;
import syspro.tm.lexer.*;
public class Main {

    public static void testLexer(){
        Lexer MyLexer = new MyLexer();

        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.Native));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.CarriageReturnLineFeed));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.Mixed));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.LineFeed));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().parallel(true));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().shuffled(true));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().repeated(true));
        //System.out.println();
        Tasks.Lexer.registerSolution(MyLexer, new TestMode().strict(true));
    }

    public static void testParser(){
        MyParser parser = new MyParser();
        WebServer.start();
        Tasks.Parser.registerSolution(parser);
        WebServer.waitForWebServerExit();
    }

    public static void main(String[] args) {

        //testLexer();
        testParser();



    }

}
