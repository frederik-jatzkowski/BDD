package bool;

import java.util.Iterator;
import java.util.LinkedList;

public class Polynom implements BooleanExpression {
    

    // Attribute
    private LinkedList<Monom> monomes;
    

    // Konstruktor
    public Polynom(String unparsedPolynom){
        this.monomes = new LinkedList<>();
        // In Monome aufteilen
        String[] unparsedMonomes = unparsedPolynom.split("\\+");
        // Jedes Monom parsen und abspeichern
        for (String monom : unparsedMonomes){
            this.monomes.add(new Monom(monom.trim()));
        }
    }
    
    public Polynom(LinkedList<Monom> monomes){
        this.monomes = monomes;
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
        // Andernfalls erstelle das verbleibende, vereinfachte Polynom
        Polynom newPolynom = new Polynom(monomesWithCofactor);
        newPolynom.simplyfy();
        // Wenn das vereinfachte Polynom ein Komplement enthält
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


    // Methoden zur Vereinfachung
    public void simplyfy(){
        // Das Absorptionsgesetz anwenden
        //this.useAbsorption();
        // Das Verfahren von Quine anwenden
        this.useQuine();
    }

    private void useAbsorption(){
        Monom outerMonom;
        Monom innerMonom;
        // Mit äußerer Schleife durch alle Monome iterieren
        for(int i = 0; i < this.monomes.size(); i++){
            outerMonom = this.monomes.get(i);
            // Mit innerer Schleife durch alle Monome iterieren
            for(int j = 0; j < this.monomes.size(); j++){
                innerMonom = this.monomes.get(j);
                // das äußere Monom mit allen inneren Monomen vergleichen
                if(i != j && innerMonom.includes(outerMonom)){
                    // Falls ein Monom komplett in einem zweite Monom enthalten ist, das zweite entfernen
                    this.monomes.remove(j);
                    // Der Entfernung in den Indizes Rechnung tragen
                    j--;
                    i--;
                }
            }
        }
    }

    public boolean hasComplementingMonomes(){
        Monom outerMonom;
        for(int i = 0; i < this.monomes.size(); i++){
            outerMonom = this.monomes.get(i);
            for(int j = 0; j < this.monomes.size(); j++){
                if(i != j && this.monomes.get(j).isComplementOf(outerMonom)){
                    return true;
                }
            }
        }
        return false;
    }

    private void useQuine(){
        int matches = 1;
        Monom outerMonom;
        Monom innerMonom;
        // Solange in der Schleife bleiben, bis es keine Matches für Quine mehr gibt
        while(matches != 0){
            // Matches auf 0 zurücksetzen
            matches = 0;
            // Mit äußerer Schleife durch alle Monome iterieren
            for(int i = 0; i < this.monomes.size(); i++){
                outerMonom = this.monomes.get(i);
                for(int j = 0; j < this.monomes.size(); j++){
                    innerMonom = this.monomes.get(j);
                    // das äußere Monom mit allen inneren Monomen vergleichen
                    if(i != j && outerMonom.canQuine(innerMonom)){
                        // Falls Quine angewendet werden kann, reduzieren
                        outerMonom.useQuine(innerMonom);
                        // Und das Redundante entfernen
                        this.monomes.remove(j);
                        // Der Entfernung in den Indizes Rechnung tragen
                        j--;
                        i--;
                        // Match mitzählen
                        matches++;
                    }
                }
            }
            //this.useAbsorption();
        }
    }
}
