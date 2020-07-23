# GuiAPI
### This API is tested for 1.15.2 ! Make sure to set your API Version in the plugin.yml !!
### Anvil Window might not work in other versions of the usage of NMS !!
An API for Minecraft Spigot that makes creating GUIs easier

Links:

[JavaDocs](https://skiftstar.github.io/guiAPI)

[Adding to Project: Maven](https://jitpack.io/#Skiftstar/guiAPI)

When building your Plugin, I recommend shading this API (it might work without shading but no 100% guarantee here). [Here's a link for the shade plugin](https://maven.apache.org/plugins/maven-shade-plugin/)

Examples:

Creating a simple chest Window with an Apple in the 6th slot that messages the player "Hello Minecraft!" once it is clicked
```
GUI gui = new GUI(p, plugin);
//Params: Title, rows
ChestWindow window = gui.createWindow("A title &cwhich can have color!", 6);
//First way of creating the item
GuiItem item = window.setItemStack(new ItemStack(Material.APPLE), 6);
//Or you can create it like so:
GuiItem item2 = window.setItemStack(Material.APPLE, "&cThe apple is red!", 7);
item.setOnClick(e -> {
  p.sendMessage("Hello Minecraft!");
});
gui.open(window);
```

ChestWindows also support pages so you can theoretically add as many items as you want to an inventory!
```
GUI gui = new GUI(p, plugin);
ChestWindow window = gui.createWindow("Title", 6);
window.setPagesEnabled(true);
window.setNextPageItem(Material.ARROW, "&aNext");
window.setPrevPageItem(Material.ARROW, "&cBack");
for (int i = 0; i < 150; i++) {
  window.addItemStack(new ItemStack(Material.APPLE));
}
gui.open(window);
```

There are also other types of Window, for example Anvil Windows (which are great for letting the player input text!)
```
GUI gui = new GUI(p, plugin);
AnvilWindow window = gui.createAnvilWindow("&aAn original Title");
window.setOnClick(e -> {
  //Tells the player what he put in the text box after clicking on the finish button
  p.sendMessage("You inputted: " + window.getText());
});
gui.open(window);
```

Or the most recent Window, a TradeWindow which even has custom Events!
```
GUI gui = new GUI(p, plugin);
//Params: Trade Partner, title
TradeWindow window = gui.createTradeWindow(partner, "&cDon't scam pls");
//One of said custom Events
window.setOnItemAdd(e -> {
  //Item will now be removed from the player inv as long as the trade is still going on
  e.setRemoveFromInv(true);
  //Would now override the last Item the player added to the trade if his offerings are already full
  e.setOnTradeFull(FullTradeAction.REPLACE_LAST);
});
//You can even force complete the trade!
window.completeTrade(true);
gui.open(window);
```

The API also has Custom Exceptions, for a list of all Exception please check the JavaDoc!

Example of an exception:
```
GUI gui = new GUI(p, plugin);
ChestWindow window = gui.createWindow("Title", 6);
//Throws a SlotOutOfBoundsException because slot 100 doesn't exist
window.setItemStack(new ItemStack(Material.BLUE_TERRACOTTA), 100);
gui.open(window);
```
