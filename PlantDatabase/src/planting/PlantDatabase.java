package planting;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.function.Predicate;

import constants.GrowthStage;


public class PlantDatabase implements Serializable, Iterable<Plant> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3416186466486409433L;
	private static final File saveData = new File("Plants.db");
	
	private Collection<Plant> plants;
	private boolean modified = false;
	
	public PlantDatabase() {
		this.plants = new HashSet<Plant>();
	}
	
	public int size() {
		return plants.size();
	}
	
	public boolean plant(Plant p) {
		boolean result = plants.add(p);
		if (result) {
			modified = true;
		}
		return result;
	}
	
	public void water(Plant p) {
		if (plants.contains(p)) {
			p.water();
			modified = true;
		}
	}
	
	/**
	 * Removes the specified plant if it is fully grown and returns the
	 * number of berries yielded by it. If it can't be removed, 
	 * throws an exception.
	 * @param p the plant to remove
	 * @return number of berries yielded
	 * @throws IllegalArgumentException when p is neither dead nor fully grown
	 * @throws NoSuchElementException when this database does not contain p
	 */
	public int pluck(Plant p) {
		if ((p.getStage() != GrowthStage.FullGrown && 
				p.getStage() != GrowthStage.Dead)) {
			
			throw new IllegalArgumentException();
		}
		if (plants.remove(p)) {
			modified = true;
			return p.getFinalYield();
		} else {
			throw new NoSuchElementException();
		}
	}
	
	public void sortBy(Comparator<Plant> comparison) {
		TreeSet<Plant> set = new TreeSet<>(comparison);
		set.addAll(plants);
		this.plants = set;
	}
	
	/**
	 * Removes all dead plants from the database
	 * @return number of plants removed
	 */
	public int clearDeadPlants() {
		int originalSize = plants.size();
		plants.removeIf(new Predicate<Plant> () {

			@Override
			public boolean test(Plant t) {
				return t.getStage() == GrowthStage.Dead;
			}
			
		});
		int result = originalSize - plants.size();
		if (result > 0) {
			modified = true;
		}
		return result;
	}
	
	public ArrayList<Plant> fullyGrownSubset() {
		ArrayList<Plant> subSet = new ArrayList<>(plants);
		subSet.removeIf(new Predicate<Plant>() {

			@Override
			public boolean test(Plant t) {
				return t.getStage() != GrowthStage.FullGrown;
			}
			
		});
		return subSet;
	}
	
	public ArrayList<Plant> currentlyGrowingSubset() {
		ArrayList<Plant> subSet = new ArrayList<>(plants);
		subSet.removeIf(new Predicate<Plant>() {

			@Override
			public boolean test(Plant t) {
				return t.getStage() == GrowthStage.FullGrown ||
						t.getStage() == GrowthStage.Dead;
			}
			
		});
		return subSet;
	}
	
	public static PlantDatabase open() throws IOException, ClassNotFoundException {
		FileInputStream file = new FileInputStream(saveData);
		ObjectInputStream input = new ObjectInputStream(file);
		PlantDatabase db = (PlantDatabase)input.readObject();
		db.modified = false;
		input.close();
		return db;
	}
	
	public boolean save() throws IOException {
		if (!modified) {
			return false;
		}
		this.plants = new HashSet<>(plants);
		saveData.delete();
		FileOutputStream file = new FileOutputStream(saveData, false);
		ObjectOutputStream output = new ObjectOutputStream(file);
		output.writeObject(this);
		output.close();
		return true;
	}

	@Override
	public Iterator<Plant> iterator() {
		return plants.iterator();
	}


}
