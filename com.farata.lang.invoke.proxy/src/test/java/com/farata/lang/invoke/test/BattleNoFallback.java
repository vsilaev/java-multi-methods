package com.farata.lang.invoke.test;

import com.farata.lang.invoke.MultiMethods;

public class BattleNoFallback {
	final private Gameplay scenario;
	
	public BattleNoFallback() {
		scenario = MultiMethods.create(Gameplay.class, this, MultiMethods.publicMethodsByName("fight"));
	}
	
	public Winner execute(final Weapon player1weapon, final Weapon player2weapon) {
		return scenario.fight(player1weapon, player2weapon);
	}
	
	public Winner fight(final Weapon.Rock player1weapon, final Weapon.Paper player2weapon) {
		return Winner.SECOND;
	}
	
	public Winner fight(final Weapon.Paper player1weapon, final Weapon.Rock player2weapon) {
		return fight(player2weapon, player1weapon).other();
	}
	
	public Winner fight(final Weapon.Rock player1weapon, final Weapon.Scissors player2weapon) {
		return Winner.FIRST;
	}

	public Winner fight(final Weapon.Scissors player1weapon, final Weapon.Rock player2weapon) {
		return fight(player2weapon, player1weapon).other();
	}
	
	public Winner fight(final Weapon.Paper player1weapon, final Weapon.Scissors player2weapon) {
		return Winner.SECOND;
	}
	
	public Winner fight(final Weapon.Scissors player1weapon, final Weapon.Paper player2weapon) {
		return fight(player2weapon, player1weapon).other();
	}
	
	public Winner fight(final Weapon.Rock player1weapon, final Weapon.Rock player2weapon) {
		return Winner.NONE;
	}
	
	public Winner fight(final Weapon.Paper player1weapon, final Weapon.Paper player2weapon) {
		return Winner.NONE;
	}
	
	public Winner fight(final Weapon.Scissors player1weapon, final Weapon.Scissors player2weapon) {
		return Winner.NONE;
	}
}
