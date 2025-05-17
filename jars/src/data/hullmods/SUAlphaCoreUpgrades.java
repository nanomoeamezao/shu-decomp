package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
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
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUAlphaCoreUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "alpha_core_shu_check_";
   public static final String ITEM = "alpha_core";
   private static final float AUTOFIRE_BONUS = 60.0F;
   private static final float TURRET_TURN_BONUS = 70.0F;
   private static final float COST_REDUCTION_LG = 4.0F;
   private static final float COST_REDUCTION_MED = 3.0F;
   private static final float COST_REDUCTION_SM = 2.0F;
   private static final float FTR_DAMAGE_BONUS = 50.0F;
   private static final float MSL_DAMAGE_BONUS = 50.0F;
   private static final float MAX_CR_STRKCRFT = 0.15F;
   private static final float TIME_MULT = 15.0F;
   private final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableDPCostPenalty = SUPlugin.DISABLE_DP_MALUS;
   float autoFireBonus = SUPlugin.CM_ALPHA_AUTOFIRE_BONUS;
   float turretTurnBonus = SUPlugin.CM_ALPHA_TURRET_TURN_BONUS;
   float largeOPReduc = SUPlugin.CM_ALPHA_COST_REDUCTION_LG;
   float mediumOPReduc = SUPlugin.CM_ALPHA_COST_REDUCTION_MED;
   float smallOPReduc = SUPlugin.CM_ALPHA_COST_REDUCTION_SM;
   boolean toggleGeneralIncompat;
   private static final String[] INCOMPAT_CORES = new String[]{};
   private static final Map HULLDP = new HashMap();

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_alpha_core_upgrades") ? INCOMPAT_CORES : null;
   }

   public SUAlphaCoreUpgrades() {
      this.ID = "SUAlphaCoreUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.RESOURCES, "alpha_core", null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getCommoditySpec("alpha_core").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getCommoditySpec("alpha_core").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getCommoditySpec("alpha_core").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.disableDPCostPenalty = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableIncreasedDPAICoresToggle");
         this.autoFireBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_AUTOFIRE_BONUS");
         this.turretTurnBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_TURRET_TURN_BONUS");
         this.largeOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_COST_REDUCTION_LG");
         this.mediumOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_COST_REDUCTION_MED");
         this.smallOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_COST_REDUCTION_SM");
      }

      if (this.enableCustomSM) {
         stats.getAutofireAimAccuracy().modifyFlat(id, this.autoFireBonus * 0.01F);
         stats.getWeaponTurnRateBonus().modifyPercent(id, this.turretTurnBonus);
         stats.getBeamWeaponTurnRateBonus().modifyPercent(id, SUPlugin.CM_ALPHA_TURRET_TURN_BONUS);
         stats.getDynamic().getMod("large_ballistic_mod").modifyFlat(id, -this.largeOPReduc);
         stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -this.largeOPReduc);
         stats.getDynamic().getMod("medium_ballistic_mod").modifyFlat(id, -this.mediumOPReduc);
         stats.getDynamic().getMod("medium_energy_mod").modifyFlat(id, -this.mediumOPReduc);
         stats.getDynamic().getMod("small_ballistic_mod").modifyFlat(id, -this.smallOPReduc);
         stats.getDynamic().getMod("small_energy_mod").modifyFlat(id, -this.smallOPReduc);
      } else if (!this.enableCustomSM) {
         stats.getAutofireAimAccuracy().modifyFlat(id, 0.59999996F);
         stats.getWeaponTurnRateBonus().modifyPercent(id, 70.0F);
         stats.getBeamWeaponTurnRateBonus().modifyPercent(id, 70.0F);
         stats.getDynamic().getMod("large_ballistic_mod").modifyFlat(id, -4.0F);
         stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -4.0F);
         stats.getDynamic().getMod("medium_ballistic_mod").modifyFlat(id, -3.0F);
         stats.getDynamic().getMod("medium_energy_mod").modifyFlat(id, -3.0F);
         stats.getDynamic().getMod("small_ballistic_mod").modifyFlat(id, -2.0F);
         stats.getDynamic().getMod("small_energy_mod").modifyFlat(id, -2.0F);
      }

      if (!this.disableDPCostPenalty) {
         stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, (Float)HULLDP.get(hullSize));
      }

      if (stats.getVariant().hasHullMod("strikeCraft")) {
         stats.getMaxCombatReadiness().modifyFlat(id, 0.15F, "Armament Support System (Alpha)");
      }

      if (stats.getVariant().getHullMods().contains("yunru_alphacore")) {
         stats.getDamageToMissiles().modifyPercent(id, 50.0F);
         stats.getDamageToFighters().modifyPercent(id, 50.0F);
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
         for (String blockedMod : INCOMPAT_CORES) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_alpha_core_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_alpha_core_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_alpha_core_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_alphacore_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_alpha_core_module_extension");
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (!Global.getCombatEngine().isPaused()) {
         boolean player = ship == Global.getCombatEngine().getPlayerShip();
         if (ship.isAlive() && !ship.isPiece()) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            if (ship.getHullSize() == HullSize.FRIGATE && ship.getHullSpec().getHullId().startsWith("armaa_")
               || ship.getHullSize() == HullSize.FRIGATE && ship.getVariant().getHullMods().contains("strikeCraft")) {
               if (player) {
                  stats.getTimeMult().modifyPercent(this.ID, 15.0F);
                  stats.getTurnAcceleration().modifyMult(this.ID, 1.5F);
                  stats.getFluxDissipation().modifyMult(this.ID, 1.5F);
                  stats.getShieldUpkeepMult().modifyMult(this.ID, 0.5F);
                  stats.getShieldTurnRateMult().modifyMult(this.ID, 1.5F);
                  stats.getShieldUnfoldRateMult().modifyMult(this.ID, 1.5F);
                  stats.getPhaseCloakUpkeepCostBonus().modifyMult(this.ID, 0.5F);
                  stats.getPhaseCloakCooldownBonus().modifyMult(this.ID, 0.5F);
                  stats.getCombatEngineRepairTimeMult().modifyMult(this.ID, 0.5F);
                  stats.getCombatWeaponRepairTimeMult().modifyMult(this.ID, 0.5F);
                  Global.getCombatEngine().getTimeMult().modifyPercent(this.ID, 0.06666667F);
                  Global.getCombatEngine()
                     .maintainStatusForPlayerShip(
                        this.ID + "_TOOLTIP_", Global.getSettings().getSpriteName("tooltips", "ains_system"), "Time Accel", "Timeflow increased: 15%", false
                     );
               } else {
                  stats.getTimeMult().modifyPercent(this.ID, 15.0F);
                  stats.getTurnAcceleration().modifyMult(this.ID, 1.5F);
                  stats.getFluxDissipation().modifyMult(this.ID, 1.5F);
                  stats.getShieldUpkeepMult().modifyMult(this.ID, 0.5F);
                  stats.getShieldTurnRateMult().modifyMult(this.ID, 1.5F);
                  stats.getShieldUnfoldRateMult().modifyMult(this.ID, 1.5F);
                  stats.getPhaseCloakUpkeepCostBonus().modifyMult(this.ID, 0.5F);
                  stats.getPhaseCloakCooldownBonus().modifyMult(this.ID, 0.5F);
                  stats.getCombatEngineRepairTimeMult().modifyMult(this.ID, 0.5F);
                  stats.getCombatWeaponRepairTimeMult().modifyMult(this.ID, 0.5F);
                  Global.getCombatEngine().getTimeMult().unmodify(this.ID);
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

         return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), INCOMPAT_CORES);
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
               || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), INCOMPAT_CORES)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Incompatible with installed AI core";
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
      return status == 0
         ? "This installation is not applicable to modules, please install it on the main module"
         : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return index == 0 ? "Alpha-class AI" : null;
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableDPCostPenalty = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableIncreasedDPAICoresToggle");
            this.autoFireBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_AUTOFIRE_BONUS");
            this.turretTurnBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_TURRET_TURN_BONUS");
            this.largeOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_COST_REDUCTION_LG");
            this.mediumOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_COST_REDUCTION_MED");
            this.smallOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_ALPHA_COST_REDUCTION_SM");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Improves autofire accuracy: %s\n• Increases turret turn rate: %s\n• Reduces OP cost for non-missile weapons: %s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.autoFireBonus) + "%",
                  Misc.getRoundedValue(this.turretTurnBonus) + "%",
                  Misc.getRoundedValue(this.smallOPReduc),
                  Misc.getRoundedValue(this.mediumOPReduc),
                  Misc.getRoundedValue(this.largeOPReduc)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Improves autofire accuracy: %s\n• Increases turret turn rate: %s\n• Reduces OP cost for non-missile weapons: %s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(60.0F) + "%",
                  Misc.getRoundedValue(70.0F) + "%",
                  Misc.getRoundedValue(2.0F),
                  Misc.getRoundedValue(3.0F),
                  Misc.getRoundedValue(4.0F)
               }
            );
         }

         if (!this.disableDPCostPenalty) {
            tooltip.addPara(
               "• Increased deployment cost: %s",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{Misc.getRoundedValue(15.0F) + "%"}
            );
         }

         if (Global.getSettings().getModManager().isModEnabled("yunrucore")) {
            if (ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")
               && ship.getVariant().getHullMods().contains("yunru_alphacore")
               && Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Core Synchronization Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI withyunrualpha = tooltip.beginImageWithText(Global.getSettings().getHullModSpec("yunru_alphacore").getSpriteName(), 36.0F);
               withyunrualpha.addPara(
                  "A ship with %s has been detected, and having multiple Alpha cores installed together will unlock their latent synchronization ability. The following additional effects will be applied.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  SUStringCodex.SHU_HULLMOD_ALPHA_NAME,
                  new String[]{"Integrated Alpha Core"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Increased damage to fighters: %s\n• Increased damage to missiles: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
               );
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))
               && ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")
               && ship.getVariant().getHullMods().contains("yunru_alphacore")) {
               tooltip.addPara(
                     "Press and hold [%s] to view the unlocked bonus.",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     Misc.getGrayColor(),
                     Misc.getStoryBrightColor(),
                     new String[]{"F1"}
                  )
                  .setAlignment(Alignment.MID);
            }
         }

         if (Global.getSettings().getModManager().isModEnabled("armaa")) {
            if (ship.getHullSize() == HullSize.FRIGATE
               && ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")
               && ship.getVariant().hasHullMod("strikeCraft")
               && Keyboard.isKeyDown(Keyboard.getKeyIndex("F3"))) {
               tooltip.addSectionHeading("A.I.N.S.", Misc.getTextColor(), SUStringCodex.SHU_TOOLTIP_ARMAA, Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI witharmaa = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "ains_system"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               witharmaa.addPara(
                  "The Artificial Intelligence Neural Synthesis is a support system specialized for strikecrafts. It forcibly enhances a normal pilot by connecting the person's mind to the installed AI core. This vastly improves the pilot's response time and enables the linked pilot to surpass the limits of humans.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Increases max combat readiness: %s\n• Accelerates the perception of time: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(15.0F) + "%", Misc.getRoundedValue(15.0F) + "%"}
               );
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F3"))
               && ship.getHullSize() == HullSize.FRIGATE
               && ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")
               && ship.getVariant().hasHullMod("strikeCraft")) {
               tooltip.addPara(
                     "Press and hold [%s] to view its effects for strikecraft.",
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
            text.addPara(
               "Not compatible with %s, %s",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               Misc.getNegativeHighlightColor(),
               new String[]{"Armament Support System (Beta)", "Armament Support System (Gamma)"}
            );
            tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
         }

         if (!disableDestruction) {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADNOTE,
               Misc.getGrayColor(),
               new String[]{
                  "This hullmod can work in conjunction with special upgrade hullmods. Furthermore, the AI core is forever lost when the ship gets destroyed in combat."
               }
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADNOTE,
               Misc.getGrayColor(),
               new String[]{"This hullmod can work in conjunction with special upgrade hullmods."}
            );
         }
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_ALPHA_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_ALPHA_NAME;
   }

   static {
      HULLDP.put(HullSize.FIGHTER, 0.0F);
      HULLDP.put(HullSize.FRIGATE, 15.0F);
      HULLDP.put(HullSize.DESTROYER, 15.0F);
      HULLDP.put(HullSize.CRUISER, 15.0F);
      HULLDP.put(HullSize.CAPITAL_SHIP, 15.0F);
   }
}
