package eecs499.eartrainer.intervalsinger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum Note{
	C0(-57), Db0(-56), D0(-55), Eb0(-54), E0(-53), F0(-52), Gb0(-51), G0(-50), Ab0(-49), A0(-48), Bb0(-47), B0(-46),
	C1(-45), Db1(-44), D1(-43), Eb1(-42), E1(-41), F1(-40), Gb1(-39), G1(-38), Ab1(-37), A1(-36), Bb1(-35), B1(-34),
	C2(-33), Db2(-32), D2(-31), Eb2(-30), E2(-29), F2(-28), Gb2(-27), G2(-26), Ab2(-25), A2(-24), Bb2(-23), B2(-22),
	C3(-21), Db3(-20), D3(-19), Eb3(-18), E3(-17), F3(-16), Gb3(-15), G3(-14), Ab3(-13), A3(-12), Bb3(-11), B3(-10),
	C4(-9), Db4(-8), D4(-7), Eb4(-6), E4(-5), F4(-4), Gb4(-3), G4(-2), Ab4(-1), A4(0), Bb4(1), B4(2),
	C5(3), Db5(4), D5(5), Eb5(6), E5(7), F5(8), Gb5(9), G5(10), Ab5(11), A5(12), Bb5(13), B5(14),
	C6(15), Db6(16), D6(17), Eb6(18), E6(19), F6(20), Gb6(21), G6(22), Ab6(23), A6(24), Bb6(25), B6(26),
	C7(27), Db7(28), D7(29), Eb7(30), E7(31), F7(32), Gb7(33), G7(34), Ab7(35), A7(36), Bb7(37), B7(38),
	C8(39), Db8(40), D8(41), Eb8(42), E8(43), F8(44), Gb8(45), G8(46), Ab8(47), A8(48), Bb8(49), B8(50),Note_Unknown(1000);
	
	
	private double freq;
	private int value;
	
	//HashMap of Integer to Note
	private static final Map<Integer, Note> intToNoteMap = new HashMap<Integer, Note>();
	//initialize the HashMap
	static {
		for (Note type : Note.values()) {
			intToNoteMap.put(type.value, type);
		}
	}
	
	public static Note fromInt(int i) {
	    Note type = intToNoteMap.get(Integer.valueOf(i));
	    if (type == null) 
	        return Note.Note_Unknown;
	    return type;
	}
	
	//Returns a random Note within a given range
	public static Note getRandom(Note lowerLimit, Note upperLimit) {
		int lowerLimitInt = lowerLimit.getValue();
		int upperLimitInt = upperLimit.getValue();
		Random rand = new Random();
		int randomNoteInt = rand.nextInt(upperLimitInt - lowerLimitInt + 1) + lowerLimitInt;
		return Note.fromInt(randomNoteInt);
	}
	
	private Note(int n){
		this.freq = Math.pow(2.0, n/12.0) * 440;
		this.value = n;
	}
	
	public double getFrequency() {
		return freq;
	}
	
	public int getValue() {
		return value;
	}
	
	/*
	 * Returns the note name value of the note, where the value is between 0 - 11, 0 mapping to A, 
	 * 1 mapping to Bb, 2 mapping to B, 3 mapping to C, etc, 11 mapping to Ab
	 */
	public int getNoteNameValue() {
		if(value == 1000) {//unknown
			throw new IllegalArgumentException("cannot get NoteNameValue for Note_Unknown");
		}
		if(value >= 0) {
			return value % 12;
		} else {
			return (12 - (Math.abs(value) % 12)) % 12;
		}
	}
	
	/*
	 * Parameters: n - describes the distance (ex: 3 meaning 3 half steps up, -1 meaning 1 half step down)
	 * 		
	 * Returns the note name value of the note that is n half steps away, where the value is between 0 - 11, 0 mapping to A, 
	 * 1 mapping to Bb, 2 mapping to B, 3 mapping to C, etc, 11 mapping to Ab
	 */
	
	public int getNthHalfStepsAway(int n) {
		return Note.fromValue(value + n).getNoteNameValue();
	}
	
	public static Note fromValue(int value) {
		return Note.fromInt(value);
	}
	
	
}


