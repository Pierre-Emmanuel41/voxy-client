package fr.pederobien.voxy.client.impl.sound;

import fr.pederobien.sound.interfaces.ISpeakers;
import fr.pederobien.voxy.client.interfaces.IVoxySpeakers;

public class VoxySpeakers implements IVoxySpeakers {
	private ISpeakers speakers;

	/**
	 * Creates an adapter for a voxy client of a speakers.
	 * 
	 * @param speakers The speakers to use to player audio streams.
	 */
	public VoxySpeakers(ISpeakers speakers) {
		this.speakers = speakers;
	}

	@Override
	public void open() throws Exception {
		speakers.open();
	}

	@Override
	public void close() throws Exception {
		speakers.close();
	}

	@Override
	public void write(String name, byte[] data) {
		speakers.getMixer().write(name, data);
	}

	@Override
	public void setVolumes(String name, float left, float right, float global) {
		speakers.getMixer().setVolumes(name, left, right, global);
	}

	@Override
	public void resetVolumes() {
		speakers.getMixer().resetVolumes();
	}
}
