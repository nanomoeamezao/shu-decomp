package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import lunalib.lunaSettings.LunaSettings;

public class SUHypershuntTapExtension extends BaseHullMod {
   private static final float VENT_BONUS = 60.0F;
   private static final float HARD_FLUX_DISSIPATION_PERCENT = 75.0F;
   private static final float FLUX_DISSIPATION_MULT = 1.1F;
   private static final float OVERLOAD_DURATION_MULT = 1.2F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableOverloadDuration = SUPlugin.DISABLE_OVERLOAD_DURATION_HYPERSHUNT;
   boolean disableResistantFluxConduitsIncompat = SUPlugin.DISABLE_FLUXBREAKERS_INCOMPATIBILITY;
   float ventBonus = SUPlugin.CM_HYPERSHUNT_VENT_BONUS;
   float hardFluxDissipation = SUPlugin.CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT;
   float fluxDissipation = SUPlugin.CM_HYPERSHUNT_FLUX_DISSIPATION_MULT;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_hypershunt_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.disableOverloadDuration = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableOverloadDurationHypershuntToggle");
         this.ventBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_VENT_BONUS");
         this.hardFluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT");
         this.fluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_FLUX_DISSIPATION_MULT");
      }

      if (this.enableCustomSM) {
         stats.getVentRateMult().modifyFlat(id, 1.0F - this.ventBonus / 100.0F);
         stats.getHardFluxDissipationFraction().modifyFlat(id, 1.0F - this.hardFluxDissipation / 100.0F);
         stats.getFluxDissipation().modifyMult(id, 1.0F + 0.01F * this.fluxDissipation);
      } else if (!this.enableCustomSM) {
         stats.getVentRateMult().modifyFlat(id, 0.59999996F);
         stats.getHardFluxDissipationFraction().modifyFlat(id, 0.75F);
         stats.getFluxDissipation().modifyMult(id, 1.1F);
      }

      if (!this.disableOverloadDuration) {
         stats.getOverloadTimeMod().modifyMult(id, 1.2F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         for (String blockedMod : ALL_INCOMPAT_IDS) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               ship.getVariant().removePermaMod(blockedMod);
            }
         }
      }

      if (SUPlugin.HASLUNALIB) {
         this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle");
      }

      if (!this.disableResistantFluxConduitsIncompat && ship.getVariant().getHullMods().contains("fluxbreakers")) {
         ship.getVariant().removeMod("fluxbreakers");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.getFluxTracker().isVenting()) {
         ship.getVentCoreColor().brighter();
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle");
         }

         if (!this.disableResistantFluxConduitsIncompat && ship.getVariant().hasHullMod("fluxbreakers")) {
            return false;
         } else {
            return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
               ? false
               : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle");
         }

         if (!this.disableResistantFluxConduitsIncompat && ship.getVariant().hasHullMod("fluxbreakers")) {
            return "Incompatible with Resistant Flux Conduits";
         } else {
            return !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
               ? super.getUnapplicableReason(ship)
               : "Only one type of special upgrade hullmod can be installed per ship";
         }
      } else {
         return "Unable to locate ship!";
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableOverloadDuration = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableOverloadDurationHypershuntToggle");
            this.ventBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_VENT_BONUS");
            this.hardFluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT");
            this.fluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_FLUX_DISSIPATION_MULT");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases flux dissipation: %s\n• Dissipates hard flux while shields are up: %s\n• Improves active vent rate: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(SUPlugin.CM_HYPERSHUNT_FLUX_DISSIPATION_MULT) + "%",
                  Misc.getRoundedValue(SUPlugin.CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT) + "%",
                  Misc.getRoundedValue(SUPlugin.CM_HYPERSHUNT_VENT_BONUS) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases flux dissipation: %s\n• Dissipates hard flux while shields are up: %s\n• Improves active vent rate: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(40.0F) + "%"}
            );
         }

         if (!this.disableOverloadDuration) {
            tooltip.addPara(
               "• Overload duration is increased: %s",
               SUStringCodex.SHU_TOOLTIP_NEG,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{Misc.getRoundedValue(20.0F) + "%"}
            );
         }

         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{"This is an extension of Flux Hypershunt and it will remove itself when the main hullmod is removed from the parent module."}
         );
      }
   }
}
