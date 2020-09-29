package comparison;

import java.util.Comparator;

import planting.Plant;

public class PlantAgeComparator implements Comparator<Plant> {

	@Override
	public int compare(Plant o1, Plant o2) {
		int timeComp = o1.getFirstTimePlanted().compareTo(o2.getFirstTimePlanted());
		if (timeComp != 0) {
			return timeComp;
		}
		int harvest =  o1.hoursUntilHarvest().compareTo(o2.hoursUntilHarvest());
		if (harvest != 0) {
			return harvest;
		}
		int remainingTime = o1.hoursUntilDry().compareTo(o2.hoursUntilDry());
		if (remainingTime != 0) {
			return remainingTime;
		}
		int nameComp = o1.getBerry().getName().compareTo(o2.getBerry().getName());
		if (nameComp != 0) {
			return nameComp;
		}
		int locationComp = o1.getLocation().compareTo(o2.getLocation());
		if (locationComp != 0) {
			return locationComp;
		}
		
		return o1.getMulch().ordinal() - o2.getMulch().ordinal();
	}

}
