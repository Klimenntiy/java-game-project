package entities.MainCharacterTypes;

public class Warrior extends PlayerClass {
    public Warrior() {
        maxHealth = 15;
        attackDamage = 2;
        attackSpeed = 0.4f;
        attackRange = 20f;
    }

    @Override
    public String getClassName() {
        return "Warrior";
    }
}
