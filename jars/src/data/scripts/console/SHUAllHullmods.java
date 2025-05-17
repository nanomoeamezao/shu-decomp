package data.scripts.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import data.scripts.util.id.SUStringCodex;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lazywizard.console.BaseCommand.CommandResult;

public class SHUAllHullmods implements BaseCommand {
   public CommandResult runCommand(String args, CommandContext context) {
      if (!context.isInCampaign()) {
         Console.showMessage("Error: This command is campaign-only.");
         return CommandResult.WRONG_CONTEXT;
      } else {
         CharacterDataAPI player = Global.getSector().getCharacterData();
         boolean isStinkyCheater = Global.getSector().getMemory().contains(SUStringCodex.SHU_CONSOLE_COMMAND_MEMKEY);
         boolean isSHULearnedAll = Global.getSector().getCharacterData().getHullMods().containsAll(SUStringCodex.SHUHULLMODCOLLECTION);
         if (!player.knowsHullMod(SUStringCodex.SHU_CONSOLE_COMMAND_MEMKEY)) {
            player.getHullMods().addAll(SUStringCodex.SHUHULLMODCOLLECTION);
            if (!isStinkyCheater) {
               Global.getSector().getMemory().set(SUStringCodex.SHU_CONSOLE_COMMAND_MEMKEY, true);
            }
         }

         if (!isSHULearnedAll) {
            Console.showMessage("You unlocked all Reverse Engineered hullmods!");
            return CommandResult.SUCCESS;
         } else if (isSHULearnedAll) {
            Console.showMessage("You already know all unlockable SHU hullmods!");
            return CommandResult.SUCCESS;
         } else {
            return CommandResult.SUCCESS;
         }
      }
   }
}
