package net.decacraft.restoducube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecyclingChest {
    private Chest chest;

    public RecyclingChest(Chest chest) {
        this.chest = chest;
    }

    public void Init() {
        this.chest.customName(Component.text(RestoDuCube.chestName).style(Style.style(TextDecoration.BOLD)));
        this.chest.update();
    }

    //Mise à jour du contenu d'un coffre de recyclage
    public void UpdateChest() {
        //On boucle sur le contenu du coffre qu'on envoie aux stocks des Restos
        Inventory inventory = this.chest.getInventory();
        if (inventory != null) {
            inventory.forEach(itemStack -> {
                if (itemStack != null) {
                    //On envoie aux restos
                    RestoItem restoItem = new RestoItem(itemStack.getType());
                    restoItem.AddToStock(itemStack.getAmount());

                    //On efface l'item du coffre
                    itemStack.setAmount(0);
                }
            });
        }
    }
}
