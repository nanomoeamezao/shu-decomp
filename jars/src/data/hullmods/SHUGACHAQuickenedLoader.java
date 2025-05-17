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

public class SHUGACHAQuickenedLoader extends BaseHullMod {
   private static final float RELOAD_TIME_BONUS = 20.0F;
   private static final float HIT_BONUS = 25.0F;
   private static final float PROJ_SPEED_BONUS = 40.0F;
   private static final float ROF_BONUS = 50.0F;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_quickened_loader")) {
         stats.getHitStrengthBonus().modifyPercent(id, 25.0F);
         stats.getBallisticRoFMult().modifyPercent(id, 50.0F);
         stats.getEnergyRoFMult().modifyPercent(id, 50.0F);
         stats.getProjectileSpeedMult().modifyPercent(id, 40.0F);
         stats.getBallisticAmmoRegenMult().modifyPercent(id, 20.0F);
         stats.getEnergyAmmoRegenMult().modifyPercent(id, 20.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_quickened_loader")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_quickened_loader")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_quickened_loader");
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_quickened_loader")) {
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
               "• Increases weapon rate of fire: %s\n• Increases projectile speed: %s\n• Increases projectile damage to armor: %s\n• Increased reload rate of non-missile weapons: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(40.0F) + "%", Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(20.0F) + "%"
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
               new String[]{"\"Violence isn't the answer. Violence is a question, and the answer is yes.\""}
            )
            .italicize();
         tooltip.addPara("%s", SUStringCodex.SHU_TOOLTIP_PADSIG, SUStringCodex.SHU_TOOLTIP_QUOTECOLOR, new String[]{"         — Qaras Pirate Lord, c175"});
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_NAME;
   }
}
