package com.fs.starfarer.api.impl.campaign.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import data.scripts.util.id.SUStringCodex;

public class SPHMU_People extends People {
   public static final String HUBERT = "sphmu_hubert";
   public static final String HUBPORTRAIT = Global.getSettings().getSpriteName("characters", "hubert");

   public static PersonAPI getPerson(String id) {
      return Global.getSector().getImportantPeople().getPerson(id);
   }

   public void advance() {
      createMiscCharacters();
   }

   public static void createMiscCharacters() {
      ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
      MarketAPI market = null;
      market = Global.getSector().getEconomy().getMarket(SUStringCodex.PORT_TSE_FRANCHISE);
      if (market != null) {
         PersonAPI person = Global.getFactory().createPerson();
         person.setId("sphmu_hubert");
         person.setFaction("independent");
         person.setGender(Gender.MALE);
         person.setRankId(Ranks.POST_ACADEMICIAN);
         person.setPostId(Ranks.POST_ACADEMICIAN);
         person.setImportance(PersonImportance.VERY_LOW);
         person.getName().setFirst("Hubert");
         person.getName().setLast("Klein");
         person.setPortraitSprite(HUBPORTRAIT);
         ip.addPerson(person);
      } else if (market == null && !Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_corvusMode")) {
         PersonAPI person = Global.getFactory().createPerson();
         person.setId("sphmu_hubert");
         person.setFaction("independent");
         person.setGender(Gender.MALE);
         person.setRankId(Ranks.POST_ACADEMICIAN);
         person.setPostId(Ranks.POST_ACADEMICIAN);
         person.setImportance(PersonImportance.VERY_LOW);
         person.getName().setFirst("Hubert");
         person.getName().setLast("Klein");
         person.setPortraitSprite(HUBPORTRAIT);
         ip.addPerson(person);
      }
   }
}
