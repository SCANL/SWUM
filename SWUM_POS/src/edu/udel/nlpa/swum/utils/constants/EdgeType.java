package edu.udel.nlpa.swum.utils.constants;


public enum EdgeType {
	FIELD_USE { // field accesses
		public String toString() {
			return "Use";
		}
		public String drawNode() {
			return "[style=solid]";
		}
	},
	CALL { // method calls
		public String toString() {
			return "Call";
		}
		public String drawNode() {
			return "[style=solid]";
		}
	},
	INHERIT { // interface implement or override
		public String toString() {
			return "Inherit";
		}
		public String drawNode() {
			return "[style=dotted]";
		}
	},
	UNKNOWN { // method calls
		public String toString() {
			return "Unknown";
		}
		public String drawNode() {
			return "[style=dashed]";
		}
	};
	
	abstract String drawNode();
}
