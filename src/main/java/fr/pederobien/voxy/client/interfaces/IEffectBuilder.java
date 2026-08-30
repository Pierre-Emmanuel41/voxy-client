package fr.pederobien.voxy.client.interfaces;

import java.util.Map;

import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.IEffectParametersHolder;

public interface IEffectBuilder {

	/**
	 * Creates a holder that contains the parameters of an effect.
	 * 
	 * @param values A map that gather parameter's name / parameter's value.
	 * @return A holder that contains effect parameters with values updated from the input map.
	 */
	IEffectParametersHolder createHolder(Map<String, Object> values);

	/**
	 * Creates an effect from a set of values.
	 * 
	 * @param sampleRate The sample rate used for audio streams.
	 * @param holder     A holder that contains the parameters of an effect.
	 * @return The created effect.
	 */
	IEffect createEffect(float sampleRate, IEffectParametersHolder holder);
}
