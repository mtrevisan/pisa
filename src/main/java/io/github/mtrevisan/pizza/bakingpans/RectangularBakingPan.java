/**
 * Copyright (c) 2021 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.pizza.bakingpans;


import java.util.Objects;


public class RectangularBakingPan extends BakingPanAbstract{

	/** Baking pan edge 1 dimension [cm]. */
	double edge1;
	/** Baking pan edge 2 dimension [cm]. */
	double edge2;


	public static RectangularBakingPan create(final double edge1, final double edge2, final BakingPanMaterial material,
			final double thickness){
		return new RectangularBakingPan(edge1, edge2, material, thickness);
	}

	public static RectangularBakingPan createWithBakingSheet(final double edge1, final double edge2, final BakingPanMaterial material,
			final double thickness){
		final RectangularBakingPan pan = create(edge1, edge2, material, thickness);
		pan.hasBakingSheet = true;
		return pan;
	}

	private RectangularBakingPan(final double edge1, final double edge2, final BakingPanMaterial material, final double thickness){
		if(edge1 <= 0. || edge2 <= 0.)
			throw new IllegalArgumentException("Edges must be positive");
		Objects.requireNonNull(material, "Material must be present");
		if(thickness <= 0.)
			throw new IllegalArgumentException("Thickness must be positive");

		this.edge1 = edge1;
		this.edge2 = edge2;
		this.material = material;
		this.thickness = thickness;
	}

	@Override
	public double area(){
		return edge1 * edge2;
	}

}
