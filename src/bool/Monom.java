package bool;

import java.util.Iterator;
import java.util.LinkedList;

public class Monom implements BooleanExpression {
    
    // Attribute
    private LinkedList<Atom> atoms;
    
    
    // Konstruktor
    public Monom(String unparsedMonom){
        this.atoms = new LinkedList<>();
        // In Atome aufteilen
        String[] unparsedAtoms = unparsedMonom.split("\\*");
        // Jedes Atom parsen und abspeichern
        for (String atom : unparsedAtoms){
            this.atoms.add(new Atom(atom.trim()));
        }
    }
    
    public Monom(LinkedList<Atom> atoms){
        this.atoms = atoms;
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
        return new Monom(atomsWithCofactor);
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

    public boolean canQuine(Monom m){
        // Größen Vergleichen
        if (this.atoms.size() != m.atoms.size())
            return false;
        // Vergleich der Werte
        Iterator<Atom> atomsIter = this.atoms.iterator();
        Atom current;
        int differences = 0;
        while (atomsIter.hasNext()){
            current = atomsIter.next();
            if(!m.atoms.contains(current)) differences++;
        }
        return differences == 1 && differences != this.atoms.size();
    }
    
    public void useQuine(Monom m){
        // Größen Vergleichen
        if (this.atoms.size() != m.atoms.size() || this.atoms.size() == 1)
            return;
        // Vergleich der Werte
        Iterator<Atom> atomsIter = this.atoms.iterator();
        Atom current;
        LinkedList<Atom> differences = new LinkedList<>();
        while (atomsIter.hasNext()){
            current = atomsIter.next();
            if(!m.atoms.contains(current)){
                differences.add(current);
            }
        }
        if(differences.size() == 1){
            this.atoms.remove(differences.getFirst());
        }
    }

    public boolean includes(Monom m){
        // Vergleich der Werte
        Iterator<Atom> atomsIter = m.atoms.iterator();
        Atom current;
        while (atomsIter.hasNext()){
            current = atomsIter.next();
            if(!this.atoms.contains(current)) return false;
        }
        return true;
    }
    
    public boolean isComplementOf(Monom m){
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
