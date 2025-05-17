package data.scripts.util;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.hullmods.SUHullmodUpgradeInstaller;

public class SUAICoreUtilityScriptZwei extends BaseHullMod {
   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      ShipVariantAPI shipVariant = ship.getVariant();
      boolean isCaptainNull = ship.getCaptain() == null;
      MutableCharacterStatsAPI currentShipStats = isCaptainNull ? null : ship.getCaptain().getStats();
      if (ship.getVariant().hasHullMod("specialsphmod_alphacore_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_alpha_core_module_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_alphacore_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_alphacore_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_betacore_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_beta_core_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_beta_core_module_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_betacore_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_betacore_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_gammacore_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_gamma_core_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_gamma_core_module_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_gammacore_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_gammacore_utilityscript");
      }
   }
}
