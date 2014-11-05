package eecs499.eartrainer.intervalsinger;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class ToneGenerator {
	private static final String TAG = "ToneGenerator";
	
	public static final int TYPE_SINE = 0;
	public static final int TYPE_TRIANGLE = 1;
	public static final int TYPE_SAWTOOTH = 2;
	public static final int TYPE_SQUARE = 3;
	final AudioTrack audioTrack;

	// private AudioTrack mAudioTrack;
	private int wave_type;
	private int duration; // in seconds
	private final int sampleRate = 44100; // 44100 sample rate
	private int numSamples;
	private double sample[];
	private double freqOfTone; // in hz
	private byte generatedSnd[];

	public ToneGenerator() {
		audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, sampleRate*2,
				AudioTrack.MODE_STREAM);
		Log.v(TAG, "Creating ToneGenerator...");
		Log.v(TAG, "setWaveType->setting wave_type to:" + TYPE_SINE);
		wave_type = TYPE_SINE; // sine wave by default
		audioTrack.play();
	}

	public void setWaveType(int wave_type_) {
		Log.v(TAG, "setWaveType->setting wave_type to:" + wave_type_);
		wave_type = wave_type_;
	}

	public void playTone(double freq, int duration_) {
		Log.v(TAG, "Duration: " + duration);
		
		duration = duration_;
		freqOfTone = freq;
		numSamples = (duration * sampleRate);
		sample = new double[numSamples];
		generatedSnd = new byte[2 * numSamples];
		
		genTone();
		playSound();
		
	}
	private void genTone() {
		Log.v(TAG, "WAVE_TYPE:" + wave_type);
		// fill out the array
		double phase = 0;
		for (int i = 0; i < numSamples; ++i) {
			switch (wave_type) {
			case TYPE_SINE:
				sample[i] = Math.sin(phase);
				break;
			case TYPE_SQUARE:
				if (phase < Math.PI) {
					sample[i] = 1;
				} else {
					sample[i] = -1;
				}
				break;
			case TYPE_SAWTOOTH:
				sample[i] =( 2 * (phase/(Math.PI * 2))) - 1;
				break;
			case TYPE_TRIANGLE:
				if (phase < Math.PI) {
					sample[i] = ( 2 / Math.PI) * phase - 1;				
				} else {
					sample[i] = (-2 / Math.PI) * phase + 3;	
				}
				break;				
			}
			phase = phase + ((2 * Math.PI * freqOfTone) / sampleRate);
			if (phase > (2 * Math.PI)) {
				phase = phase - (2 * Math.PI);
			}
		}

		// add attack and decay
		int idx = 0;
		int i = 0;

		int ramp = numSamples / 3; // Amplitude ramp as a percent(3%) of
									// sample
									// count
		double amplitude_factor = 32767/1;

		for (i = 0; i < ramp; ++i) { // Ramp amplitude up (to avoid clicks)
			double dVal = sample[i];
			// Ramp up to maximum
			final short val = (short) ((dVal * amplitude_factor * i / ramp));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}

		for (; i < numSamples - ramp; ++i) { // Max amplitude for most of
												// the samples
			double dVal = sample[i];
			// scale to maximum amplitude
			final short val = (short) ((dVal * amplitude_factor));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}

		for (; i < numSamples; ++i) { // Ramp amplitude down
			double dVal = sample[i];
			// Ramp down to zero
			final short val = (short) ((dVal * amplitude_factor * (numSamples - i) / ramp));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}

	}

	private void playSound() {
		try {
			audioTrack.write(generatedSnd, 0, generatedSnd.length);
		} catch (IllegalStateException e) {
			Log.v(TAG, "CAUGHT AN ERROR DURING PLAYING TONE");
			e.printStackTrace();
		}
	}	
}
