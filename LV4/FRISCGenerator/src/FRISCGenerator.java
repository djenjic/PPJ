import java.io.*;
import java.util.*;
import java.nio.file.Files;

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
    private int tokenCounter = 0;
    private String variables = "\t\t; varijable";
    private String commands = "";
    private StringBuilder strBuilder = new StringBuilder();

    public Analyzer(List<String> astStr) {
        this.astStr = astStr;
    }

    public List<Token> getTokens(){
        return tokens;
    }

    public String getVariables(){
        return variables;
    }
    public String getCommands() {
        return commands;
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
        strBuilder.append("\n\n\t\t; \"vrati\" rez\n\t\tLOAD R6, (V").append(findDefOnStack("rez")).append(")").append("\n\t\tHALT");
        commands = strBuilder.toString();
    }

    private void assingmentOperation() {
        currentIndex++;
        String[] parts = astStr.get(currentIndex).split(" ");
        Token token = new Token(parts[1], parts[1], parts[2]);

        boolean tokenPresent = stack.contains(token);
        if (!tokenPresent) {
            stack.push(token);
            variables += "\nV" + parts[1] + "  DW 0 ; " + parts[2];
        }
        int varNum = Integer.parseInt(findDefOnStack(parts[2]));
        printAssignment(varNum, parts[2]);
        currentIndex++;
    }

    private void printAssignment(int varNum, String varName){
        boolean varSignNegative = false;
        boolean add = false;
        boolean sub = false;
        boolean mul = false;
        boolean div = false;
        boolean mulpending = false;
        boolean divpending = false;
        boolean addpending = false;
        boolean subpending = false;
        while(!Objects.equals(astStr.get(currentIndex + 1), "<lista_naredbi>") && currentIndex < astStr.size() - 1){
            System.out.print("\nNOVA ITERACIJA" + "INDEX + " + currentIndex);
            if(currentIndex < astStr.size() - 1)
                currentIndex++;
            System.out.print("\nDIGNUT INDEX NA " + currentIndex);
            if(Objects.equals(astStr.get(currentIndex), "<P>")){
                if(currentIndex < astStr.size() - 1)
                    currentIndex++;
                System.out.print("\nPROVJERA NEGATIVNOG INDEX NA " + currentIndex);
                if(Objects.equals(astStr.get(currentIndex).split(" ")[0], "OP_MINUS"))
                    varSignNegative = true;
            }
            if(Objects.equals(astStr.get(currentIndex).split(" ")[0], "BROJ") || Objects.equals(astStr.get(currentIndex).split(" ")[0], "IDN")) {
                String[] parts = astStr.get(currentIndex).split(" ");
                if(Objects.equals(parts[0], "BROJ")){
                    if(!varSignNegative){
                        strBuilder.append("\n\n\t\t; ").append(parts[2]);
                        strBuilder.append("\n\t\tMOVE %D ").append(parts[2]).append(", R0");
                        strBuilder.append("\n\t\tPUSH R0");
                        System.out.print("\nBROJ NAPISAN");
                    } else {
                        strBuilder.append("\n\n\t\t; -").append(parts[2]);
                        strBuilder.append("\n\t\tMOVE %D -").append(parts[2]).append(", R0");
                        strBuilder.append("\n\t\tPUSH R0");
                        varSignNegative = false;
                        System.out.print("\nNEGATIVNI BROJ NAPISAN");
                    }
                }
                if(Objects.equals(parts[0], "IDN")){
                    strBuilder.append("\n\n\t\t; ").append(parts[2]);
                    strBuilder.append("\n\t\tLOAD R0").append(", (V").append(findDefOnStack(parts[2])).append(")");
                    strBuilder.append("\n\t\tPUSH R0");
                    System.out.print("\nIDN NAPISAN" + findDefOnStack(parts[2]));
                }
                if(add || sub){
                    if(!mul && !div){
                        System.out.println("Uslo u add/sub");
                        if(Objects.equals(astStr.get(currentIndex + 2).split(" ")[0], "OP_PUTA")){
                            mulpending = true;
                            System.out.println("mulpending u add subu sad " + mulpending);
                            if(add){
                                addpending = true;
                            } else if (sub){
                                subpending = true;
                            }
                        }
                        if(Objects.equals(astStr.get(currentIndex + 2).split(" ")[0], "OP_DIJELI")){
                            divpending = true;
                            System.out.println("divpending u add subu sad " + divpending);
                            if(add){
                                addpending = true;
                            } else if (sub){
                                subpending = true;
                            }
                        }
                        if(!mulpending && !divpending){
                            if(add){
                                System.out.println("Radi add");
                                addOperation();
                                add = false;
                            } else if(sub){
                                System.out.println("Radi sub");
                                subOperation();
                                sub = false;
                            }
                        }
                    }
                }
                if(mul){
                    System.out.println("\nRadi mul i index je " + currentIndex);
                    mul = false;
                    mulOperation();
                    if(addpending){
                        addOperation();
                    } else if(subpending){
                        subOperation();
                    }
                } else if(div){
                    System.out.println("Radi div i index je " + currentIndex);
                    div = false;
                    divOperation();
                    if(addpending){
                        addOperation();
                    } else if(subpending){
                        subOperation();
                    }
                }

            }
            if(Objects.equals(astStr.get(currentIndex), "<T_lista>")){
                System.out.print("\nUSLO U T LISTU");
                if(currentIndex < astStr.size() - 1)
                    currentIndex++;
                System.out.print("\nDIGNUT INDEX NA " + currentIndex);
                String[] parts = astStr.get(currentIndex).split(" ");
                if(Objects.equals(parts[0], "OP_PUTA")){
                    mul = true;
                    mulpending = false;
                    System.out.print("\nmul sada -" + mul);
                }
                if(Objects.equals(parts[0], "OP_DIJELI")){
                    div = true;
                    divpending = false;
                    System.out.print("\nsub sada -" + sub);
                }
            }
            if(Objects.equals(astStr.get(currentIndex), "<E_lista>")){
                System.out.print("\nUSLO U E LISTU");
                if(currentIndex < astStr.size() - 1)
                    currentIndex++;
                System.out.print("\nDIGNUT INDEX NA " + currentIndex);
                String[] parts = astStr.get(currentIndex).split(" ");
                if(Objects.equals(parts[0], "OP_PLUS")) {
                    add = true;
                    System.out.print("\nadd sada -" + sub);
                }
                if(Objects.equals(parts[0], "OP_MINUS")){
                    sub = true;
                    System.out.print("\nsub sada -" + sub);
                }
            }
            System.out.print("\nMAKS VELICINA " + astStr.size());
        }
        strBuilder.append("\n\n\t\t;spremi pridruzivanje varijable ").append(varName).append("\n\t\tPOP R0").append("\n\t\tSTORE R0, (V").append(varNum).append(")");
        System.out.print("\nspremljena nova vrijednost varijable " + varName);
    }

    private void loopOperation() {
        boolean varSignNegative = false;
        StringBuilder loopString = new StringBuilder();
        currentIndex++;
        stack.push(new Token(astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[2]));
        currentIndex++;
        stack.push(new Token(astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[1], astStr.get(currentIndex).split(" ")[2]));
        String varName = astStr.get(currentIndex).split(" ")[2];
        String varNum = astStr.get(currentIndex).split(" ")[1];

        currentIndex++;
    }

    private void mulOperation(){
        strBuilder.append("\n\t\tCALL MUL");
    }

    private void divOperation(){
        strBuilder.append("\n\t\tCALL DIV");
    }

    private void addOperation(){
        strBuilder.append("\n\n\t\t;zbrajanje zadnja dva na stogu");
        strBuilder.append("\n\t\tPOP R1");
        strBuilder.append("\n\t\tPOP R0");
        strBuilder.append("\n\t\tADD R0, R1, R2");
        strBuilder.append("\n\t\tPUSH R2");
    }

    private void subOperation(){
        strBuilder.append("\n\n\t\t;oduzimanje zadnja dva na stogu");
        strBuilder.append("\n\t\tPOP R1");
        strBuilder.append("\n\t\tPOP R0");
        strBuilder.append("\n\t\tSUB R0, R1, R2");
        strBuilder.append("\n\t\tPUSH R2");
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

public class FRISCGenerator {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        List<String> data = new ArrayList<>();
        String line;
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            data.add(line.trim());
        }

        //for (String token : data)
        //    System.out.println(token);

        Analyzer analyzer = new Analyzer(data);
        analyzer.analyze();
        //for (Token token : analyzer.getTokens()) {
        //    System.out.println(token);
        //}
        PrintWriter writer = new PrintWriter(Files.newOutputStream(new File("a.frisc").toPath()));
        writer.print("\t\t; init stog\n\t\tMOVE %D 40000, r7");
        writer.print(analyzer.getCommands());
        writer.print("\nMD_SGN  MOVE 0, R6\n\t\tXOR R0, 0, R0\n\t\tJP_P MD_TST1\n\t\tXOR R0, -1, R0\n\t\tADD R0, 1, R0\n\t\tMOVE 1, R6\nMD_TST1 XOR R1, 0, R1\n\t\tJP_P MD_SGNR\n\t\tXOR R1, -1, R1\n\t\tADD R1, 1, R1\n\t\tXOR R6, 1, R6\nMD_SGNR RET\n\nMD_INIT POP R4 ; MD_INIT ret addr\n\t\tPOP R3 ; M/D ret addr\n\t\tPOP R1 ; op2\n\t\tPOP R0 ; op1\n\t\tCALL MD_SGN\n\t\tMOVE 0, R2 ; init rezultata\n\t\tPUSH R4 ; MD_INIT ret addr\n\t\tRET\n\nMD_RET  XOR R6, 0, R6 ; predznak?\n\t\tJP_Z MD_RET1\n\t\tXOR R2, -1, R2 ; promijeni predznak\n\t\tADD R2, 1, R2\nMD_RET1 POP R4 ; MD_RET ret addr\n\t\tPUSH R2 ; rezultat\n\t\tPUSH R3 ; M/D ret addr\n\t\tPUSH R4 ; MD_RET ret addr\n\t\tRET\n\nMUL     CALL MD_INIT\n\t\tXOR R1, 0, R1\n\t\tJP_Z MUL_RET ; op2 == 0\n\t\tSUB R1, 1, R1\nMUL_1   ADD R2, R0, R2\n\t\tSUB R1, 1, R1\n\t\tJP_NN MUL_1 ; >= 0?\nMUL_RET CALL MD_RET\n\t\tRET\n\nDIV     CALL MD_INIT\n\t\tXOR R1, 0, R1\n\t\tJP_Z DIV_RET ; op2 == 0\nDIV_1   ADD R2, 1, R2\n\t\tSUB R0, R1, R0\n\t\tJP_NN DIV_1\n\t\tSUB R2, 1, R2\nDIV_RET CALL MD_RET\n\t\tRET\n");
        writer.print(analyzer.getVariables());
        writer.close();
        System.out.println("\n\n\t\t; init stog\n\t\tMOVE %D 40000, r7");
        System.out.print(analyzer.getCommands());
        System.out.print("\n\nMD_SGN  MOVE 0, R6\n\t\tXOR R0, 0, R0\n\t\tJP_P MD_TST1\n\t\tXOR R0, -1, R0\n\t\tADD R0, 1, R0\n\t\tMOVE 1, R6\nMD_TST1 XOR R1, 0, R1\n\t\tJP_P MD_SGNR\n\t\tXOR R1, -1, R1\n\t\tADD R1, 1, R1\n\t\tXOR R6, 1, R6\nMD_SGNR RET\n\nMD_INIT POP R4 ; MD_INIT ret addr\n\t\tPOP R3 ; M/D ret addr\n\t\tPOP R1 ; op2\n\t\tPOP R0 ; op1\n\t\tCALL MD_SGN\n\t\tMOVE 0, R2 ; init rezultata\n\t\tPUSH R4 ; MD_INIT ret addr\n\t\tRET\n\nMD_RET  XOR R6, 0, R6 ; predznak?\n\t\tJP_Z MD_RET1\n\t\tXOR R2, -1, R2 ; promijeni predznak\n\t\tADD R2, 1, R2\nMD_RET1 POP R4 ; MD_RET ret addr\n\t\tPUSH R2 ; rezultat\n\t\tPUSH R3 ; M/D ret addr\n\t\tPUSH R4 ; MD_RET ret addr\n\t\tRET\n\nMUL     CALL MD_INIT\n\t\tXOR R1, 0, R1\n\t\tJP_Z MUL_RET ; op2 == 0\n\t\tSUB R1, 1, R1\nMUL_1   ADD R2, R0, R2\n\t\tSUB R1, 1, R1\n\t\tJP_NN MUL_1 ; >= 0?\nMUL_RET CALL MD_RET\n\t\tRET\n\nDIV     CALL MD_INIT\n\t\tXOR R1, 0, R1\n\t\tJP_Z DIV_RET ; op2 == 0\nDIV_1   ADD R2, 1, R2\n\t\tSUB R0, R1, R0\n\t\tJP_NN DIV_1\n\t\tSUB R2, 1, R2\nDIV_RET CALL MD_RET\n\t\tRET\n\n");

        System.out.print(analyzer.getVariables());


        //writer.print
        //writer.print("\nMD_SGN  MOVE 0, R6\n\t\tXOR R0, 0, R0\n\t\tJP_P MD_TST1\n\t\tXOR R0, -1, R0\n\t\tADD R0, 1, R0\n\t\tMOVE 1, R6\nMD_TST1 XOR R1, 0, R1\n\t\tJP_P MD_SGNR\n\t\tXOR R1, -1, R1\n\t\tADD R1, 1, R1\n\t\tXOR R6, 1, R6\nMD_SGNR RET\n\nMD_INIT POP R4 ; MD_INIT ret addr\n\t\tPOP R3 ; M/D ret addr\n\t\tPOP R1 ; op2\n\t\tPOP R0 ; op1\n\t\tCALL MD_SGN\n\t\tMOVE 0, R2 ; init rezultata\n\t\tPUSH R4 ; MD_INIT ret addr\n\t\tRET\n\nMD_RET  XOR R6, 0, R6 ; predznak?\n\t\tJP_Z MD_RET1\n\t\tXOR R2, -1, R2 ; promijeni predznak\n\t\tADD R2, 1, R2\nMD_RET1 POP R4 ; MD_RET ret addr\n\t\tPUSH R2 ; rezultat\n\t\tPUSH R3 ; M/D ret addr\n\t\tPUSH R4 ; MD_RET ret addr\n\t\tRET\n\nMUL     CALL MD_INIT\n\t\tXOR R1, 0, R1\n\t\tJP_Z MUL_RET ; op2 == 0\n\t\tSUB R1, 1, R1\nMUL_1   ADD R2, R0, R2\n\t\tSUB R1, 1, R1\n\t\tJP_NN MUL_1 ; >= 0?\nMUL_RET CALL MD_RET\n\t\tRET\n\nDIV     CALL MD_INIT\n\t\tXOR R1, 0, R1\n\t\tJP_Z DIV_RET ; op2 == 0\nDIV_1   ADD R2, 1, R2\n\t\tSUB R0, R1, R0\n\t\tJP_NN DIV_1\n\t\tSUB R2, 1, R2\nDIV_RET CALL MD_RET\n\t\tRET\n");

        //tu varijable zapisat





    }
}
//writer.print("MD_SGN  MOVE 0, R6\n" +
//                "\t\tXOR R0, 0, R0\n" +
//                "\t\tJP_P MD_TST1\n" +
//                "\t\tXOR R0, -1, R0\n" +
//                "\t\tADD R0, 1, R0\n" +
//                "\t\tMOVE 1, R6\n" +
//                "MD_TST1 XOR R1, 0, R1\n" +
//                "\t\tJP_P MD_SGNR\n" +
//                "\t\tXOR R1, -1, R1\n" +
//                "\t\tADD R1, 1, R1\n" +
//                "\t\tXOR R6, 1, R6\n" +
//                "MD_SGNR RET\n\n" +
//                "MD_INIT POP R4 ; MD_INIT ret addr\n" +
//                "\t\tPOP R3 ; M/D ret addr\n" +
//                "\t\tPOP R1 ; op2\n" +
//                "\t\tPOP R0 ; op1\n" +
//                "\t\tCALL MD_SGN\n" +
//                "\t\tMOVE 0, R2 ; init rezultata\n" +
//                "\t\tPUSH R4 ; MD_INIT ret addr\n" +
//                "\t\tRET\n\n" +
//                "MD_RET  XOR R6, 0, R6 ; predznak?\n" +
//                "\t\tJP_Z MD_RET1\n" +
//                "\t\tXOR R2, -1, R2 ; promijeni predznak\n" +
//                "\t\tADD R2, 1, R2\n" +
//                "MD_RET1 POP R4 ; MD_RET ret addr\n" +
//                "\t\tPUSH R2 ; rezultat\n" +
//                "\t\tPUSH R3 ; M/D ret addr\n" +
//                "\t\tPUSH R4 ; MD_RET ret addr\n" +
//                "\t\tRET\n\n" +
//                "MUL     CALL MD_INIT\n" +
//                "\t\tXOR R1, 0, R1\n" +
//                "\t\tJP_Z MUL_RET ; op2 == 0\n" +
//                "\t\tSUB R1, 1, R1\n" +
//                "MUL_1   ADD R2, R0, R2\n" +
//                "\t\tSUB R1, 1, R1\n" +
//                "\t\tJP_NN MUL_1 ; >= 0?\n" +
//                "MUL_RET CALL MD_RET\n" +
//                "\t\tRET\n\n" +
//                "DIV     CALL MD_INIT\n" +
//                "\t\tXOR R1, 0, R1\n" +
//                "\t\tJP_Z DIV_RET ; op2 == 0\n" +
//                "DIV_1   ADD R2, 1, R2\n" +
//                "\t\tSUB R0, R1, R0\n" +
//                "\t\tJP_NN DIV_1\n" +
//                "\t\tSUB R2, 1, R2\n" +
//                "DIV_RET CALL MD_RET\n" +
//                "\t\tRET\n");