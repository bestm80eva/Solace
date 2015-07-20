package solace.game;

import java.util.*;
import solace.util.*;

/**
 * Manages battles between players and mobiles.
 * @author Ryan Sandor Richards
 */
public class BattleManager {
  static Set<Battle> battles = Collections.synchronizedSet(
    new HashSet<Battle>()
  );
  static Clock.Event roundEvent = null;

  /**
   * Initializes and starts the battle manager.
   */
  public static void start() {
    if (roundEvent != null) { return; }
    Log.info("Starting battle manager");
    roundEvent = Clock.getInstance().interval("battle-round", 2, new Runnable() {
      public void run() { BattleManager.round(); }
    });
  }

  /**
   * Performs a round for each battle being managed.
   */
  protected static void round() {
    Log.trace("Battle Round");
    synchronized(battles) {
      Iterator<Battle> iterator = battles.iterator();
      while (iterator.hasNext()) {
        Battle b = iterator.next();
        Log.trace("Battle round for battle: " + b);
        b.round();
        if (b.isOver()) {
          iterator.remove();
          cleanup(b);
        }
      }
    }
  }

  /**
   * Cleans up a battle once it is done.
   */
  protected static void cleanup(Battle b) {
    for (Player p : b.getParticipants()) {
      p.setPlayState(PlayState.STANDING);
    }
  }

  /**
   * Stops the battle manager.
   */
  public static void stop() {
    if (roundEvent == null) { return; }
    Log.info("Stopping battle manager");
    roundEvent.cancel();
    roundEvent = null;
  }

  /**
   * Initiates a battle between two plaers.
   * @param attacker The player who attacked.
   * @param target The target of the attack.
   */
  public static void initiate(Player attacker, Player target) {
    Log.trace(String.format(
      "Initiating battle between %s and %s.",
      attacker.getName(),
      target.getName()
    ));

    attacker.setPlayState(PlayState.FIGHTING);
    target.setPlayState(PlayState.FIGHTING);

    Battle battle = new Battle();
    battle.add(attacker);
    battle.add(target);
    battle.setAttacking(attacker, target);

    // Attacker always gets the first shots
    battle.round();
    if (battle.isOver()) {
      cleanup(battle);
      return;
    }

    battle.setAttacking(target, attacker);

    synchronized(battles) {
      battles.add(battle);
    }
  }


}