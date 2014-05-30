package com.farata.lang.invoke.test;

import org.junit.Assert;
import org.junit.Test;

import com.farata.lang.invoke.MultiMethodException;
import com.farata.lang.invoke.MultiMethods;

public class BattleTest {

	@Test
	public void testNormal() {
		final BattleRules rules = new BattleRules();
		final BattleExecutor scenario = MultiMethods.create(BattleExecutor.class, rules, MultiMethods.publicMethodsByName("fight"));
		Assert.assertEquals(Winner.FIRST, scenario.run(new Weapon.Paper(), new Weapon.Rock()));
		Assert.assertEquals(Winner.SECOND, scenario.run(new Weapon.Paper(), new Weapon.Scissors()));
		Assert.assertEquals(Winner.NONE, scenario.run(new Weapon.Rock(), new Weapon.Rock()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testErrorDispatch1() {
		final BattleRules rules = new BattleRules();
		final BattleExecutor scenario = MultiMethods.create(BattleExecutor.class, rules, MultiMethods.publicMethodsByName("fight"));
		scenario.run(new Weapon.Lizard(), new Weapon.Spock());
	}
	
	@Test(expected=MultiMethodException.class)
	public void testErrorDispatch2() {
		final BattleRulesNoFallback rules = new BattleRulesNoFallback();
		final BattleExecutor scenario = MultiMethods.create(BattleExecutor.class, rules, MultiMethods.publicMethodsByName("fight"));		
		scenario.run(new Weapon.Lizard(), new Weapon.Spock());
	}

}
