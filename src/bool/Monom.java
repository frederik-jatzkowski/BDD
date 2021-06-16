package bool;

import java.util.Iterator;
import java.util.LinkedList;

public class Monom implements BooleanExpression {
    
    // Attribute
    private LinkedList<Literal> literals;
    private String[] variables;
    // Quine & McCluskey
    private int negationCount;
    private LinkedList<String> usedVariables;
    
    // Konstruktor
    public Monom(String unparsedMonom, String[] variables){
        this.literals = new LinkedList<>();
        this.variables = variables;
        // In Literale aufteilen
        String[] unparsedLiterals = unparsedMonom.split("\\*");
        // Jedes Literal parsen und abspeichern
        Literal currLiteral;
        for (String literal : unparsedLiterals){
            currLiteral = new Literal(literal.trim());
            this.literals.add(currLiteral);
        }
        // Normalisieren
        this.normalize();
    }
    
    public Monom(LinkedList<Literal> literals, String[] variables){
        this.literals = literals;
        this.variables = variables;
        // Normalisieren
        this.normalize();
    }
    
    
    // Public API
    public BooleanExpression evalWithCofactor(String varName, boolean value){
        // Jedes Literale mit Kofaktor auswerten
        LinkedList<Literal> literalsWithCofactor = new LinkedList<>();
        // Alle Literale mit dem Kofaktor auswerten
        BooleanExpression current;
        for (Literal literal : this.literals){
            // Das untersuchte Literale mit dem Kofaktor auswerten
            current = literal.evalWithCofactor(varName, value);
            if (current instanceof Constant && !current.getValue()){
                // Wenn das Literal eine Konstante 0 ist, ist das gesamte Monom 0
                return new Constant(false);
            } else if (!(current instanceof Constant)){
                // Wenn das Literal keine Konstante ist, dem neuen Monom übergeben
                literalsWithCofactor.add((Literal) current);
            }
            // Wenn das Literal eine Konstante 1 ist, fällt es weg
        }
        // Falls das Monom leer ist, ist es eine konstante 1
        if (literalsWithCofactor.size() == 0)
            return new Constant(true);
        // Andernfalls gib das verbleibende Monom zurück
        return new Monom(literalsWithCofactor, this.variables);
    }
    
    public boolean getValue(){
        return false;
    }
    
    @Override
    public boolean equals(Object o){
        // Guards
        if(o == null) return false;
        if(!(o instanceof Monom)) return false;
        Monom o2 = (Monom) o;
        // Größen Vergleichen
        if (this.literals.size() != o2.literals.size())
            return false;
        // Vergleich der Werte
        Iterator<Literal> literalsIter = this.literals.iterator();
        Literal current;
        while (literalsIter.hasNext()){
            current = literalsIter.next();
            if(!o2.literals.contains(current)) return false;
        }
        return true;
    }

    // Implementierung von Quine & McCluskey
    public void normalize(){
        this.sortVariables();
        this.evalNegationCount();
        this.evalUsedVariables();
    }

    private void sortVariables(){
        // Variablen deklarieren
        LinkedList<Literal> sortedLiterals = new LinkedList<>();
        String currVar;
        Literal currLiteral;
        // Durch die vorgegebenen Variablen iterieren
        for (int i = 0; i < this.variables.length; i++){
            currVar = this.variables[i];
            // Über Literale iterieren
            for(int j = 0; j < this.literals.size(); j++){
                currLiteral = this.literals.get(j);
                // Falls das Literal den gesuchten Namen hat
                if(currVar.equals(currLiteral.varName)){
                    // In der richtigen Ordnung an die neue Liste anhängen
                    sortedLiterals.add(currLiteral);
                    // Aus der inneren Schleife ausbrechen
                    break;
                }
            }
        }
        // Die sortierte Liste einsetzen
        this.literals = sortedLiterals;
    }
    
    private void evalNegationCount(){
        this.negationCount = 0;
        Literal currLiteral;
        // Durch Literale iterieren
        for (int i = 0; i < this.literals.size();i++){
            currLiteral = this.literals.get(i);
            // Falls das aktuelle Literal negiert ist, zählen
            if(currLiteral.isNegated)
                this.negationCount++;
        }
    }

    private void evalUsedVariables(){
        this.usedVariables = new LinkedList<>();
        Literal currLiteral;
        // Durch Literale iterieren
        for (int i = 0; i < this.literals.size();i++){
            currLiteral = this.literals.get(i);
            // Variable des aktuellen Literals auflisten
            this.usedVariables.add(currLiteral.varName);
        }
    }

    public int size(){
        return this.literals.size();
    }

    public Monom extendForMintermWithVar(String unusedVarName){
        // Kopieren
        Monom secondMonom = this.copy();
        // Gegensäzlich erweitern
        this.addLiteral(new Literal(unusedVarName, true));
        secondMonom.addLiteral(new Literal(unusedVarName, false));
        return secondMonom;
    }

    public boolean usesVariable(String varName){
        return this.usedVariables.contains(varName);
    }

    private void removeLiteral(Literal a){
        // Entfernen
        this.literals.remove(a);
        // Und wieder normalisieren
        this.normalize();
    }

    private void addLiteral(Literal a){
        // Hinzufügen
        this.literals.add(a);
        // Und wieder normalisieren
        this.normalize();
    }

    private Monom copy(){
        return new Monom(this.toString(), this.variables);
    }

    public boolean isQuineMcCluskeyComplatibleTo(Monom m){
        // Wenn die Anzahl an Negationen sich nicht um 1 unterscheidet
        if(Math.abs(this.negationCount - m.negationCount) != 1)
            return false;
        // Wenn nicht dieselben Variablen verwendet werden
        if(!this.usedVariables.equals(m.usedVariables))
            return false;
        // Andernfalls sind die beiden Monome potenziell kompatibel
        // Es bleibt festzustellen, dass es nur eine unterschiedliche Variable gibt
        // Über Literale iterieren
        Literal currLiteral1;
        Literal currLiteral2;
        int differences = 0;
        for (int i = 0; i < this.literals.size(); i++){
            // Setzt vorraus, das beide Monome normalisierte Ordnung haben und kompatibel sind
            currLiteral1 = this.literals.get(i);
            currLiteral2 = m.literals.get(i);
            // Falls sie sich unterscheiden
            if (currLiteral1.isNegated != currLiteral2.isNegated)
                differences++;
        }
        return differences == 1;
    }

    public Monom reduceWithQuineMcCluskey(Monom m){
        // Durch die Literale iterieren
        Literal currLiteral1;
        Literal currLiteral2;
        for (int i = 0; i < this.literals.size(); i++){
            // Setzt vorraus, das beide Monome normalisierte Ordnung haben und kompatibel sind
            currLiteral1 = this.literals.get(i);
            currLiteral2 = m.literals.get(i);
            // Falls es nicht das gesuchte Literal ist
            if (currLiteral1.isNegated == currLiteral2.isNegated)
                continue;
            // Andernfalls
            Monom copy = this.copy();
            copy.removeLiteral(currLiteral1);
            return copy;
        }
        // Für den Compiler
        return this;
    }
    
    // Sonstige Optimierungen
    public boolean complements(Monom m){
        // Beide Monome müssen 1 Literal groß sein
        if(m.literals.size() > 1 || this.literals.size() > 1)
            return false;
        // Vergleich der Werte
        if(this.literals.getFirst().isComplementOf(m.literals.getFirst()))
            return true;
        return false;
    }


    @Override
    public String toString(){
        // Guard
        if (this.literals.size() == 0) return "0";
        // Über Literale iterieren
        Iterator<Literal> literalsIter = this.literals.iterator();
        String str = literalsIter.next().toString();
        while(literalsIter.hasNext()){
            str += "*" + literalsIter.next().toString();
        }
        // Rückgabe
        return str;
    }

}
