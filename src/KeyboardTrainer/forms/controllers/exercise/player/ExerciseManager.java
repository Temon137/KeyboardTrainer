package KeyboardTrainer.forms.controllers.exercise.player;


import KeyboardTrainer.data.exercise.Exercise;
import KeyboardTrainer.data.statistics.Statistics;
import KeyboardTrainer.data.user.User;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


class ExerciseManager {
	private final Exercise             exercise;
	private final Consumer<Statistics> onUpdateStatistics;
	private final EndHandler           onEndExerciseHandler;
	private final String               text;
	private final StatisticsBuilder    statisticsBuilder;
	private final ExerciseVisualizer   exerciseVisualizer;
	private final KeyboardVisualizer   keyboardVisualizer;
	
	private ScheduledExecutorService clockExecutor;
	private int                      currentLetterIndex;
	private boolean                  isStarted;
	private boolean                  isFinish;
	
	ExerciseManager(User user,
	                Exercise exercise,
	                Consumer<Statistics> onUpdateStatistics,
	                EndHandler onEndExerciseHandler) {
		isStarted = false;
		isFinish = true;
		
		this.exercise = exercise;
		this.onUpdateStatistics = onUpdateStatistics;
		this.onEndExerciseHandler = onEndExerciseHandler;
		
		text = exercise.getText();
		
		statisticsBuilder = new StatisticsBuilder();
		statisticsBuilder.setUserId(user);
		statisticsBuilder.setExerciseId(exercise);
		
		exerciseVisualizer = new GodlikeVisualizer(exercise.getText());
		keyboardVisualizer = new KeyboardVisualizer(580); //TODO: проклятые размеры
	}
	
	void startExercise() {
		currentLetterIndex = 0;
		statisticsBuilder.setExerciseId(exercise);
		clockExecutor = Executors.newScheduledThreadPool(1);
		isFinish = false;
		
		exerciseVisualizer.start();
		keyboardVisualizer.drawGoodKey(text.charAt(0));
	}
	
	/**
	 * Нужно для начала отсчёта времени после первого нажатия клавиши.
	 * Крутое название. Кто-нибудь, придумайте другое.
	 */
	private void realStartExercise() {
		statisticsBuilder.startBuild();
		
		// Тикает раз в секунду и обновляет статистику
		clockExecutor.scheduleAtFixedRate(() -> Platform.runLater(this::updateState),
		                                  0L,
		                                  1L,
		                                  TimeUnit.SECONDS);
	}
	
	void handleKey(String key) {
		if (isFinish) {
			return;
		}
		if (!isStarted) {
			realStartExercise();
			isStarted = true;
		}
		
		String currentLetter = String.valueOf(text.charAt(currentLetterIndex));
		if (currentLetter.equals(key)
		    || (isEndLine(currentLetter) && isEndLine(key))) {
			exerciseVisualizer.handleGoodKey();
			currentLetterIndex++;
			if (currentLetterIndex < text.length()) {
				keyboardVisualizer.drawGoodKey(text.charAt(currentLetterIndex));
			}
		} else {
			exerciseVisualizer.handleBadKey();
			keyboardVisualizer.drawBadKey(text.charAt(currentLetterIndex));
			statisticsBuilder.incrementErrorsCount();
		}
		statisticsBuilder.incrementPressingsCount();
		
		updateState();
	}
	
	private boolean isEndLine(String s) {
		return "\n".equals(s) || "\r".equals(s);
	}
	
	private void updateState() {
		Statistics currentStatistics = statisticsBuilder.getStatistics();
		onUpdateStatistics.accept(currentStatistics);
		
		if (checkEndExercise(currentStatistics)) {
			endExercise();
		}
	}
	
	private synchronized boolean checkEndExercise(Statistics currentStatistics) {
		return currentLetterIndex >= text.length()
		       || currentStatistics.getErrorsCount() >= exercise.getMaxErrorsCount()
		       || currentStatistics.getAveragePressingTime() >= exercise.getMaxAveragePressingTime();
	}
	
	private synchronized void endExercise() {
		if (isFinish) {
			return;
		}
		
		breakExercise();
		Statistics statistics = statisticsBuilder.getStatistics();
		onEndExerciseHandler.endExercise(statistics);
	}
	
	void breakExercise() {
		isStarted = false;
		isFinish = true;
		clockExecutor.shutdown();
	}
	
	ExerciseVisualizer getExerciseVisualizer() {
		return exerciseVisualizer;
	}
	
	boolean isFinish() {
		return isFinish;
	}
	
	KeyboardVisualizer getKeyboardVisualizer() {
		return keyboardVisualizer;
	}
}