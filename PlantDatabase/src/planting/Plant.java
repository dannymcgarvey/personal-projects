package planting;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import constants.GrowthStage;
import constants.Mulch;

public class Plant implements Serializable {

	private static final long serialVersionUID = -6975184195530348282L;


	// Immutable State
	private Berry berry;
	private String location;
	private Mulch mulch;
	private LocalDateTime timePlanted;

	//Mutable State
	private int moistureLevel;
	private int finalYield;
	private LocalDateTime timeOfLastUpdate;



	public Plant(Berry berry, String location, Mulch mulch) {
		this.berry = berry;
		this.location = location;
		this.mulch = mulch;
		timeOfLastUpdate = timePlanted = LocalDateTime.now();
		moistureLevel = 100;
		finalYield = berry.getMaxYield();
	}

	private void updateState() {
		LocalDateTime now = LocalDateTime.now();
		GrowthStage currentStage = getStage(timeOfLastUpdate);
		for (LocalDateTime currentTime = timeOfLastUpdate.plusMinutes(1); 
				currentTime.isBefore(now); 
				currentTime = currentTime.plusMinutes(1)) {
			
			if (currentStage == GrowthStage.Dead) {
				break;
			}
			
			//Subtract Moisture each hour (Unless plant is grown)
			if (ChronoUnit.MINUTES.between(timePlanted, currentTime) % 60 == 0
					&& currentStage != GrowthStage.FullGrown) {
				subtractMoisture();
			}
			
			//update growth stage
			GrowthStage nextStage = getStage(currentTime);
			if (currentStage != nextStage) {
				//reset yield on replant
				if (currentStage == GrowthStage.FullGrown) {
					finalYield = berry.getMaxYield();
				}
				currentStage = nextStage;
			}

		}

		timeOfLastUpdate = now;
	}
	
	private void subtractMoisture() {
		if (moistureLevel == 0) {
			int berriesToSubtract = berry.getMaxYield() / 5;
			if (finalYield - berriesToSubtract < berry.getMinYield()) {
				finalYield = berry.getMinYield();
			} else {
				finalYield -= berriesToSubtract;
			}
		} else if (moistureLevel < getModifiedDryRate()) {
			moistureLevel = 0;
		} else {
			moistureLevel -= getModifiedDryRate();
		}

	}
	
	/*
	 * Methods that use mutable state need to update state before running
	 */


	/**
	 * 
	 * @return number of hours until the plant's moisture level becomes zero, 
	 * rounded up
	 */
	public Integer hoursUntilDry() {
		updateState();
		int moistureRemaining = moistureLevel;
		int hoursRemaining = 0;
		int dryRate = getModifiedDryRate();
		while (moistureRemaining > 0) {
			moistureRemaining -= dryRate;
			hoursRemaining++;
		}
		return hoursRemaining;
	}

	protected void water() {
		updateState();
		moistureLevel = 100;
	}
	
	/**
	 * @return a String containing detailed info on the plant
	 */
	@Override
	public String toString() {
		updateState();

		String timeStr = timePlanted.toString().substring(0,19);
		
		long minsUntilHarvest = minsUntilHarvest();
		if (minsUntilHarvest < 0) {
			minsUntilHarvest = (getModifiedGrowthTime() * 60 + minsUntilHarvest);
		}
		long hours = minsUntilHarvest / 60;
		long mins = minsUntilHarvest % 60;

		return berry + " @ " + location +  ", \ntimePlanted: "
		+ timeStr + ", \nmoistureLevel: " + moistureLevel + ", finalYield: " 
		+ finalYield + ", timesReplanted: " + getTimesReplanted() + ", mulch: " + mulch 
		+ ", stage: " + getStage() + "\nHours until dry: " + hoursUntilDry() + ", " 
		+ ((hoursUntilHarvest() > 0) ? "Time until harvest: "  :
			"Time remaining to harvest: ") +  hours + "h" + mins + "m";
	}

	/**
	 * 
	 * @return a String containing the most important info on the plant
	 */
	public String shortInfo() {
		updateState();
		return berry + " @ " + location + ",\nHours until Dry: " + hoursUntilDry() + ", " + 
		((hoursUntilHarvest() > 0) ? "Hours until harvest: " + hoursUntilHarvest() :
			"Hours remaining to harvest: " + (getModifiedGrowthTime() + hoursUntilHarvest()));
	}

	/**
	 * @return the moistureLevel
	 */
	public int getMoistureLevel() {
		updateState();
		return moistureLevel;
	}

	/**
	 * @return the finalYield
	 */
	public int getFinalYield() {
		updateState();
		return finalYield;
	}


	/*
	 * ^ methods that need to access mutable state, must update when calling
	 * v methods that should only access immutable state
	 */

	/**
	 * 
	 * @return the number of hours until the plant will become fully grown
	 * If it's already full grown, returns negative value of how many
	 * hours it has been in the full grown state.
	 */
	public Long hoursUntilHarvest() {
		//round up rather than down
		return  (minsUntilHarvest()+ 59)/ 60;
	}
	
	/**
	 * 
	 * @return the number of minutes until the plant will become fully grown
	 * If it's already full grown, returns negative value of how many
	 * minutes it has been in the full grown state.
	 */
	public long minsUntilHarvest() {
		return minsUntilHarvest(LocalDateTime.now());
	}
	
	private long minsUntilHarvest(LocalDateTime now) {
		long minsIntoCycle = adjustMinsForCycle(now);
		return 60 * getModifiedGrowthTime() - minsIntoCycle;
	}

	private int getModifiedDryRate() {
		if (mulch == Mulch.Growth) {
			return 3 * berry.getDryRate() / 2;

		} else if (mulch == Mulch.Damp) {
			return berry.getDryRate() / 2;

		} else {
			return berry.getDryRate();
		}
	}

	private int getModifiedGrowthTime() {
		if (mulch == Mulch.Growth) {
			return 3 * berry.getGrowthTime() / 4;
		} else if (mulch == Mulch.Damp) {
			return 3 * berry.getGrowthTime() / 2;

		} else {
			return berry.getGrowthTime();
		}
	}

	private int getModifiedHarvestTime() {
		if (mulch == Mulch.Stable) {
			return 3 * getModifiedGrowthTime() / 2;
		} else {
			return getModifiedGrowthTime();
		}
	}

	private int getMaxPlantings() {
		if (mulch == Mulch.Gooey) {
			return 14;
		} else {
			return 9;
		}
	}

	/**
	 * @return the berry
	 */
	public Berry getBerry() {
		return berry;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the mulch
	 */
	public Mulch getMulch() {
		return mulch;
	}

	/**
	 * @return the firstTimePlanted
	 */
	public LocalDateTime getFirstTimePlanted() {
		return timePlanted;
	}

	/**
	 * @return the timesReplanted or -1 if plant is dead
	 */
	public int getTimesReplanted() {
		return getTimesReplanted(LocalDateTime.now());
	}

	private int getTimesReplanted(LocalDateTime now) {
		int modifiedGrowthTime = getModifiedGrowthTime();
		int modifiedHarvestTime = getModifiedHarvestTime();
		int maxPlantings = getMaxPlantings();

		long growthMins = modifiedGrowthTime * 60;
		long harvestMins = modifiedHarvestTime * 60;
		long firstCycleMins = growthMins + harvestMins;
		long subsequentCycleMins = (3*growthMins/4) + harvestMins;

		long minsSincePlanted = ChronoUnit.MINUTES.between(timePlanted, now);
		if (minsSincePlanted < firstCycleMins) {
			return 0;
		} else {
			minsSincePlanted -= firstCycleMins;
			long cycles = 1 +  (minsSincePlanted / subsequentCycleMins);
			if (cycles <= maxPlantings) {
				return (int)cycles;
			} else {
				return -1;
			}
		}
	}

	/**
	 * @return the stage
	 */
	public GrowthStage getStage() {
		return getStage(LocalDateTime.now());
	}


	private GrowthStage getStage(LocalDateTime now) {
		GrowthStage stage = GrowthStage.Dead;

		int modifiedGrowthTime = getModifiedGrowthTime();
		int modifiedHarvestTime = getModifiedHarvestTime();

		long growthMins = modifiedGrowthTime * 60;
		long harvestMins = modifiedHarvestTime * 60;
		long firstCycleMins = growthMins + harvestMins;
		long stageMins = growthMins / 4;

		long minsSincePlanted = adjustMinsForCycle(now);

		if (minsSincePlanted < stageMins) {
			stage = GrowthStage.Seed;
		} else if (minsSincePlanted < 2 * stageMins) {
			stage = GrowthStage.Sprouting;
		} else if (minsSincePlanted < 3 * stageMins) {
			stage = GrowthStage.Growing;
		} else if (minsSincePlanted < 4 * stageMins) {
			stage = GrowthStage.Blooming;
		} else if (minsSincePlanted < firstCycleMins){
			stage = GrowthStage.FullGrown;
		} 
		return stage;
	}

	private long adjustMinsForCycle(LocalDateTime now) {
		int modifiedGrowthTime = getModifiedGrowthTime();
		int modifiedHarvestTime = getModifiedHarvestTime();

		long growthMins = modifiedGrowthTime * 60;
		long harvestMins = modifiedHarvestTime * 60;
		long firstCycleMins = growthMins + harvestMins;
		long subsequentCycleMins = (3*growthMins/4) + harvestMins;
		long stageMins = growthMins / 4;

		//return if dead
		int timesReplanted = getTimesReplanted(now);

		long minsSincePlanted = ChronoUnit.MINUTES.between(timePlanted, now);

		//weird thing to adjust for replantings
		if (timesReplanted > 0) {
			minsSincePlanted -= firstCycleMins;
			minsSincePlanted %= subsequentCycleMins;
			minsSincePlanted += stageMins;
		}

		return minsSincePlanted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((berry == null) ? 0 : berry.hashCode());
		result = prime * result + ((timePlanted == null) ? 0 : timePlanted.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + mulch.ordinal();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Plant)) {
			return false;
		}
		Plant other = (Plant) obj;
		if (berry != other.berry) {
			return false;
		}
		if (timePlanted == null) {
			if (other.timePlanted != null) {
				return false;
			}
		} else if (!timePlanted.equals(other.timePlanted)) {
			return false;
		}
		if (location == null) {
			if (other.location != null) {
				return false;
			}
		} else if (!location.equals(other.location)) {
			return false;
		}
		if (mulch != other.mulch) {
			return false;
		}
		return true;
	}



}
