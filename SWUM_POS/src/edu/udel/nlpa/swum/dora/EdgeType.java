package edu.udel.nlpa.swum.dora;


public enum EdgeType {
	PLAIN { // field accesses or method calls
		public String toString() {
			return "Use";
		}
		public String drawNode() {
			return "[style=solid]";
		}
	},
	INITIALIZER { // field accesses or method calls
		public String toString() {
			return "Init";
		}
		public String drawNode() {
			return "[style=dashed]";
		}
	},
	IMPLEMENT { // interface implement or override
		public String toString() {
			return "Implement";
		}
		public String drawNode() {
			return "[style=dashed]";
		}
	},
	CONNECT { // interface implement or override
		public String toString() {
			return "Connect";
		}
		public String drawNode() {
			return "[style=dotted]";
		}
	};
	
	abstract String drawNode();
}
