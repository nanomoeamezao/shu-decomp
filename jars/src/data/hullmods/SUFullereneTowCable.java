package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SUFullereneTowCable implements HullModEffect {
   public static final String SHU_TOW_CABLE_KEY = "Fullerene_TowCable_PersistentBuffs";
   public static final Float FUEL_REDUC = 20.0F;
   public static final Float TOW_BONUS = 1.0F;

   public void init(HullModSpecAPI spec) {
   }

   public void addRequiredItemSection(TooltipMakerAPI tmapi, FleetMemberAPI fmapi, ShipVariantAPI svapi, MarketAPI mapi, float f, boolean bln) {
   }

   public CargoStackAPI getRequiredItem() {
      return null;
   }

   public void applyEffectsAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getMaxBurnLevel().getBaseValue() <= 7.0F) {
         stats.getMaxBurnLevel().modifyFlat(id, TOW_BONUS);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (!ship.getVariant().hasHullMod("specialsphmod_fullerenespool_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_fullerene_tow_cable");
         ship.getVariant().removeMod("specialsphmod_fullerene_tow_cable");
      }
   }

   public void advanceInCampaign(FleetMemberAPI member, float amount) {
      if (member != null) {
         if (member.getFleetData() != null) {
            if (member.getFleetData().getFleet() != null) {
               if (member.getFleetData().getFleet().isPlayerFleet()) {
                  if (!member.getVariant().getHullMods().contains("specialsphmod_fullerene_tow_cable")) {
                     this.cleanUpTowCableBuffBy(member);
                  } else if (!member.canBeDeployedForCombat()) {
                     this.cleanUpTowCableBuffBy(member);
                  } else {
                     FleetDataAPI data = member.getFleetData();
                     List<FleetMemberAPI> all = data.getMembersListCopy();
                     int numCables = 0;
                     int thisCableIndex = -1;

                     for (FleetMemberAPI curr : all) {
                        if (curr.canBeDeployedForCombat() && curr.getVariant().getHullMods().contains("specialsphmod_fullerene_tow_cable")) {
                           if (curr == member) {
                              thisCableIndex = numCables;
                           }

                           numCables++;
                           break;
                        }
                     }

                     if (numCables > 0 && thisCableIndex != -1) {
                        SUFullereneTowCable.FullereneTowCableBuff buff = this.getTowCableBuffBy(member, true);
                        Map<FleetMemberAPI, Integer> cables = new HashMap<>();
                        float towSpeed = member.getStats().getMaxBurnLevel().getModifiedValue();
                        FleetMemberAPI thisCableTarget = null;

                        for (int cableIndex = 0; cableIndex < numCables; cableIndex++) {
                           FleetMemberAPI slowest = this.getSlowest(all, towSpeed, cables);
                           if (slowest == null) {
                              break;
                           }

                           Integer bonus = cables.get(slowest);
                           if (bonus == null) {
                              bonus = 0;
                           }

                           bonus = bonus + 1;
                           cables.put(slowest, bonus);
                           if (cableIndex == thisCableIndex) {
                              thisCableTarget = slowest;
                              Buff existing = slowest.getBuffManager().getBuff(buff.getId());
                              if (existing == buff) {
                                 buff.frames = 0;
                                 System.out.println("renewed on " + slowest);
                              } else {
                                 buff.frames = 0;
                                 slowest.getBuffManager().addBuff(buff);
                                 if (slowest.isMothballed()) {
                                    buff.frames = 0;
                                 }

                                 System.out.println("Num: " + slowest.getBuffManager().getBuffs().size());
                                 System.out.println("added to " + slowest);
                              }

                              data.setForceNoSync(true);
                              break;
                           }
                        }

                        for (FleetMemberAPI currx : all) {
                           if (currx != thisCableTarget) {
                              currx.getBuffManager().removeBuff(buff.getId());
                              currx.updateStats();
                              data.setForceNoSync(true);
                              break;
                           }
                        }
                     } else {
                        this.cleanUpTowCableBuffBy(member);
                     }
                  }
               }
            }
         }
      }
   }

   private FleetMemberAPI getSlowest(List<FleetMemberAPI> all, float speedCutoff, Map<FleetMemberAPI, Integer> cables) {
      FleetMemberAPI slowest = null;
      float minLevel = Float.MAX_VALUE;

      for (FleetMemberAPI curr : all) {
         if (this.isSuitable(curr)) {
            float baseBurn = this.getMaxBurnWithoutCables(curr);
            Integer bonus = cables.get(curr);
            if (bonus == null) {
               bonus = 0;
            }

            if (bonus < this.getMaxCablesFor(curr)) {
               float burnLevel = baseBurn + bonus.intValue();
               if (!(burnLevel >= speedCutoff) && burnLevel < minLevel) {
                  minLevel = burnLevel;
                  slowest = curr;
               }
            }
         }
      }

      return slowest;
   }

   private int getMaxCablesFor(FleetMemberAPI member) {
      switch (member.getHullSpec().getHullSize()) {
         case CAPITAL_SHIP:
            return 4;
         case CRUISER:
            return 3;
         case DESTROYER:
            return 2;
         case FRIGATE:
            return 1;
         case FIGHTER:
            return 0;
         default:
            return 1;
      }
   }

   private float getMaxBurnWithoutCables(FleetMemberAPI member) {
      MutableStat burn = member.getStats().getMaxBurnLevel();
      float val = burn.getModifiedValue();
      float sub = 0.0F;

      for (StatMod mod : burn.getFlatMods().values()) {
         if (mod.getSource().startsWith("specialsphmod_fullerene_tow_cable")) {
            sub++;
         }
      }

      return Math.max(0.0F, val - sub);
   }

   private boolean isSuitable(FleetMemberAPI member) {
      return !member.isFighterWing();
   }

   private void cleanUpTowCableBuffBy(FleetMemberAPI member) {
      if (member.getFleetData() != null) {
         FleetDataAPI data = member.getFleetData();
         SUFullereneTowCable.FullereneTowCableBuff buff = this.getTowCableBuffBy(member, false);
         if (buff != null) {
            for (FleetMemberAPI curr : data.getMembersListCopy()) {
               curr.getBuffManager().removeBuff(buff.getId());
            }
         }
      }
   }

   private SUFullereneTowCable.FullereneTowCableBuff getTowCableBuffBy(FleetMemberAPI member, boolean createIfMissing) {
      Map<FleetMemberAPI, SUFullereneTowCable.FullereneTowCableBuff> buffs;
      if (Global.getSector().getPersistentData().containsKey("Fullerene_TowCable_PersistentBuffs")) {
         buffs = (Map<FleetMemberAPI, SUFullereneTowCable.FullereneTowCableBuff>)Global.getSector()
            .getPersistentData()
            .get("Fullerene_TowCable_PersistentBuffs");
      } else {
         buffs = new HashMap<>();
         Global.getSector().getPersistentData().put("Fullerene_TowCable_PersistentBuffs", buffs);
      }

      SUFullereneTowCable.FullereneTowCableBuff buff = buffs.get(member);
      if (buff == null && createIfMissing) {
         String id = "specialsphmod_fullerene_tow_cable_" + member.getId();
         buff = new SUFullereneTowCable.FullereneTowCableBuff(id);
         buffs.put(member, buff);
      }

      return buff;
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      return true;
   }

   public String getUnapplicableReason(ShipAPI ship) {
      return null;
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      return true;
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      return null;
   }

   public boolean affectsOPCosts() {
      return false;
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
      return this.getDescriptionParam(index, hullSize);
   }

   public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
      return true;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{"This hullmod is an extension of Orbital Filament Anchor. It will remove itself when its parent hullmod is no longer present."}
         );
      }
   }

   public Color getBorderColor() {
      return null;
   }

   public Color getNameColor() {
      return null;
   }

   public int getDisplaySortOrder() {
      return 90;
   }

   public int getDisplayCategoryIndex() {
      return -1;
   }

   public boolean hasSModEffectSection(HullSize hs, ShipAPI sapi, boolean bln) {
      return false;
   }

   public void addSModSection(TooltipMakerAPI tmapi, HullSize hs, ShipAPI sapi, float f, boolean bln, boolean bln1) {
   }

   public void addSModEffectSection(TooltipMakerAPI tmapi, HullSize hs, ShipAPI sapi, float f, boolean bln, boolean bln1) {
   }

   public boolean hasSModEffect() {
      return false;
   }

   public String getSModDescriptionParam(int i, HullSize hs) {
      return null;
   }

   public String getSModDescriptionParam(int i, HullSize hs, ShipAPI sapi) {
      return null;
   }

   public float getTooltipWidth() {
      return 0.0F;
   }

   public boolean isSModEffectAPenalty() {
      return false;
   }

   public boolean showInRefitScreenModPickerFor(ShipAPI sapi) {
      return false;
   }

   public static class FullereneTowCableBuff implements Buff {
      private final String buffId;
      private int frames = 0;

      public FullereneTowCableBuff(String buffId) {
         this.buffId = buffId;
      }

      public boolean isExpired() {
         return this.frames >= 2;
      }

      public String getId() {
         return this.buffId;
      }

      public void apply(FleetMemberAPI member) {
         member.getStats().getMaxBurnLevel().modifyFlat(this.buffId, SUFullereneTowCable.TOW_BONUS);
         if (member.isMothballed()) {
            member.getStats().getFuelUseMod().modifyPercent(this.buffId, -SUFullereneTowCable.FUEL_REDUC);
         }
      }

      public void advance(float days) {
         this.frames++;
      }
   }
}
