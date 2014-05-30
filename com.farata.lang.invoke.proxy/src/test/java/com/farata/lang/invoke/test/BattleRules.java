package com.farata.lang.invoke.test;

public class BattleRules extends BattleRulesNoFallback {
	public Winner fight(final Weapon player1weapon, final Weapon player2weapon) {
		throw new IllegalArgumentException("Sheldon Cooper's version is not supported yet");
	}

}
