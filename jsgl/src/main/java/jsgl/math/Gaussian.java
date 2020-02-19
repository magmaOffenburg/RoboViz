/*
 *  Copyright 2011 Justin Stoecker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jsgl.math;

import jsgl.math.vector.Vec2f;

public class Gaussian
{
	public static class BlurParams
	{
		public float[] offsets;
		public float[] weights;

		public BlurParams(Vec2f[] offsets, float[] weights)
		{
			this.offsets = new float[offsets.length * 2];
			for (int i = 0; i < offsets.length; i++) {
				this.offsets[i * 2] = offsets[i].x;
				this.offsets[i * 2 + 1] = offsets[i].y;
			}

			this.weights = weights;
		}
	}

	private static float gaussian(int n, float blurriness)
	{
		return (float) ((1.0f / Math.sqrt(2 * Math.PI * blurriness)) *
						Math.exp(-(n * n) / (2 * blurriness * blurriness)));
	}

	/** Calculates horizontal and vertical blur parameters */
	public static BlurParams[] calcBlurParams(float blurriness, int samples, int w, int h)
	{
		BlurParams hBlur = calcBlurParams(1.0f / w, 0, samples, blurriness);
		BlurParams vBlur = calcBlurParams(0, 1.0f / h, samples, blurriness);
		return new BlurParams[] {hBlur, vBlur};
	}

	private static BlurParams calcBlurParams(float x, float y, int samples, float blurriness)
	{
		float[] weights = new float[samples];
		Vec2f[] offsets = new Vec2f[samples];

		// the starting pixel
		weights[0] = gaussian(0, blurriness);
		offsets[0] = new Vec2f(0);

		// calculate weights / offsets
		float totalWeight = weights[0];
		for (int i = 0; i < samples / 2; i++) {
			// weight for pixels on both sides
			float weight = gaussian(i + 1, blurriness);
			weights[i * 2 + 1] = weight;
			weights[i * 2 + 2] = weight;
			totalWeight += weight * 2;

			// offset for texture coordinates
			Vec2f offset = new Vec2f(x, y).times(i * 2 + 1.5f);
			offsets[i * 2 + 1] = offset;
			offsets[i * 2 + 2] = offset.times(-1);
		}

		// average the weights by total weight
		for (int i = 0; i < samples; i++)
			weights[i] /= totalWeight;

		return new BlurParams(offsets, weights);
	}
}
