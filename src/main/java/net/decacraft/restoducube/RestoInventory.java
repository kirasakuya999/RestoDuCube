package net.decacraft.restoducube;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RestoInventory {
    public static Map<UUID, RestoInventory> inventories = new HashMap<>();
    private final Inventory inventory;
    private final Player player;
    private int itemPerPage = 45;
    private int step = 0;
    private int page = 1;
    private int slot;
    private int currMat;


    public RestoInventory(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player.getInventory().getHolder(), 54, Component.text("Les Restos du Cube").color(NamedTextColor.GOLD));
        updateInventory();
    }

    public void OnClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        if (step == 0) {
            ItemStack item = event.getCurrentItem();
            this.slot = event.getSlot();
            this.currMat = event.getSlot();
            if (item != null && item.getType() != Material.AIR) {
                this.page = 1;
                this.step = 1;
            }
            updateInventory();
        }
        else if (step == 1) {
            ItemStack item = event.getCurrentItem();
            this.slot = event.getSlot();
            if (item != null && item.getType() != Material.AIR) {
                //Bouton Précédent
                if (slot == 45) this.page = (this.page > 1) ? this.page - 1 : 1;
                //Bouton Suivant
                else if (slot == 53) this.page++;
                //Bouton Retour
                else if (slot == 49) this.step = 0;
                //Récupération d'un stack de l'objet cliqué
                else {
                    RestoItem restoItem = new RestoItem(item.getType());
                    restoItem.giveOneStack(player);
                }

                updateInventory();
            }
        }
    }

    public void updateInventory() {
        inventory.clear();

        switch (step) {
            case 0:
                //On récupère la liste des catégories d'items
                ConfigurationSection categories = RestoDuCube.instance.getItemConfig().getConfigurationSection("categories");
                for (String key : categories.getKeys(false)) {
                    ConfigurationSection cat = categories.getConfigurationSection(key);
                    Material material = Material.getMaterial(cat.getString("icon"));
                    int amount = getCatTypeAmount(cat.getName());

                    inventory.setItem(cat.getInt("index"), Utils.createInventoryItem(material, Component.text(cat.getString("name")).color(NamedTextColor.BLUE), Lists.newArrayList(Component.text(amount + " type" + (amount > 1 ? "s" : "") + " d'objet" + (amount > 1 ? "s" : "") + " disponible" + (amount > 1 ? "s" : "")).color(NamedTextColor.GRAY))));
                }
                break;
            case 1:
                //On récupère la catégorie cliquée
                ConfigurationSection cat = null;
                ConfigurationSection catsTmp = RestoDuCube.instance.getItemConfig().getConfigurationSection("categories");
                for (String key : catsTmp.getKeys(false)) {
                    ConfigurationSection catTmp = catsTmp.getConfigurationSection(key);
                    if (catTmp.getInt("index") == this.currMat) {
                        cat = catTmp;
                    }
                }

                //On affiche les objets présents dans la catégorie
                int pages = 1;
                if (cat != null) {
                    List<String> items = getCategoryItems(cat.getName());

                    if (items.stream().count() > 0) {
                        //Partitionnement en pages
                        int listStart = 0;
                        int listEnd = (int) items.stream().count();

                        if (items.stream().count() > this.itemPerPage) {
                            pages = (int) (items.stream().count() / this.itemPerPage) + 1;
                            if (this.page <= pages) {
                                listStart = ((this.page - 1) * this.itemPerPage);
                                listEnd = (this.page * this.itemPerPage);
                                if (listEnd > (int) items.stream().count())
                                    listEnd = (int) items.stream().count();
                            }
                            else {
                                this.page = 1;
                                listStart = 0;
                                listEnd = this.itemPerPage;
                            }
                        }

                        //Affichage des stocks
                        int idx = 0;
                        for (String key : items.subList(listStart, listEnd)) {
                            Material material = Material.getMaterial(key);
                            RestoItem restoItem = new RestoItem(material);
                            inventory.setItem(idx, Utils.createInventoryItem(material, Component.text(material.name()).color(NamedTextColor.BLUE), Lists.newArrayList(Component.text("Stock : " + restoItem.GetStock()).color(NamedTextColor.GRAY))));
                            idx++;
                        }
                    }
                }

                //Elements de navigation
                // - Page précédente
                if (this.page > 1)
                    inventory.setItem(45, Utils.createInventoryItem(Material.BOOK, Component.text("Page " + (this.page - 1)).color(NamedTextColor.BLUE), null));

                // - Page suivante
                if (this.page < pages)
                    inventory.setItem(53, Utils.createInventoryItem(Material.BOOK, Component.text("Page " + (this.page + 1)).color(NamedTextColor.BLUE), null));

                // - Retour
                inventory.setItem(49, Utils.createInventoryItem(Material.CHEST, Component.text("Retour").color(NamedTextColor.BLUE), null));

                break;
        }
    }

    public void openInventory() {
        player.openInventory(inventory);
    }

    private int getCatTypeAmount(String cat) {
        int ret = 0;

        //Chargement des stocks
        ConfigurationSection stocks = RestoDuCube.instance.getConfig().getConfigurationSection("items");

        //On décompte dans les stocks ceux qui correspondent à la catégorie ciblée
        for (String key : stocks.getKeys(false)) {
            RestoItem restoItem = new RestoItem(Material.getMaterial(key));

            //Si on a du stock et que la catégorie correspond, on ajoute l'item à la liste
            if (restoItem.GetStock() > 0 && cat.equals(RestoDuCube.instance.getItemConfig().getString("items." + key, "misc")))
                ret++;
        }

        return ret;
    }

    private List<String> getCategoryItems(String cat) {
        List<String> ret = new ArrayList<>();

        //Chargement des stocks
        ConfigurationSection stocks = RestoDuCube.instance.getConfig().getConfigurationSection("items");

        //On récupère dans les stocks ceux qui correspondent à la catégorie ciblée
        for (String key : stocks.getKeys(false)) {
            RestoItem restoItem = new RestoItem(Material.getMaterial(key));

            //Si on a du stock et que la catégorie correspond, on ajoute l'item à la liste
            if (restoItem.GetStock() > 0 && cat.equals(RestoDuCube.instance.getItemConfig().getString("items." + key, "misc")))
                ret.add(key);
        }

        //On trie la liste par ordre alphabétique
        ret.sort(Comparator.naturalOrder());

        return ret;
    }
}
