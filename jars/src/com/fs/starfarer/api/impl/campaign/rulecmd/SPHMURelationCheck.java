package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SPHMURelationCheck extends BaseCommandPlugin {
   public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
      if (dialog == null) {
         return false;
      } else {
         int relation = params.get(0).getInt(memoryMap);
         int current = params.get(1).getInt(memoryMap);
         SectorEntityToken entity = dialog.getInteractionTarget();
         if (entity.getActivePerson() == null) {
            return false;
         } else {
            return current == 100
               ? entity.getActivePerson().getRelToPlayer().getRepInt() >= relation && entity.getActivePerson().getRelToPlayer().getRepInt() <= current
               : entity.getActivePerson().getRelToPlayer().getRepInt() >= relation && entity.getActivePerson().getRelToPlayer().getRepInt() < current;
         }
      }
   }
}
