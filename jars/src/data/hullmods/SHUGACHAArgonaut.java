package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;

public class SHUGACHAArgonaut extends BaseHullMod {
   private static final float SALVAGE_BONUS_RARE = 15.0F;
   private static final float CORONA_EFFECT_REDUCTION = 0.1F;
   private static final float SENSOR_MULT = 100.0F;
   private static final Map salvaging = new HashMap();
   private static final Map surveying = new HashMap();
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_eutec_exploration")) {
         stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, (Float)surveying.get(hullSize));
         stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, (Float)surveying.get(hullSize));
         stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, (Float)salvaging.get(hullSize));
         stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, 0.14999999F);
         stats.getSensorStrength().modifyMult(id, 2.0F);
         stats.getDynamic().getStat("corona_resistance").modifyMult(id, 0.1F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_eutec_exploration")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_eutec_exploration")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_eutec_exploration");
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_eutec_exploration")) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_sr"), 368.0F, 40.0F, 5.0F);
            if (SUPlugin.HASLUNALIB) {
               this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
            }

            if (this.enableCheatModeForRetards) {
               LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
               retardrius.setAlignment(Alignment.MID);
               retardrius.italicize();
            }

            tooltip.addPara(
               "• Improves ship's sensor strength: %s\n• Resistance to solar corona and hyperspace storm: %s\n• Increases chance of getting rare loot from salvage: %s\n• Reduces cost for survey operation: %s/%s/%s/%s\n• Increased salvaging bonus up to a fleetwide maximum equal to the salvage difficulty rating: %s/%s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(100.0F) + "%",
                  Misc.getRoundedValue(90.0F) + "%",
                  Misc.getRoundedValue(15.0F) + "%",
                  Misc.getRoundedValue(15.0F),
                  Misc.getRoundedValue(30.0F),
                  Misc.getRoundedValue(40.0F),
                  Misc.getRoundedValue(50.0F),
                  Misc.getRoundedValue(10.0F) + "%",
                  Misc.getRoundedValue(15.0F) + "%",
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(30.0F) + "%"
               }
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"No bonus applied. The information will only appear if this hullmod is S-modded."}
            );
         }

         tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADQUOTE,
               SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
               new String[]{
                  "\"To those of you who know me: you should be aware, by now, that my ambition is unlimited. You know that I will settle for nothing short of greatness or die trying.\""
               }
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Speech given by Eridani-Utopia Terraforming Corporation's CEO at the Mars Trade Show, c534 pre-Collapse"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SR_NAME;
   }

   static {
      salvaging.put(HullSize.FIGHTER, 0.0F);
      salvaging.put(HullSize.FRIGATE, 0.1F);
      salvaging.put(HullSize.DESTROYER, 0.15F);
      salvaging.put(HullSize.CRUISER, 0.2F);
      salvaging.put(HullSize.CAPITAL_SHIP, 0.3F);
      surveying.put(HullSize.FIGHTER, 0.0F);
      surveying.put(HullSize.FRIGATE, 15.0F);
      surveying.put(HullSize.DESTROYER, 30.0F);
      surveying.put(HullSize.CRUISER, 40.0F);
      surveying.put(HullSize.CAPITAL_SHIP, 50.0F);
   }
}
