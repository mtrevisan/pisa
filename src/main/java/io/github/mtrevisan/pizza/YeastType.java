/**
 * Copyright (c) 2019-2020 Mauro Trevisan
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.pizza;


//https://www.bakeinfo.co.nz/files/file/117/BIRT_Yeast_Info_Sheet.pdf
//https://www.researchgate.net/publication/235699337_Microbiological_Quality_of_Active_Dry_and_Compressed_Baker's_Yeast_sold_in_Egypt
public enum YeastType{
	/**
	 * Compressed yeast is, in essence, cream yeast with most of the liquid removed. It is a soft solid, beige in color, and best known in
	 * the consumer form as small, foil-wrapped cubes of cake yeast. It is also available in a larger-block form for bulk usage. It is
	 * highly perishable. It is still widely available for commercial use, and is somewhat more tolerant of low temperatures than other
	 * forms of commercial yeast.
	 */
	FRESH(1., 0.7),
	/**
	 * It consists of coarse oblong granules of yeast, with live yeast cells encapsulated in a thick jacket of dry, dead cells with some
	 * growth medium. Under most conditions, ADY must first be proofed or rehydrated. It can be stored at room temperature for a year, or
	 * frozen for more than a decade, which means that it has better keeping qualities than other forms, but it is generally considered more
	 * sensitive than other forms to thermal shock when actually used in recipes.
	 */
	ACTIVE_DRY(2.4, 0.08),
	/**
	 * Appears similar to ADY, but has smaller granules with substantially higher percentages of live cells per comparable unit volumes. It
	 * is more perishable than ADY but also does not require rehydration, and can usually be added directly to all but the driest doughs. In
	 * general, instant yeast has a small amount of ascorbic acid added as a preservative. Some producers provide specific variants for
	 * doughs with high sugar contents, and such yeasts are more generally known as osmotolerant yeasts.
	 */
	INSTANT_DRY(3.125, 0.05);


	/**
	 * There's an average of 8.71e9±0.84e9 CFU/g (10^(9.94±0.04) cell/g)
	 *
	 * https://www.researchgate.net/publication/235699337_Microbiological_Quality_of_Active_Dry_and_Compressed_Baker's_Yeast_sold_in_Egypt
	 */
	public static final double FY_CELL_COUNT = 8.71e9 * ACTIVE_DRY.factor;

	final double factor;
	final double moistureContent;


	YeastType(final double factor, final double moistureContent){
		this.factor = factor;
		this.moistureContent = moistureContent;
	}

}
