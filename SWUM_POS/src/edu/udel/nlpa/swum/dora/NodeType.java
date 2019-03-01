package edu.udel.nlpa.swum.dora;


public enum NodeType {
	NO_TYPE {
		public String toString() {
			return "x";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=diamond]\n";
		}
		public int getRelevance() { return 0; }
	},
	LS {
		public String toString() {
			return "s";
		}
		public String drawNode() {
			return "node [style=dashed,	shape=egg]\n";
		}
		public int getRelevance() { return 1; }
	},
	L { // major library
		public String toString() {
			return "l";
		}
		public String drawNode() {
			return "node [style=solid,	shape=egg]\n";
		}
		public int getRelevance() { return 2; }
	},
	P {
		public String toString() {
			return "p";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=ellipse]\n";
		}
		public int getRelevance() { return 3; }
	},
	K {
		public String toString() {
			return "k";
		}
		public String drawNode() {
			return "node [style=dashed,	shape=ellipse]\n";
		}
		public int getRelevance() { return 3; }
	},
	E {
		public String toString() {
			return "e";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=ellipse]\n";
		}
		public int getRelevance() { return 3; }
	},
	C {
		public String toString() {
			return "c";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=ellipse]\n";
		}
		public int getRelevance() { return 5; }
	},
	I {
		public String toString() {
			return "i";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=ellipse]\n";
		}
		public int getRelevance() { return 3; }
	},
	S {
		public String toString() {
			return "s";
		}
		public String drawNode() {
			return "node [style=dashed,	shape=ellipse]\n";
		}
		public int getRelevance() { return 4; }
	},
	M { 
		public String toString() {
			return "m";
		}
		public String drawNode() {
			return "node [style=solid,	shape=ellipse]\n";
		}
		public int getRelevance() { return 5; }
	},
	SEED {
		public String toString() {
			return "m";
		}
		public String drawNode() {
			return "node [style=bold,	shape=ellipse]\n";
		}
		public int getRelevance() { return 6; }
	},
	GROUP {
		public String toString() {
			return "g";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=box]\n";
		}
		public int getRelevance() { return 0; }
	},
	E_FIELD {
		public String toString() {
			return "e";
		}
		public String drawNode() {
			return "node [style=dashed,	shape=box]\n";
		}
		public int getRelevance() { return 3; }
	},
	C_FIELD {
		public String toString() {
			return "c";
		}
		public String drawNode() {
			return "node [style=dashed,	shape=box]\n";
		}
		public int getRelevance() { return 3; }
	},
	SEED_FIELD {
		public String toString() {
			return "m";
		}
		public String drawNode() {
			return "node [style=bold,	shape=box]\n";
		}
		public int getRelevance() { return 5; }
	},
	RELEVANT_FIELD { // m f
		public String toString() {
			return "m";
		}
		public String drawNode() {
			return "node [style=solid,	shape=box]\n";
		}
		public int getRelevance() { return 3; } // 4
	},
	LIBRARY_FIELD { // shouldn't exist
		public String toString() {
			return "f";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=box]\n";
		}
		public int getRelevance() { return 1; }
	},
	S_FIELD { // f/s field?
		public String toString() {
			return "s";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=box]\n";
		}
		public int getRelevance() { return 3; }
	},
	FIELD { // f/s field?
		public String toString() {
			return "f";
		}
		public String drawNode() {
			return "node [style=dotted,	shape=box]\n";
		}
		public int getRelevance() { return 3; }
	};
	
	public abstract String drawNode();
	public abstract int getRelevance();
}
