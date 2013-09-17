package lbd.FSNER.Utils;

import java.io.Serializable;
import java.util.HashMap;

/** This Class is only a HashMap with an addition of specific id control (for especial occasions) **/

public class MapId<T, S> extends HashMap<T, S> implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected int id;
	
	public MapId(int id) {
		super();
		this.id = id;
	}
	
	public int getId() {
		return(id);
	}
}
