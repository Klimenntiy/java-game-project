package entities.MainCharacterTypes;

public class Archer extends PlayerClass {
    public Archer() {
        maxHealth = 10;
        attackDamage = 1;
        attackSpeed = 0.3f;
        attackRange = 100f;
    }

    @Override
    public String getClassName() {
        return "Archer";
    }
}
