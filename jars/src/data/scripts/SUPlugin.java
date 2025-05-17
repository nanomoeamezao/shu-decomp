package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.SPHMU_People;
import data.scripts.everyframe.SUBioFactoryProductionScript;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.listeners.SUBattleResultListener;
import data.scripts.listeners.SUIntelListener;
import data.scripts.util.id.SUStringCodex;
import java.io.IOException;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;

public class SUPlugin extends BaseModPlugin {
   public static final int LEVEL_REQ = 9;
   public static final String SHUBEKEY = SUStringCodex.SHU_BE_MEMKEY;
   public static final String SHUCONSOLEKEY = SUStringCodex.SHU_CONSOLE_COMMAND_MEMKEY;
   private static final String SHU_SETTINGS = SUStringCodex.SHU_CONFIG_INI;
   private static final String LAZYLIB = "lw_lazylib";
   private static final String LUNALIB = "lunalib";
   private static final String MAGICLIB = "MagicLib";
   private static final String GRAPHICSLIB = "shaderLib";
   private final SUBattleResultListener resultListener = new SUBattleResultListener(false);
   final boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
   final boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
   final boolean hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
   public static boolean HASLUNALIB = Global.getSettings().getModManager().isModEnabled("lunalib");
   public static boolean DISABLE_GENERALUPGRADEINCOMPATIBILITY;
   public static boolean DISABLE_ITEMDESTRUCTION;
   public static boolean ENABLE_CHEAT_FOR_RETARDS;
   public static boolean DISABLE_ALPHAHMOD;
   public static boolean DISABLE_BETAHMOD;
   public static boolean DISABLE_GAMMAHMOD;
   public static boolean DISABLE_BIOFACTORYHMOD;
   public static boolean DISABLE_DEALMAKERHOLOSUITEHMOD;
   public static boolean DISABLE_DRONEREPLICATORHMOD;
   public static boolean DISABLE_CORONALTAPHMOD;
   public static boolean DISABLE_MANTLEBOREHMOD;
   public static boolean DISABLE_CATALYTICCOREHMOD;
   public static boolean DISABLE_CNANOFORGEHMOD;
   public static boolean DISABLE_PNANOFORGEHMOD;
   public static boolean DISABLE_SOILNANITEHMOD;
   public static boolean DISABLE_FULLERENESPOOLHMOD;
   public static boolean DISABLE_PLASMADYNAMOHMOD;
   public static boolean DISABLE_CRYOARITHMETICHMOD;
   public static boolean DISABLE_SYNCHROTONCOREHMOD;
   public static boolean DISABLE_FUSIONLAMPHMOD;
   public static boolean DISABLE_DP_MALUS;
   public static boolean DISABLE_OVERLOAD_DURATION_HYPERSHUNT;
   public static boolean DISABLE_EXPANDEDMISSILERACKS_INCOMPATIBILITY;
   public static boolean DISABLE_MISSILEAUTOLOADER_INCOMPATIBILITY;
   public static boolean DISABLE_EXPANDEDDECKCREW_INCOMPATIBILITY;
   public static boolean DISABLE_FLUXBREAKERS_INCOMPATIBILITY;
   public static boolean DISABLE_EFFICIENCYOVERHAUL_INCOMPATIBILITY;
   public static boolean DISABLE_AUTOMATEDREPAIRUNIT_INCOMPATIBILITY;
   public static boolean DISABLE_SURVEYINGEQUIPMENT_INCOMPATIBILITY;
   public static boolean DISABLE_VANILLASHIELDFUCK_INCOMPATIBILITY;
   public static boolean DISABLE_ADVANCEDOPTICS_INCOMPATIBILITY;
   public static boolean DISABLE_SFC_ADAPTIVEPHASECOILS_INCOMPATIBILITY;
   public static boolean DISABLE_SFC_PHASEANCHOR_INCOMPATIBILITY;
   public static boolean DISABLE_BIOFACTORYHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_DEALMAKERHOLOSUITEHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_DRONEREPLICATORHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_CORONALTAPHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_MANTLEBOREHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_CATALYTICCOREHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_CNANOFORGEHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_PNANOFORGEHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_SOILNANITEHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_FULLERENESPOOLHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_PLASMADYNAMOHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_CRYOARITHMETICHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_SYNCHROTONCOREHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_FUSIONLAMPHMOD_EXTRA_EFFECT;
   public static boolean DISABLE_AQUATICSTIMULATOR_EXTRA_EFFECT;
   public static boolean DISABLE_MOTEMEGACONDENSER_EXTRA_EFFECT;
   public static boolean DISABLE_MOTEMEGACONDENSER_ZIGGURAT_EXTRA_EFFECT;
   public static boolean DISABLE_SERVOSYNCPUMP_EXTRA_EFFECT;
   public static boolean DISABLE_DIMENSIONALSTOVE_EXTRA_EFFECT;
   public static boolean DISABLE_DIMENSIONALNANOFORGE_EXTRA_EFFECT;
   public static boolean DISABLE_GARRISONTRANSMITTER_EXTRA_EFFECT;
   public static boolean DISABLE_MODULARPURIFIER_EXTRA_EFFECT;
   public static boolean DISABLE_INTERPLANETARYACCESSROUTER_EXTRA_EFFECT;
   public static boolean DISABLE_INTERPLANETARYRICECOOKER_EXTRA_EFFECT;
   public static boolean ENABLE_CUSTOM_STATS_MODE;
   public static float CM_ALPHA_AUTOFIRE_BONUS;
   public static float CM_ALPHA_TURRET_TURN_BONUS;
   public static float CM_ALPHA_COST_REDUCTION_LG;
   public static float CM_ALPHA_COST_REDUCTION_MED;
   public static float CM_ALPHA_COST_REDUCTION_SM;
   public static float CM_BETA_AUTOFIRE_BONUS;
   public static float CM_BETA_TURRET_TURN_BONUS;
   public static float CM_BETA_COST_REDUCTION_LG;
   public static float CM_BETA_COST_REDUCTION_MED;
   public static float CM_BETA_COST_REDUCTION_SM;
   public static float CM_GAMMA_AUTOFIRE_BONUS;
   public static float CM_GAMMA_TURRET_TURN_BONUS;
   public static float CM_GAMMA_COST_REDUCTION_LG;
   public static float CM_GAMMA_COST_REDUCTION_MED;
   public static float CM_GAMMA_COST_REDUCTION_SM;
   public static float CM_CNANOFORGE_MISSILE_AMMO_BONUS;
   public static float CM_CNANOFORGE_AMMO_BONUS;
   public static float CM_PNANOFORGE_MISSILE_AMMO_BONUS;
   public static float CM_PNANOFORGE_AMMO_BONUS;
   public static float CM_PNANOFORGE_WEAPON_RELOAD_BONUS;
   public static float CM_BIOFACTORY_REQUIRED_ORGANICS;
   public static float CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER;
   public static float CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS;
   public static float CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC;
   public static float CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC;
   public static float CM_DRONE_REPLICATOR_EXTRA_BAY;
   public static float CM_DEALMAKER_ECM_FRIGATE_BONUS;
   public static float CM_DEALMAKER_ECM_DESTROYER_BONUS;
   public static float CM_DEALMAKER_ECM_CRUISER_BONUS;
   public static float CM_DEALMAKER_ECM_CAPITAL_BONUS;
   public static float CM_DEALMAKER_FLEET_SPEED_FRIGATE_BONUS;
   public static float CM_DEALMAKER_FLEET_SPEED_DESTROYER_BONUS;
   public static float CM_DEALMAKER_FLEET_SPEED_CRUISER_BONUS;
   public static float CM_DEALMAKER_FLEET_SPEED_CAPITAL_BONUS;
   public static float CM_DEALMAKER_COMMAND_RECOVERY_BONUS;
   public static float CM_HYPERSHUNT_VENT_BONUS;
   public static float CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT;
   public static float CM_HYPERSHUNT_FLUX_DISSIPATION_MULT;
   public static float CM_MANTLEBORE_BALLISTIC_RANGE_BONUS;
   public static float CM_MANTLEBORE_HIT_STR;
   public static float CM_MANTLEBORE_PROJ_SPEED_BONUS;
   public static float CM_MANTLEBORE_RECOIL_BONUS;
   public static float CM_CATALYTIC_CORE_MALFUNCTION_REDUCTION;
   public static float CM_CATALYTIC_DEGRADE_REDUCTION_PERCENT;
   public static float CM_CATALYTIC_CR_PEAK_BONUS;
   public static float CM_CATALYTIC_SUPPLY_REDUCTION;
   public static float CM_SOILNANITE_COMBAT_REPAIR_BONUS;
   public static float CM_SOILNANITE_ARMOR_FRIGATE_BONUS;
   public static float CM_SOILNANITE_ARMOR_DESTROYER_BONUS;
   public static float CM_SOILNANITE_ARMOR_CRUISER_BONUS;
   public static float CM_SOILNANITE_ARMOR_CAPITAL_BONUS;
   public static float CM_FULLLERENESPOOL_SURVEY_REDUCTION_FRIGATE;
   public static float CM_FULLLERENESPOOL_SURVEY_REDUCTION_DESTROYER;
   public static float CM_FULLLERENESPOOL_SURVEY_REDUCTION_CRUISER;
   public static float CM_FULLLERENESPOOL_SURVEY_REDUCTION_CAPITAL;
   public static float CM_FULLLERENESPOOL_SALVAGE_BONUS_FRIGATE;
   public static float CM_FULLLERENESPOOL_SALVAGE_BONUS_DESTROYER;
   public static float CM_FULLLERENESPOOL_SALVAGE_BONUS_CRUISER;
   public static float CM_FULLLERENESPOOL_SALVAGE_BONUS_CAPITAL;
   public static float CM_FULLLERENESPOOL_RARE_LOOT_BONUS_FRIGATE;
   public static float CM_FULLLERENESPOOL_RARE_LOOT_BONUS_DESTROYER;
   public static float CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CRUISER;
   public static float CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CAPITAL;
   public static float CM_PLASMADYNAMO_SHIELD_BONUS;
   public static float CM_PLASMADYNAMO_SHIELD_RATE;
   public static float CM_PLASMADYNAMO_SHIELD_UPKEEP_BONUS;
   public static float CM_PLASMADYNAMO_SHIELD_ARC_BONUS;
   public static float CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT;
   public static float CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS;
   public static float CM_CRYOARITHMETICENGINE_SPEED_FRIGATE_BONUS;
   public static float CM_CRYOARITHMETICENGINE_SPEED_DESTROYER_BONUS;
   public static float CM_CRYOARITHMETICENGINE_SPEED_CRUISER_BONUS;
   public static float CM_CRYOARITHMETICENGINE_SPEED_CAPITAL_BONUS;
   public static float CM_SYNCHROTON_MAX_BURN_BONUS;
   public static float CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_FRIGATE_BONUS;
   public static float CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_DESTROYER_BONUS;
   public static float CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CRUISER_BONUS;
   public static float CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CAPITAL_BONUS;
   public static float CM_FUSIONLAMP_DAMAGE_BONUS;
   public static float CM_FUSIONLAMP_FLUX_REDUC;
   public static float CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS;
   public static float CM_FUSIONLAMP_RANGE_BONUS;
   public static float CM_SFC_AQUATICSTIMULATOR_BEAM_DAMAGE_ENERGY_REDUCTION_BONUS;
   public static float CM_SFC_AQUATICSTIMULATOR_EMP_DAMAGE_REDUCTION_BONUS;
   public static float CM_SFC_AQUATICSTIMULATOR_SOLAR_CORONA_STORM_NEGATIVE_REDUCTION_BONUS;
   public static float CM_SFC_MOTEMEGACONDENSER_PHASE_CD_REDUCTION_BONUS;
   public static float CM_SFC_MOTEMEGACONDENSER_PHASE_UPKEEP_REDUCTION_BONUS;
   public static float CM_SFC_MOTEMEGACONDENSER_PHASE_ACTIVATION_COSE_REDUCTION_BONUS;
   public static float CM_SFC_MOTEMEGACONDENSER_PHASE_HARDFLUX_IMPACT_REDUCTION_BONUS;
   public static float CM_UAF_SERVOSYNCPUMP_ZERO_FLUX_SPEED_BONUS;
   public static float CM_UAF_SERVOSYNCPUMP_ZERO_FLUX_LEVEL_MOVEMENT_BONUS;
   public static float CM_UAF_SERVOSYNCPUMP_ENGINE_DURABILITY_BONUS;
   public static float CM_UAF_DIMENSIONALSTOVE_MAX_CR_BONUS;
   public static float CM_UAF_DIMENSIONALSTOVE_SYSTEM_COOLDOWN_REDUCTION;
   public static float CM_UAF_DIMENSIONALSTOVE_SYSTEM_REGEN_BONUS;
   public static float CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_REFIT_TIME_REDUCTION;
   public static float CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_HULL_ARMOR_BONUS;
   public static float CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_WEAPON_AMMO_BONUS;
   public static float CM_UAF_GARRISONTRANSMITTER_FIGHTER_SPEED_BONUS;
   public static float CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_SHIP_BONUS;
   public static float CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_MISSILE_BONUS;
   public static float CM_UAF_MODULARPURIFIER_MIN_CREW_REQUIREMENT_REDUCTION;
   public static float CM_UAF_MODULARPURIFIER_SUPPLY_COST_RECOVER_FROM_DEPLOYMENT_REDUCTION;
   public static float CM_UAF_MODULARPURIFIER_DEPLOYMENT_COST_REDUCTION_REDUCTION;
   public static float CM_UAF_INTERPLANETARYACCESSROUTER_SIGHT_COMBAT_BONUS;
   public static float CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_TARGET_LEAD_BONUS;
   public static float CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_ENGAGEMENT_RANGE_BONUS;
   public static float CM_UAF_INTERPLANETARYRICECOOKER_SMALL_MED_PD_WEAPON_REDUCTION;
   public static float CM_UAF_INTERPLANETARYRICECOOKER_PD_WEAPON_DAMAGE_BONUS;
   public static float CM_UAF_INTERPLANETARYRICECOOKER_DAMAGE_TO_FIGHTERS_MISSILES_BONUS;
   public static String KEYPRESS_HIMEMIKO;
   public static String KEYPRESS_UAF_DIMENSIONAL_NANOFORGE;
   public static String KEYPRESS_UAF_GARRISON_TRANSMITTER;
   public static String KEYPRESS_UAF_INTERPLANETARY_RICECOOKER;

   public void onApplicationLoad() {
      if (!this.hasLazyLib) {
         throw new RuntimeException(
            "Special Hullmod Upgrades requires LazyLib to run.\nYou can download LazyLib at https://fractalsoftworks.com/forum/index.php?topic=5444.0"
         );
      } else if (!this.hasMagicLib) {
         throw new RuntimeException(
            "Special Hullmod Upgrades requires MagicLib to run.\nYou can download MagicLib at https://fractalsoftworks.com/forum/index.php?topic=13718.0"
         );
      } else if (!this.hasShaderLib) {
         throw new RuntimeException(
            "Special Hullmod Upgrades requires GraphicsLib to run.\nYou can download MagicLib at https://fractalsoftworks.com/forum/index.php?topic=10982.0"
         );
      } else {
         try {
            loadSPHMUOptions();
         } catch (JSONException | IOException var2) {
            Global.getLogger(SUPlugin.class).log(Level.ERROR, "sphmu_options.ini has failed to load!" + var2.getMessage());
            throw new RuntimeException(
               "SHU has encountered a problem when loading \"sphmu_options.ini\" and here are the possible reasons:\n1. You edited an entry and forgot to add a comma (,).\n2. Incorrect input or you accidentally deleted a line.\n3. sphmu_options.ini file is missing."
            );
         }

         hullmodIconChanger();
      }
   }

   public void onGameLoad(boolean isNewGame) {
      SUBioFactoryProductionScript Bioproduction = new SUBioFactoryProductionScript();
      SUHullmodDisplayBlockScript HMODBLOCKER = new SUHullmodDisplayBlockScript();
      boolean SHUMemkey = Global.getSector().getMemory().contains(SHUBEKEY);
      Global.getSector().addTransientScript(Bioproduction);
      Global.getSector().addTransientScript(HMODBLOCKER);
      SectorAPI sector = Global.getSector();
      MarketAPI market = null;
      market = Global.getSector().getEconomy().getMarket(SUStringCodex.PORT_TSE_FRANCHISE);
      boolean disableDestruction = DISABLE_ITEMDESTRUCTION;
      if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
         disableDestruction = LunaSettings.getBoolean("mayu_specialupgrades", "shu_itemdestructionToggle");
      }

      if (sector != null && sector.getListenerManager() != null) {
         if (market != null && !market.hasCondition("abandoned_station")) {
            if (!SHUMemkey && !Global.getSector().getCharacterData().getHullMods().containsAll(SUStringCodex.SHUHULLMODCOLLECTION)) {
               sector.addTransientListener(new SUIntelListener());
            } else if (Global.getSector().getCharacterData().getHullMods().containsAll(SUStringCodex.SHUHULLMODCOLLECTION)) {
               sector.removeListener(new SUIntelListener());
            }
         }

         if (!disableDestruction) {
            sector.addListener(this.resultListener);
            if (!sector.getListenerManager().hasListener(this.resultListener)) {
               sector.getListenerManager().addListener(this.resultListener);
            }
         }
      }

      new SPHMU_People().advance();
      hullmodToggle();
      CheatToggleForRetards();
   }

   public void onNewGameAfterTimePass() {
      SectorAPI sector = Global.getSector();
      boolean SHUMemkey = Global.getSector().getMemory().contains(SHUBEKEY);
      MarketAPI market = null;
      market = Global.getSector().getEconomy().getMarket(SUStringCodex.PORT_TSE_FRANCHISE);
      if (market == null) {
         sector.removeListener(new SUIntelListener());
      }

      if (Global.getSector().getCharacterData().getHullMods().containsAll(SUStringCodex.SHUHULLMODCOLLECTION)) {
         sector.removeListener(new SUIntelListener());
      }

      if (SHUMemkey) {
         sector.removeListener(new SUIntelListener());
      }

      if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_corvusMode")) {
         sector.removeListener(new SUIntelListener());
      }

      CheatToggleForRetards();
   }

   private static void loadSPHMUOptions() throws IOException, JSONException {
      JSONObject setting = Global.getSettings().loadJSON(SHU_SETTINGS);
      KEYPRESS_HIMEMIKO = setting.getString("shu_subsystem_hotkey_mikohime");
      KEYPRESS_UAF_DIMENSIONAL_NANOFORGE = setting.getString("shu_subsystem_hotkey_uaf_dimensional");
      KEYPRESS_UAF_GARRISON_TRANSMITTER = setting.getString("shu_subsystem_hotkey_uaf_garrison");
      KEYPRESS_UAF_INTERPLANETARY_RICECOOKER = setting.getString("shu_subsystem_hotkey_uaf_ricecooker");
      DISABLE_ITEMDESTRUCTION = setting.getBoolean("disableItemDestruction");
      DISABLE_GENERALUPGRADEINCOMPATIBILITY = setting.getBoolean("disableGeneralUpgradeIncompatibility");
      ENABLE_CHEAT_FOR_RETARDS = setting.getBoolean("enableGachasmodCheatToggle");
      DISABLE_ALPHAHMOD = setting.getBoolean("disableAlphaCoreHullmod");
      DISABLE_BETAHMOD = setting.getBoolean("disableBetaCoreHullmod");
      DISABLE_GAMMAHMOD = setting.getBoolean("disableGammaCoreHullmod");
      DISABLE_BIOFACTORYHMOD = setting.getBoolean("disableBiofactoryHullmod");
      DISABLE_DRONEREPLICATORHMOD = setting.getBoolean("disableDroneReplicatorHullmod");
      DISABLE_CORONALTAPHMOD = setting.getBoolean("disableHypershuntTapHullmod");
      DISABLE_MANTLEBOREHMOD = setting.getBoolean("disableMantleBoreHullmod");
      DISABLE_CATALYTICCOREHMOD = setting.getBoolean("disableCatalyticCoreHullmod");
      DISABLE_CNANOFORGEHMOD = setting.getBoolean("disableCorruptedNanoforgeHullmod");
      DISABLE_PNANOFORGEHMOD = setting.getBoolean("disablePristineNanoforgeHullmod");
      DISABLE_SOILNANITEHMOD = setting.getBoolean("disableSoilNaniteHullmod");
      DISABLE_FULLERENESPOOLHMOD = setting.getBoolean("disableFullereneSpoolHullmod");
      DISABLE_PLASMADYNAMOHMOD = setting.getBoolean("disablePlasmaDynamoHullmod");
      DISABLE_CRYOARITHMETICHMOD = setting.getBoolean("disableCryoarithmeticEngineHullmod");
      DISABLE_SYNCHROTONCOREHMOD = setting.getBoolean("disableSynchrotonCoreHullmod");
      DISABLE_FUSIONLAMPHMOD = setting.getBoolean("disableFusionLampReactorHullmod");
      DISABLE_DP_MALUS = setting.getBoolean("disableIncreasedDPAICores");
      DISABLE_OVERLOAD_DURATION_HYPERSHUNT = setting.getBoolean("disableOverloadDurationHypershunt");
      DISABLE_DEALMAKERHOLOSUITEHMOD = setting.getBoolean("disableDealmakerHolosuiteHullmod");
      DISABLE_EXPANDEDMISSILERACKS_INCOMPATIBILITY = setting.getBoolean("disableExpandedMissileRacksIncompatibility");
      DISABLE_MISSILEAUTOLOADER_INCOMPATIBILITY = setting.getBoolean("disableMissileAutoloaderIncompatibility");
      DISABLE_EXPANDEDDECKCREW_INCOMPATIBILITY = setting.getBoolean("disableExpandedDeckCrewIncompatibility");
      DISABLE_FLUXBREAKERS_INCOMPATIBILITY = setting.getBoolean("disableResistantFluxConduitsIncompatibility");
      DISABLE_EFFICIENCYOVERHAUL_INCOMPATIBILITY = setting.getBoolean("disableEfficiencyOverhaulIncompatibility");
      DISABLE_AUTOMATEDREPAIRUNIT_INCOMPATIBILITY = setting.getBoolean("disableAutomatedRepairUnitIncompatibility");
      DISABLE_SURVEYINGEQUIPMENT_INCOMPATIBILITY = setting.getBoolean("disableSurveyingEquipmentIncompatibility");
      DISABLE_VANILLASHIELDFUCK_INCOMPATIBILITY = setting.getBoolean("disableVanillaShieldHullmodsIncompatibility");
      DISABLE_ADVANCEDOPTICS_INCOMPATIBILITY = setting.getBoolean("disableAdvancedOpticsIncompatibility");
      DISABLE_SFC_ADAPTIVEPHASECOILS_INCOMPATIBILITY = setting.getBoolean("disableSFCAdaptivePhaseCoilsIncompatibility");
      DISABLE_SFC_PHASEANCHOR_INCOMPATIBILITY = setting.getBoolean("disableSFCPhaseAnchorIncompatibility");
      DISABLE_BIOFACTORYHMOD_EXTRA_EFFECT = setting.getBoolean("disableBiofactoryEmbryoFH");
      DISABLE_DEALMAKERHOLOSUITEHMOD_EXTRA_EFFECT = setting.getBoolean("disableDealmakerHolosuiteDTU");
      DISABLE_DRONEREPLICATORHMOD_EXTRA_EFFECT = setting.getBoolean("disableCombatDroneReplicatorAIR");
      DISABLE_CORONALTAPHMOD_EXTRA_EFFECT = setting.getBoolean("disableHypeshuntFluxDischarge");
      DISABLE_MANTLEBOREHMOD_EXTRA_EFFECT = setting.getBoolean("disableMantleBoreUVR");
      DISABLE_CATALYTICCOREHMOD_EXTRA_EFFECT = setting.getBoolean("disableCatalyticCoreMRA");
      DISABLE_CNANOFORGEHMOD_EXTRA_EFFECT = setting.getBoolean("disableCorruptedNanoforgeMF");
      DISABLE_PNANOFORGEHMOD_EXTRA_EFFECT = setting.getBoolean("disablePristineNanoforgeMF");
      DISABLE_SOILNANITEHMOD_EXTRA_EFFECT = setting.getBoolean("disableSoilNaniteNRepair");
      DISABLE_FULLERENESPOOLHMOD_EXTRA_EFFECT = setting.getBoolean("disableFullereneSpoolFTC");
      DISABLE_PLASMADYNAMOHMOD_EXTRA_EFFECT = setting.getBoolean("disablePlasmaDynamoCA");
      DISABLE_CRYOARITHMETICHMOD_EXTRA_EFFECT = setting.getBoolean("disableCryoarithmeticEngineCFlares");
      DISABLE_SYNCHROTONCOREHMOD_EXTRA_EFFECT = setting.getBoolean("disableSynchrotonCoreAMC");
      DISABLE_FUSIONLAMPHMOD_EXTRA_EFFECT = setting.getBoolean("disableFusionLampReactorSC");
      DISABLE_AQUATICSTIMULATOR_EXTRA_EFFECT = setting.getBoolean("disableAquaticStimulatorFRB");
      DISABLE_MOTEMEGACONDENSER_EXTRA_EFFECT = setting.getBoolean("disableMoteMegacondenserPA");
      DISABLE_MOTEMEGACONDENSER_ZIGGURAT_EXTRA_EFFECT = setting.getBoolean("disableMoteMegacondenserHFM");
      DISABLE_SERVOSYNCPUMP_EXTRA_EFFECT = setting.getBoolean("disableServosyncPumpSM");
      DISABLE_DIMENSIONALSTOVE_EXTRA_EFFECT = setting.getBoolean("disableDimensionalStoveRFC");
      DISABLE_DIMENSIONALNANOFORGE_EXTRA_EFFECT = setting.getBoolean("disableDimensionalNanoforgeFRS");
      DISABLE_GARRISONTRANSMITTER_EXTRA_EFFECT = setting.getBoolean("disableGarrisonTransmitterFDC");
      DISABLE_MODULARPURIFIER_EXTRA_EFFECT = setting.getBoolean("disableModularPurifierMM");
      DISABLE_INTERPLANETARYACCESSROUTER_EXTRA_EFFECT = setting.getBoolean("disableInterplanetaryAccessRouterFDF");
      DISABLE_INTERPLANETARYRICECOOKER_EXTRA_EFFECT = setting.getBoolean("disableInterplanetaryRiceCookerWOM");
      ENABLE_CUSTOM_STATS_MODE = setting.getBoolean("activateCustomStatsMode");
      CM_ALPHA_AUTOFIRE_BONUS = (float)setting.getDouble("shu_alpha_core_autofire_bonus");
      CM_ALPHA_TURRET_TURN_BONUS = (float)setting.getDouble("shu_alpha_core_turret_turn_bonus");
      CM_ALPHA_COST_REDUCTION_LG = (float)setting.getDouble("shu_alpha_core_cost_reduction_large_bonus");
      CM_ALPHA_COST_REDUCTION_MED = (float)setting.getDouble("shu_alpha_core_cost_reduction_medium_bonus");
      CM_ALPHA_COST_REDUCTION_SM = (float)setting.getDouble("shu_alpha_core_cost_reduction_small_bonus");
      CM_BETA_AUTOFIRE_BONUS = (float)setting.getDouble("shu_beta_core_autofire_bonus");
      CM_BETA_TURRET_TURN_BONUS = (float)setting.getDouble("shu_beta_core_turret_turn_bonus");
      CM_BETA_COST_REDUCTION_LG = (float)setting.getDouble("shu_beta_core_cost_reduction_large_bonus");
      CM_BETA_COST_REDUCTION_MED = (float)setting.getDouble("shu_beta_core_cost_reduction_medium_bonus");
      CM_BETA_COST_REDUCTION_SM = (float)setting.getDouble("shu_beta_core_cost_reduction_small_bonus");
      CM_GAMMA_AUTOFIRE_BONUS = (float)setting.getDouble("shu_gamma_core_autofire_bonus");
      CM_GAMMA_TURRET_TURN_BONUS = (float)setting.getDouble("shu_gamma_core_turret_turn_bonus");
      CM_GAMMA_COST_REDUCTION_LG = (float)setting.getDouble("shu_gamma_core_cost_reduction_large_bonus");
      CM_GAMMA_COST_REDUCTION_MED = (float)setting.getDouble("shu_gamma_core_cost_reduction_medium_bonus");
      CM_GAMMA_COST_REDUCTION_SM = (float)setting.getDouble("shu_gamma_core_cost_reduction_small_bonus");
      CM_CNANOFORGE_MISSILE_AMMO_BONUS = (float)setting.getDouble("shu_cnanoforge_missile_capacity_bonus");
      CM_CNANOFORGE_AMMO_BONUS = (float)setting.getDouble("shu_cnanoforge_weapon_capacity_bonus");
      CM_PNANOFORGE_MISSILE_AMMO_BONUS = (float)setting.getDouble("shu_pnanoforge_missile_capacity_bonus");
      CM_PNANOFORGE_AMMO_BONUS = (float)setting.getDouble("shu_pnanoforge_weapon_capacity_bonus");
      CM_PNANOFORGE_WEAPON_RELOAD_BONUS = (float)setting.getDouble("shu_pnanoforge_reload_rate_bonus");
      CM_BIOFACTORY_REQUIRED_ORGANICS = setting.getInt("shu_biofactory_required_organics");
      CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER = (float)setting.getDouble("shu_dronereplicator_fighter_replacement_rate_bonus");
      CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS = (float)setting.getDouble("shu_dronereplicator_fighter_refit_time_bonus");
      CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC = (float)setting.getDouble("shu_dronereplicator_cost_reduction_fighter_bonus");
      CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC = (float)setting.getDouble("shu_dronereplicator_cost_reduction_bomber_bonus");
      CM_DRONE_REPLICATOR_EXTRA_BAY = (float)setting.getDouble("shu_dronereplicator_extra_launch_bay_bonus");
      CM_DEALMAKER_ECM_FRIGATE_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_frigate_ecm_bonus");
      CM_DEALMAKER_ECM_DESTROYER_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_destroyer_ecm_bonus");
      CM_DEALMAKER_ECM_CRUISER_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_cruiser_ecm_bonus");
      CM_DEALMAKER_ECM_CAPITAL_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_capital_ecm_bonus");
      CM_DEALMAKER_FLEET_SPEED_FRIGATE_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_frigate_fleet_speed_bonus");
      CM_DEALMAKER_FLEET_SPEED_DESTROYER_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_destroyer_fleet_speed_bonus");
      CM_DEALMAKER_FLEET_SPEED_CRUISER_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_cruiser_fleet_speed_bonus");
      CM_DEALMAKER_FLEET_SPEED_CAPITAL_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_capital_fleet_speed_bonus");
      CM_DEALMAKER_COMMAND_RECOVERY_BONUS = (float)setting.getDouble("shu_dealmakerholosuite_command_point_recovery_bonus");
      CM_HYPERSHUNT_VENT_BONUS = (float)setting.getDouble("shu_hypershunt_vent_rate_bonus");
      CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT = (float)setting.getDouble("shu_hypershunt_hard_flux_dissipation_bonus");
      CM_HYPERSHUNT_FLUX_DISSIPATION_MULT = (float)setting.getDouble("shu_hypershunt_flux_dissipation_bonus");
      CM_MANTLEBORE_BALLISTIC_RANGE_BONUS = (float)setting.getDouble("shu_mantlebore_ballistic_range_bonus");
      CM_MANTLEBORE_HIT_STR = (float)setting.getDouble("shu_mantlebore_weapon_hit_str_bonus");
      CM_MANTLEBORE_PROJ_SPEED_BONUS = (float)setting.getDouble("shu_mantlebore_projectile_speed_bonus");
      CM_MANTLEBORE_RECOIL_BONUS = (float)setting.getDouble("shu_mantlebore_recoil_reduction_bonus");
      CM_CATALYTIC_CORE_MALFUNCTION_REDUCTION = (float)setting.getDouble("shu_catalyticcore_malfunction_reduction_bonus");
      CM_CATALYTIC_DEGRADE_REDUCTION_PERCENT = (float)setting.getDouble("shu_catalyticcore_cr_degrade_reduction_bonus");
      CM_CATALYTIC_CR_PEAK_BONUS = (float)setting.getDouble("shu_catalyticcore_peak_operating_time_bonus");
      CM_CATALYTIC_SUPPLY_REDUCTION = (float)setting.getDouble("shu_catalyticcore_supply_cost_reduction_bonus");
      CM_SOILNANITE_COMBAT_REPAIR_BONUS = (float)setting.getDouble("shu_soilnanite_engine_weapon_combat_repair_bonus");
      CM_SOILNANITE_ARMOR_FRIGATE_BONUS = (float)setting.getDouble("shu_soilnanite_frigate_armor_bonus");
      CM_SOILNANITE_ARMOR_DESTROYER_BONUS = (float)setting.getDouble("shu_soilnanite_destroyer_armor_bonus");
      CM_SOILNANITE_ARMOR_CRUISER_BONUS = (float)setting.getDouble("shu_soilnanite_cruiser_armor_bonus");
      CM_SOILNANITE_ARMOR_CAPITAL_BONUS = (float)setting.getDouble("shu_soilnanite_capital_armor_bonus");
      CM_FULLLERENESPOOL_SURVEY_REDUCTION_FRIGATE = (float)setting.getDouble("shu_fullerenespool_frigate_survey_cost_reduction_bonus");
      CM_FULLLERENESPOOL_SURVEY_REDUCTION_DESTROYER = (float)setting.getDouble("shu_fullerenespool_destroyer_survey_cost_reduction_bonus");
      CM_FULLLERENESPOOL_SURVEY_REDUCTION_CRUISER = (float)setting.getDouble("shu_fullerenespool_cruiser_survey_cost_reduction_bonus");
      CM_FULLLERENESPOOL_SURVEY_REDUCTION_CAPITAL = (float)setting.getDouble("shu_fullerenespool_capital_survey_cost_reduction_bonus");
      CM_FULLLERENESPOOL_SALVAGE_BONUS_FRIGATE = (float)setting.getDouble("shu_fullerenespool_frigate_salvage_bonus");
      CM_FULLLERENESPOOL_SALVAGE_BONUS_DESTROYER = (float)setting.getDouble("shu_fullerenespool_destroyer_salvage_bonus");
      CM_FULLLERENESPOOL_SALVAGE_BONUS_CRUISER = (float)setting.getDouble("shu_fullerenespool_cruiser_salvage_bonus");
      CM_FULLLERENESPOOL_SALVAGE_BONUS_CAPITAL = (float)setting.getDouble("shu_fullerenespool_capital_salvage_bonus");
      CM_FULLLERENESPOOL_RARE_LOOT_BONUS_FRIGATE = (float)setting.getDouble("shu_fullerenespool_frigate_rare_loot_bonus");
      CM_FULLLERENESPOOL_RARE_LOOT_BONUS_DESTROYER = (float)setting.getDouble("shu_fullerenespool_destroyer_rare_loot_bonus");
      CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CRUISER = (float)setting.getDouble("shu_fullerenespool_cruiser_rare_loot_bonus");
      CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CAPITAL = (float)setting.getDouble("shu_fullerenespool_capital_rare_loot_bonus");
      CM_PLASMADYNAMO_SHIELD_BONUS = (float)setting.getDouble("shu_plasmadynamo_shield_efficiency_bonus");
      CM_PLASMADYNAMO_SHIELD_RATE = (float)setting.getDouble("shu_plasmadynamo_shield_raise_turn_rate_bonus");
      CM_PLASMADYNAMO_SHIELD_UPKEEP_BONUS = (float)setting.getDouble("shu_plasmadynamo_shield_upkeep_reduction_bonus");
      CM_PLASMADYNAMO_SHIELD_ARC_BONUS = (float)setting.getDouble("shu_plasmadynamo_shield_arc_bonus");
      CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT = (float)setting.getDouble("shu_cryoarithmetic_engine_weapon_flux_reduction_bonus");
      CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS = (float)setting.getDouble("shu_cryoarithmetic_engine_engine_health_bonus");
      CM_CRYOARITHMETICENGINE_SPEED_FRIGATE_BONUS = (float)setting.getDouble("shu_cryoarithmetic_engine_frigate_speed_bonus");
      CM_CRYOARITHMETICENGINE_SPEED_DESTROYER_BONUS = (float)setting.getDouble("shu_cryoarithmetic_engine_destroyer_speed_bonus");
      CM_CRYOARITHMETICENGINE_SPEED_CRUISER_BONUS = (float)setting.getDouble("shu_cryoarithmetic_engine_cruiser_speed_bonus");
      CM_CRYOARITHMETICENGINE_SPEED_CAPITAL_BONUS = (float)setting.getDouble("shu_cryoarithmetic_engine_capital_speed_bonus");
      CM_SYNCHROTON_MAX_BURN_BONUS = (float)setting.getDouble("shu_synchrotoncore_max_burn_bonus");
      CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_FRIGATE_BONUS = (float)setting.getDouble("shu_synchrotoncore_frigate_bombard_cost_reduction_bonus");
      CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_DESTROYER_BONUS = (float)setting.getDouble("shu_synchrotoncore_destroyer_bombard_cost_reduction_bonus");
      CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CRUISER_BONUS = (float)setting.getDouble("shu_synchrotoncore_cruiser_bombard_cost_reduction_bonus");
      CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CAPITAL_BONUS = (float)setting.getDouble("shu_synchrotoncore_capital_bombard_cost_reduction_bonus");
      CM_FUSIONLAMP_DAMAGE_BONUS = (float)setting.getDouble("shu_fusionlamp_beam_energy_damage_bonus");
      CM_FUSIONLAMP_FLUX_REDUC = (float)setting.getDouble("shu_fusionlamp_beam_flux_reduction_bonus");
      CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS = (float)setting.getDouble("shu_fusionlamp_damage_dealt_to_shields_bonus");
      CM_FUSIONLAMP_RANGE_BONUS = (float)setting.getDouble("shu_fusionlamp_beam_and_energy_range_bonus");
      CM_SFC_AQUATICSTIMULATOR_BEAM_DAMAGE_ENERGY_REDUCTION_BONUS = (float)setting.getDouble("shu_sfc_aquaticstimulator_beam_energy_damage_reduction_bonus");
      CM_SFC_AQUATICSTIMULATOR_EMP_DAMAGE_REDUCTION_BONUS = (float)setting.getDouble("shu_sfc_aquaticstimulator_beam_emp_damage_reduction_bonus");
      CM_SFC_AQUATICSTIMULATOR_SOLAR_CORONA_STORM_NEGATIVE_REDUCTION_BONUS = (float)setting.getDouble(
         "shu_sfc_aquaticstimulator_solar_corona_hyperspace_negative_reduction"
      );
      CM_SFC_MOTEMEGACONDENSER_PHASE_CD_REDUCTION_BONUS = (float)setting.getDouble("shu_sfc_motemegacondenser_phase_cooldown_reduction_bonus");
      CM_SFC_MOTEMEGACONDENSER_PHASE_UPKEEP_REDUCTION_BONUS = (float)setting.getDouble("shu_sfc_motemegacondenserbeam_phase_upkeep_cost_reduction_bonus");
      CM_SFC_MOTEMEGACONDENSER_PHASE_ACTIVATION_COSE_REDUCTION_BONUS = (float)setting.getDouble(
         "shu_sfc_motemegacondenserbeam_phase_activation_cost_reduction_bonus"
      );
      CM_SFC_MOTEMEGACONDENSER_PHASE_HARDFLUX_IMPACT_REDUCTION_BONUS = (float)setting.getDouble(
         "shu_sfc_motemegacondenserbeam_phase_hardflux_impact_reduction_bonus"
      );
      CM_UAF_SERVOSYNCPUMP_ZERO_FLUX_SPEED_BONUS = (float)setting.getDouble("shu_uaf_servosyncpump_zero_flux_speed_bonus");
      CM_UAF_SERVOSYNCPUMP_ZERO_FLUX_LEVEL_MOVEMENT_BONUS = (float)setting.getDouble("shu_uaf_servosyncpump_zero_flux_level_movement_min_percentage");
      CM_UAF_SERVOSYNCPUMP_ENGINE_DURABILITY_BONUS = (float)setting.getDouble("shu_uaf_servosyncpump_engine_durability_bonus");
      CM_UAF_DIMENSIONALSTOVE_MAX_CR_BONUS = (float)setting.getDouble("shu_uaf_dimensionalstove_max_cr_bonus");
      CM_UAF_DIMENSIONALSTOVE_SYSTEM_COOLDOWN_REDUCTION = (float)setting.getDouble("shu_uaf_dimensionalstove_system_cooldown_reduction");
      CM_UAF_DIMENSIONALSTOVE_SYSTEM_REGEN_BONUS = (float)setting.getDouble("shu_uaf_dimensionalstove_system_regen_bonus");
      CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_REFIT_TIME_REDUCTION = (float)setting.getDouble("shu_uaf_dimensionalnanoforge_fighter_refit_time_reduction");
      CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_HULL_ARMOR_BONUS = (float)setting.getDouble("shu_uaf_dimensionalnanoforge_fighter_hull_armor_bonus");
      CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_WEAPON_AMMO_BONUS = (float)setting.getDouble("shu_uaf_dimensionalnanoforge_fighter_weapon_ammo_bonus");
      CM_UAF_GARRISONTRANSMITTER_FIGHTER_SPEED_BONUS = (float)setting.getDouble("shu_uaf_garrisontransmitter_fighter_speed_bonus");
      CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_SHIP_BONUS = (float)setting.getDouble("shu_uaf_garrisontransmitter_fighter_damage_to_ship_bonus");
      CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_MISSILE_BONUS = (float)setting.getDouble("shu_uaf_garrisontransmitter_fighter_damage_to_missile_bonus");
      CM_UAF_MODULARPURIFIER_MIN_CREW_REQUIREMENT_REDUCTION = (float)setting.getDouble("shu_uaf_modularpurifier_minimum_crew_requirement_reduction");
      CM_UAF_MODULARPURIFIER_SUPPLY_COST_RECOVER_FROM_DEPLOYMENT_REDUCTION = (float)setting.getDouble(
         "shu_uaf_modularpurifier_supply_cost_recover_from_deployment_reduction"
      );
      CM_UAF_MODULARPURIFIER_DEPLOYMENT_COST_REDUCTION_REDUCTION = (float)setting.getDouble("shu_uaf_modularpurifier_deployment_cost_reduction");
      CM_UAF_INTERPLANETARYACCESSROUTER_SIGHT_COMBAT_BONUS = (float)setting.getDouble("shu_uaf_interplanetaryaccessrouter_sight_combat_bonus");
      CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_TARGET_LEAD_BONUS = (float)setting.getDouble("shu_uaf_interplanetaryaccessrouter_fighter_target_accuracy_bonus");
      CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_ENGAGEMENT_RANGE_BONUS = (float)setting.getDouble(
         "shu_uaf_interplanetaryaccessrouter_fighter_engagement_range_bonus"
      );
      CM_UAF_INTERPLANETARYRICECOOKER_SMALL_MED_PD_WEAPON_REDUCTION = (float)setting.getDouble("shu_uaf_interplanetaryricecooker_pd_cost_reduction_bonus");
      CM_UAF_INTERPLANETARYRICECOOKER_PD_WEAPON_DAMAGE_BONUS = (float)setting.getDouble("shu_uaf_interplanetaryricecooker_pd_weapon_damage_bonus");
      CM_UAF_INTERPLANETARYRICECOOKER_DAMAGE_TO_FIGHTERS_MISSILES_BONUS = (float)setting.getDouble(
         "shu_uaf_interplanetaryricecooker_damage_to_fighters_missiles_bonus"
      );
   }

   private static void hullmodIconChanger() {
      if (Global.getSettings().getModManager().isModEnabled("uaf")) {
         try {
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_access.png");
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_dimen_nanofab.png");
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_dimen_stove.png");
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_garrison_transmitter.png");
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_modular_purifier.png");
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_servosynco_pump.png");
            Global.getSettings().loadTexture("graphics/icons/cargo/uaf_rice_cooker.png");
         } catch (IOException var1) {
         }

         Global.getSettings().getHullModSpec("specialsphmod_uaf_interplanetaryaccessrouter_upgrades").setSpriteName("graphics/icons/cargo/uaf_access.png");
         Global.getSettings().getHullModSpec("specialsphmod_uaf_dimensionalnanoforge_upgrades").setSpriteName("graphics/icons/cargo/uaf_dimen_nanofab.png");
         Global.getSettings().getHullModSpec("specialsphmod_uaf_dimensionalstove_upgrades").setSpriteName("graphics/icons/cargo/uaf_dimen_stove.png");
         Global.getSettings()
            .getHullModSpec("specialsphmod_uaf_garrisontransmitter_upgrades")
            .setSpriteName("graphics/icons/cargo/uaf_garrison_transmitter.png");
         Global.getSettings().getHullModSpec("specialsphmod_uaf_modularpurifier_upgrades").setSpriteName("graphics/icons/cargo/uaf_modular_purifier.png");
         Global.getSettings().getHullModSpec("specialsphmod_uaf_servosyncpump_upgrades").setSpriteName("graphics/icons/cargo/uaf_servosynco_pump.png");
         Global.getSettings().getHullModSpec("specialsphmod_uaf_interplanetary_ricecooker_upgrades").setSpriteName("graphics/icons/cargo/uaf_rice_cooker.png");
      }
   }

   private static void CheatToggleForRetards() {
      boolean enableCheatModeForRetards = ENABLE_CHEAT_FOR_RETARDS;
      if (HASLUNALIB) {
         enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (enableCheatModeForRetards) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_exotic_shielding");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_abyss_gazer");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_eutec_exploration");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_domain_libellum");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_dynamic_tuning");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_heavy_ordnance");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_orionshipyards_designs");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_langelaan_field");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_lensing_article");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_mark_of_the_slayer");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_omega_lol");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_phase_conductor");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_hga_logistics");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_psyche_enhancer");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_quantum_theory");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_quickened_loader");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gacha_redsuns_tuning");
         Global.getSector().getCharacterData().addHullMod("specialsphmod_mikohime_blessings");
         Global.getSettings().getHullModSpec("specialsphmod_gacha_exotic_shielding").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_abyss_gazer").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_eutec_exploration").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_domain_libellum").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_dynamic_tuning").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_heavy_ordnance").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_orionshipyards_designs").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_langelaan_field").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_lensing_article").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_mark_of_the_slayer").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_omega_lol").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_mikohime_blessings").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_phase_conductor").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_hga_logistics").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_psyche_enhancer").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_quantum_theory").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_quickened_loader").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_redsuns_tuning").setHidden(false);
         Global.getSettings().getHullModSpec("specialsphmod_mikohime_blessings").setHidden(false);
      } else if (!enableCheatModeForRetards) {
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_exotic_shielding");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_abyss_gazer");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_eutec_exploration");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_domain_libellum");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_dynamic_tuning");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_heavy_ordnance");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_orionshipyards_designs");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_langelaan_field");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_lensing_article");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_mark_of_the_slayer");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_omega_lol");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_phase_conductor");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_hga_logistics");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_psyche_enhancer");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_quantum_theory");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_quickened_loader");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_gacha_redsuns_tuning");
         Global.getSector().getCharacterData().removeHullMod("specialsphmod_mikohime_blessings");
         Global.getSettings().getHullModSpec("specialsphmod_gacha_exotic_shielding").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_abyss_gazer").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_eutec_exploration").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_domain_libellum").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_dynamic_tuning").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_heavy_ordnance").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_orionshipyards_designs").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_langelaan_field").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_lensing_article").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_mark_of_the_slayer").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_omega_lol").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_mikohime_blessings").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_phase_conductor").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_hga_logistics").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_psyche_enhancer").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_quantum_theory").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_quickened_loader").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_gacha_redsuns_tuning").setHidden(true);
         Global.getSettings().getHullModSpec("specialsphmod_mikohime_blessings").setHidden(true);
      }
   }

   private static void hullmodToggle() {
      boolean SHUMemkey = Global.getSector().getMemory().contains(SHUBEKEY);
      boolean SHUCONSOLEMemkey = Global.getSector().getMemory().contains(SHUCONSOLEKEY);
      if (HASLUNALIB) {
         DISABLE_ALPHAHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAlphaCoreHullmodToggle");
         DISABLE_BETAHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableBetaCoreHullmodToggle");
         DISABLE_GAMMAHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableGammaCoreHullmodToggle");
         DISABLE_CNANOFORGEHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCorruptedNanoforgeHullmodToggle");
         DISABLE_PNANOFORGEHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePristineNanoforgeHullmodToggle");
         DISABLE_BIOFACTORYHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableBiofactoryHullmodToggle");
         DISABLE_DRONEREPLICATORHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDroneReplicatorHullmodToggle");
         DISABLE_DEALMAKERHOLOSUITEHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDealmakerHolosuiteHullmodToggle");
         DISABLE_CORONALTAPHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableHypershuntTapHullmodToggle");
         DISABLE_MANTLEBOREHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMantleBoreHullmodToggle");
         DISABLE_CATALYTICCOREHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCatalyticCoreHullmodToggle");
         DISABLE_SOILNANITEHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSoilNaniteHullmodToggle");
         DISABLE_FULLERENESPOOLHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFullereneSpoolHullmodToggle");
         DISABLE_PLASMADYNAMOHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePlasmaDynamoHullmodToggle");
         DISABLE_CRYOARITHMETICHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCryoarithmeticEngineHullmodToggle");
         DISABLE_SYNCHROTONCOREHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSynchrotonCoreHullmodToggle");
         DISABLE_FUSIONLAMPHMOD = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFusionLampReactorHullmodToggle");
      }

      if (DISABLE_ALPHAHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_alpha_core_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_alpha_core_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_ALPHAHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_alpha_core_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_alpha_core_upgrades");
      }

      if (DISABLE_BETAHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_beta_core_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_beta_core_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_BETAHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_beta_core_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_beta_core_upgrades");
      }

      if (DISABLE_GAMMAHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_gamma_core_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_gamma_core_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_GAMMAHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_gamma_core_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_gamma_core_upgrades");
      }

      if (DISABLE_CNANOFORGEHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_corruptednanoforge_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_corruptednanoforge_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_CNANOFORGEHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_corruptednanoforge_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_corruptednanoforge_upgrades");
      }

      if (DISABLE_PNANOFORGEHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_pristinenanoforge_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_pristinenanoforge_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_PNANOFORGEHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_pristinenanoforge_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_pristinenanoforge_upgrades");
      }

      if (DISABLE_BIOFACTORYHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_biofactoryembryo_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_biofactoryembryo_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_BIOFACTORYHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_biofactoryembryo_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_biofactoryembryo_upgrades");
      }

      if (DISABLE_DRONEREPLICATORHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_combatdronereplicator_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_combatdronereplicator_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_DRONEREPLICATORHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_combatdronereplicator_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_combatdronereplicator_upgrades");
      }

      if (DISABLE_DEALMAKERHOLOSUITEHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_dealmakerholosuite_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_dealmakerholosuite_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_DEALMAKERHOLOSUITEHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_dealmakerholosuite_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_dealmakerholosuite_upgrades");
      }

      if (DISABLE_CORONALTAPHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_hypershunt_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_hypershunt_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_CORONALTAPHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_hypershunt_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_hypershunt_upgrades");
      }

      if (DISABLE_MANTLEBOREHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_mantlebore_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_mantlebore_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_MANTLEBOREHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_mantlebore_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_mantlebore_upgrades");
      }

      if (DISABLE_CATALYTICCOREHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_catalyticcore_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_catalyticcore_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_CATALYTICCOREHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_catalyticcore_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_catalyticcore_upgrades");
      }

      if (DISABLE_SOILNANITEHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_soilnanites_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_soilnanites_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_SOILNANITEHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_soilnanites_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_soilnanites_upgrades");
      }

      if (DISABLE_FULLERENESPOOLHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_fullerenespool_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_fullerenespool_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_FULLERENESPOOLHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_fullerenespool_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_fullerenespool_upgrades");
      }

      if (DISABLE_PLASMADYNAMOHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_plasmadynamo_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_plasmadynamo_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_PLASMADYNAMOHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_plasmadynamo_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_plasmadynamo_upgrades");
      }

      if (DISABLE_CRYOARITHMETICHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_cryoarithmeticengine_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_cryoarithmeticengine_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_CRYOARITHMETICHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_cryoarithmeticengine_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_cryoarithmeticengine_upgrades");
      }

      if (DISABLE_SYNCHROTONCOREHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_synchrotoncore_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_synchrotoncore_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_SYNCHROTONCOREHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_synchrotoncore_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_synchrotoncore_upgrades");
      }

      if (DISABLE_FUSIONLAMPHMOD) {
         if (Global.getSector().getCharacterData().knowsHullMod("specialsphmod_fusionlampreactor_upgrades")) {
            Global.getSector().getCharacterData().removeHullMod("specialsphmod_fusionlampreactor_upgrades");
         }
      } else if ((SHUMemkey || SHUCONSOLEMemkey && !DISABLE_FUSIONLAMPHMOD)
         && !Global.getSector().getCharacterData().knowsHullMod("specialsphmod_fusionlampreactor_upgrades")) {
         Global.getSector().getCharacterData().addHullMod("specialsphmod_fusionlampreactor_upgrades");
      }
   }
}
