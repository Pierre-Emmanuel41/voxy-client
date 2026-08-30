package fr.pederobien.voxy.client.impl.config;

import java.util.Map;

import fr.pederobien.sound.impl.effects.NoEffect;
import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.IEffectParametersHolder;
import fr.pederobien.voxy.client.interfaces.IEffectBuilder;

public class NoEffectBuilder implements IEffectBuilder {

	@Override
	public IEffectParametersHolder createHolder(Map<String, Object> values) {
		return NoEffect.holder();
	}

	@Override
	public IEffect createEffect(float sampleRate, IEffectParametersHolder holder) {
		return new NoEffect();
	}
}
