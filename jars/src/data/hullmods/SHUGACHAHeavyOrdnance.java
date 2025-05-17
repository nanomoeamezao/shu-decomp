package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;

public class SHUGACHAHeavyOrdnance extends BaseHullMod {
   private static final float AMMO_BONUS = 80.0F;
   private static final float HEALTH_BONUS = 150.0F;
   private static final float COST_REDUCTION_LG = 6.0F;
   private static final float MANEUVER_PENALTY = 20.0F;
   private static final float ARMOR_BONUS = 10.0F;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      stats.getDynamic().getMod("large_ballistic_mod").modifyFlat(id, -6.0F);
      stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -6.0F);
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_heavy_ordnance")) {
         float slottedWeapons = 0.0F;
         stats.getWeaponHealthBonus().modifyPercent(id, 150.0F);
         stats.getBallisticAmmoBonus().modifyPercent(id, 80.0F);
         stats.getEnergyAmmoBonus().modifyPercent(id, 80.0F);
         stats.getAcceleration().modifyMult(id, 0.8F);
         stats.getDeceleration().modifyMult(id, 0.8F);
         stats.getTurnAcceleration().modifyMult(id, 0.8F);
         stats.getMaxTurnRate().modifyMult(id, 0.8F);

         for (WeaponSlotAPI slot : stats.getVariant().getHullSpec().getAllWeaponSlotsCopy()) {
            if (!slot.isDecorative() && !slot.isHidden() && !slot.isStationModule() && !slot.isSystemSlot()) {
               slottedWeapons++;
            }
         }

         stats.getArmorBonus().modifyFlat(id, slottedWeapons * 10.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_heavy_ordnance")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_heavy_ordnance")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_heavy_ordnance");
      }
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_heavy_ordnance")) {
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
               "• Increases max ammo capacity: %s\n• Increases weapon durability: %s\n• Reduces OP cost of large non-missile weapons: %s\n• Increases the armor per weapon mount: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(80.0F) + "%", Misc.getRoundedValue(150.0F) + "%", Misc.getRoundedValue(6.0F), Misc.getRoundedValue(10.0F)}
            );
            tooltip.addPara(
               "• Decreases ship's maneuverability: %s",
               SUStringCodex.SHU_TOOLTIP_NEG,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{Misc.getRoundedValue(20.0F) + "%"}
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
               new String[]{"\"It's classified. I'd tell you, but then I'd have to kill you.\""}
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Annoyed Bhilai Astra CTO at Eventide's Weapon Systems Software Summit"}
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
