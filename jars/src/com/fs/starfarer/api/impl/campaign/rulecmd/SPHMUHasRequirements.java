package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SPHMUHasRequirements extends BaseCommandPlugin {
   public static final int ONEHUNDREDTHOUSANDFUCKS = 100000;

   public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
      if (dialog == null) {
         return false;
      } else {
         String command = params.get(0).getString(memoryMap);
         if (command == null) {
            return false;
         } else {
            byte var7 = -1;
            switch (command.hashCode()) {
               case 1837927009:
                  if (command.equals("playerHasCredits")) {
                     var7 = 0;
                  }
               default:
                  switch (var7) {
                     case 0:
                        return this.playerHasCredits();
                     default:
                        return false;
                  }
            }
         }
      }
   }

   protected boolean playerHasCredits() {
      return Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= 100000.0F;
   }
}
