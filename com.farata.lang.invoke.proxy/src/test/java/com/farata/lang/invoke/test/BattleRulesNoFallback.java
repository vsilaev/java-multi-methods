package com.farata.lang.invoke.test;

public class BattleRulesNoFallback {

	public Winner fight(final Weapon.Rock player1weapon, final Weapon.Scissors player2weapon) {
		return Winner.FIRST;
	}

	public Winner fight(final Weapon.Paper player1weapon, final Weapon.Rock player2weapon) {
		return Winner.FIRST;
	}
	
	public Winner fight(final Weapon.Scissors player1weapon, final Weapon.Paper player2weapon) {
		return Winner.FIRST;
	}
	
	public Winner fight(final Weapon.Rock player1weapon, final Weapon.Paper player2weapon) {
		return fight(player2weapon, player1weapon).other();
	}

	public Winner fight(final Weapon.Paper player1weapon, final Weapon.Scissors player2weapon) {
		return fight(player2weapon, player1weapon).other();
	}
	
	public Winner fight(final Weapon.Scissors player1weapon, final Weapon.Rock player2weapon) {
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
