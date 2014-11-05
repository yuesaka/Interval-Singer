package eecs499.eartrainer.intervalsinger;

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final String[] NOTE_NAMES = { "A", "Bb", "B", "C", "Db",
			"D", "Eb", "E", "F", "F#", "G", "Ab" };
	private static final String[] intervals = { "Unison", "minor 2nd",
			"Major 2nd", "minor 3rd", "Major 3rd", "Perfect 4th", "Tritone",
			"Perfect 5th", "minor 6th", "Major 6th", "minor 7th", "Major 7th",
			"Octave" };

	// Questions related variables
	private Note baseNote;
	private Note answerNote;
	private int answerInterval;
	private int answerDirection;
	private int numCorrects, numIncorrects;
	private float percentage = 1;

	// UI
	private TextView mFreqText;
	private TextView mAnswerText;
	private TextView mIntervalText;
	private TextView mPercentage;
	//private TextView mCountDown;
	private Button mPlayNote;
	private Button mPlayAnswer;
	private Button mNextQuestion;
	private ToggleButton mRecordToggle;
	private Tuner mTuner;
	private CountDownTimer mCountDownTimer;
	private final Handler mHandler = new Handler();
	private HashMap<Integer, Integer> mPossibleAnswers = new HashMap<Integer, Integer>();
	private Integer mUserAnswer;

	//private CountDownTimer mTimeLimitTimer;
	private boolean timerRunning;

	private static ToneGenerator mToneGenerator;

	// preferences
	private int timelimit;
	private int wavetype;
	private int max_interval; // 0 = unison, 12 = octave, 13 = m9 etc...

	private final Runnable callback = new Runnable() {
		public void run() {
			Log.v(TAG, "in callback...");
			double linearFreq = Math.log(mTuner.currentFrequency / 440.0)
					/ Math.log(2) + 4;
			double octave = Math.floor(linearFreq);
			double cents = 1200 * (linearFreq - octave);
			int noteNum = (int) Math.round(cents / 100);
			cents = Math.round(cents - noteNum * 100);
			int key = noteNum % 12;
			Log.v(TAG, "possible answer:" + key);
			if (mPossibleAnswers.containsKey(key)) {
				mPossibleAnswers.put(key, mPossibleAnswers.get(key) + 1);
			} else {
				mPossibleAnswers.put(key, 1);
			}
			// mFreqText.setText(NOTE_NAMES[key]);
			// mFreqText.setText(Double.toString(mTuner.currentFrequency));
		}
	};

	@Override
	protected void onResume() {
		Log.v(TAG, "in onResume()...");
		super.onResume();
		SharedPreferences SP = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		timelimit = Integer.parseInt(SP.getString("timelimit", "5"));
		wavetype = Integer.parseInt(SP.getString("waveform", "0"));
		max_interval = Integer.parseInt(SP.getString("interval", "12"));
		mToneGenerator.setWaveType(wavetype);
		//mCountDown.setText("" + timelimit);
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "in onPause()...");
		super.onPause();
		// Log.v(TAG, "Stopping Tuner...");
		// mTuner.close();
	}

	private void startTuner() {
		Log.v(TAG, "Instantiating Tuner obj...");
		mTuner = new Tuner(mHandler, callback);
		Log.v(TAG, "Starting tuner...");
		mTuner.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFreqText = (TextView) findViewById(R.id.freq);
		mAnswerText = (TextView) findViewById(R.id.answer_pitch);
		mIntervalText = (TextView) findViewById(R.id.question_interval);
		mPlayNote = (Button) findViewById(R.id.play_note_button);
		mPlayAnswer = (Button) findViewById(R.id.play_answer);
		mPercentage = (TextView) findViewById(R.id.percentage);
		//mCountDown = (TextView) findViewById(R.id.countdown);
		mNextQuestion = (Button) findViewById(R.id.generate_question_button);
		mToneGenerator = new ToneGenerator();

		// get your ToggleButton
		mRecordToggle = (ToggleButton) findViewById(R.id.record_toggle);
		generate_question(null);
		// set preferences
		SharedPreferences SP = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		timelimit = Integer.parseInt(SP.getString("timelimit", "5"));
		wavetype = Integer.parseInt(SP.getString("waveform", "0"));
		max_interval = Integer.parseInt(SP.getString("interval", "12"));
		mToneGenerator.setWaveType(wavetype);
		//mCountDown.setText("" + timelimit);

		numCorrects = 0;
		numIncorrects = 0;
		
		// attach an OnClickListener
		mRecordToggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mRecordToggle.isChecked()) {// ON
					startTuner();
					mRecordToggle.setEnabled(false);
					mCountDownTimer = new CountDownTimer(2000, 100) {

						public void onTick(long millisUntilFinished) {
							// mCountdown.setText("" + millisUntilFinished /
							// 1000);
						}

						public void onFinish() {
							mRecordToggle.performClick();
							Log.v(TAG, "Finished Recording!");
							for (HashMap.Entry<Integer, Integer> entry : mPossibleAnswers
									.entrySet()) {
								if (mUserAnswer == null
										|| entry.getValue().compareTo(
												mPossibleAnswers
														.get(mUserAnswer)) > 0) {
									mUserAnswer = entry.getKey();
								}
							}
							if (mUserAnswer == answerNote.getNoteNameValue()) {
								Toast.makeText(getApplicationContext(),
										"Correct!", Toast.LENGTH_SHORT).show();
								numCorrects++;
								calculatePercentage();
							} else {
								Toast.makeText(getApplicationContext(),
										"Wrong!", Toast.LENGTH_SHORT).show();
								numIncorrects++;
								calculatePercentage();
							}
							mFreqText.setText("Your Answer:" + NOTE_NAMES[mUserAnswer]);
							mAnswerText.setText("Correct Answer:" + NOTE_NAMES[answerNote
									.getNoteNameValue()]);
							mPlayAnswer.setEnabled(true);

							// Toast.makeText(getApplicationContext(),
							// "Answer:" + NOTE_NAMES[mUserAnswer],
							// Toast.LENGTH_SHORT).show();
							mUserAnswer = null;
							mPossibleAnswers.clear();
							//mTimeLimitTimer.cancel();
							mRecordToggle.setEnabled(false);
							//mNextQuestion.setEnabled(true);

						}
					};
					mCountDownTimer.start();

				} else {// OFF
					mRecordToggle.setEnabled(true);
					mTuner.close();

				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// Log.v("MainActivity", Integer.toString(item.getItemId()));
		if (item.getItemId() == R.id.menu_settings) {
			Intent i = new Intent(getBaseContext(), AppPreferences.class);
			startActivity(i);
		}
		return true;
	}

	public void play_note(View Button) {
		mPlayNote.setEnabled(false);
		mToneGenerator.playTone(baseNote.getFrequency(), 1);
//		mTimeLimitTimer = new CountDownTimer(timelimit * 1000, 100) {
//
//			public void onTick(long millisUntilFinished) {
//				mCountDown.setText("" + millisUntilFinished / 1000);
//			}
//
//			public void onFinish() {
//				// if the user has not started recording the note, force start
//				if (mRecordToggle.isEnabled()) {
//					mRecordToggle.performClick();
//				}
//			}
//		};
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			public void run() {
//				timerRunning = true;
//				mTimeLimitTimer.start();
//			}
//		}, 1000);

		// starting
		// notes are
		// between
		// A3 and A5
		// Random rand = new Random();
		// int interval = rand.nextInt(13);// for now, 0(unison) to 12(octave)
		// correctInterval = interval;
		// int direction = rand.nextInt(2);// 0 = up, 1 = down;
		// correctDirection = direction;
	}

	public void play_answer(View button) {

		mToneGenerator.playTone(baseNote.getFrequency(), 1);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				if (answerDirection == 0) {// Up
					mToneGenerator.playTone(
							Note.fromInt(baseNote.getValue() + answerInterval)
									.getFrequency(), 1);
				} else {// Down
					mToneGenerator.playTone(
							Note.fromInt(baseNote.getValue() - answerInterval)
									.getFrequency(), 1);
				}
			}
		}, 1000);
	}

	public void generate_question(View Button) {
		Random rand = new Random();
		baseNote = Note.getRandom(Note.A3, Note.A5);// for now,
		// Log.v(TAG, "generate_question -> base note: " +
		// NOTE_NAMES[baseNote.getValue()])
		int interval = rand.nextInt(1 + max_interval);
		int direction = rand.nextInt(2);// 0 = up, 1 = down;
		answerInterval = interval;
		answerDirection = direction;
		int answerNoteValue; // 0 - 11 where 0 is A and 11 is Ab
		if (direction == 0) {
			answerNoteValue = baseNote.getNthHalfStepsAway(interval);
		} else {
			answerNoteValue = baseNote.getNthHalfStepsAway(0 - interval);
		}
		answerNote = Note.fromValue(answerNoteValue);
		Log.v(TAG,
				"generate_question -> base note: "
						+ NOTE_NAMES[baseNote.getNoteNameValue()]);
		Log.v(TAG, "generate_question -> correct answer:"
				+ NOTE_NAMES[answerNote.getNoteNameValue()]);

		mIntervalText.setText(intervals[interval] + " "
				+ (direction == 0 ? "Up" : "Down") + " from "
				+ NOTE_NAMES[baseNote.getNoteNameValue()]);
		mPlayNote.setText("Play " + NOTE_NAMES[baseNote.getNoteNameValue()]);
		//mCountDown.setText("" + timelimit);
		mPlayAnswer.setEnabled(false);
		mPlayNote.setEnabled(true);
		//mNextQuestion.setEnabled(false);
		mRecordToggle.setEnabled(true);
		mFreqText.setText("Your Answer:");
		mAnswerText.setText("Correct Answer:");
		calculatePercentage();
		
	}

	private void calculatePercentage() {
		int numQuestions = numCorrects + numIncorrects;
		percentage =  numQuestions != 0 ? ((float)numCorrects / numQuestions) : 1;
		mPercentage.setText(Float.toString(percentage * 100) + "%" + 
				" (" + Integer.toString(numCorrects) + " out of " + Integer.toString(numQuestions) + ")");
	}
}
