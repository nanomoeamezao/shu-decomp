package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.ai.MegaCondenserAIScript;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class SUMegaCondenserUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "megacondenser_shu_sfc_check_";
   public static final String ITEM = "sfc_motemegacondenser";
   public static final float PHASE_ACT_BONUS = 20.0F;
   public static final float PHASE_UPKEEP_BONUS = 15.0F;
   public static final float PHASE_COOLDOWN_BONUS = 10.0F;
   public static final float FLUX_THRESHOLD_INCREASE_PERCENT = 60.0F;
   public final IntervalUtil moteInterval = new IntervalUtil(0.05F, 0.05F);
   public final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_MOTEMEGACONDENSER_EXTRA_EFFECT;
   boolean disableExtraEffectZigger = SUPlugin.DISABLE_MOTEMEGACONDENSER_ZIGGURAT_EXTRA_EFFECT;
   boolean disableAdaptivePhaseCoilsIncompat = SUPlugin.DISABLE_SFC_ADAPTIVEPHASECOILS_INCOMPATIBILITY;
   boolean disableAdaptivePhaseAnchorIncompat = SUPlugin.DISABLE_SFC_PHASEANCHOR_INCOMPATIBILITY;
   float phaseCDReduction = SUPlugin.CM_SFC_MOTEMEGACONDENSER_PHASE_CD_REDUCTION_BONUS;
   float phaseUpkeepReduction = SUPlugin.CM_SFC_MOTEMEGACONDENSER_PHASE_UPKEEP_REDUCTION_BONUS;
   float phaseActivationCostReduction = SUPlugin.CM_SFC_MOTEMEGACONDENSER_PHASE_ACTIVATION_COSE_REDUCTION_BONUS;
   float phaseHardFluxReduction = SUPlugin.CM_SFC_MOTEMEGACONDENSER_PHASE_HARDFLUX_IMPACT_REDUCTION_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_sfc_phasemote_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUMegaCondenserUpgrades() {
      this.ID = "SUMegaCondenserUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      boolean isSFCPresent = Global.getSettings().getModManager().isModEnabled("PAGSM");
      return isSFCPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("sfc_motemegacondenser", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("sfc_motemegacondenser").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("sfc_motemegacondenser").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("sfc_motemegacondenser").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.phaseCDReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_CD_REDUCTION_BONUS");
         this.phaseUpkeepReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_UPKEEP_REDUCTION_BONUS");
         this.phaseActivationCostReduction = LunaSettings.getFloat(
            "mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_ACTIVATION_COSE_REDUCTION_BONUS"
         );
         this.phaseHardFluxReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_HARDFLUX_IMPACT_REDUCTION_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getPhaseCloakCooldownBonus().modifyPercent(id, -this.phaseCDReduction);
         stats.getPhaseCloakUpkeepCostBonus().modifyPercent(id, -this.phaseUpkeepReduction);
         stats.getPhaseCloakActivationCostBonus().modifyPercent(id, -this.phaseActivationCostReduction);
         stats.getDynamic().getMod("phase_cloak_flux_level_for_min_speed_mod").modifyPercent(id, this.phaseHardFluxReduction);
      } else if (!this.enableCustomSM) {
         stats.getPhaseCloakCooldownBonus().modifyPercent(id, -10.0F);
         stats.getPhaseCloakUpkeepCostBonus().modifyPercent(id, -15.0F);
         stats.getPhaseCloakActivationCostBonus().modifyPercent(id, -20.0F);
         stats.getDynamic().getMod("phase_cloak_flux_level_for_min_speed_mod").modifyPercent(id, 60.0F);
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
         this.disableExtraEffectZigger = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMoteMegacondenserHFMToggle");
         this.disableAdaptivePhaseCoilsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCAdaptivePhaseCoilsIncompatibilityToggle");
         this.disableAdaptivePhaseAnchorIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCPhaseAnchorIncompatibilityToggle");
      }

      if (!this.disableAdaptivePhaseCoilsIncompat && ship.getVariant().getHullMods().contains("adaptive_coils")) {
         ship.getVariant().removeMod("adaptive_coils");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (!this.disableAdaptivePhaseAnchorIncompat && ship.getVariant().getHullMods().contains("phase_anchor")) {
         ship.getVariant().removeMod("phase_anchor");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (!this.disableExtraEffectZigger
         && ship.getHullSpec().getHullId().contains("ziggurat")
         && ship.getVariant().getHullMods().contains("specialsphmod_sfc_phasemote_upgrades")
         && !ship.getVariant().getHullMods().contains("high_frequency_attractor")) {
         ship.getVariant().addPermaMod("high_frequency_attractor");
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_sfc_phasemote_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_sfc_phasemote_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_sfc_phasemote_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_sfc_phasemote_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_sfc_phasemote_utilityscript");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMoteMegacondenserPAToggle");
      }

      if (!this.disableExtraEffect) {
         if (Global.getCombatEngine().isPaused()) {
            return;
         }

         if (!ship.isAlive() || ship.isPiece()) {
            return;
         }

         if (ship.isAlive()) {
            CombatEngineAPI engine = Global.getCombatEngine();
            String SFCAnomalyDataKey = ship.getId() + "_megacondenser_mote_data";
            Object targetDataObj = Global.getCombatEngine().getCustomData().get(SFCAnomalyDataKey);
            if (targetDataObj == null) {
               Global.getCombatEngine().getCustomData().put(SFCAnomalyDataKey, new SUMegaCondenserUpgrades.SUMCData(ship.getHullSize(), 10.0F, 1));
            } else if (ship.getHullSize() != ((SUMegaCondenserUpgrades.SUMCData)targetDataObj).size) {
               Global.getCombatEngine().getCustomData().remove(SFCAnomalyDataKey);
               Global.getCombatEngine().getCustomData().put(SFCAnomalyDataKey, new SUMegaCondenserUpgrades.SUMCData(ship.getHullSize(), 10.0F, 1));
            }

            SUMegaCondenserUpgrades.SUMCData data = (SUMegaCondenserUpgrades.SUMCData)targetDataObj;
            if (data == null) {
               return;
            }

            this.moteInterval.advance(amount);
            if (data.isReloading) {
               data.reloadingInterval.advance(amount);
               boolean hasElapsed = data.reloadingInterval.intervalElapsed();
               int elapsed = Math.round((float)((int)data.reloadingInterval.getElapsed()));
               if (!hasElapsed) {
                  String systitle = "Phase Anomalies";
                  engine.maintainStatusForPlayerShip(
                     this.ID + "_TOOLTIP",
                     Global.getSettings().getSpriteName("tooltips", "anomalous_motes"),
                     systitle,
                     "Conjuring motes in " + elapsed + " / 10 seconds",
                     false
                  );
                  return;
               }

               data.isReloading = false;
            }

            if (!this.moteInterval.intervalElapsed()) {
               return;
            }

            float variance = MathUtils.getRandomNumberInRange(-0.3F, 0.3F);
            Vector2f location = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, 90.0F);
            Vector2f location2 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, 180.0F);
            Vector2f location3 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, 360.0F);
            Vector2f location4 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, 270.0F);
            Global.getSoundPlayer().playSound("launch_flare_1", 1.0F + variance, 1.0F + variance, location, ship.getVelocity());
            HullSize shipSize = ship.getHullSize();
            if (null != shipSize) {
               switch (shipSize) {
                  case FRIGATE: {
                     MissileAPI newMote4 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location4, 270.0F, ship.getVelocity());
                     newMote4.setFromMissile(true);
                     newMote4.setMissileAI(new MegaCondenserAIScript(newMote4));
                     break;
                  }
                  case DESTROYER: {
                     MissileAPI newMote2 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location2, 180.0F, ship.getVelocity());
                     MissileAPI newMote3 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location4, 360.0F, ship.getVelocity());
                     newMote2.setFromMissile(true);
                     newMote3.setFromMissile(true);
                     newMote2.setMissileAI(new MegaCondenserAIScript(newMote2));
                     newMote3.setMissileAI(new MegaCondenserAIScript(newMote3));
                     break;
                  }
                  case CRUISER: {
                     MissileAPI newMote = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location, 90.0F, ship.getVelocity());
                     MissileAPI newMote2 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location2, 180.0F, ship.getVelocity());
                     MissileAPI newMote3 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location4, 360.0F, ship.getVelocity());
                     newMote.setFromMissile(true);
                     newMote2.setFromMissile(true);
                     newMote3.setFromMissile(true);
                     newMote.setMissileAI(new MegaCondenserAIScript(newMote));
                     newMote2.setMissileAI(new MegaCondenserAIScript(newMote2));
                     newMote3.setMissileAI(new MegaCondenserAIScript(newMote3));
                     break;
                  }
                  case CAPITAL_SHIP: {
                     MissileAPI newMote = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location, 90.0F, ship.getVelocity());
                     MissileAPI newMote2 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location2, 180.0F, ship.getVelocity());
                     MissileAPI newMote3 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location3, 360.0F, ship.getVelocity());
                     MissileAPI newMote4 = (MissileAPI)Global.getCombatEngine()
                        .spawnProjectile(ship, (WeaponAPI)null, "shu_sfc_mote", location4, 270.0F, ship.getVelocity());
                     newMote.setFromMissile(true);
                     newMote2.setFromMissile(true);
                     newMote3.setFromMissile(true);
                     newMote4.setFromMissile(true);
                     newMote.setMissileAI(new MegaCondenserAIScript(newMote));
                     newMote2.setMissileAI(new MegaCondenserAIScript(newMote2));
                     newMote3.setMissileAI(new MegaCondenserAIScript(newMote3));
                     newMote4.setMissileAI(new MegaCondenserAIScript(newMote4));
                  }
               }
            }

            data.motesLeft--;
            if (data.motesLeft <= 0) {
               data.isReloading = true;
               data.motesLeft = data.maxMotes;
               data.reloadingInterval.setElapsed(0.0F);
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship == null || ship.getVariant() == null) {
         return false;
      } else if (!ship.getHullSpec().isPhase()) {
         return false;
      } else {
         if (SUPlugin.HASLUNALIB) {
            this.disableAdaptivePhaseCoilsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCAdaptivePhaseCoilsIncompatibilityToggle");
            this.disableAdaptivePhaseAnchorIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCPhaseAnchorIncompatibilityToggle");
         }

         if (!this.disableAdaptivePhaseCoilsIncompat && ship.getVariant().hasHullMod("adaptive_coils")) {
            return false;
         } else if (!this.disableAdaptivePhaseAnchorIncompat && ship.getVariant().hasHullMod("phase_anchor")) {
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
      } else if (!ship.getHullSpec().isPhase()) {
         return "Can only be installed on phase ships";
      } else {
         if (SUPlugin.HASLUNALIB) {
            this.disableAdaptivePhaseCoilsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCAdaptivePhaseCoilsIncompatibilityToggle");
            this.disableAdaptivePhaseAnchorIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCPhaseAnchorIncompatibilityToggle");
         }

         if (!this.disableAdaptivePhaseCoilsIncompat && ship.getVariant().hasHullMod("adaptive_coils")) {
            return "Incompatible with Adaptive Phase Coils";
         } else if (!this.disableAdaptivePhaseAnchorIncompat && ship.getVariant().hasHullMod("phase_anchor")) {
            return "Incompatible with Phase Anchor";
         } else {
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
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return false;
      } else {
         boolean isSFCPresent = Global.getSettings().getModManager().isModEnabled("PAGSM");
         return !isSFCPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("sfc_motemegacondenser")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         boolean isSFCPresent = Global.getSettings().getModManager().isModEnabled("PAGSM");
         return !isSFCPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("sfc_motemegacondenser")
            ? "Installation requires [Mote Megacondenser] (1)"
            : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMoteMegacondenserPAToggle");
            this.disableExtraEffectZigger = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMoteMegacondenserHFMToggle");
            this.disableAdaptivePhaseCoilsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCAdaptivePhaseCoilsIncompatibilityToggle");
            this.disableAdaptivePhaseAnchorIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSFCPhaseAnchorIncompatibilityToggle");
            this.phaseCDReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_CD_REDUCTION_BONUS");
            this.phaseUpkeepReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_UPKEEP_REDUCTION_BONUS");
            this.phaseActivationCostReduction = LunaSettings.getFloat(
               "mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_ACTIVATION_COSE_REDUCTION_BONUS"
            );
            this.phaseHardFluxReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_MOTEMEGACONDENSER_PHASE_HARDFLUX_IMPACT_REDUCTION_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces phase cooldown: %s\n• Reduces phase upkeep cost: %s\n• Reduces phase activation cost: %s\n• Reduces impact of hard flux level on top speed while phased: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.phaseCDReduction) + "%",
                  Misc.getRoundedValue(this.phaseUpkeepReduction) + "%",
                  Misc.getRoundedValue(this.phaseActivationCostReduction) + "%",
                  Misc.getRoundedValue(this.phaseHardFluxReduction) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces phase cooldown: %s\n• Reduces phase upkeep cost: %s\n• Reduces phase activation cost: %s\n• Reduces impact of hard flux level on top speed while phased: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(15.0F) + "%", Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(60.0F) + "%"
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Extra System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "anomalous_motes"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Phase Anomalies",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Phase Anomalies"}
               );
               text2.addPara(
                  "For every %s seconds, the installed Mote Megacondenser will conjure anomalous motes. These motes will flutter around the ship and attack incoming missiles. Due to the complexity of the Mote Megacondenser and the nature of P-Space, there's still no definite explanation behind this phenomenon.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(10.0F)}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• The number of motes that can spawn depends on hull size: %s/%s/%s/%s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(1.0F), Misc.getRoundedValue(2.0F), Misc.getRoundedValue(3.0F), Misc.getRoundedValue(4.0F)}
               );
               tooltip.addPara(
                  "• Each motes will disappear after %s seconds.\n• Motes can deal %s damage on-hit.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(6.0F), "EMP"}
               );
               tooltip.addPara(
                     "%s",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
                     new String[]{
                        "\"That's not good enough! Just because we were able to actually observe the anomalies this time doesn't mean anything if we can't keep them around longer than a few seconds. Now do it again and keep the containment field off this time!\""
                     }
                  )
                  .italicize();
               tooltip.addPara(
                  "%s",
                  SUStringCodex.SHU_TOOLTIP_PADSIG,
                  SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
                  new String[]{"         — Yunris Kween on Project Asphodel before its discontinuation."}
               );
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addPara(
                     "Press and hold [%s] to view its extra system.",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     Misc.getGrayColor(),
                     Misc.getStoryBrightColor(),
                     new String[]{"F1"}
                  )
                  .setAlignment(Alignment.MID);
            }
         }

         if (!this.disableExtraEffectZigger) {
            if (ship.getHullSpec().getHullId().contains("ziggurat") && Keyboard.isKeyDown(Keyboard.getKeyIndex("F3"))) {
               tooltip.addSectionHeading("Additional Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpriteName("tooltips", "ziggy_hf"), SUStringCodex.SHU_TOOLTIP_IMG);
               text3.addPara(
                  "High Frequency Motes",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"High Frequency Motes"}
               );
               text3.addPara(
                  "Drawing power from the anomalous capabilities of the Ziggurat, the Mote Megacondenser is capable of greatly enhancing the motes summoned. This will revert the Ziggurat to its prime by reapplying the %s hullmod and furthermore, the spawned phase anomalies will also be controlled by the ship's system.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{"High Volition Attractor"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            }

            if (ship.getHullSpec().getHullId().contains("ziggurat") && !Keyboard.isKeyDown(Keyboard.getKeyIndex("F3"))) {
               tooltip.addPara(
                     "Press and hold [%s] to view its effect for the Ziggurat.",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     Misc.getGrayColor(),
                     Misc.getStoryBrightColor(),
                     new String[]{"F3"}
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
            if (!this.disableAdaptivePhaseCoilsIncompat && !this.disableAdaptivePhaseAnchorIncompat) {
               text.addPara(
                  "Not compatible with %s, %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Adaptive Phase Coils", "Phase Anchor", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableAdaptivePhaseCoilsIncompat && !this.disableAdaptivePhaseAnchorIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Phase Anchor", "Other Special Upgrade Hullmods"}
               );
            } else if (!this.disableAdaptivePhaseCoilsIncompat && this.disableAdaptivePhaseAnchorIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Adaptive Phase Coils", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableAdaptivePhaseCoilsIncompat && this.disableAdaptivePhaseAnchorIncompat) {
               text.addPara(
                  "Not compatible with %s", SUStringCodex.SHU_TOOLTIP_PADZERO, Misc.getNegativeHighlightColor(), new String[]{"Other Special Upgrade Hullmods"}
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

   public static class SUMCData {
      public HullSize size;
      public int motesLeft;
      public float reloadTime;
      public float maxReload;
      public int maxMotes;
      public boolean isReloading;
      public final IntervalUtil reloadingInterval;

      public SUMCData(HullSize size, float reloadTime, int motes) {
         this.size = size;
         this.reloadTime = 0.0F;
         this.maxReload = reloadTime;
         this.maxMotes = motes;
         this.motesLeft = motes;
         this.isReloading = false;
         this.reloadingInterval = new IntervalUtil(reloadTime, reloadTime);
      }
   }
}
