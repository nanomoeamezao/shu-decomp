package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;

public class SHUGACHADynamicEngineTuning extends BaseHullMod {
   public static final float SPEED_BONUS = 30.0F;
   public static final float ZERO_FLUX_MIN = 0.2F;
   private static final float ENGINE_DAMAGE_MULT = 1.4F;
   private static final float SENSOR_PROFILE = 200.0F;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_dynamic_tuning")) {
         stats.getMaxSpeed().modifyFlat(id, 30.0F);
         stats.getAcceleration().modifyFlat(id, 45.0F);
         stats.getDeceleration().modifyFlat(id, 37.5F);
         stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 0.2F);
         stats.getEngineDamageTakenMult().modifyMult(id, 1.4F);
         stats.getSensorProfile().modifyFlat(id, 200.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_dynamic_tuning")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_dynamic_tuning")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_dynamic_tuning");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      ship.getEngineController().extendFlame(this, 0.5F, 0.5F, 0.5F);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_dynamic_tuning")) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_r"), 368.0F, 40.0F, 5.0F);
            if (SUPlugin.HASLUNALIB) {
               this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
            }

            if (this.enableCheatModeForRetards) {
               LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
               retardrius.setAlignment(Alignment.MID);
               retardrius.italicize();
            }

            tooltip.addPara(
               "• Improves ship's top speed: %s flat\n• Zero-flux movement speed bonus at %s flux level and below.",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(30.0F), Misc.getRoundedValue(20.0F) + "%"}
            );
            tooltip.addPara(
               "• Increased sensor profile: %s\n• Engines damage taken increased: %s",
               SUStringCodex.SHU_TOOLTIP_NEG,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{Misc.getRoundedValue(200.0F), Misc.getRoundedValue(40.0F) + "%"}
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
               new String[]{"\"Racers should race; Sindrian patrols should eat lobster rolls.\""}
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{
               "         — Captain King of the ISS For The Family, prior to arrest by and violent confrontation with Sindrian Diktat patrol forces, c204"
            }
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_NAME;
   }
}
