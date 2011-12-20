package nl.klpd.tde.ocfa.evidence;

public enum ValueType {
	STRING("string"), DATETIME("datetime"), INT("int");
	
	private ValueType(String inDescription){
		
		description = inDescription;
	}
	private String description;
	public String getDescription() {
		return description;
	}
	
}
