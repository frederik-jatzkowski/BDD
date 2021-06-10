package bool;

public class Constant implements BooleanExpression {
    
    
    // Attribute
    private boolean value;


    // Konstruktor
    public Constant(boolean value){
        this.value = value;
    }


    // Public API
    public BooleanExpression evalWithCofactor(String varName, boolean value){
        return this;
    }
    
    public boolean getValue(){
        return this.value;
    }
    
    @Override
    public boolean equals(Object o){
        // Guards
        if(o == null) return false;
        if(!(o instanceof Constant)) return false;
        // Vergleich der Werte
        Constant o2 = (Constant) o;
        if(this.value == o2.value) return true;
        return false;
    }

    @Override
    public String toString(){
        return this.value ? "1" : "0";
    }


}
