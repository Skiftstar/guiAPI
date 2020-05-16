# guiAPI
### This API is tested for 1.13 - 1.15.2 ! Make sure to set your API Version in the plugin.yml !!
An API for Minecraft Spigot that makes creating GUIs easier

Links:

[JavaDocs](https://skiftstar.github.io/guiAPI)

[Adding to Project: Maven](https://jitpack.io/#Skiftstar/guiAPI)

Example:
```
GUI gui = new GUI(player, plugin);
guiWindow window = gui.createWindow("abc", 5, WindowType.SPLIT_2);
guiItem item = window.addItemStack(Material.DIAMOND, "abc", 5);
item.setOnClick(e -> {
  p.sendMessage("abc");
});
gui.open(window);
```
