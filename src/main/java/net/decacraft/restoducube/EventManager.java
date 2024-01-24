package net.decacraft.restoducube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EventManager implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        //Clic droit sur un coffre
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {
            Player player = event.getPlayer();

            //On vérifie si le joueur à une commande en attente
            if (RestoDuCube.commands.containsKey(event.getPlayer().getUniqueId())) {

                //On récupère la commande et on la supprime de la liste d'attente
                String[] cmd = RestoDuCube.commands.get(player.getUniqueId());
                RestoDuCube.commands.remove(player.getUniqueId());

                //On annule l'action par défaut
                event.setCancelled(true);

                //Vérification du mode de jeu
                if (!player.getGameMode().equals(GameMode.SURVIVAL) && !player.getGameMode().equals(GameMode.ADVENTURE)) {
                    player.sendMessage(RestoDuCube.prefix.append(Component.text("Vous n'avez pas la permission de faire ça ici.").color(NamedTextColor.RED)));
                    return;
                }

                //Vérification des droits
                if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.adminPerm"))) return;

                switch (cmd[0]) {
                    //Création du coffre de Recyclage
                    case "recyclage":
                        Block block = event.getClickedBlock();
                        Chest chest = (Chest) block.getState();

                        //*********************
                        //TODO : Vérification si le coffre nous appartient
                        //*********************

                        //On nomme le coffre pour le rendre fonctionnel
                        RecyclingChest recyclingChest = new RecyclingChest(chest);
                        recyclingChest.Init();
                        player.sendMessage(RestoDuCube.prefix.append(Component.text("Coffre de recyclage créé.").color(NamedTextColor.GREEN)));
                        break;
                }
            } else {
                //Si on avait une commande en attente et qu'on clique sur autre chose qu'un coffre, on annule la commande
                if (RestoDuCube.commands.containsKey(player.getUniqueId())) {
                    player.sendMessage(RestoDuCube.prefix.append(Component.text("Action annulée.").color(NamedTextColor.RED)));
                    RestoDuCube.commands.remove(player.getUniqueId());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand().equals(EquipmentSlot.HAND) && event.getRightClicked().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getPlayer();

            if (RestoDuCube.commands.containsKey(player.getUniqueId())) {
                //On récupère la commande et on la supprime de la liste d'attente
                String[] cmd = RestoDuCube.commands.get(player.getUniqueId());
                RestoDuCube.commands.remove(player.getUniqueId());

                //On annule l'action par défaut
                event.setCancelled(true);

                Entity entity = event.getRightClicked();
                RestoNPC npc = new RestoNPC(entity);
                switch (cmd[0]) {
                    //Définition d'un NPC des restos du cube
                    case "setnpc":
                        if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.adminPerm"))) break;
                        npc.Init();
                        player.sendMessage(RestoDuCube.prefix.append(Component.text("NPC des restos du cube créé.").color(NamedTextColor.GREEN)));
                        break;
                    //Suppression d'un NPC des restos du cube
                    case "removenpc":
                        if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.adminPerm"))) break;
                        npc.Remove();
                        player.sendMessage(RestoDuCube.prefix.append(Component.text("NPC des restos du cube supprimé.").color(NamedTextColor.GREEN)));
                        break;
                }
            }
            else {
                //Si on avait une commande en attente et qu'on clique sur autre chose qu'un NPC, on annule la commande
                if (RestoDuCube.commands.containsKey(player.getUniqueId())) {
                    player.sendMessage(RestoDuCube.prefix.append(Component.text("Action annulée.").color(NamedTextColor.RED)));
                    RestoDuCube.commands.remove(player.getUniqueId());
                    event.setCancelled(true);
                }
                else {
                    Entity entity = event.getRightClicked();
                    RestoNPC npc = new RestoNPC(entity);
                    //Si le NPC est bien défini comme vendeur des Restos
                    if (npc.IsRegistered()) {
                        if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.usePerm"))) return;

                        //Si on est sur un NPC des restos, on ouvre le menu
                        player.sendMessage(RestoDuCube.prefix.append(Component.text("Bienvenue dans les Restos du Cube").color(NamedTextColor.GREEN)));
                        RestoInventory restoInventory = new RestoInventory(player);
                        RestoInventory.inventories.put(player.getUniqueId(), restoInventory);
                        restoInventory.openInventory();
                    }
                }
            }
        }
    }

    //Gestion du déplacement d'items dans le coffre de recyclage
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Inventory inventory = event.getDestination();
        if (inventory != null) {
            if (event.getDestination() != null && event.getDestination().getType().equals(InventoryType.CHEST)) {
                Block block = inventory.getLocation().getBlock();
                Chest chest = (Chest) block.getState();
                if (Utils.isChestRecycling(chest)) {
                    RecyclingChest recyclingChest = new RecyclingChest(chest);
                    //On envoie le contenu du coffre aux Restos
                    Bukkit.getScheduler().runTaskLater(RestoDuCube.instance, recyclingChest::UpdateChest, 2);
                }
            }
        }

    }

    //Gestion du dépot d'items dans le coffre de recyclage
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        UUID uuid = event.getWhoClicked().getUniqueId();
        if (RestoInventory.inventories.containsKey(uuid))
            RestoInventory.inventories.get(uuid).OnClick(event);
        else {
            Location location = null;
            switch (event.getAction()) {
                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                    if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.CHEST))
                        location = event.getClickedInventory().getLocation();
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                    if (event.getView().getTopInventory().getType().equals(InventoryType.CHEST))
                        location = event.getView().getTopInventory().getLocation();
                    break;
            }

            if (location != null) {
                Block block = location.getBlock();
                Chest chest = (Chest) block.getState();
                if (Utils.isChestRecycling(chest)) {
                    RecyclingChest recyclingChest = new RecyclingChest(chest);
                    //On envoie le contenu du coffre aux Restos
                    Bukkit.getScheduler().runTaskLater(RestoDuCube.instance, recyclingChest::UpdateChest, 2);
                }
            }
        }
    }

    //Si on déplace un élément dans le coffre
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDragEvent(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory != null) {
            if (event.getInventory().getLocation() != null && event.getType().equals(InventoryType.CHEST)) {
                Block block = inventory.getLocation().getBlock();
                Chest chest = (Chest) block.getState();
                if (chest != null && Utils.isChestRecycling(chest)) {
                    RecyclingChest recyclingChest = new RecyclingChest(chest);
                    //On envoie le contenu du coffre aux Restos
                    Bukkit.getScheduler().runTaskLater(RestoDuCube.instance, recyclingChest::UpdateChest, 2);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (RestoInventory.inventories.containsKey(uuid))
            RestoInventory.inventories.remove(uuid);
    }
}
