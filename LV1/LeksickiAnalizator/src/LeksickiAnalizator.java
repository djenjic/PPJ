import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;



public class LeksickiAnalizator {
    //funkcija za provjeru je li string zapravo sadrzi broj
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static boolean isIdentificator(String str) {
        try{
            if(!(Character.isLetter(str.charAt(0)))) {
                return false;
            }}catch(StringIndexOutOfBoundsException e){
            return false;
        }

        if(str.length() == 1)
            return true;
        boolean prvi = true;
        char[] znakovi = str.toCharArray();
        for(char znak : znakovi){
            if(prvi) {
                prvi = false;
                continue;
            }
            if(!(Character.isLetter(znak) || Character.isDigit(znak)))
                return false;
        }
        return true;
    }


    public static void main(String[] args) {
        //prihvacanje i obradivanje cijelog inputa
        Scanner sc = new Scanner(System.in);
        String input;
        int line_num = 0;
        while(sc.hasNextLine()){
            input = sc.nextLine();
            line_num += 1;
            String[] split_input = input.split("( )|( {3})");
            for (String znak : split_input ){
                if (Objects.equals(znak, "//")) {
                    break;
                } else if (Objects.equals(znak, "=")){
                    System.out.println("OP_PRIDRUZI" + " " + line_num + " " + "=");
                } else if (Objects.equals(znak, "+")){
                    System.out.println("OP_PLUS" + " " + line_num + " " + "+");
                } else if (Objects.equals(znak, "-")){
                    System.out.println("OP_MINUS" + " " + line_num + " " + "-");
                } else if (Objects.equals(znak, "*")){
                    System.out.println("OP_PUTA" + " " + line_num + " " + "*");
                } else if (Objects.equals(znak, "/")){
                    System.out.println("OP_DIJELI" + " " + line_num + " " + "/");
                } else if (Objects.equals(znak, "(")){
                    System.out.println("L_ZAGRADA" + " " + line_num + " " + "(");
                } else if (Objects.equals(znak, ")")){
                    System.out.println("D_ZAGRADA" + " " + line_num + " " + ")");
                } else if (Objects.equals(znak, "za")){
                    System.out.println("KR_ZA" + " " + line_num + " " + "za");
                } else if (Objects.equals(znak, "od")){
                    System.out.println("KR_OD" + " " + line_num + " " + "od");
                } else if (Objects.equals(znak, "do")){
                    System.out.println("KR_DO" + " " + line_num + " " + "do");
                } else if (Objects.equals(znak, "az")){
                    System.out.println("KR_AZ" + " " + line_num + " " + "az");
                } else if (isNumeric(znak)){
                    System.out.println("BROJ" + " " + line_num + " " + znak);
                } else if (isIdentificator(znak.trim())){
                    System.out.println("IDN" + " " + line_num + " " + znak.trim());
                } else {
                    for(char znk : znak.toCharArray()){
                    if (znk == '='){
                        System.out.println("OP_PRIDRUZI" + " " + line_num + " " + "=");
                    } else if (Objects.equals(znk, '+')){
                        System.out.println("OP_PLUS" + " " + line_num + " " + "+");
                    } else if (Objects.equals(znk, '-')){
                        System.out.println("OP_MINUS" + " " + line_num + " " + "-");
                    } else if (Objects.equals(znk, '*')){
                        System.out.println("OP_PUTA" + " " + line_num + " " + "*");
                    } else if (Objects.equals(znk, '/')){
                        System.out.println("OP_DIJELI" + " " + line_num + " " + "/");
                    } else if (Objects.equals(znk, '(')){
                        System.out.println("L_ZAGRADA" + " " + line_num + " " + "(");
                    } else if (Objects.equals(znk, ')')){
                        System.out.println("D_ZAGRADA" + " " + line_num + " " + ")");
                    } else if (Character.isDigit(znk)){
                        System.out.println("BROJ" + " " + line_num + " " + znk);
                    } else if (Character.isLetter(znk)){
                        System.out.println("IDN" + " " + line_num + " " + znk);
                    }
                    }

                }


            }
        }

    }
}
