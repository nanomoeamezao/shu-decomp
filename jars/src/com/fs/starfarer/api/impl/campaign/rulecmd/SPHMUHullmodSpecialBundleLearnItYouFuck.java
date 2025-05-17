package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SPHMUHullmodSpecialBundleLearnItYouFuck extends BaseCommandPlugin {
   public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
      if (dialog == null) {
         return false;
      } else {
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_alpha_core_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_beta_core_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_gamma_core_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_biofactoryembryo_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_catalyticcore_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_combatdronereplicator_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_cryoarithmeticengine_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_dealmakerholosuite_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_fullerenespool_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_fusionlampreactor_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_hypershunt_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_mantlebore_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_corruptednanoforge_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_pristinenanoforge_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_plasmadynamo_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_soilnanites_upgrades");
         Global.getSector().getCharacterData().getHullMods().add("specialsphmod_synchrotoncore_upgrades");
         Global.getSoundPlayer().playUISound("ui_acquired_hullmod", 1.0F, 1.0F);
         return true;
      }
   }
}
