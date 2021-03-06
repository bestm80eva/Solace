package solace.cmd.core;

import solace.game.*;

/**
 * Buy command, allows the player to buy items from shops.
 * @author Ryan Sandor Richards
 */
public class ShopBuy extends ShopCommand {
  public ShopBuy() {
     super("buy");
  }

  public void command(solace.game.Character character, String[] params, Shop shop, Room room) {
    if (character.isFighting()) {
      character.sendln("You cannot buy items while in battle!");
      return;
    }

    if (params.length < 2) {
      character.wrapln("What would you like to buy?");
      return;
    }

    String name = params[1];
    ShopItem item;

    try {
      item = shop.getItemByIndex(Integer.parseInt(name));
    }
    catch (NumberFormatException e) {
      item = shop.getItemByName(name);
    }

    if (item == null) {
      character.wrapln(String.format(
        "This shop does not sell %s.", name
      ));
      return;
    }

    character.resetVisibilityOnAction("buy");

    Item sample = item.getSampleItem();
    if (sample == null) {
      // This is a weird scenario but possible at the time being since we don't
      // remove shop items that have invalid item ids.
      // TODO Fix shop items list in the shop class to get rid of this.
      character.wrapln(String.format(
        "The owner refuses to sell %s.", name
      ));
      return;
    }

    try {
      long price = shop.sellPrice(item);
      character.removeGold(price);
      character.addItem(item.getInstance());
      character.wrapln(String.format(
        "You buy %s for {y}%dg{x}.",
        sample.get("description.inventory"),
        price
      ));
    }
    catch (TemplateNotFoundException tnfe) {
      // This is a weird scenario but possible at the time being since we don't
      // remove shop items that have invalid item ids.
      // TODO Fix shop items list in the shop class to get rid of this.
      character.wrapln(String.format(
        "The owner refuses to sell %s.", name
      ));
    }
    catch (CurrencyException ce) {
      character.wrapln(String.format(
        "You do not have enough gold to buy %s.",
        sample.get("description.inventory")
      ));
    }
  }
}
