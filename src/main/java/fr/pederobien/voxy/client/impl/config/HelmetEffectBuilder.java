package fr.pederobien.voxy.client.impl.config;

import java.util.Map;

import fr.pederobien.sound.impl.effects.HelmetEffect;
import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.IEffectParametersHolder;
import fr.pederobien.voxy.client.interfaces.IEffectBuilder;

public class HelmetEffectBuilder implements IEffectBuilder {

	@Override
	public String getEffectName() {
		return HelmetEffect.NAME;
	}

	@Override
	public IEffectParametersHolder createHolder(Map<String, Object> values) {
		IEffectParametersHolder holder = HelmetEffect.holder();
		holder.update(values);
		return holder;
	}

	@Override
	public IEffect createEffect(float sampleRate, IEffectParametersHolder holder) {
		float frequency = (float) holder.getValue(HelmetEffect.FREQUENCY);
		float qualityFactor = (float) holder.getValue(HelmetEffect.QUALITY_FACTOR);
		return new HelmetEffect(sampleRate, frequency, qualityFactor);
	}

}
