package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.id.SUStringCodex;
import java.util.List;
import java.util.Map;

public class SPHMULearnedHullmod extends BaseCommandPlugin {
   public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
      if (dialog == null) {
         return false;
      } else {
         String hullmodId = params.get(0).getString(memoryMap);
         return Global.getSector().getCharacterData().getHullMods().containsAll(SUStringCodex.SHUHULLMODCOLLECTION);
      }
   }
}
