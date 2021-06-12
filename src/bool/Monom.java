package bool;

import java.util.Iterator;
import java.util.LinkedList;

public class Monom implements BooleanExpression {
    
    // Attribute
    private LinkedList<Atom> atoms;
    private String[] variables;
    // Quine & McCluskey
    private int negationCount;
    private LinkedList<String> usedVariables;
    
    // Konstruktor
    public Monom(String unparsedMonom, String[] variables){
        this.atoms = new LinkedList<>();
        this.variables = variables;
        // In Atome aufteilen
        String[] unparsedAtoms = unparsedMonom.split("\\*");
        // Jedes Atom parsen und abspeichern
        Atom currAtom;
        for (String atom : unparsedAtoms){
            currAtom = new Atom(atom.trim());
            this.atoms.add(currAtom);
        }
        // Normalisieren
        this.normalize();
    }
    
    public Monom(LinkedList<Atom> atoms, String[] variables){
        this.atoms = atoms;
        this.variables = variables;
        // Normalisieren
        this.normalize();
    }
    
    
    // Public API
    public BooleanExpression evalWithCofactor(String varName, boolean value){
        // Jedes Atome mit Kofaktor auswerten
        LinkedList<Atom> atomsWithCofactor = new LinkedList<>();
        // Alle Atome mit dem Kofaktor auswerten
        BooleanExpression current;
        for (Atom atom : this.atoms){
            // Das untersuchte Atome mit dem Kofaktor auswerten
            current = atom.evalWithCofactor(varName, value);
            if (current instanceof Constant && !current.getValue()){
                // Wenn das Atom eine Konstante 0 ist, ist das gesamte Monom 0
                return new Constant(false);
            } else if (!(current instanceof Constant)){
                // Wenn das Atom keine Konstante ist, dem neuen Monom übergeben
                atomsWithCofactor.add((Atom) current);
            }
            // Wenn das Atom eine Konstante 1 ist, fällt es weg
        }
        // Falls das Monom leer ist, ist es eine konstante 1
        if (atomsWithCofactor.size() == 0)
            return new Constant(true);
        // Andernfalls gib das verbleibende Monom zurück
        return new Monom(atomsWithCofactor, this.variables);
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
        if (this.atoms.size() != o2.atoms.size())
            return false;
        // Vergleich der Werte
        Iterator<Atom> atomsIter = this.atoms.iterator();
        Atom current;
        while (atomsIter.hasNext()){
            current = atomsIter.next();
            if(!o2.atoms.contains(current)) return false;
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
        LinkedList<Atom> sortedAtoms = new LinkedList<>();
        String currVar;
        Atom currAtom;
        // Durch die vorgegebenen Variablen iterieren
        for (int i = 0; i < this.variables.length; i++){
            currVar = this.variables[i];
            // Über Atome iterieren
            for(int j = 0; j < this.atoms.size(); j++){
                currAtom = this.atoms.get(j);
                // Falls das Atom den gesuchten Namen hat
                if(currVar.equals(currAtom.varName)){
                    // In der richtigen Ordnung an die neue Liste anhängen
                    sortedAtoms.add(currAtom);
                    // Aus der inneren Schleife ausbrechen
                    break;
                }
            }
        }
        // Die sortierte Liste einsetzen
        this.atoms = sortedAtoms;
    }
    
    private void evalNegationCount(){
        this.negationCount = 0;
        Atom currAtom;
        // Durch Atome iterieren
        for (int i = 0; i < this.atoms.size();i++){
            currAtom = this.atoms.get(i);
            // Falls das aktuelle Atom negiert ist, zählen
            if(currAtom.isNegated)
                this.negationCount++;
        }
    }

    private void evalUsedVariables(){
        this.usedVariables = new LinkedList<>();
        Atom currAtom;
        // Durch Atome iterieren
        for (int i = 0; i < this.atoms.size();i++){
            currAtom = this.atoms.get(i);
            // Variable des aktuellen Atoms auflisten
            this.usedVariables.add(currAtom.varName);
        }
    }

    public int size(){
        return this.atoms.size();
    }

    public Monom extendForMintermWithVar(String unusedVarName){
        // Kopieren
        Monom secondMonom = this.copy();
        // Gegensäzlich erweitern
        this.addAtom(new Atom(unusedVarName, true));
        secondMonom.addAtom(new Atom(unusedVarName, false));
        return secondMonom;
    }

    public boolean usesVariable(String varName){
        return this.usedVariables.contains(varName);
    }

    private void removeAtom(Atom a){
        // Entfernen
        this.atoms.remove(a);
        // Und wieder normalisieren
        this.normalize();
    }

    private void addAtom(Atom a){
        // Hinzufügen
        this.atoms.add(a);
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
        // Über Atome iterieren
        Atom currAtom1;
        Atom currAtom2;
        int differences = 0;
        for (int i = 0; i < this.atoms.size(); i++){
            // Setzt vorraus, das beide Monome normalisierte Ordnung haben und kompatibel sind
            currAtom1 = this.atoms.get(i);
            currAtom2 = m.atoms.get(i);
            // Falls sie sich unterscheiden
            if (currAtom1.isNegated != currAtom2.isNegated)
                differences++;
        }
        return differences == 1;
    }

    public Monom reduceWithQuineMcCluskey(Monom m){
        // Durch die Atome iterieren
        Atom currAtom1;
        Atom currAtom2;
        for (int i = 0; i < this.atoms.size(); i++){
            // Setzt vorraus, das beide Monome normalisierte Ordnung haben und kompatibel sind
            currAtom1 = this.atoms.get(i);
            currAtom2 = m.atoms.get(i);
            // Falls es nicht das gesuchte Atom ist
            if (currAtom1.isNegated == currAtom2.isNegated)
                continue;
            // Andernfalls
            Monom copy = this.copy();
            copy.removeAtom(currAtom1);
            return copy;
        }
        // Für den Compiler
        return this;
    }
    
    // Sonstige Optimierungen
    public boolean complements(Monom m){
        // Beide Monome müssen 1 Atom groß sein
        if(m.atoms.size() > 1 || this.atoms.size() > 1)
            return false;
        // Vergleich der Werte
        if(this.atoms.getFirst().isComplementOf(m.atoms.getFirst()))
            return true;
        return false;
    }


    @Override
    public String toString(){
        // Guard
        if (this.atoms.size() == 0) return "0";
        // Über Atome iterieren
        Iterator<Atom> atomsIter = this.atoms.iterator();
        String str = atomsIter.next().toString();
        while(atomsIter.hasNext()){
            str += "*" + atomsIter.next().toString();
        }
        // Rückgabe
        return str;
    }

}
