package KeyboardTrainer.forms.components.tree.exercise;


import KeyboardTrainer.data.exercise.Exercise;


public class ExerciseTreeItem {
	private final boolean  isExercise;
	private final int      level;
	private final Exercise exercise;
	
	ExerciseTreeItem(Exercise exercise) {
		isExercise = true;
		this.exercise = exercise;
		this.level = -13; // Прост весело
	}
	
	ExerciseTreeItem(int level) {
		isExercise = false;
		this.level = level;
		this.exercise = null;
	}
	
	public boolean isExercise() {
		return isExercise;
	}
	
	@SuppressWarnings("WeakerAccess")
	public int getLevel() {
		return level;
	}
	
	public Exercise getExercise() {
		return exercise;
	}
}