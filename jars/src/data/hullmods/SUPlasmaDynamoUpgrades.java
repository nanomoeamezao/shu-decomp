package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUPlasmaDynamoUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "plasma_dynamo_shu_check_";
   public static final String ITEM = "plasma_dynamo";
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
      return super.spec.getId().equals("specialsphmod_plasmadynamo_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUPlasmaDynamoUpgrades() {
      this.ID = "SUPlasmaDynamoUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("plasma_dynamo", null), null);
   }

   public void addRequiredItemSection(
      TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt, float width, boolean isForModSpec
   ) {
      CargoStackAPI req = this.getRequiredItem();
      if (req != null) {
         float opad = 2.0F;
         if (isForModSpec || Global.CODEX_TOOLTIP_MODE) {
            Color color = Misc.getBasePlayerColor();
            if (isForModSpec) {
               color = Misc.getHighlightColor();
            }

            String name = req.getDisplayName();
            String aOrAn = Misc.getAOrAnFor(name);
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("plasma_dynamo").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("plasma_dynamo").getIconName(), 20.0F);
                  text2.addPara("Using item: " + req.getDisplayName(), color, opad);
                  tooltip.addImageWithText(5.0F);
               }
            } else {
               int available = HullModItemManager.getInstance().getNumAvailableMinusUnconfirmed(req, member, currentVariant, dockedAt);
               Color color = Misc.getPositiveHighlightColor();
               if (available < 1) {
                  color = Misc.getNegativeHighlightColor();
               }

               if (available < 0) {
                  available = 0;
               }

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("plasma_dynamo").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
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

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      ShipVariantAPI shipVariant = ship.getVariant();
      MutableCharacterStatsAPI currentShipStats = ship.getCaptain() == null ? null : ship.getCaptain().getStats();
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         for (String blockedMod : ALL_INCOMPAT_IDS) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               SUHullmodDisplayBlockScript.showBlocked(ship);
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
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_plasmadynamo_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_plasmadynamo_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_plasmadynamo_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_plasmadynamo_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_plasmadynamo_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_plasmadynamo_extension");
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
               ship.getShield().setInnerRotationRate(15.0F);
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
            if (SUPlugin.HASLUNALIB) {
               this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            } else {
               this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
            }

            return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
         }
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship == null || ship.getVariant() == null) {
         return "Unable to locate ship!";
      } else if (ship.getShield() == null) {
         return "Ship has no shields";
      } else if (ship.getVariant().hasHullMod("shield_shunt")) {
         return "Incompatible with Shield Shunt";
      } else {
         if (SUPlugin.HASLUNALIB) {
            this.disableVanillaShieldHullmodsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableVanillaShieldHullmodsIncompatibilityToggle");
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

         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         return this.toggleGeneralIncompat
               || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Only one type of special upgrade hullmod can be installed per ship";
      }
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0 ? false : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0
         ? "This installation is not applicable to modules, please install it on the main module"
         : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePlasmaDynamoCAToggle");
            this.disableVanillaShieldHullmodsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableVanillaShieldHullmodsIncompatibilityToggle");
            this.shieldBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_BONUS");
            this.shieldRateBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_RATE");
            this.shieldUpkeepBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_UPKEEP_BONUS");
            this.shieldArcBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PLASMADYNAMO_SHIELD_ARC_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
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

         boolean disableDestruction = SUPlugin.DISABLE_ITEMDESTRUCTION;
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            disableDestruction = LunaSettings.getBoolean("mayu_specialupgrades", "shu_itemdestructionToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         if (!this.toggleGeneralIncompat) {
            tooltip.addSectionHeading(
               "Incompatibilities",
               SUStringCodex.SHU_HULLMOD_NEGATIVE_TEXT_COLOR,
               SUStringCodex.SHU_HULLMOD_NEGATIVE_HEADER_BG,
               Alignment.MID,
               SUStringCodex.SHU_TOOLTIP_PADMAIN
            );
            TooltipMakerAPI text = tooltip.beginImageWithText(
               Global.getSettings().getSpriteName("tooltips", "hullmod_incompatible"), SUStringCodex.SHU_TOOLTIP_IMG
            );
            if (!this.disableVanillaShieldHullmodsIncompat) {
               text.addPara(
                  "Not compatible with %s, %s, %s, %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Extended Shields", "Hardened Shields", "Shield Shunt", "Stabilized Shields", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableVanillaShieldHullmodsIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Shield Shunt", "Other Special Upgrade Hullmods"}
               );
            }

            tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
         }

         if (!disableDestruction) {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADNOTE,
               Misc.getGrayColor(),
               new String[]{
                  "This hullmod counts as a special upgrade and it can work in conjunction with Armament Support System hullmod. Furthermore, the item is forever lost when the ship gets destroyed in combat."
               }
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADNOTE,
               Misc.getGrayColor(),
               new String[]{"This hullmod counts as a special upgrade and it can work in conjunction with Armament Support System hullmod."}
            );
         }
      }
   }
}
