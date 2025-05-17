package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
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
import data.scripts.everyframe.SUBioFactoryProductionScript;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUBiofactoryEmbryoUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "biofactory_embryo_shu_check_";
   public static final String ITEM = "biofactory_embryo";
   public static final String ORGANICS = "organics";
   public static final float CASUALTY_REDUCTION = 25.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_BIOFACTORYHMOD_EXTRA_EFFECT;
   int RequiredOrganics = (int)SUPlugin.CM_BIOFACTORY_REQUIRED_ORGANICS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_biofactoryembryo_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("biofactory_embryo", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("biofactory_embryo").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("biofactory_embryo").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("biofactory_embryo").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      stats.getCrewLossMult().modifyMult(id, 0.75F);
      stats.getDynamic().getStat("fighter_crew_loss_mult").modifyMult(id, 0.75F);
      if (Global.getSettings().getModManager().isModEnabled("Sunrider") && stats.getVariant().getHullSpec().getBaseHullId().contains("Sunridership")) {
         stats.getCrewLossMult().modifyPercent(id, -25.0F);
         stats.getDynamic().getStat("fighter_crew_loss_mult").modifyPercent(id, -25.0F);
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
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableBiofactoryEmbryoFHToggle");
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_biofactoryembryo_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_biofactoryembryo_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_biofactoryembryo_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_upgrades") && !this.disableExtraEffect) {
         ship.getVariant().addPermaMod("specialsphmod_biofactory_fleet_med");
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
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
      } else {
         return "Unable to locate ship!";
      }
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0 ? false : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0 ? "This installation is not applicable to modules" : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      FleetMemberAPI currShips = null;
      if (!isForModSpec && playerFleet != null && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableBiofactoryEmbryoFHToggle");
            this.RequiredOrganics = LunaSettings.getInt("mayu_specialupgrades", "LUNA_CM_BIOFACTORY_REQUIRED_ORGANICS");
         }

         float organicsCount = Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity("organics");
         boolean hasBiofactoryHMOD = ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_upgrades");
         String fleetPlayerID = ship.getFleetMemberId();
         if (fleetPlayerID != null) {
            for (FleetMemberAPI fleetMember : playerFleet.getFleetData().getMembersListCopy()) {
               if (fleetMember.getId().equals(fleetPlayerID)) {
                  currShips = fleetMember;
                  break;
               }
            }
         }

         if (currShips != null) {
            boolean isMothballed = currShips.isMothballed();
            float currentFleetCapacity = SUBioFactoryProductionScript.getCapacity(playerFleet);
            tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
            if (hasBiofactoryHMOD && isMothballed) {
               tooltip.addPara(
                  "%s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"• This ship is currently mothballed, preventing the Biologic Commodity Replicator from functioning."}
               );
            } else {
               if (this.enableCustomSM) {
                  tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
                  tooltip.addPara(
                     "• Converts %s units of organics into %s unit of organs.\n• Production report will be generated every %s days.\n• Production capacity per hull size: %s/%s/%s/%s\n• Your fleet can currently use %s organics for conversion.",
                     SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{
                        Misc.getRoundedValue(this.RequiredOrganics),
                        Misc.getRoundedValue(1.0F),
                        Misc.getRoundedValue(10.0F),
                        Misc.getRoundedValue(50.0F),
                        Misc.getRoundedValue(100.0F),
                        Misc.getRoundedValue(200.0F),
                        Misc.getRoundedValue(300.0F),
                        (int)currentFleetCapacity + ""
                     }
                  );
               } else if (!this.enableCustomSM) {
                  tooltip.addPara(
                     "• Converts %s units of organics into %s unit of organs.\n• Production report will be generated every %s days.\n• Production capacity per hull size: %s/%s/%s/%s\n• Your fleet can currently use %s organics for conversion.",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{
                        Misc.getRoundedValue(8.0F),
                        Misc.getRoundedValue(1.0F),
                        Misc.getRoundedValue(10.0F),
                        Misc.getRoundedValue(50.0F),
                        Misc.getRoundedValue(100.0F),
                        Misc.getRoundedValue(200.0F),
                        Misc.getRoundedValue(300.0F),
                        (int)currentFleetCapacity + ""
                     }
                  );
               }

               TooltipMakerAPI organicsText = tooltip.beginImageWithText(Global.getSettings().getSpriteName("tooltips", "organics"), 30.0F);
               organicsText.addPara(
                  "The conversion process requires organics and your fleet currently has %s units of organics in its cargo holds.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{(int)organicsCount + ""}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            }

            if (!this.disableExtraEffect) {
               if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addSectionHeading("Fleet-Wide Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "fleet_hospital"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  extrasystxt.addPara(
                     "Fleet Hospital",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                     new String[]{"Fleet Hospital"}
                  );
                  extrasystxt.addPara(
                     "This vessel can function as a makeshift hospital and the Biofactory can provide its needed organs and medical materials for immediate surgeries. The fleet-wide effect %s with another Biologic Commodity Replicator and has a diminishing return.",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getHighlightColor(),
                     new String[]{"stacks"}
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  tooltip.addPara(
                     "• Reduces chance of crew casualty: %s\n• Reduces chance of fighter crew casualty: %s",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(25.0F) + "%"}
                  );
                  if (Global.getSettings().getModManager().isModEnabled("Sunrider") && ship.getVariant().getHullSpec().getBaseHullId().contains("Sunridership")
                     )
                   {
                     tooltip.addSectionHeading("Sunrider Interaction", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     TooltipMakerAPI extrasyssunrider = tooltip.beginImageWithText(
                        Global.getSettings().getSpriteName("tooltips", "sunrider_collaboration"), SUStringCodex.SHU_TOOLTIP_IMG
                     );
                     extrasyssunrider.addPara(
                        "Enhanced Medical Facilities",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                        new String[]{"Enhanced Medical Facilities"}
                     );
                     extrasyssunrider.addPara(
                        "The Sunrider houses cutting-edge medical facilities and equipment created by otherworldly technology, this increases the fleet hospital's efficiency and capabilities. The fleet-wide effects are further increased by %s.",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Misc.getHighlightColor(),
                        new String[]{Misc.getRoundedValue(25.0F) + "%"}
                     );
                     tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  }
               }

               if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addPara(
                        "Press and hold [%s] to view its fleet-wide effect.",
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
               text.addPara(
                  "Not compatible with %s", SUStringCodex.SHU_TOOLTIP_PADZERO, Misc.getNegativeHighlightColor(), new String[]{"Other Special Upgrade Hullmods"}
               );
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
}
