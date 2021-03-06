package solace.cmd.admin;

import solace.cmd.CommandRegistry;
import solace.game.*;
import solace.game.Character;
import solace.io.*;
import solace.net.Connection;
import solace.script.ScriptingEngine;
import solace.cmd.CompositeCommand;
import solace.util.Log;

import java.util.Collection;
import java.util.Collections;

/**
 * Command for reloading game data while the engine is running (e.g areas,
 * scripts, help files, etc.).
 * @author Ryan Sandor Richards
 */
public class Reload extends CompositeCommand {
  public Reload() {
    super("reload");
    addSubCommand("scripts", this::scripts);
    addSubCommand("messages", this::messages);
    addSubCommand("help", this::help);
    addSubCommand("areas", this::areas);
    addSubCommand("emotes", this::emotes);
    addSubCommand("skills", this::skills);
    addSubCommand("races", this::races);
    addSubCommand("dreams", this::dreams);
    addSubCommand("damage-types", this::damageTypes);
  }

  @Override
  public boolean hasCommand(Player player) {
    return player.getAccount().isAdmin();
  }

  @Override
  protected void defaultCommand(Player player, String[] params) {
    player.sendln("Usage: reload (script|messages|help|areas|emotes|skills|races)");
  }

  /**
   * Reloads game scripts.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void scripts(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated script reload...", player.getName()));
    ScriptingEngine.reload();
    CommandRegistry.reload();
    player.sendln("Game scripts reloaded.");
  }

  /**
   * Reloads game messages.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void messages(Player player, String[] params) {
    try {
      Log.info(String.format("User '{m}%s{x}' initiated messages reload", player.getName()));
      Messages.reload();
      player.sendln("Game messages reloaded.");
    } catch (Throwable t) {
      player.sendln("Error encountered when reloading messages...");
      Log.error("Unable to reload game messages.");
      t.printStackTrace();
    }
  }

  /**
   * Reloads game help files.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void help(Player player, String[] params) {
    try {
      Log.info(String.format("User '{m}%s{x}' initiated help reload...", player.getName()));
      HelpSystem.getInstance().reload();
      player.sendln("Help articles reloaded.");
    } catch (Throwable t) {
      Log.error("Could not reload help system.");
      Log.error(t.getMessage());
    }
  }

  /**
   * Reloads game areas.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void areas(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated area reload...", player.getName()));
    Collection<Character> characters =
      Collections.synchronizedCollection(Game.getActiveCharacters());
    synchronized (characters) {
      try {
        // Freeze all the players (ignore their input)
        for (solace.game.Character ch : characters) {
          Connection con = ch.getConnection();
          con.sendln("\n{y}Game areas being reloaded, please stand by...{x}");
          con.setIgnoreInput(true);
        }

        // Reload all the areas
        Areas.getInstance().reload();
        Room defaultRoom = Areas.getInstance().getDefaultRoom();

        // Place players into their original rooms if available, or the
        // default room if not
        for (solace.game.Character ch : characters) {
          Connection con = ch.getConnection();

          if (!con.hasAccount())
            continue;

          Account act = con.getAccount();
          if (act == null) {
            Log.error("Null account encountered on area reload.");
            continue;
          }

          if (!act.hasActiveCharacter()) {
            Log.error(
              "Account without active character (" +
                act.getName().toLowerCase() +
                ") encountered on area reload."
            );
            continue;
          }

          Room room = ch.getRoom();

          if (room == null) {
            Log.error("Null room encountered on area reload.");
            ch.setRoom(defaultRoom);
          }
          else {
            Area area = room.getArea();
            if (area == null) {
              ch.setRoom(defaultRoom);
            }
            else {
              String roomId = room.getId();
              String areaId = area.getId();
              try {
                Area newArea = Areas.getInstance().get(areaId);
                Room newRoom = area.getRoom(roomId);
                ch.setRoom(newRoom);
              } catch (Throwable t) {
                Log.warn(String.format("reload areas - room for character '%s' not found.", ch.getName()));
                ch.setRoom(defaultRoom);
              }
            }
          }
        }

        player.sendln("Areas reloaded.");
      } finally {
        // Un-freeze the players and force them to take a look around :)
        for (solace.game.Character ch : characters) {
          Connection con = ch.getConnection();
          con.sendln("{y}Areas reloaded, thanks for your patience!{x}\n");
          con.setIgnoreInput(false);
          con.send(con.getStateController().getPrompt());
        }
      }
    }
  }

  /**
   * Reloads game emotes.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void emotes(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated emotes reload...", player.getName()));
    try {
      Emotes.getInstance().reload();
      player.sendln("Emotes articles reloaded.");
    } catch (Throwable t) {
      player.sendln("An error occurred when reloading emotes.");
    }
  }

  /**
   * Reloads game skills.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void skills(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated skills reload...", player.getName()));
    try {
      Skills.getInstance().reload();
      Game.getActiveCharacters().forEach(Character::resetSkills);
      player.sendln("Skills reloaded.");
    } catch (Throwable t) {
      player.sendln("An error occurred when reloading emotes.");
    }
  }

  /**
   * Reloads game races.
   * @param player Player initiating the reload.
   * @param params Original command parameters.
   */
  @SuppressWarnings("unused")
  private void races(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated races reload...", player.getName()));
    try {
      Races.getInstance().reload();
      Game.getActiveCharacters().forEach(Character::resetRace);
      player.sendln("Races reloaded.");
    } catch (Throwable t) {
      player.sendln("An error occurred when reloading emotes.");
    }
  }

  @SuppressWarnings("unused")
  private void dreams(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated dreams reload...", player.getName()));
    try {
      Dreams.getInstance().reload();
      player.sendln("Dreams reloaded.");
    } catch (Throwable t) {
      player.sendln("An error occurred when reloading dreams.");
      t.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  private void damageTypes(Player player, String[] params) {
    Log.info(String.format("User '{m}%s{x}' initiated damage type reload...", player.getName()));
    try {
      DamageTypes.getInstance().reload();
      player.sendln("Damage types reloaded.");
    } catch (Throwable t) {
      player.sendln("An error occurred when reloading dreams.");
      t.printStackTrace();
    }
  }
}
