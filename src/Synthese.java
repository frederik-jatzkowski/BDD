import java.io.*;

import robdd.ROBDD;

public class Synthese {
    
    public static void main(String[] args) throws IOException{
        
        // Dateipfad aufnehmen
        String filePath;
        try{
            filePath = args[0];
        } catch (IndexOutOfBoundsException e){
            System.out.println("Kein Dateiname angegeben.");
            return;
        }
        // String filePath = "./test/data/aufgabe1.bool";

        // Datei öffnen
        BufferedReader input;
        try{
            input = new BufferedReader(new FileReader(filePath));
        } catch(FileNotFoundException e){
            System.out.println("Die angegebene Datei wurde nicht gefunden: " + filePath);
            return;
        }
        
        // Load array into memory
        String variableLine = input.readLine() + "<blatt";
        String expressionLine = input.readLine();
        input.close();

        // Make Array out of Variables;
        String[] variables = variableLine.split("<");

        ROBDD diagram = new ROBDD(variables, expressionLine);
        diagram.synthetize();
        System.out.println(diagram);
    }

}
