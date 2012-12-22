package com.hackhalo2.rendering.advsound;

import java.net.URI;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.hackhalo2.rendering.interfaces.core.IManager;
import com.hackhalo2.rendering.interfaces.core.ISoundSystem;
import com.hackhalo2.rendering.interfaces.sound.ICodecable;
import com.hackhalo2.rendering.interfaces.sound.IDimensionalSound;
import com.hackhalo2.rendering.interfaces.sound.ISoundLibrary;
import com.paulscode.sound.ICodec;
import com.paulscode.sound.SoundSystem;
import com.paulscode.sound.SoundSystemConfig;
import com.paulscode.sound.SoundSystemException;
import com.paulscode.sound.SoundSystemLogger;
import com.paulscode.sound.libraries.LibraryLWJGLOpenAL;

/**
 * The LWJGLSoundSystem library, powered by Paul's LWJGLOpenAL Library.<br />
 * <br />
 * This SoundSystem supports Codecs, is backed by the LibraryLWJGLOpenAL Library<br />
 * 
 * 
 * @author Jacob "HACKhalo2" Litewski
 */
public class LWJGLSoundSystem implements ISoundSystem, ICodecable, ISoundLibrary<LibraryLWJGLOpenAL>, 
IDimensionalSound, IManager {

	/**
	 * The API Version of the implementation
	 */
	public final int apiVersion = 1;

	private SoundSystem soundSystem = null; //The SoundSystem reference
	private Map<Integer, Sound> soundMap = null;
	private int codecCount = 0;
	private BitSet available = null;

	private LibraryLWJGLOpenAL library = null;

	/**
	 * Processes status messages, warnings, and error messages.
	 */
	private SoundSystemLogger logger = SoundSystemConfig.getLogger();

	public LWJGLSoundSystem() {
		try {
			this.library = new LibraryLWJGLOpenAL();
			this.soundMap = new TreeMap<Integer, Sound>();
			this.available = new BitSet(128);
		} catch (SoundSystemException e) {
			this.logger.printStackTrace(e, 0);
		}
	}

	public LWJGLSoundSystem(int soundSpace) {
		try {
			this.library = new LibraryLWJGLOpenAL();
			this.soundMap = new TreeMap<Integer, Sound>();
			this.available = new BitSet(soundSpace);
		} catch (Exception e) {
			this.logger.printStackTrace(e, 0);
		}
	}

	@Override
	public void initialize() {
		this.logger.message("Initializing LWJGLSoundSystem...", 0);
		try {
			this.logger.message("Linking Library 'LibraryLWJGLOpenAL'...", 1);
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);

			this.logger.message("Linking "+this.codecCount+" Codecs...", 1);

		} catch (SoundSystemException e) {
			this.logger.printStackTrace(e, 0);
		}

		this.soundSystem = new SoundSystem();
		this.logger.message("LWJGLSoundSystem set up sucessfully", 0);
	}

	@Override
	public int queue(URI soundPath, String filename, boolean isLooped, boolean priority) {
		int index = -1;
		try {
			Sound sound = new Sound(filename, soundPath, this.soundSystem);
			index = this.available.nextClearBit(0);
			if(this.soundMap.containsKey(index)) { //saftey check
				this.soundMap.remove(index);
			}
			this.available.flip(index); //set the index to true
			this.soundMap.put(index, sound); //put it in the soundMap
			sound.load();
			sound.setupStream(priority, isLooped, SoundSystemConfig.ATTENUATION_ROLLOFF, 0.8f);
			return index;
		} catch(Exception e) {
			this.logger.printStackTrace(e, 0);
			if(index != -1 && this.available.get(index)) {
				this.available.clear(index);
			}
			return -1;
		}
	}

	@Override
	public void dequeue(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			this.soundSystem.dequeueSound(sound.getName(), sound.getName());
			if(this.available.get(soundID)) { // Failsafe
				this.available.flip(soundID); //reset the bit so it can be reused
			}
			this.soundMap.remove(soundID); //Remove it from the map
		}
	}

	@Override
	public boolean isLooping(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			return sound.isLooping();
		} else return false;
	}

	@Override
	public boolean play(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			sound.play();
			return true;
		} else return false;
	}

	@Override
	public boolean play2D(int soundID, Vector2f vector) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			this.soundSystem.setPosition(sound.getName(), vector.x, vector.y, 0);
			sound.play();
			return true;
		} else return false;
	}

	@Override
	public boolean play3D(int soundID, Vector3f vector) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			this.soundSystem.setPosition(sound.getName(), vector.x, vector.y, vector.z);
			sound.play();
			return true;
		} else return false;
	}

	@Override
	public boolean isPlaying(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			return this.soundSystem.playing(sound.getName());
		} else return false;
	}

	@Override
	public byte pause(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			if(this.isPlaying(soundID)) {
				Sound sound = this.soundMap.get(soundID);
				sound.pause();
				return 1;
			} else return 0;
		} else return -1;
	}

	@Override
	public boolean isPaused(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			return !this.soundSystem.playing(sound.getName());
		} else return false;
	}

	@Override
	public byte resume(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			if(this.isPaused(soundID)) {
				Sound sound = this.soundMap.get(soundID);
				sound.play();
				return 1;
			} else return 0;
		} else return -1;
	}

	@Override
	public byte mute(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			if(!this.isMuted(soundID)) {
				Sound sound = this.soundMap.get(soundID);
				sound.mute();
				return 1;
			} else return 0;
		} else return -1;
	}

	@Override
	public boolean isMuted(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			return sound.isMuted();
		} else return false;
	}

	@Override
	public byte unmute(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			if(this.isMuted(soundID)) {
				Sound sound = this.soundMap.get(soundID);
				sound.unmute();
				return 1;
			} else return 0;
		} else return -1;
	}

	@Override
	public int getVolume(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			return sound.getVolume();
		} else return -1;
	}

	@Override
	public void setVolume(int soundID, int volume) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			sound.setVolume(volume);
		}
	}

	@Override
	public void stop() {
		Iterator<Sound> it = this.soundMap.values().iterator();
		
		while(it.hasNext()) {
			Sound sound = it.next();
			if(sound.isPlaying()) sound.stop();
		}
	}

	@Override
	public void stop(int soundID) {
		if(this.soundMap.containsKey(soundID)) {
			Sound sound = this.soundMap.get(soundID);
			sound.stop();
		}
	}

	@Override
	public void installCodec(String format, ICodec codec) throws SoundSystemException {
		this.codecCount++;
		SoundSystemConfig.setCodec(format, codec.getClass());
	}

	@Override
	public boolean hasCustomMIDICodec() {
		return SoundSystemConfig.midiCodec();
	}

	@Override
	public LibraryLWJGLOpenAL getSoundLibrary() {
		return this.library;
	}

	@Override
	public void cleanup() {
		this.soundMap = null;
		this.logger = null;
		this.soundSystem.cleanup();
		this.soundSystem = null;
		this.library = null;

	}

}
