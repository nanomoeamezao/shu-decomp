package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;

public class SUHullmodBlocker extends BaseHullMod {
   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_hullmod_blocker")
         || stats.getVariant().getHullSpec().isBuiltInMod("specialsphmod_hullmod_blocker")) {
         stats.getVariant().removePermaMod("specialsphmod_hullmod_blocker");
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading(
            "Incompatibilities",
            SUStringCodex.SHU_HULLMOD_NEGATIVE_TEXT_COLOR,
            SUStringCodex.SHU_HULLMOD_NEGATIVE_HEADER_BG,
            Alignment.MID,
            SUStringCodex.SHU_TOOLTIP_PADMAIN
         );
         tooltip.addPara(
            "%s", SUStringCodex.SHU_TOOLTIP_PADMAIN, Misc.getNegativeHighlightColor(), new String[]{"• Hullmod incompatibilities have been detected!"}
         );
         tooltip.addPara("%s", SUStringCodex.SHU_TOOLTIP_PADMAIN, Misc.getGrayColor(), new String[]{"This hullmod warning will be removed shortly."});
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_WARNING_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_WARNING_NAME;
   }
}
