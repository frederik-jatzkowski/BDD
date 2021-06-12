package bool;

import java.util.Iterator;
import java.util.LinkedList;

public class Polynom implements BooleanExpression {
    
    // Attribute
    private LinkedList<Monom> monomes;
    private String[] variables;
    
    // Konstruktor
    public Polynom(String unparsedPolynom, String[] variables){
        this.monomes = new LinkedList<>();
        this.variables = variables;
        // In Monome aufteilen
        String[] unparsedMonomes = unparsedPolynom.split("\\+");
        // Jedes Monom parsen und abspeichern
        Monom currMonom;
        for (String monom : unparsedMonomes){
            currMonom = new Monom(monom.trim(), this.variables);
            this.monomes.add(currMonom);
        }
    }
    
    public Polynom(LinkedList<Monom> monomes, String[] variables){
        this.monomes = monomes;
        this.variables = variables;
    }
    
    
    // Public API
    
    public BooleanExpression evalWithCofactor(String varName, boolean value){
        // Jedes Monom mit Kofaktor auswerten
        LinkedList<Monom> monomesWithCofactor = new LinkedList<>();
        // Alle Monome mit dem Kofaktor auswerten
        BooleanExpression current;
        for (Monom monom : this.monomes){
            // Das untersuchte Monom mit dem Kofaktor auswerten
            current = monom.evalWithCofactor(varName, value);
            if (current instanceof Constant  && current.getValue()){
                // Wenn das Monom eine Konstante 1 ist, ist das gesamte Polynom 1
                return new Constant(true);
            } else if (!(current instanceof Constant)){
                // Wenn das Monom keine Konstante ist, dem neuen Polynom übergeben
                monomesWithCofactor.add((Monom) current);
            }
            // Wenn das Monom eine Konstante 0 ist, fällt es weg
        }
        // Falls das Polynom leer ist, ist es eine konstante 0
        if (monomesWithCofactor.size() == 0)
            return new Constant(false);
        // Andernfalls erstelle das verbleibende Polynom und normalisiere
        Polynom newPolynom = new Polynom(monomesWithCofactor, this.variables);
        newPolynom.normalizeWithQuineMcCluskey();
        // Wenn das Polynom ein Komplement enthält
        if(newPolynom.hasComplementingMonomes())
            return new Constant(true);
        // Ansonsten gib das vereinfachte Polynom zurück
        return newPolynom;
    }
    
    public boolean getValue(){
        return false;
    }
    
    @Override
    public boolean equals(Object o){
        // Guards
        if(o == null) return false;
        if(!(o instanceof Polynom)) return false;
        // Vergleich der Werte
        Polynom o2 = (Polynom) o;
        // Größen Vergleichen
        if (this.monomes.size() != o2.monomes.size())
            return false;
        // Monome vergleichen
        Iterator<Monom> monomesIter = this.monomes.iterator();
        Monom current;
        while (monomesIter.hasNext()){
            current = monomesIter.next();
            if(!o2.monomes.contains(current)) return false;
        }
        return true;
    }

    @Override
    public String toString(){
        // Guard
        if (this.monomes.size() == 0) return "0";
        // Über Monome iterieren
        Iterator<Monom> monomesIter = this.monomes.iterator();
        String str = monomesIter.next().toString();
        while(monomesIter.hasNext()){
            str += "+" + monomesIter.next().toString();
        }
        // Rückgabe
        return str;
    }

    // Implementierung von Quine&McCluskey
    private void toMintermPolynom(){
        /* Diese Methode erweitert das Polynom so,
        *  dass es dieselbe boolesche Funktion
        *  mit Mintermen darstellt (Terme mit allen gegebenen Variablen).
        */
        // Über die Variablen iterieren
        String currVar;
        Monom currMonom;
        for (int i = 0; i < this.variables.length; i++){
            currVar = this.variables[i];
            // Überprüfen, in welchen Monomen die Variable nicht enthalten ist
            for (int j = 0; j < this.monomes.size(); j++){
                currMonom = this.monomes.get(j);
                // Falls sie bereits benutzt wird
                if(currMonom.usesVariable(currVar))
                    continue;
                // Ansonsten erweitern
                this.monomes.add(currMonom.extendForMintermWithVar(currVar));
            }
        }

        // Nun gibt es nur noch Minterme
        // Es ist aber nicht ausgeschlossen, dass Monome doppelt vorkommen
        this.removeDuplicates();
    }
    
    private void removeDuplicates(){
        // Diese Funktion entfernt doppelt vorkommende Monome
        // Über alle Monome iterieren
        Monom outerMonom;
        Monom innerMonom;
        for(int i = 0; i < this.monomes.size(); i++){
            outerMonom = this.monomes.get(i);
            //Mit allen anderen Monomen vergleichen
            for(int j = i + 1; j < this.monomes.size(); j++){
                innerMonom = this.monomes.get(j);
                // Falls nicht äquivalent
                if(!outerMonom.equals(innerMonom))
                    continue;
                // Ansonsten entfernen
                this.monomes.remove(j);
                j--;
            }
        }
        // Nun sind keine Duplikate mehr enthalten
    }
    
    public void normalizeWithQuineMcCluskey(){
        // Diese Methode implementiert den Quine & McCluskey Algorithmus
        // Das Polynom zu einem Minterm-Polynom machen
        this.toMintermPolynom();
        LinkedList<Monom> prevLevelPartnered;
        LinkedList<Monom> currLevelUnpartnered;
        LinkedList<Monom> currLevelPartnered = this.monomes;
        // Über die Längen absteigend iterieren solange es auf der letzten Ebene noch Implikanten zum Vereinfachen gab
        LinkedList<Monom> prim = new LinkedList<Monom>();
        Monom outerMonom;
        Monom innerMonom;
        for(int i = this.variables.length; i >= 0 && currLevelPartnered.size() != 0; i--){
            // Die aktuelle Liste Prim anhängen und leeren
            // Weitergehen
            prevLevelPartnered = currLevelPartnered;
            // Verkehrungen für nächsten Durchgang
            currLevelUnpartnered = (LinkedList<Monom>) prevLevelPartnered.clone();
            currLevelPartnered = new LinkedList<Monom>();
            // Über alle Monome iterieren
            for (int j = 0; j < prevLevelPartnered.size(); j++){
                outerMonom = prevLevelPartnered.get(j);
                // Mit den übrigen vergleichen
                for(int k = j + 1; k < prevLevelPartnered.size(); k++){
                    innerMonom = prevLevelPartnered.get(k);
                    // Wenn die Monome nicht Quine & McCluskey-Kompatibel sind:
                    if(!outerMonom.isQuineMcCluskeyComplatibleTo(innerMonom))
                        continue;
                    // Ansonsten füge das reduzierte Monom zu den verkuppelten hinzu
                    currLevelPartnered.add(outerMonom.reduceWithQuineMcCluskey(innerMonom));
                    // und entferne beide aus den nicht verkuppelten
                    currLevelUnpartnered.remove(outerMonom);
                    currLevelUnpartnered.remove(innerMonom);
                }
            }
            // Füge nun die in der vorigen Ebene nicht verkuppelten der Menge prim hinzu
            prim.addAll(currLevelUnpartnered);
        }
        this.monomes = prim;
        // Nun besteht das Polynom nur aus Primimplikanten
        this.removeDuplicates();
    }

    // Methoden zur Vereinfachung

    public boolean hasComplementingMonomes(){
        Monom outerMonom;
        for(int i = 0; i < this.monomes.size(); i++){
            outerMonom = this.monomes.get(i);
            for(int j = 0; j < this.monomes.size(); j++){
                if(i != j && this.monomes.get(j).complements(outerMonom)){
                    return true;
                }
            }
        }
        return false;
    }


}
