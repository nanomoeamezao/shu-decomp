package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUPlasmaDynamoExtension extends BaseHullMod {
   private static final float SHIELD_BONUS = 25.0F;
   private static final float SHIELD_RATE = 50.0F;
   private static final float SHIELD_UPKEEP_BONUS = 60.0F;
   private static final float SHIELD_ARC_BONUS = 90.0F;
   private static final float SHIELD_TURNRAISE_CAP = 0.5F;
   private static final float FLUX_CHECKER = 0.01F;
   private static final float SHIELD_FLUX_BONUS = 0.5F;
   private final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_PLASMADYNAMOHMOD_EXTRA_EFFECT;
   boolean disableVanillaShieldHullmodsIncompat = SUPlugin.DISABLE_VANILLASHIELDFUCK_INCOMPATIBILITY;
   float shieldBonus = SUPlugin.CM_PLASMADYNAMO_SHIELD_BONUS;
   float shieldRateBonus = SUPlugin.CM_PLASMADYNAMO_SHIELD_RATE;
   float shieldUpkeepBonus = SUPlugin.CM_PLASMADYNAMO_SHIELD_UPKEEP_BONUS;
   float shieldArcBonus = SUPlugin.CM_PLASMADYNAMO_SHIELD_ARC_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private static final String[] ALL_INCOMPAT_VANILLA_IDS = new String[]{"hardenedshieldemitter", "extendedshieldemitter", "stabilizedshieldemitter"};

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_plasmadynamo_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public SUPlasmaDynamoExtension() {
      this.ID = "SUPlasmaDynamoExtension";
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getHullSpec().getShieldSpec() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.shieldBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_BONUS");
            this.shieldRateBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_RATE");
            this.shieldUpkeepBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_UPKEEP_BONUS");
            this.shieldArcBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_ARC_BONUS");
         }

         if (this.enableCustomSM) {
            stats.getShieldDamageTakenMult().modifyMult(id, 1.0F - this.shieldBonus * 0.01F);
            stats.getShieldUnfoldRateMult().modifyPercent(id, this.shieldRateBonus);
            stats.getShieldTurnRateMult().modifyPercent(id, this.shieldRateBonus);
            stats.getShieldUpkeepMult().modifyMult(id, 1.0F - this.shieldUpkeepBonus * 0.01F);
            stats.getShieldArcBonus().modifyFlat(id, this.shieldArcBonus);
         } else if (!this.enableCustomSM) {
            stats.getShieldDamageTakenMult().modifyMult(id, 0.75F);
            stats.getShieldUnfoldRateMult().modifyPercent(id, 50.0F);
            stats.getShieldTurnRateMult().modifyPercent(id, 50.0F);
            stats.getShieldUpkeepMult().modifyMult(id, 0.40000004F);
            stats.getShieldArcBonus().modifyFlat(id, 90.0F);
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
         this.disableVanillaShieldHullmodsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableVanillaShieldHullmodsIncompatibilityToggle");
      }

      if (!this.disableVanillaShieldHullmodsIncompat) {
         for (String blockedModVN : ALL_INCOMPAT_VANILLA_IDS) {
            if (ship.getVariant().getHullMods().contains(blockedModVN)) {
               ship.getVariant().removeMod(blockedModVN);
            }
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePlasmaDynamoCAToggle");
      }

      if (!this.disableExtraEffect) {
         CombatEngineAPI engine = Global.getCombatEngine();
         ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
         float FLUX_LEVEL = Math.min(ship.getFluxTracker().getFluxLevel() / 0.5F, 1.0F);
         if (!ship.isAlive()) {
            return;
         }

         if (ship.getShield() != null) {
            ship.getMutableStats().getShieldTurnRateMult().modifyPercent(this.ID, 1.0F + 0.5F * FLUX_LEVEL);
            ship.getMutableStats().getShieldUnfoldRateMult().modifyPercent(this.ID, 1.0F + 0.5F * FLUX_LEVEL);
            if (ship.getFluxLevel() >= 0.01F && ship == playerShip) {
               engine.maintainStatusForPlayerShip(
                  this.ID,
                  Global.getSettings().getSpriteName("tooltips", "plasma_charged"),
                  "Plasma-Charged Shields:",
                  "+" + Math.round((0.0F + 0.5F * FLUX_LEVEL) * 100.0F) + "% Increased shield raise and turn speed",
                  false
               );
            }

            if (ship.getFluxLevel() >= 0.5F) {
               ship.getShield().getRingColor().brighter();
               ship.getShield().setInnerRotationRate(10.0F);
               ship.setJitterShields(true);
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship == null || ship.getVariant() == null) {
         return false;
      } else if (ship.getShield() == null) {
         return false;
      } else {
         if (SUPlugin.HASLUNALIB) {
            this.disableVanillaShieldHullmodsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableVanillaShieldHullmodsIncompatibilityToggle");
         }

         if (!this.disableVanillaShieldHullmodsIncompat && SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_VANILLA_IDS)
            )
          {
            return false;
         } else {
            return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
               ? false
               : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
         }
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (ship.getVariant().hasHullMod("shield_shunt")) {
            return "Incompatible with Shield Shunt";
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.disableVanillaShieldHullmodsIncompat = LunaSettings.getBoolean(
                  "mayu_specialupgrades", "shu_disableVanillaShieldHullmodsIncompatibilityToggle"
               );
            }

            if (!this.disableVanillaShieldHullmodsIncompat) {
               if (ship.getVariant().hasHullMod("hardenedshieldemitter")) {
                  return "Incompatible with Hardened Shields";
               }

               if (ship.getVariant().hasHullMod("extendedshieldemitter")) {
                  return "Incompatible with Extended Shields";
               }

               if (ship.getVariant().hasHullMod("stabilizedshieldemitter")) {
                  return "Incompatible with Stabilized Shields";
               }
            }

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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePlasmaDynamoCAToggle");
            this.shieldBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_BONUS");
            this.shieldRateBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_RATE");
            this.shieldUpkeepBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_UPKEEP_BONUS");
            this.shieldArcBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_ARC_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getShield() == null) {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"• The module has no standard shields, no bonus applied."}
            );
         } else {
            if (this.enableCustomSM) {
               tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
               tooltip.addPara(
                  "• Reduces damage taken by shields: %s\n• Improves shield raise and turn rate: %s\n• Reduces shield upkeep: %s\n• Increases the shield's coverage: %s degrees ",
                  SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{
                     Misc.getRoundedValue(this.shieldBonus) + "%",
                     Misc.getRoundedValue(this.shieldRateBonus) + "%",
                     Misc.getRoundedValue(this.shieldUpkeepBonus) + "%",
                     Misc.getRoundedValue(this.shieldArcBonus)
                  }
               );
            } else if (!this.enableCustomSM) {
               tooltip.addPara(
                  "• Reduces damage taken by shields: %s\n• Improves shield raise and turn rate: %s\n• Reduces shield upkeep: %s\n• Increases the shield's coverage: %s degrees ",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{
                     Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(60.0F) + "%", Misc.getRoundedValue(90.0F)
                  }
               );
            }

            if (!this.disableExtraEffect) {
               if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "plasma_charged"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  extrasystxt.addPara(
                     "Charged Acceleration",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                     new String[]{"Charged Acceleration"}
                  );
                  extrasystxt.addPara(
                     "The rising flux level energizes the shield emitter and this further %s the shield deployment and turn speed.",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getHighlightColor(),
                     new String[]{"boost"}
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  tooltip.addPara(
                     "• Additional shield turn rate bonus caps at %s.\n• Additional shield raise speed bonus caps at %s.",
                     SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
                  );
               }

               if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addPara(
                        "Press and hold [%s] to view its passive system.",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        Misc.getGrayColor(),
                        Misc.getStoryBrightColor(),
                        new String[]{"F1"}
                     )
                     .setAlignment(Alignment.MID);
               }
            }
         }

         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{
               "This is an extension of Plasma-Charged Shield Emitter and it will remove itself when the main hullmod is removed from the parent module."
            }
         );
      }
   }
}
