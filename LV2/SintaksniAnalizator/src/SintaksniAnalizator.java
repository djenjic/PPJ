import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Token {
    String type;
    String value;
    int line;

    public Token(String type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public String print(){
        return (this.type + " " + this.line + " " + this.value);

    }
}

class SyntaxAnalyzer {
    private List<Token> tokens;
    private int currentIndex;

    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentIndex = 0;
    }

    public void analyze() {
        try {
            parseProgram();
            System.out.println("Sintaksna analiza uspjesna!");
        } catch (Exception e) {
            System.out.println("Sintaksna greska: " + e.getMessage());
        }
    }

    private void match(String expectedType) throws Exception {
        if (currentIndex < tokens.size() && tokens.get(currentIndex).type.equals(expectedType)) {
            System.out.println(tokens.get(currentIndex).print());
            currentIndex++;

        } else {
            throw new Exception("Ocekivan token: " + expectedType + ", pronaden: " + tokens.get(currentIndex).type
                    + " (Linija: " + tokens.get(currentIndex).line + ")");
        }
    }

    private void parseProgram() throws Exception { //nepotrebna funkcija zapravo
        System.out.println("<program>");
        parseListNaredbi();
    }

    private void parseListNaredbi() throws Exception {
        while (currentIndex < tokens.size() && !tokens.get(currentIndex).type.equals("$")) {
            parseNaredba();
        }
        match("$");
    }

    private void parseNaredba() throws Exception {
        System.out.println("<lista_naredbi>");
        System.out.println("<naredba>");
        if (tokens.get(currentIndex).type.equals("IDN")) {
            parseNaredbaPridruzivanja();
        } else if (tokens.get(currentIndex).type.equals("KR_ZA")) {
            parseZaPetlja();
        } else {
            throw new Exception("Ocekivan token: IDN ili KR_ZA, pronaden: " + tokens.get(currentIndex).type
                    + " (Linija: " + tokens.get(currentIndex).line + ")");
        }
    }

    private void parseNaredbaPridruzivanja() throws Exception {
        System.out.println("<naredba_pridruzivanja>");
        match("IDN");
        match("OP_PRIDRUZI");
        parseE();
    }

    private void parseZaPetlja() throws Exception {
        System.out.println("<za_petlja>");
        match("KR_ZA");
        match("IDN");
        match("KR_OD");
        parseE();
        match("KR_DO");
        parseE();
        parseListNaredbi();
        match("KR_AZ");
    }

    private void parseE() throws Exception {
        System.out.println("<E>");
        parseT();
        System.out.println("<E_lista>");
        parseELista();
        System.out.println("$");

    }

    private void parseELista() throws Exception {
        if(currentIndex < tokens.size()) {
            if (tokens.get(currentIndex).type.equals("OP_PLUS")) {
                match("OP_PLUS");
                parseE();
            } else if (tokens.get(currentIndex).type.equals("OP_MINUS")) {
                match("OP_MINUS");
                parseE();
            }
        }
    }

    private void parseT() throws Exception {
        System.out.println("<T>");
        parseP();
        System.out.println("<T_lista>");
        parseTLista();
        System.out.println("$");
    }

    private void parseTLista() throws Exception {
        if(currentIndex < tokens.size()) {
            if (tokens.get(currentIndex).type.equals("OP_PUTA")) {
                match("OP_PUTA");
                parseT();
            } else if (tokens.get(currentIndex).type.equals("OP_DIJELI")) {
                match("OP_DIJELI");
                parseT();
            }
        }
    }

    private void parseP() throws Exception {
        System.out.println("<P>");
        if(currentIndex < tokens.size()) {
            switch (tokens.get(currentIndex).type) {
                case "OP_PLUS" -> {
                    match("OP_PLUS");
                    parseP();
                }
                case "OP_MINUS" -> {
                    match("OP_MINUS");
                    parseP();
                }
                case "L_ZAGRADA" -> {
                    match("L_ZAGRADA");
                    parseE();
                    match("D_ZAGRADA");
                }
                case "IDN", "BROJ" -> match(tokens.get(currentIndex).type);
                default -> throw new Exception("Neocekivan token: " + tokens.get(currentIndex).type
                        + " (Linija: " + tokens.get(currentIndex).line + ")");
            }
        }
    }
}

public class SintaksniAnalizator {
    public static void main(String[] args){
        List<Token> tokens = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            String[] parts = line.split("\\s+");
            String type = parts[0];
            String value = parts[2];
            int lineNum = Integer.parseInt(parts[1]);
            System.out.println(parts[0] + " " + parts[1] + " " + parts[2]);
            tokens.add(new Token(type, value, lineNum));
        }

        SyntaxAnalyzer SA = new SyntaxAnalyzer(tokens);
        SA.analyze();

    }
}
