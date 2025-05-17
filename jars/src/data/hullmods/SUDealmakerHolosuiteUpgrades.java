package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Keyboard;
import org.magiclib.util.MagicRender;

public class SUDealmakerHolosuiteUpgrades extends BaseHullMod {
   protected Object STATUSKEY1;
   public static final String DATA_PREFIX = "dealmaker_holosuite_shu_check_";
   public static final String ITEM = "dealmaker_holosuite";
   private static final int MAX_FLEET_RELAY = 5;
   private static final float RECOVERY_BONUS = 150.0F;
   private static final float MAX_LINKED_RANGE = 9000.0F;
   private static final float LINKED_RANGE_BONUS_PERCENTAGE = 10.0F;
   private static final float LINKED_RANGE_BONUS_FLAT = 100.0F;
   private final String ID;
   private ShipAPI ship;
   private final List<ShipAPI> buffed;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_DRONEREPLICATORHMOD_EXTRA_EFFECT;
   float frigateSpeedNavRelay = SUPlugin.CM_DEALMAKER_FLEET_SPEED_FRIGATE_BONUS;
   float destroyerSpeedNavRelay = SUPlugin.CM_DEALMAKER_FLEET_SPEED_DESTROYER_BONUS;
   float cruiserSpeedNavRelay = SUPlugin.CM_DEALMAKER_FLEET_SPEED_CRUISER_BONUS;
   float capitalSpeedNavRelay = SUPlugin.CM_DEALMAKER_FLEET_SPEED_CAPITAL_BONUS;
   float frigateECM = SUPlugin.CM_DEALMAKER_ECM_FRIGATE_BONUS;
   float destroyerECM = SUPlugin.CM_DEALMAKER_ECM_DESTROYER_BONUS;
   float cruiserECM = SUPlugin.CM_DEALMAKER_ECM_CRUISER_BONUS;
   float capitalECM = SUPlugin.CM_DEALMAKER_ECM_CAPITAL_BONUS;
   float commandRecoveryBonus = SUPlugin.CM_DEALMAKER_COMMAND_RECOVERY_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private static final Map NAVHULL = new HashMap();
   private static final Map ECMHULL = new HashMap();

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_dealmakerholosuite_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUDealmakerHolosuiteUpgrades() {
      this.ID = "SUDealmakerHolosuiteUpgrades";
      this.STATUSKEY1 = new Object();
      this.buffed = new ArrayList<>();
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("dealmaker_holosuite", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("dealmaker_holosuite").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("dealmaker_holosuite").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("dealmaker_holosuite").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.frigateSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_FRIGATE_BONUS");
         this.destroyerSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_DESTROYER_BONUS");
         this.cruiserSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_CRUISER_BONUS");
         this.capitalSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_CAPITAL_BONUS");
         this.frigateECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_FRIGATE_BONUS");
         this.destroyerECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_DESTROYER_BONUS");
         this.cruiserECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_CRUISER_BONUS");
         this.capitalECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_CAPITAL_BONUS");
      }

      if (stats.getFleetMember() != null && stats.getFleetMember().getVariant() != null) {
         if (this.enableCustomSM) {
            HullSize shipSize = stats.getVariant().getHullSpec().getHullSize();
            if (shipSize != null) {
               switch (shipSize) {
                  case FRIGATE:
                     stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(id, this.frigateSpeedNavRelay);
                     stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, this.frigateECM);
                     break;
                  case DESTROYER:
                     stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(id, this.destroyerSpeedNavRelay);
                     stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, this.destroyerECM);
                     break;
                  case CRUISER:
                     stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(id, this.cruiserSpeedNavRelay);
                     stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, this.cruiserECM);
                     break;
                  case CAPITAL_SHIP:
                     stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(id, this.capitalSpeedNavRelay);
                     stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, this.capitalECM);
                     break;
                  case FIGHTER:
                     stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(id, 0.0F);
                     stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, 0.0F);
               }
            }
         } else if (!this.enableCustomSM) {
            stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(id, (Float)NAVHULL.get(hullSize));
            stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, (Float)ECMHULL.get(hullSize));
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
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_dealmakerholosuite_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_dealmakerholosuite_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_dealmakerholosuite_upgrades");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDealmakerHolosuiteDTUToggle");
         this.commandRecoveryBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_COMMAND_RECOVERY_BONUS");
      }

      MutableShipStatsAPI stats = ship.getMutableStats();
      CombatEngineAPI engine = Global.getCombatEngine();
      this.ship = (ShipAPI)stats.getEntity();
      boolean visible = MagicRender.screenCheck(0.1F, this.ship.getLocation());
      List<ShipAPI> nearby = AIUtils.getNearbyAllies(this.ship, 9000.0F);
      List<ShipAPI> previous = new ArrayList<>(this.buffed);
      if (engine != null) {
         if (Global.getCurrentState() == GameState.COMBAT && engine.isEntityInPlay(ship) && ship.isAlive()) {
            if (!engine.isPaused()) {
               CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
               if (manager != null) {
                  DeployedFleetMemberAPI member = manager.getDeployedFleetMember(ship);
                  if (member != null) {
                     boolean apply = ship == engine.getPlayerShip();
                     PersonAPI commander = null;
                     if (member.getMember() != null) {
                        commander = member.getMember().getFleetCommander();
                        if (member.getMember().getFleetCommanderForStats() != null) {
                           commander = member.getMember().getFleetCommanderForStats();
                        }
                     }

                     apply |= commander != null && ship.getCaptain() == commander;
                     if (apply) {
                        if (this.enableCustomSM) {
                           ship.getMutableStats().getDynamic().getMod("command_point_rate_flat").modifyFlat(this.ID, this.commandRecoveryBonus * 0.01F);
                        } else if (!this.enableCustomSM) {
                           ship.getMutableStats().getDynamic().getMod("command_point_rate_flat").modifyFlat(this.ID, 1.5F);
                        }
                     } else {
                        ship.getMutableStats().getDynamic().getMod("command_point_rate_flat").unmodify(this.ID);
                     }

                     if (!this.disableExtraEffect) {
                        int fleetTacticalRelay = 0;

                        for (ShipAPI interlinkedTargeting : CombatUtils.getShipsWithinRange(ship.getLocation(), 9000.0F)) {
                           if (interlinkedTargeting.getVariant().getHullMods().contains("specialsphmod_dealmakerholosuite_upgrades")
                              && interlinkedTargeting.isAlive()
                              && !interlinkedTargeting.isHulk()) {
                              fleetTacticalRelay++;
                           }

                           if (fleetTacticalRelay == 5) {
                              break;
                           }
                        }

                        float effectMult = fleetTacticalRelay;
                        ship.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(this.ID, 100.0F * effectMult);
                        ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat(this.ID, 100.0F * effectMult);
                        ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(this.ID, 100.0F * effectMult);
                        int rangeBonus = (int)(10.0F * effectMult);
                        Global.getCombatEngine()
                           .maintainStatusForPlayerShip(
                              this.ID + "_SHU_FTR_TOOLTIP",
                              Global.getSettings().getSpriteName("tooltips", "interlink_targeting"),
                              "Distributed Targeting Uplink",
                              "Weapon range increased: " + rangeBonus + "%",
                              false
                           );
                        if (!nearby.isEmpty()) {
                           for (ShipAPI affected : nearby) {
                              if (!previous.contains(affected) && !affected.isFighter()) {
                                 this.interlinkedTargetEffect(affected, this.ship, 5.0F, visible);
                                 this.buffed.add(affected);
                              }

                              if (previous.contains(affected)) {
                                 previous.remove(affected);
                                 this.interlinkedTargetEffect(affected, this.ship, 5.0F, visible);
                              }

                              if (affected == Global.getCombatEngine().getPlayerShip()) {
                                 Global.getCombatEngine()
                                    .maintainStatusForPlayerShip(
                                       this.ID + "_SHU_FTR_TOOLTIP",
                                       Global.getSettings().getSpriteName("tooltips", "interlink_targeting"),
                                       "Distributed Targeting Uplink",
                                       "Weapon range increased: " + rangeBonus + "%",
                                       false
                                    );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
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
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDealmakerHolosuiteDTUToggle");
            this.frigateSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_FRIGATE_BONUS");
            this.destroyerSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_DESTROYER_BONUS");
            this.cruiserSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_CRUISER_BONUS");
            this.capitalSpeedNavRelay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_FLEET_SPEED_CAPITAL_BONUS");
            this.frigateECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_FRIGATE_BONUS");
            this.destroyerECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_DESTROYER_BONUS");
            this.cruiserECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_CRUISER_BONUS");
            this.capitalECM = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_ECM_CAPITAL_BONUS");
            this.commandRecoveryBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DEALMAKER_COMMAND_RECOVERY_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Grants bonus ECM rating in combat: %s/%s/%s/%s\n• Increases top speed of allies in combat: %s/%s/%s/%s\n• Increases command point recovery rate: %s (flagship)",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.frigateECM) + "%",
                  Misc.getRoundedValue(this.destroyerECM) + "%",
                  Misc.getRoundedValue(this.cruiserECM) + "%",
                  Misc.getRoundedValue(this.capitalECM) + "%",
                  Misc.getRoundedValue(this.frigateSpeedNavRelay) + "%",
                  Misc.getRoundedValue(this.destroyerSpeedNavRelay) + "%",
                  Misc.getRoundedValue(this.cruiserSpeedNavRelay) + "%",
                  Misc.getRoundedValue(this.capitalSpeedNavRelay) + "%",
                  Misc.getRoundedValue(this.commandRecoveryBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Grants bonus ECM rating in combat: %s/%s/%s/%s\n• Increases top speed of allies in combat: %s/%s/%s/%s\n• Increases command point recovery rate: %s (flagship)",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(4.0F) + "%",
                  Misc.getRoundedValue(6.0F) + "%",
                  Misc.getRoundedValue(8.0F) + "%",
                  Misc.getRoundedValue(10.0F) + "%",
                  Misc.getRoundedValue(5.0F) + "%",
                  Misc.getRoundedValue(10.0F) + "%",
                  Misc.getRoundedValue(15.0F) + "%",
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(150.0F) + "%"
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "interlink_targeting"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Distributed Targeting Uplink",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Distributed Targeting Uplink"}
               );
               text2.addPara(
                  "A special targeting unit that extends the maximum range of all non-missile weapons in the fleet by %s in combat. When linked with another ship equipped with the Fleet Tactical Relay, this bonus increases up to a maximum of %s. The bonus will only take effect %s.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(50.0F) + "%", "in combat"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
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

   private void interlinkedTargetEffect(ShipAPI ship, ShipAPI source, float level, boolean visible) {
      int fleetTacticalRelay = 0;

      for (ShipAPI interlinkedTargeting : CombatUtils.getShipsWithinRange(ship.getLocation(), 9000.0F)) {
         if (interlinkedTargeting.getVariant().getHullMods().contains("specialsphmod_dealmakerholosuite_upgrades") && !interlinkedTargeting.isHulk()) {
            fleetTacticalRelay++;
         }

         if (fleetTacticalRelay == 5) {
            break;
         }
      }

      float effectMult = fleetTacticalRelay;
      ship.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(this.ID, 100.0F * effectMult);
      ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat(this.ID, 100.0F * effectMult);
      ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(this.ID, 100.0F * effectMult);
   }

   static {
      NAVHULL.put(HullSize.FIGHTER, 0.0F);
      NAVHULL.put(HullSize.FRIGATE, 4.0F);
      NAVHULL.put(HullSize.DESTROYER, 6.0F);
      NAVHULL.put(HullSize.CRUISER, 8.0F);
      NAVHULL.put(HullSize.CAPITAL_SHIP, 10.0F);
      ECMHULL.put(HullSize.FIGHTER, 0.0F);
      ECMHULL.put(HullSize.FRIGATE, 5.0F);
      ECMHULL.put(HullSize.DESTROYER, 10.0F);
      ECMHULL.put(HullSize.CRUISER, 15.0F);
      ECMHULL.put(HullSize.CAPITAL_SHIP, 20.0F);
   }
}
