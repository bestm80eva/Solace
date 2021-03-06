package solace.script;
import java.util.List;
import java.util.LinkedList;

import solace.cmd.Command;
import solace.cmd.InvalidTargetException;
import solace.cmd.CooldownCommand;
import solace.cmd.ResourceCost;
import solace.game.Player;

/**
 * Data model for scripted gameplay cooldowns (`CooldownCommand`).
 * @author Ryan Sandor Richards
 */
public class ScriptedCooldown extends AbstractScriptedCommand {
  /**
   * Functional interface for execution function.
   */
  @FunctionalInterface
  public interface CooldownExecuteFunction {
    public boolean execute(
      Player player,
      Player target,
      int level,
      CooldownCommand cooldown
    );
  }

  /**
   * Functional interface for the check valid target hook.
   */
  @FunctionalInterface
  public interface CooldownCheckValidTargetFunction {
    public void execute(Player target) throws Throwable;
  }

  private int cooldownDuration = 0;
  private boolean initiatesCombat = false;
  private int castTime;
  private String castMessage;
  private List<ResourceCost> resourceCosts = new LinkedList<ResourceCost>();
  private String combosWith;
  private int basePotency = 0;
  private int comboPotency = 0;
  private String savingThrow;
  private CooldownExecuteFunction executeLambda;
  private CooldownCheckValidTargetFunction checkValidTarget;

  /**
   * Creates a new scripted cooldown.
   * @param name Name of the cooldown.
   * @param displayName Display name of the cooldown.
   */
  public ScriptedCooldown(String name, String displayName) {
    super(name, displayName);
  }

  /**
   * @return Amount of time for the cooldown.
   */
  public int getCooldownDuration() {
    return cooldownDuration;
  }

  /**
   * Sets the coodown duration for the command.
   * @param d The duration to set.
   */
  public void setCooldownDuration(int d) {
    cooldownDuration = d;
  }

  /**
   * @return True if the command initiates combat, false otherwise.
   */
  public boolean getInitiatesCombat() {
    return initiatesCombat;
  }

  /**
   * Sets whether or not the cooldown initiates combat.
   * @param combat [description]
   */
  public void setInitiatesCombat(boolean combat) {
    initiatesCombat = combat;
  }

  /**
   * @return The cast time for the cooldown.
   */
  public int getCastTime() { return castTime; }

  /**
   * Sets the cast time for the cooldown.
   * @param ct Casting time in seconds.
   */
  public void setCastTime(int ct) { castTime = ct; }

  /**
   * @return The message to send when the player begins casting for this action.
   */
  public String getCastMessage() { return castMessage; }

  /**
   * Sets the casting message for this cooldown.
   * @param m Message to set.
   */
  public void setCastMessage(String m) { castMessage = m; }

  /**
   * @return The name of the cooldown with which this combos.
   */
  public String getCombosWith() { return combosWith; }

  /**
   * Sets the name of the cooldown off which this skill combos.
   * @param c The name of the combo cooldown.
   */
  public void setCombosWith(String c) { combosWith = c; }

  /**
  * @return The base attack potency for the cooldown.
  */
  public int getBasePotency() { return basePotency; }

  /**
   * Sets the nase attack potency for the cooldown.
   * @param i Potency to set.
   */
  public void setBasePotency(int i) { basePotency = i; }

  /**
  * @return The combo potency for the cooldown.
  */
  public int getComboPotency() { return comboPotency; }

  /**
   * Sets the combo potency for the cooldown.
   * @param i Potency to set.
   */
  public void setComboPotency(int i) { comboPotency = i; }

  /**
   * @return The name of the saving throw associated with the cooldown.
   */
  public String getSavingThrow() { return savingThrow; }

  /**
   * Sets the name of the saving throw for the cooldown.
   * @param s Name of the saving throw to set.
   */
  public void setSavingThrow(String s) { savingThrow = s; }

  /**
   * Adds a resource cost to this cooldown action.
   * @param c The cost to add.
   */
  public void addResourceCost(ResourceCost c) {
    resourceCosts.add(c);
  }

  /**
   * @return The execution lambda for the cooldown.
   */
  public CooldownExecuteFunction getExecuteLambda() {
    return executeLambda;
  }

  /**
   * Sets the execution lambda for the cooldown.
   * @param l The lamdba to set.
   */
  public void setExecuteLambda(CooldownExecuteFunction l) {
    executeLambda = l;
  }

  /**
   * Sets the "check valid target" hook handler for the cooldown.
   * @param l The handler.
   */
  public void setCheckValidTarget(CooldownCheckValidTargetFunction l) {
    checkValidTarget = l;
  }

  /**
   * Creates an instance of the play command for use by the game engine.
   * @param player Character for the play command.
   * @return The play command instance.
   */
  public Command getInstance() {
    CooldownCommand command = new CooldownCommand(getName(), getDisplayName()) {
      public void checkValidTarget(Player target)
        throws InvalidTargetException
      {
        super.checkValidTarget(target);
        if (checkValidTarget == null) {
          return;
        }
        try {
          checkValidTarget.execute(target);
        } catch (Throwable e) {
          throw new InvalidTargetException(e.getMessage());
        }
      }

      public boolean execute(Player player, Player target, int level) {
        return executeLambda.execute(player, target, level, this);
      }
    };
    command.setCooldownDuration(getCooldownDuration());
    command.setInitiatesCombat(getInitiatesCombat());
    command.setCastTime(getCastTime());
    command.setCastMessage(getCastMessage());
    command.setCombosWith(getCombosWith());
    command.setBasePotency(getBasePotency());
    command.setComboPotency(getComboPotency());
    command.setSavingThrow(getSavingThrow());
    resourceCosts.forEach((cost) -> command.addResourceCost(cost));
    return command;
  }
}
