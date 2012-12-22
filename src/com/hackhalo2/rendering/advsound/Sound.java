package com.hackhalo2.rendering.advsound;

import java.net.MalformedURLException;
import java.net.URI;

import com.hackhalo2.rendering.interfaces.sound.ISoundObject;
import com.paulscode.sound.FilenameURL;
import com.paulscode.sound.SoundSystem;
import com.paulscode.sound.SoundSystemException;

public class Sound implements ISoundObject {
	private FilenameURL file = null;
	private SoundSystem soundSystem = null;
	private float previousVolume = 0;
	private boolean looping = false;

	public Sound(String name, URI path, SoundSystem soundSystem) throws SoundSystemException, MalformedURLException {
		this.file = new FilenameURL(path.toURL(), name);
		this.soundSystem = soundSystem;
	}
	
	@Override
	public void setupStream(boolean priority, boolean looped, int attenuation, float distOrRoll) {
		this.looping = looped;
		this.soundSystem.newStreamingSource(priority, this.file.getFilename(), this.file.getURL(),
				this.file.getFilename(), looped, 0, 0, 0, attenuation, distOrRoll);
	}
	
	@Override
	public void load() {
		this.soundSystem.loadSound(this.file.getURL(), this.getName());
	}

	@Override
	public void play() {
		this.soundSystem.play(this.getName());
	}

	@Override
	public void playInBackground() {
		this.playInBackground(false);
	}
	
	@Override
	public boolean isPlaying() {
		return this.soundSystem.playing(this.getName());
	}

	@Override
	public void playInBackground(boolean looped) {
		this.soundSystem.backgroundMusic(""+this.file.hashCode(), this.getName(), looped);
	}

	@Override
	public void pause() {
		this.soundSystem.pause(this.getName());
	}

	@Override
	public void mute() {
		if(!this.isMuted()) {
			this.previousVolume = this.soundSystem.getVolume(this.getName());
			this.soundSystem.setVolume(this.getName(), 0);
		}
	}

	@Override
	public void unmute() {
		if(this.isMuted())
			this.soundSystem.setVolume(this.getName(), this.previousVolume);
	}

	@Override
	public boolean isMuted() {
		return (this.soundSystem.getVolume(this.getName()) == 0);
	}

	@Override
	public void stop() {
		this.soundSystem.stop(this.getName());
	}

	@Override
	public void setVolume(int volume) {
		if(volume > 100) volume = 100;
		if(volume < 0) volume = 0;

		float convertedVolume = ((float)(volume/100));
		this.soundSystem.setVolume(this.getName(), convertedVolume);
	}

	@Override
	public int getVolume() {
		final float volume = this.soundSystem.getVolume(this.getName());
		return ((int)(volume*100));
	}

	@Override
	public String getName() {
		return this.file.getFilename();
	}
	
	@Override
	public boolean isLooping() {
		return this.looping;
	}

}
