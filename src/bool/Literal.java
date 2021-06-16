package bool;

public class Literal implements BooleanExpression {
    
    
    // Attribute
    public final String varName;
    public final boolean isNegated;
    
    
    // Konstruktor
    public Literal(String unparsedLiteral){
        // Literal aus der Textdarstellung parsen
        unparsedLiteral = unparsedLiteral.trim();
        if(unparsedLiteral.charAt(0) == '!'){
            this.isNegated = true;
            this.varName = unparsedLiteral.substring(1);
        } else {
            this.isNegated = false;
            this.varName = unparsedLiteral;
        }
    }
    
    public Literal(String varName, boolean isNegated){
        // Literal direkt erstellen
        this.varName = varName;
        this.isNegated = isNegated;
    }
    
    
    // Public API
    public BooleanExpression evalWithCofactor(String varName, boolean value){
        // Falls die hier beschriebene Variable nicht gemeint ist
        if (!(this.varName.equals(varName)))
            return this;
        // Andernfalls
        if(this.isNegated)
            return new Constant(!value);
        return new Constant(value);
    }
    
    public boolean getValue(){
        return false;
    }
    
    @Override
    public boolean equals(Object o){
        // Guards
        if(o == null) return false;
        if(!(o instanceof Literal)) return false;
        // Vergleich der Werte
        Literal o2 = (Literal) o;
        if(this.varName.equals(o2.varName) && this.isNegated == o2.isNegated) return true;
        return false;
    }

    public boolean isComplementOf(Literal a){
        // Beide Literale müssen dieselbe Variable haben
        if(!a.varName.equals(this.varName))
            return false;
        // Vergleich der Werte
        if(this.isNegated != a.isNegated)
            return true;
        return false;
    }

    @Override
    public String toString(){
        if(this.isNegated)
            return "!" + varName;
        return varName;
    }

    
}
