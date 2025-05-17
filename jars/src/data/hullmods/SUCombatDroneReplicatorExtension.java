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

public class SUCombatDroneReplicatorExtension extends BaseHullMod {
   private static final float FIGHTER_REPLACEMENT_RATE_BONUS = 10.0F;
   private static final float RATE_DECREASE_MODIFIER = 20.0F;
   private static final float COST_REDUCTION_LPC = 1.0F;
   private static final float EXTRA_BAY = 1.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExpandedDeckCrewIncompat = SUPlugin.DISABLE_EXPANDEDDECKCREW_INCOMPATIBILITY;
   float fighterReplacementRate = SUPlugin.CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS;
   float fighterRateDecrease = SUPlugin.CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER;
   float fighterordnancePointReductionCost = SUPlugin.CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC;
   float bomberordnancePointReductionCost = SUPlugin.CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC;
   float extraFighterBay = SUPlugin.CM_DRONE_REPLICATOR_EXTRA_BAY;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_combatdronereplicator_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fighterReplacementRate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS");
         this.fighterRateDecrease = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER");
         this.fighterordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC");
         this.bomberordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC");
         this.extraFighterBay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_EXTRA_BAY");
      }

      int builtIn = stats.getVariant().getHullSpec().getBuiltInWings().size();
      int bays = Math.round(stats.getNumFighterBays().getBaseValue());
      if (!(stats.getNumFighterBays().getBaseValue() <= 0.0F) && builtIn < bays && (stats.getNumFighterBays().getBaseValue() >= 0.0F || builtIn <= bays)) {
         if (this.enableCustomSM) {
            float timeMultCSM = 1.0F / ((100.0F + this.fighterReplacementRate) / 100.0F);
            stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 1.0F - this.fighterRateDecrease / 100.0F);
            stats.getFighterRefitTimeMult().modifyMult(id, timeMultCSM);
            stats.getDynamic().getMod("all_fighter_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
            stats.getDynamic().getMod("fighter_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
            stats.getDynamic().getMod("interceptor_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
            stats.getDynamic().getMod("support_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
            stats.getDynamic().getMod("bomber_cost_mod").modifyFlat(id, -this.bomberordnancePointReductionCost);
            stats.getNumFighterBays().modifyFlat(id, this.extraFighterBay);
         } else if (!this.enableCustomSM) {
            float timeMult = 0.9090909F;
            stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 0.8F);
            stats.getFighterRefitTimeMult().modifyMult(id, 0.9090909F);
            stats.getDynamic().getMod("all_fighter_cost_mod").modifyFlat(id, -1.0F);
            stats.getDynamic().getMod("bomber_cost_mod").modifyFlat(id, -2.0F);
            stats.getDynamic().getMod("fighter_cost_mod").modifyFlat(id, -1.0F);
            stats.getDynamic().getMod("interceptor_cost_mod").modifyFlat(id, -1.0F);
            stats.getDynamic().getMod("support_cost_mod").modifyFlat(id, -1.0F);
            stats.getNumFighterBays().modifyFlat(id, 1.0F);
         }
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
         this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
      }

      if (!this.disableExpandedDeckCrewIncompat && ship.getVariant().getHullMods().contains("expanded_deck_crew")) {
         ship.getVariant().removeMod("expanded_deck_crew");
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
         }

         if (!this.disableExpandedDeckCrewIncompat && ship.getVariant().hasHullMod("expanded_deck_crew")) {
            return false;
         } else if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
            return false;
         } else {
            int builtIn = ship.getHullSpec().getBuiltInWings().size();
            int bays = Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
            if (builtIn >= bays) {
               return false;
            } else {
               return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  ? false
                  : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
            }
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
         }

         if (!this.disableExpandedDeckCrewIncompat && ship.getVariant().hasHullMod("expanded_deck_crew")) {
            return "Incompatible with Expanded Deck Crew";
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

   public boolean affectsOPCosts() {
      return true;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.fighterReplacementRate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS");
            this.fighterRateDecrease = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER");
            this.fighterordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC");
            this.bomberordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC");
            this.extraFighterBay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_EXTRA_BAY");
         }

         int builtIn = ship.getHullSpec().getBuiltInWings().size();
         int bays = Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"• This module does not have standard fighter bays, no bonus applied."}
            );
         } else if (builtIn >= bays) {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"• Requires at least one modular fighter bay, no bonus applied."}
            );
         } else if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces decay of fighter replacement rate: %s\n• Reduces fighter refit time: %s\n• Reduces OP cost of fighter & bomber LPC: %s/%s\n• Adds extra fighter bay: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.fighterReplacementRate) + "%",
                  Misc.getRoundedValue(this.fighterRateDecrease) + "%",
                  Misc.getRoundedValue(this.fighterordnancePointReductionCost),
                  Misc.getRoundedValue(this.bomberordnancePointReductionCost),
                  Misc.getRoundedValue(this.extraFighterBay)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces decay of fighter replacement rate: %s\n• Reduces fighter refit time: %s\n• Reduces OP cost of fighter & bomber LPC: %s/%s\n• Adds extra fighter bay: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(10.0F) + "%",
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(1.0F),
                  Misc.getRoundedValue(2.0F),
                  Misc.getRoundedValue(1.0F)
               }
            );
         }

         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{
               "This is an extension of Expanded Fighter Manufactory and it will remove itself when the main hullmod is removed from the parent module."
            }
         );
      }
   }
}
