package net.decacraft.restoducube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RestoItem {
    private Material material;

    public RestoItem(Material material) {
        if (material != null) {
            this.material = material;
        }
    }

    public void AddToStock(int amount) {
        if (this.material != null) {
            int stock = GetStock() + amount;
            SetStock(stock);
        }
    }

    public void RemoveFromStock(int amount) {
        if (this.material != null) {
            int stock = GetStock() - amount;
            SetStock(stock);
        }
    }

    public int GetStock() {
        if (this.material != null)
            return RestoDuCube.instance.getConfig().getInt("items." + this.material.name(), 0);
        else
            return 0;
    }

    public void SetStock(int amount) {
        RestoDuCube.instance.getConfig().set("items." + this.material.name(), amount);
    }

    public void giveOneStack(Player player) {
        //Vérification d'un espace dispo dans l'inventaire
        if (player.getInventory().firstEmpty() != -1) {
            if (this.material != null) {
                //Définition de la quantité
                int amount = 0;
                if (GetStock() >= this.material.getMaxStackSize())
                    amount = this.material.getMaxStackSize();
                else
                    amount = GetStock();

                //Création de l'ItemStack
                ItemStack itemStack = new ItemStack(this.material, amount);

                //Dépot dans l'inventaire du joueur
                player.getInventory().addItem(itemStack);

                //On retire des stocks
                RemoveFromStock(amount);
            }
        }
        else {
            player.sendMessage(RestoDuCube.prefix.append(Component.text("Aucune place disponible dans l'inventaire").color(NamedTextColor.RED)));
        }
    }
}
