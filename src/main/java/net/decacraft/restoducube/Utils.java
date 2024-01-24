package net.decacraft.restoducube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {
    public static boolean checkNotPerm(CommandSender p, String name) {
        if (p.hasPermission(name))
            return false;
        p.sendMessage(RestoDuCube.instance.prefix.append(Component.text("Vous n'avez pas la permission d'utiliser cette commande").color(NamedTextColor.RED)));
        return true;
    }

    public static List<String> argsFilter(String arg, Collection c) {
        List<String> choice = new ArrayList<String>(c);
        choice.removeIf(value -> !value.contains(arg));
        return choice;
    }

    public static boolean isChestRecycling(Chest chest) {
        //return ((TextComponent)chest.customName()).content().contains(RestoDuCube.chestName);
        return chest.customName().toString().contains(RestoDuCube.chestName);
    }

    public static ItemStack createInventoryItem(Material material, Component name, List<Component> lores) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (lores != null) {
            meta.lore(lores);
        }
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
