package entities.MainCharacterTypes;

public class Mage extends PlayerClass {
    public Mage() {
        maxHealth = 8;
        attackDamage = 3;
        attackSpeed = 0.6f;
        attackRange = 120f;
    }

    @Override
    public String getClassName() {
        return "Mage";
    }
}
