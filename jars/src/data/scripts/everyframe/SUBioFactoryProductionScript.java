package data.scripts.everyframe;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.scripts.SUPlugin;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.util.vector.Vector2f;

public class SUBioFactoryProductionScript implements EveryFrameScript {
   private static final String BIOKEY = "$SHUBiofactoryHMOD";
   private static final Vector2f ZERO = new Vector2f();
   private final IntervalUtil tracker;
   public static final String BIOSFX = "ui_cargo_organs_drop";
   public static final float DAYS_INTERVAL_MIN = 9.5F;
   public static final float DAYS_INTERVAL_MAX = 10.5F;
   public static final float DEFAULT_ZERO_CAP = 0.0F;
   public static final int PRODUCTION_CAPACITY = 50;
   public static final int REQ_ORGANICS = 8;
   public static final String ORGANICS = "organics";
   public static final String LEGAL_MEAT = "organs";
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   int RequiredOrganics = (int)SUPlugin.CM_BIOFACTORY_REQUIRED_ORGANICS;
   public static final Map SHIP_PER_PRODUCTION_CAP = new HashMap();

   public static int getShipSizes(HullSize size) {
      return (Integer)SHIP_PER_PRODUCTION_CAP.get(size);
   }

   private IntervalUtil getTracker() {
      IntervalUtil tracker = (IntervalUtil)Global.getSector().getMemoryWithoutUpdate().get("$SHUBiofactoryHMOD");
      if (tracker == null) {
         tracker = new IntervalUtil(9.5F, 10.5F);
         Global.getSector().getMemoryWithoutUpdate().set("$SHUBiofactoryHMOD", tracker);
      }

      return tracker;
   }

   public SUBioFactoryProductionScript() {
      this.tracker = this.getTracker();
   }

   public void advance(float amount) {
      SectorAPI sector = Global.getSector();
      amount = sector.getClock().convertToDays(amount);
      this.tracker.advance(amount);
      if (this.tracker.intervalElapsed()) {
         this.BiofactoryConversion(sector);
      }
   }

   public void BiofactoryConversion(SectorAPI sector) {
      if (sector != null) {
         CampaignFleetAPI fleet = sector.getPlayerFleet();
         CargoAPI cargo = fleet.getCargo();
         float capacity = getCapacity(fleet);
         if (capacity > 0.0F) {
            Pair<Integer, Integer> ConvertResult = this.ConvertOrganics(cargo, (int)capacity);
            if (ConvertResult.one == null) {
               return;
            }

            this.addIntel(ConvertResult);
         }
      }
   }

   private Pair<Integer, Integer> ConvertOrganics(CargoAPI cargo, int capacity) {
      Pair<Integer, Integer> pair = new Pair();
      pair.one = 0;
      int organicsAmount = (int)cargo.getCommodityQuantity("organics");
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.RequiredOrganics = LunaSettings.getInt("mayu_specialupgrades", "LUNA_CM_BIOFACTORY_REQUIRED_ORGANICS");
      }

      if (this.enableCustomSM) {
         if (organicsAmount >= this.RequiredOrganics) {
            int totalOrgans = organicsAmount / this.RequiredOrganics;
            int maximumOutput = capacity / this.RequiredOrganics;
            int output = Math.min(totalOrgans, maximumOutput);
            int input = output * this.RequiredOrganics;
            cargo.removeCommodity("organics", input);
            cargo.addCommodity("organs", output);
            pair.one = output;
         }
      } else if (!this.enableCustomSM && organicsAmount >= 8) {
         int totalOrgans = organicsAmount / 8;
         int maximumOutput = capacity / 8;
         int output = Math.min(totalOrgans, maximumOutput);
         int input = output * 8;
         cargo.removeCommodity("organics", input);
         cargo.addCommodity("organs", output);
         pair.one = output;
      }

      return pair;
   }

   public static int getCapacity(CampaignFleetAPI fleet) {
      int production_max_capacity = 0;

      for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
         boolean shipMothballed = ship.getRepairTracker().isMothballed();
         boolean shiphasBIO = ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_upgrades");
         if (!shipMothballed && fleet.getFleetData().getFleet() != null && shiphasBIO) {
            production_max_capacity += getShipSizes(ship.getHullSpec().getHullSize());
         }
      }

      return production_max_capacity * 50;
   }

   private void addIntel(Pair<Integer, Integer> ConversionResult) {
      if (ConversionResult != null) {
         if ((Integer)ConversionResult.one != 0) {
            MessageIntel intel = new MessageIntel();
            Color default_color = Misc.getTextColor();
            Color green = new Color(55, 245, 65, 255);
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            String[] conversionString = new String[]{((Integer)ConversionResult.one).toString()};
            intel.setIcon(Global.getSettings().getHullModSpec("specialsphmod_biofactoryembryo_upgrades").getSpriteName());
            intel.addLine("Biologic Commodity Replicator", Misc.getDesignTypeColor("Reverse Engineered"));
            intel.addLine("The reconfigured biofactories in your fleet has produced %s human organs.", default_color, conversionString, new Color[]{green});
            Global.getSoundPlayer().playSound("ui_cargo_organs_drop", 1.0F, 1.0F, fleet.getLocation(), ZERO);
            Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.NOTHING);
         }
      }
   }

   public boolean isDone() {
      return false;
   }

   public boolean runWhilePaused() {
      return false;
   }

   static {
      SHIP_PER_PRODUCTION_CAP.put(HullSize.FIGHTER, 0);
      SHIP_PER_PRODUCTION_CAP.put(HullSize.FRIGATE, 1);
      SHIP_PER_PRODUCTION_CAP.put(HullSize.DESTROYER, 2);
      SHIP_PER_PRODUCTION_CAP.put(HullSize.CRUISER, 4);
      SHIP_PER_PRODUCTION_CAP.put(HullSize.CAPITAL_SHIP, 6);
   }
}
