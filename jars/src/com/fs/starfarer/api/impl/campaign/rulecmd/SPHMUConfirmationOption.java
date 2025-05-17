package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SPHMUConfirmationOption extends BaseCommandPlugin {
   public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
      if (dialog == null) {
         return false;
      } else {
         String command = params.get(0).getString(memoryMap);
         String optionID = params.get(1).getString(memoryMap);
         if (command == null) {
            return false;
         } else {
            byte var8 = -1;
            switch (command.hashCode()) {
               case 195143782:
                  if (command.equals("setConfirmationPurchaseSHUHullmods")) {
                     var8 = 0;
                  }
               default:
                  switch (var8) {
                     case 0:
                        this.setConfirmationPurchaseSHUHullmods(dialog, optionID);
                     default:
                        return true;
                  }
            }
         }
      }
   }

   private void setConfirmationPurchaseSHUHullmods(InteractionDialogAPI dialog, String optionID) {
      dialog.getOptionPanel().addOptionConfirmation(optionID, "Confirm the transaction?", "Proceed", "Cancel");
   }
}
