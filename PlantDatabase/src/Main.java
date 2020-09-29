import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import comparison.BerryComparator;
import comparison.HoursUntilHarvestComparator;
import comparison.PlantAgeComparator;
import comparison.PlantLocationComparator;
import comparison.PlantRemainingTimeComparator;
import constants.Mulch;
import planting.Berry;
import planting.Plant;
import planting.PlantDatabase;

public class Main {

	private static final int PLANT = 1;
	private static final int PLUCK = 2;
	private static final int WATER = 3;
	private static final int VIEW_MOISTURE = 4;
	private static final int VIEW_HARVEST = 5;
	private static final int VIEW_AGE = 6;
	private static final int VIEW_LOCATION = 7;
	private static final int VIEW_NAME = 8;
	private static final int SWITCH = 9;
	private static final int CLOSE = 0;

	private static final boolean VERBOSE_MODE = false;
	private static final boolean NORMAL_MODE = true;

	public static void main(String[] args) {
		
		boolean mode = NORMAL_MODE;
		PlantDatabase db;
		try {
			db = PlantDatabase.open();
		} catch (IOException e) {
			db = new PlantDatabase();
		} catch (ClassNotFoundException e) {
			db = new PlantDatabase();
		}
		db.clearDeadPlants();
		Scanner keyboardInput = new Scanner(System.in);
		try {
			int pathChosen = -1;
			while (pathChosen != CLOSE) {
				System.out.println("What would you like to do?");
				System.out.println("1: Plant Berry");
				System.out.println("2: Pick Berry");
				System.out.println("3: Water Berries");
				System.out.println("4: View Berries by moisture");
				System.out.println("5: View Berries by time until harvest");
				System.out.println("6: View Berries by age");
				System.out.println("7: View Berries by location");
				System.out.println("8: View Berries by name");
				System.out.println("9: Switch Modes");
				System.out.println("0: Save and Exit");
				pathChosen = keyboardInput.nextInt();
				keyboardInput.nextLine();
				if (pathChosen == PLANT) {
					System.out.println("Choose mulch.");
					Mulch mulchNum = Mulch.None;
					try {
						mulchNum = chooseMulch(keyboardInput.nextLine());
					} catch (IllegalArgumentException e) {
						System.out.println(e.getMessage());
					}
					System.out.println("Choose berry name or number.");
					int number = -1;
					String name = null;
					if (keyboardInput.hasNextInt()) {
						number = keyboardInput.nextInt();
						keyboardInput.nextLine();
					} else {
						name = keyboardInput.nextLine();
					}
					Berry berryChosen; 
					try {
						berryChosen = parseBerry(number, name);
						System.out.println("Enter location.");
						String location = keyboardInput.nextLine();
						Plant plant = new Plant(berryChosen, location, mulchNum);
						if (db.plant(plant)) {
							System.out.println("Successfully planted " + 
									berryChosen + "!");
						} else {
							System.out.println("Could not plant the " + 
									berryChosen + ".");
						}
					} catch (IllegalArgumentException e) {
						System.out.println(e.getMessage());
					}
				} else if (pathChosen == PLUCK) {
					ArrayList<Plant> plants = db.fullyGrownSubset();
					if (plants.size() == 0) {
						System.out.println("No plants are ready.");
					} else {
						System.out.println("Pick which plant?");
						for (int i = 0; i < plants.size(); i++) {
							System.out.print((i+1) + ": ");
							Plant p = plants.get(i);
							if (mode == VERBOSE_MODE) {
								System.out.println(p.toString());
							} else {
								System.out.println(p.shortInfo());
							}
							System.out.println();
						}
						int plantChoice = keyboardInput.nextInt();
						keyboardInput.nextLine();
						if (plantChoice < 1 || plantChoice > plants.size()) {
							System.out.println("No such berry exists.");
						} else {
							Plant plantChosen = plants.get(plantChoice - 1);
							int numberPicked = db.pluck(plantChosen);
							System.out.println(numberPicked + " " +
									plantChosen.getBerry() + "(s) Picked!");


						}
					}


				} else if (pathChosen == WATER) {
					db.sortBy(new PlantRemainingTimeComparator());
					ArrayList<Plant> plants = new ArrayList<>();
					for (Plant p : db) {
						plants.add(p);
					}
					if (plants.size() == 0) {
						System.out.println("No plants are ready.");
					} else {
						System.out.println("Water which plant?");
						for (int i = 0; i < plants.size(); i++) {
							System.out.print((i+1) + ": ");
							Plant p = plants.get(i);
							if (mode == VERBOSE_MODE) {
								System.out.println(p.toString());
							} else {
								System.out.println(p.shortInfo());
							}
							System.out.println();
						}
						int plantChoice = keyboardInput.nextInt();
						keyboardInput.nextLine();
						if (plantChoice < 1 || plantChoice > plants.size()) {
							System.out.println("No such berry exists.");
						} else {
							Plant plantChosen = plants.get(plantChoice - 1);
							db.water(plantChosen);
							System.out.println("The " +
									plantChosen.getBerry() + " was watered.");
						}
					}


				} else if (pathChosen == VIEW_HARVEST) {
					db.sortBy(new HoursUntilHarvestComparator());
				} else if (pathChosen == VIEW_MOISTURE) {
					db.sortBy(new PlantRemainingTimeComparator());
				} else if (pathChosen == VIEW_AGE) {
					db.sortBy(new PlantAgeComparator());
				} else if (pathChosen == VIEW_NAME) {
					db.sortBy(new BerryComparator());
				} else if (pathChosen == VIEW_LOCATION) {
					db.sortBy(new PlantLocationComparator());
				} else if (pathChosen == SWITCH) {
					if (mode == VERBOSE_MODE) {
						mode = NORMAL_MODE;
						System.out.println("Switched to short output mode.");
					} else {
						mode = VERBOSE_MODE;
						System.out.println("Switched to long output mode.");
					}
				}
				if (pathChosen == VIEW_HARVEST ||
						pathChosen == VIEW_MOISTURE ||
						pathChosen == VIEW_AGE ||
						pathChosen == VIEW_NAME ||
						pathChosen == VIEW_LOCATION) {
					if (db.size() == 0) {
						System.out.println("No plants are present.");
					} else {
						for (Plant p : db) {
							if (mode == VERBOSE_MODE) {
								System.out.println(p.toString());
							} else {
								System.out.println(p.shortInfo());
							}
							System.out.println();
						}
					}

				}
				if (pathChosen != CLOSE) {
					keyboardInput.nextLine();
				}

			}
		} finally {


			try {
				db.clearDeadPlants();
				if(db.save()) {
					System.out.println("Saved.");
				} else {
					System.out.println("No new data to save.");
				}
			} catch (IOException e) {
				System.out.println("Failed to save data.");
				e.printStackTrace();
			}
			keyboardInput.close();
		}
	}

	private static Mulch chooseMulch(String name) {
		try {
			return Mulch.valueOf(name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("This mulch does not exist");
		}
	}

	private static Berry parseBerry(int number, String name) {
		try {
			if (name != null) {
				return Berry.valueOf(name);
			} else {
				return Berry.values()[number];
			}
		} catch (IllegalArgumentException e) {
			
		} catch (IndexOutOfBoundsException e) {
			
		}
		throw new IllegalArgumentException("This berry does not exist.");
	}

}
