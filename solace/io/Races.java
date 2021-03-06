package solace.io;

import java.nio.file.Files;
import java.util.*;
import java.io.*;
import org.json.*;
import solace.game.Race;
import solace.util.Log;

/**
 * Utility class for loading and referencing races by name.
 * @author Ryan Sandor Richards
 */
public class Races {
  public static final Races instance = new Races();

  private final Hashtable<String, Race> races = new Hashtable<>();

  /**
   * @return The singleton instance of the races utility.
   */
  public static Races getInstance() { return instance; }

  /**
   * Initializes the skills helper by loading all the skills provided in the
   * game data directory.
   */
  public void reload() throws IOException {
    Log.info("Loading races");
    races.clear();
    GameFiles.findRaces().forEach(path -> {
      String name = path.getFileName().toString();
      Log.trace(String.format("Loading race '%s'", name));
      try {
        String contents = new String(Files.readAllBytes(path));
        Race race = Race.parseJSON(contents);
        races.put(race.getName(), race);
      } catch (JSONException je) {
        Log.error(String.format("Malformed json in race %s: %s", name, je.getMessage()));
      } catch (IOException ioe) {
        Log.error(String.format("Unable to load race: %s", name));
        ioe.printStackTrace();
      }
    });
  }

  /**
   * Determines if there is a race with the given name.
   * @param name Name of the race.
   * @return True if a race with the given name exists, false otherwise.
   */
  public boolean has(String name) {
    return races.containsKey(name);
  }

  /**
   * Gets the race with the given name.
   * @param name Name of the race.
   * @return The race with the given name.
   */
  public Race get(String name) {
    return races.get(name);
  }
}
