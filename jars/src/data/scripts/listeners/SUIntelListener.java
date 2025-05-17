package data.scripts.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.SUHintIntel;
import data.scripts.util.id.SUStringCodex;

public class SUIntelListener extends BaseCampaignEventListener {
   public SUIntelListener() {
      super(false);
   }

   public void reportPlayerClosedMarket(MarketAPI market) {
      boolean SHUMemkey = Global.getSector().getMemory().contains("$SpecialHMODBarOffer");
      SUHintIntel intel = new SUHintIntel();
      if (Global.getSector().getCharacterData().getPerson().getStats().getLevel() >= 9
         && !Global.getSector().getCharacterData().getHullMods().containsAll(SUStringCodex.SHUHULLMODCOLLECTION)) {
         if (!SHUMemkey) {
            Global.getSector().getIntelManager().addIntel(intel, false);
            Global.getSector().addScript(intel);
            Global.getSector().removeListener(this);
         } else if (SHUMemkey) {
            Global.getSector().getIntelManager().removeIntel(intel);
            Global.getSector().removeScript(intel);
            Global.getSector().removeListener(this);
         }
      }
   }
}
