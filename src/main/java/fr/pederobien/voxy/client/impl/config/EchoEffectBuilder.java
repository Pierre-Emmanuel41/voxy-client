package fr.pederobien.voxy.client.impl.config;

import java.util.Map;

import fr.pederobien.sound.impl.effects.EchoEffect;
import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.IEffectParametersHolder;
import fr.pederobien.voxy.client.interfaces.IEffectBuilder;

public class EchoEffectBuilder implements IEffectBuilder {

	@Override
	public IEffectParametersHolder createHolder(Map<String, Object> values) {
		IEffectParametersHolder holder = EchoEffect.holder();
		holder.update(values);
		return holder;
	}

	@Override
	public IEffect createEffect(float sampleRate, IEffectParametersHolder holder) {
		int delay = (int) holder.getValue(EchoEffect.DELAY);
		float feedback = (float) holder.getValue(EchoEffect.FEEDBACK);
		float gain = (float) holder.getValue(EchoEffect.GAIN);
		return new EchoEffect(sampleRate, delay, feedback, gain);
	}

}
