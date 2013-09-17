package lbd.NewModels.FeatureFilters;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class EntityProbabilityComponent implements Serializable{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Integer> entityList;
	protected int id;
	protected String filename;

	public EntityProbabilityComponent() {
		entityList = new HashMap<String, Integer>();

		id = 0;
		filename = "./Data/Temp/CRFFeaturesAsFilters/" + "entityProbability";
	}

	public void addTerm(String term) {
		entityList.put(term, ++id);
	}

	public int getId(String term) {
		return((entityList.containsKey(term))? entityList.get(term) : -1);
	}

	public void read(EntityProbabilityComponent target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		EntityProbabilityComponent clone = (EntityProbabilityComponent) in.readObject();
		clone(target, clone);

		in.close();
	}

	private void clone(EntityProbabilityComponent target, EntityProbabilityComponent clone) {
		target.entityList = clone.entityList;
	}

	public void write() throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

}
