package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SPHMUHullmodSpecialBundleLearnItYouFuckUAFCollab extends BaseCommandPlugin {
   public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
      if (dialog == null) {
         return false;
      } else {
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_interplanetaryaccessrouter_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_dimensionalnanoforge_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_garrisontransmitter_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_modularpurifier_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_servosyncpump_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_dimensionalstove_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_uaf_interplanetary_ricecooker_upgrades");
         Global.getSoundPlayer().playUISound("ui_acquired_hullmod", 1.0F, 1.0F);
         return true;
      }
   }
}
