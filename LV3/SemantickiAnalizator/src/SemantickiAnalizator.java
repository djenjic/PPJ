
import java.util.*;

class Token {
    private final String lnUsage;
    private final String lnDef;
    private final String value;

    public Token(String lnUsage, String lnDef, String value) {
        this.lnUsage = lnUsage;
        this.lnDef = lnDef;
        this.value = value;
    }

    public String getLnUsage() {
        return lnUsage;
    }

    public String getLnDef() {
        return lnDef;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return lnUsage + " " + lnDef + " " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lnUsage, lnDef, value);
    }
}

class Analyzer{
    private final List<Token> tokens = new ArrayList<>();
    private final Deque<Token> stack = new ArrayDeque<>();
    private final List<String> astStr;
    private int currentIndex = 0;

    public Analyzer(List<String> astStr) {
        this.astStr = astStr;
    }

    public List<Token> getTokens(){
        return tokens;
    }

    public void analyze(){
        while (currentIndex < astStr.size()) {
            String current = astStr.get(currentIndex);
            switch (current) {
                case "<naredba_pridruzivanja>":
                    assingmentOperation();
                    continue;
                case "<za_petlja>":
                    loopOperation();
                    continue;
                default:
                    if (current.startsWith("IDN")) {
                        addToken();
                    } else if (current.endsWith("az")) {
                        removeFromStack();
                    }
                    currentIndex++;
                    break;
            }
        }
    }

    private void assingmentOperation() {
        currentIndex++;
        String[] parts = astStr.get(currentIndex).split(" ");
        Token token = new Token(parts[1], parts[1], parts[2]);

        boolean tokenPresent = stack.contains(token);
        if (!tokenPresent) {
            stack.push(token);
        }
        currentIndex++;
    }

    private void loopOperation() {
        currentIndex++;
        stack.push(new Token(astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[2]));
        currentIndex++;
        stack.push(new Token(astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[2]));
        currentIndex++;
    }

    private void addToken(){
        String[] parts = astStr.get(currentIndex).split(" ");
        String idnValue = parts[2];
        String lnUsage = parts[1];
        String lnDef = findDefOnStack(idnValue);

        if (lnDef != null && !lnDef.equals(lnUsage)) {
            tokens.add(new Token(lnUsage, lnDef, idnValue));
        } else {
            tokens.add(new Token("err", lnUsage, idnValue));
        }
        currentIndex++;
    }

    private void removeFromStack() {
        while (!stack.isEmpty() && !"za".equals(stack.peek().getValue())) {
            stack.pop();
        }
        if (!stack.isEmpty()) {
            stack.pop();
        }
        currentIndex++;
    }

    private String findDefOnStack(String idnValue) {
        for (Token token : stack) {
            if (token.getValue().equals(idnValue)) {
                return token.getLnDef();
            }
        }
        return null;
    }
}

public class SemantickiAnalizator {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        List<String> data = new ArrayList<>();
        String line;
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            data.add(line.trim());
        }

        Analyzer analyzer = new Analyzer(data);
        analyzer.analyze();
        for (Token token : analyzer.getTokens()) {
            System.out.println(token);
            if (Objects.equals(token.getLnUsage(), "err"))
                break;
        }



    }
}
