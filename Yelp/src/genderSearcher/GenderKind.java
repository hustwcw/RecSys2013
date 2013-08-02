package genderSearcher;

import java.io.Serializable;

public class GenderKind implements Serializable{
	private static final long serialVersionUID = 9142302765045502525L;

	public static enum Gender {MALE, FEMALE, UNKNOWN, UNPROCESSED};
}
