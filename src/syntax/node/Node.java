package syntax.node;

import java.util.ArrayList;
import java.util.List;
import syspro.tm.lexer.Token;
import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

public class Node implements SyntaxNode {
    private final AnySyntaxKind kind;
    private final Token token;
    private final List<SyntaxNode> children;

    public Node(SyntaxKind kind) {
        this(kind, null);
    }

    public Node(AnySyntaxKind kind, Token token) {
        this.kind = kind;
        this.token = token;
        this.children = new ArrayList<>();
    }

    public void addChild(SyntaxNode child) {
        children.add(child);
    }

    @Override
    public AnySyntaxKind kind() {
        return kind;
    }

    @Override
    public int slotCount() {
        return children.size();
    }

    @Override
    public SyntaxNode slot(int index) {
        if (index < 0 || index >= children.size()) {
            return null;
        }
        return children.get(index);
    }

    @Override
    public Token token() {
        return token;
    }


}
