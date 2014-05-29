package com.farata.lang.invoke.test;

import org.junit.Assert;
import org.junit.Test;

import com.farata.lang.invoke.MultiMethodException;

public class BattleTest {

	@Test
	public void testNormal() {
		final Battle battle = new Battle();
		Assert.assertEquals(Winner.FIRST, battle.execute(new Weapon.Paper(), new Weapon.Rock()));
		Assert.assertEquals(Winner.SECOND, battle.execute(new Weapon.Paper(), new Weapon.Scissors()));
		Assert.assertEquals(Winner.NONE, battle.execute(new Weapon.Rock(), new Weapon.Rock()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testErrorDispatch1() {
		final Battle battle = new Battle();
		battle.execute(new Weapon.Lizard(), new Weapon.Spock());
	}
	
	@Test(expected=MultiMethodException.class)
	public void testErrorDispatch2() {
		final BattleNoFallback battle = new BattleNoFallback();
		battle.execute(new Weapon.Lizard(), new Weapon.Spock());
	}

}
