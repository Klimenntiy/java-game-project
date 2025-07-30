package entities.MainCharacterTypes;

public abstract class PlayerClass {
    protected int maxHealth;
    protected int attackDamage;
    protected float attackSpeed;
    protected float attackRange;

    public int getMaxHealth() { return maxHealth; }
    public int getAttackDamage() { return attackDamage; }
    public float getAttackSpeed() { return attackSpeed; }

    public abstract String getClassName();
}
