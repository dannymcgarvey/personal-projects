package planting;

public enum Berry {
	
	Cheri,
	Chesto,
	Pecha,
	Rawst,
	Aspear,
	Leppa,
	Oran,
	Persim,
	Lum,
	Sitrus,
	Figy,
	Wiki,
	Mago,
	Aguav,
	Iapapa,
	Razz,
	Bluk,
	Nanab,
	Wepear,
	Pinap,
	Pomeg,
	Kelpsy,
	Qualot,
	Hondew,
	Grepa,
	Tamato,
	Cornn,
	Magost,
	Rabuta,
	Nomel,
	Spelon,
	Pamtre,
	Watmel,
	Durin,
	Belue,
	Occa,
	Passho,
	Wacan,
	Rindo,
	Yache,
	Chople,
	Kebia,
	Shuca,
	Coba,
	Payapa,
	Tanga,
	Charti,
	Kasib,
	Haban,
	Colbur,
	Babiri,
	Chilan,
	Liechi,
	Ganlon,
	Salac,
	Petaya,
	Apicot,
	Lansat,
	Starf,
	Enigma,
	Micle,
	Custap,
	Jaboca,
	Rowap;
	

	private final int growthTime;
	private final int dryRate;
	private final int minYield;
	private final int maxYield;
	
	private Berry() {
		int number = this.ordinal();
		if (number <= 5) {
			growthTime = 12;
			dryRate = 15;
			minYield = 2;
			maxYield = 5;
		} else if (number <= 8) {
			growthTime = 16;
			dryRate = 15;
			minYield = 2;
			maxYield = 5;
		} else if (number == 9) {
			growthTime = 48;
			dryRate = 8;
			minYield = 2;
			maxYield = 5;
		} else if (number == 10) {
			growthTime = 32;
			dryRate = 7;
			minYield = 2;
			maxYield = 5;
		} else if (number <= 15) {
			growthTime = 20;
			dryRate = 10;
			minYield = 1;
			maxYield = 5;
		} else if (number <= 20) {
			growthTime = 8;
			dryRate = 35;
			minYield = 2;
			maxYield = 10;
		} else if (number <= 26) {
			growthTime = 32;
			dryRate = 8;
			minYield = 1;
			maxYield = 5;
		} else if (number <= 30) {
			growthTime = 24;
			dryRate = 10;
			minYield = 2;
			maxYield = 10;
		} else if (number <= 35) {
			growthTime = 60;
			dryRate = 8;
			minYield = 2;
			maxYield = 15;
		} else if (number <= 52) {
			growthTime = 72;
			dryRate = 6;
			minYield = 1;
			maxYield = 5;
		} else if (number <= 59) {
			growthTime = 96;
			dryRate = 4;
			minYield = 1;
			maxYield = 5;
		} else if (number <= 64) {
			growthTime = 96;
			dryRate = 7;
			minYield = 1;
			maxYield = 5;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public int getNumber() {
		return this.ordinal();
	}

	public String toString() {
		return super.toString() + " Berry";
	}

	public int getGrowthTime() {
		return growthTime;
	}

	public int getDryRate() {
		return dryRate;
	}

	public int getMinYield() {
		return minYield;
	}

	public int getMaxYield() {
		return maxYield;
	}
	
	

}
