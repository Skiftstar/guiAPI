# guiAPI
### This API is tested for 1.13 - 1.15.2 ! Make sure to set your API Version in the plugin.yml !!
An API for Minecraft Spigot that makes creating GUIs easier

Links:

[JavaDocs](https://skiftstar.github.io/guiAPI)

[Adding to Project: Maven](https://jitpack.io/#Skiftstar/guiAPI)

Example:
```
GUI gui = new GUI(player, plugin);
GuiWindow window = gui.createWindow("title", 5, WindowType.NORMAL);
GuiItem item = window.addItemStack(Material.DIAMOND, "name", 5);
item.setOnClick(e -> {
  p.sendMessage("abc");
});
gui.open(window);
```
