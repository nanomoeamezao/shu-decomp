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

public class SHUGACHAPhaseConductor extends BaseHullMod {
   private static final float PHASE_ACTIVATION_REDUC = 60.0F;
   private static final float PHASE_UPKEEP_REDUC = 50.0F;
   private static final float PHASE_COOLDOWN_REDUC = 40.0F;
   private static final float PHASE_SPEED_BONUS = 30.0F;
   private static final String PHASE_CONDUCTOR = "SHUGACHAPhaseConductor";
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_phase_conductor")) {
         stats.getPhaseCloakActivationCostBonus().modifyPercent(id, -60.0F);
         stats.getPhaseCloakUpkeepCostBonus().modifyPercent(id, -50.0F);
         stats.getPhaseCloakCooldownBonus().modifyPercent(id, -40.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_phase_conductor")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_phase_conductor")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_phase_conductor");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_phase_conductor")) {
         if (!ship.isAlive()) {
            return;
         }

         if (ship.getPhaseCloak() == null) {
            return;
         }

         MutableShipStatsAPI stats = ship.getMutableStats();
         if (ship.isPhased()) {
            stats.getMaxSpeed().modifyFlat("SHUGACHAPhaseConductor", 30.0F);
         } else {
            stats.getMaxSpeed().unmodify("SHUGACHAPhaseConductor");
         }
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_phase_conductor")) {
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
               "• Reduces phase activation cost: %s\n• Reduces phase upkeep cost: %s\n• Reduces phase cooldown: %s\n• Increased top speed while phase cloak is active: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(60.0F) + "%", Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(40.0F) + "%", Misc.getRoundedValue(30.0F)
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
                  "\"This is the last time I let those pencil pushers in Corporate make my staffing assignments for me. All of you incompetents are fired, effective immediately.\""
               }
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — K. Astraia, Culann Research Arcology 21-Omicron"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_NAME;
   }
}
