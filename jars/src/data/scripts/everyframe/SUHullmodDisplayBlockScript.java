package data.scripts.everyframe;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
import org.apache.log4j.Logger;

public class SUHullmodDisplayBlockScript extends BaseEveryFrameCombatPlugin implements EveryFrameScript {
   private static final Logger Log = Logger.getLogger(SUHullmodDisplayBlockScript.class);
   private static final String NOTIFICATION_HULLMOD = "specialsphmod_hullmod_blocker";
   private static final String NOTIFICATION_SOUND = "spuhm_hullmod_conflict";
   private static ShipAPI ship;

   public static void showBlocked(ShipAPI blocked) {
      stopDisplaying();
      ship = blocked;
      ship.getVariant().addMod("specialsphmod_hullmod_blocker");
      Global.getSoundPlayer().playUISound("spuhm_hullmod_conflict", 1.0F, 1.0F);
   }

   public static void stopDisplaying() {
      if (ship != null) {
         ship.getVariant().removeMod("specialsphmod_hullmod_blocker");
         ship = null;
      }
   }

   public void advance(float amount) {
      stopDisplaying();
   }

   public void advance(float amount, List<InputEventAPI> events) {
      stopDisplaying();
   }

   public void init(CombatEngineAPI engine) {
      if (Global.getSettings().getCurrentState() != GameState.TITLE) {
         stopDisplaying();
         Global.getCombatEngine().removePlugin(this);
      }
   }

   public boolean isDone() {
      return false;
   }

   public boolean runWhilePaused() {
      return true;
   }
}
