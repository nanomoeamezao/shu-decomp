package data.hullmods;

import com.fs.starfarer.api.Global;
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
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class SUAquaticStimulatorUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "aquaticstim_shu_sfc_check_";
   public static final String ITEM = "sfc_aquaticstimulator";
   public static final float ENERGY_DAMAGE_REDUCTION = 0.8F;
   public static final float EMP_DAMAGE_REDUCTION = 0.75F;
   public static final float CORONA_EFFECT_REDUCTION = 0.75F;
   private static final float FTR_EMP_DAMAGE_REDUCTION = 0.75F;
   private static final float FTR_ARMOR_BONUS = 100.0F;
   private static final float FTR_ARMOR_DAMAGE_TAKEN_REDUCTION = 0.8F;
   private static final float NANITE_PARTICLE_OPACITY = 0.65F;
   private static final float NANITE_PARTICLE_RADIUS = 100.0F;
   private static final float NANITE_PARTICLE_SIZE = 3.0F;
   private static final int MAX_NANITE_PARTICLES_PER_FRAME = 6;
   private static final Color NANITE_REPAIR_COLOR = new Color(180, 0, 120, 55);
   private final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_AQUATICSTIMULATOR_EXTRA_EFFECT;
   float BeamEnergyReduc = SUPlugin.CM_SFC_AQUATICSTIMULATOR_BEAM_DAMAGE_ENERGY_REDUCTION_BONUS;
   float EMPReduc = SUPlugin.CM_SFC_AQUATICSTIMULATOR_EMP_DAMAGE_REDUCTION_BONUS;
   float CoronaReduc = SUPlugin.CM_SFC_AQUATICSTIMULATOR_SOLAR_CORONA_STORM_NEGATIVE_REDUCTION_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_sfc_aquaticstimulator_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUAquaticStimulatorUpgrades() {
      this.ID = "SUAquaticStimulatorUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      boolean isSFCPresent = Global.getSettings().getModManager().isModEnabled("PAGSM");
      return isSFCPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("sfc_aquaticstimulator", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("sfc_aquaticstimulator").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("sfc_aquaticstimulator").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("sfc_aquaticstimulator").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.BeamEnergyReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_AQUATICSTIMULATOR_BEAM_DAMAGE_ENERGY_REDUCTION_BONUS");
         this.EMPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_AQUATICSTIMULATOR_EMP_DAMAGE_REDUCTION_BONUS");
         this.CoronaReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_AQUATICSTIMULATOR_SOLAR_CORONA_STORM_NEGATIVE_REDUCTION_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBeamDamageTakenMult().modifyMult(id, 1.0F - this.BeamEnergyReduc / 100.0F);
         stats.getBeamShieldDamageTakenMult().modifyMult(id, 1.0F - this.BeamEnergyReduc / 100.0F);
         stats.getEnergyDamageTakenMult().modifyMult(id, 1.0F - this.BeamEnergyReduc / 100.0F);
         stats.getEnergyShieldDamageTakenMult().modifyMult(id, 1.0F - this.BeamEnergyReduc / 100.0F);
         stats.getEmpDamageTakenMult().modifyMult(id, 1.0F - this.EMPReduc / 100.0F);
         stats.getDynamic().getStat("corona_resistance").modifyMult(id, 1.0F - this.CoronaReduc / 100.0F);
      } else if (!this.enableCustomSM) {
         stats.getBeamDamageTakenMult().modifyMult(id, 0.8F);
         stats.getBeamShieldDamageTakenMult().modifyMult(id, 0.8F);
         stats.getEnergyDamageTakenMult().modifyMult(id, 0.8F);
         stats.getEnergyShieldDamageTakenMult().modifyMult(id, 0.8F);
         stats.getEmpDamageTakenMult().modifyMult(id, 0.75F);
         stats.getDynamic().getStat("corona_resistance").modifyMult(id, 0.75F);
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAquaticStimulatorFRBToggle");
      }

      if (!this.disableExtraEffect) {
         fighter.getMutableStats().getEmpDamageTakenMult().modifyMult(id, 0.75F);
         fighter.getMutableStats().getArmorBonus().modifyFlat(id, 100.0F);
         fighter.getMutableStats().getArmorDamageTakenMult().modifyMult(id, 0.8F);
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

      if (ship.getVariant().getSMods().contains("specialsphmod_sfc_aquaticstimulator_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_sfc_aquaticstimulator_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_sfc_aquaticstimulator_upgrades");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAquaticStimulatorFRBToggle");
      }

      if (!this.disableExtraEffect) {
         if (!ship.isAlive()) {
            return;
         }

         for (ShipAPI fighter : this.getFighters(ship)) {
            if (!fighter.isHulk()) {
               MutableShipStatsAPI fStats = fighter.getMutableStats();
               float fighterHPLeft = fighter.getHitpoints() / fighter.getMaxHitpoints();
               if (fighterHPLeft < 0.75F && fighter.getFluxLevel() < 0.5F) {
                  fStats.getHullCombatRepairRatePercentPerSecond().modifyFlat(this.ID, 0.4F);
                  fStats.getMaxCombatHullRepairFraction().modifyFlat(this.ID, 0.75F);
                  Vector2f initialOffset = MathUtils.getRandomPointInCircle(null, 15.0F);
                  Vector2f specificOffset = MathUtils.getRandomPointInCircle(initialOffset, 10.0F);
                  fighter.addAfterimage(NANITE_REPAIR_COLOR, specificOffset.x, specificOffset.y, 0.0F, 0.0F, 0.05F, 0.1F, 0.05F, 0.1F, true, false, true);
                  int numParticlesThisFrame = Math.round(fighterHPLeft * 6.0F);

                  for (int x = 0; x < numParticlesThisFrame; x++) {
                     Vector2f particlePos = MathUtils.getRandomPointOnCircumference(fighter.getLocation(), 100.0F);
                     Vector2f particleVel = Vector2f.sub(fighter.getLocation(), particlePos, null);
                     Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, 3.0F, 0.65F, 1.0F, NANITE_REPAIR_COLOR);
                  }

                  Global.getCombatEngine()
                     .maintainStatusForPlayerShip(
                        "FORMERLY_SNEEDER",
                        Global.getSettings().getSpriteName("tooltips", "fighter_repair"),
                        "Fighter Repair Bots: Active",
                        "Repair in progress...",
                        false
                     );
               }
            }
         }

         super.advanceInCombat(ship, amount);
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
            return false;
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            } else {
               this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
            }

            return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
            return "Ship does not have standard fighter bays";
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
      } else {
         return "Unable to locate ship!";
      }
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return false;
      } else {
         boolean isSFCPresent = Global.getSettings().getModManager().isModEnabled("PAGSM");
         return !isSFCPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("sfc_aquaticstimulator")
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
         return !isSFCPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("sfc_aquaticstimulator")
            ? "Installation requires [Aquatic Stimulator] (1)"
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAquaticStimulatorFRBToggle");
            this.BeamEnergyReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_AQUATICSTIMULATOR_BEAM_DAMAGE_ENERGY_REDUCTION_BONUS");
            this.EMPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_AQUATICSTIMULATOR_EMP_DAMAGE_REDUCTION_BONUS");
            this.CoronaReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SFC_AQUATICSTIMULATOR_SOLAR_CORONA_STORM_NEGATIVE_REDUCTION_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces beam and energy damage taken: %s\n• Reduces EMP damage taken: %s\n• Resistance to solar corona and hyperspace storm: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.BeamEnergyReduc) + "%", Misc.getRoundedValue(this.EMPReduc) + "%", Misc.getRoundedValue(this.CoronaReduc) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces beam and energy damage taken: %s\n• Reduces EMP damage taken: %s\n• Resistance to solar corona and hyperspace storm: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(25.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "fighter_repair"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Fighter Repair Bots",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Fighter Repair Bots"}
               );
               text2.addPara(
                  "When a fighter's hull percentage falls below %s, the repair bots will be deployed to repair the damage. This recovers %s of the fighter's hull point every second until it reach %s again. Additionally, each deployed fighters is more sturdier and has increased resilience to EMP damage.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(75.0F) + "%", "0.4%", Misc.getRoundedValue(75.0F) + "%"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Increases fighter's armor rating: %s\n• Fighter's EMP damage taken reduced: %s\n• Fighter's armor has reduced damage taken: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(100.0F), Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(20.0F) + "%"}
               );
               tooltip.addPara(
                  "• The repair bots will only function when the fighter's flux level is below %s.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(50.0F) + "%"}
               );
               tooltip.addPara(
                     "%s",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
                     new String[]{
                        "\"...I'll be honest, if the higher ups find out what we're doing with these things, they're gonna ship us off to a labor camp for the rest of our days. So we better make sure that we have everything working perfectly before showing this off.\""
                     }
                  )
                  .italicize();
               tooltip.addPara(
                  "%s",
                  SUStringCodex.SHU_TOOLTIP_PADSIG,
                  SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
                  new String[]{"         — Recording of Sindrian Fuel Company Unpaid Labor Intern #CR-1244411"}
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

   private List<ShipAPI> getFighters(ShipAPI carrier) {
      List<ShipAPI> result = new ArrayList<>();

      for (ShipAPI ship : Global.getCombatEngine().getShips()) {
         if (ship.isFighter() && ship.getWing() != null && ship.getWing().getSourceShip() == carrier) {
            result.add(ship);
         }
      }

      return result;
   }
}
