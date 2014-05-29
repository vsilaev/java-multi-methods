package com.farata.lang.invoke.test;

public enum Winner {
	FIRST {
		public Winner other() { return SECOND; }
	}, 
	SECOND {
		public Winner other() { return FIRST; }
	},
	NONE {
		public Winner other() { return NONE; }
	}
	;
	
	abstract public Winner other();
}
