package net.decacraft.restoducube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandListener implements CommandExecutor, TabExecutor {
    public final RestoDuCube inst;
    public CommandListener(RestoDuCube instance) {
        this.inst = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            //Vérification de la permission d'utilisation
            if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.usePerm"))) return false;

            if (args.length == 0)
                args = new String[]{"help"};
            switch (args[0]) {
                case "help":
                    //Aide sur l'utilisation des Restos du Cube
                    //player.sendMessage(Component.text("Blablabla, aide à définir").color(NamedTextColor.GRAY).style(Style.style(TextDecoration.BOLD)));
                    //TODO : rédiger une notice d'utilisation
                    break;
                case "recyclage":
                    //Création du coffre de recyclage
                    player.sendMessage(RestoDuCube.prefix.append(Component.text("Clic droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                    RestoDuCube.commands.put(player.getUniqueId(), args);
                    break;
                case "setnpc":
                    //Vérification de la permission d'administration
                    if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.adminPerm"))) break;
                    player.sendMessage(RestoDuCube.prefix.append(Component.text("Clic droit sur le NPC pour l'initialiser.").color(NamedTextColor.YELLOW)));
                    RestoDuCube.commands.put(player.getUniqueId(), args);
                    break;
                case "removenpc":
                    //Vérification de la permission d'administration
                    if (Utils.checkNotPerm(player, RestoDuCube.instance.getConfig().getString("parameters.adminPerm"))) break;
                    player.sendMessage(RestoDuCube.prefix.append(Component.text("Clic droit sur le NPC pour le délier.").color(NamedTextColor.YELLOW)));
                    RestoDuCube.commands.put(player.getUniqueId(), args);
                    break;
            }

            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            //On ne met pas le setnpc/removenpc dans l'autocompletion
            return Utils.argsFilter(args[0], Arrays.asList("recyclage"));//TODO : Ajouter help
        }
        return new ArrayList<>();
    }
}
