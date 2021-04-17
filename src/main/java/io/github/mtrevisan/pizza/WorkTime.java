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

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;


public class WorkTime{

	final Duration doughMaking;
	final Duration[] stagesWork;
	final Duration seasoning;
	final LocalTime timeToBake;


	public static WorkTime create(final Duration doughMaking, final Duration[] stagesWork, final Duration seasoning,
			final LocalTime timeToBake){
		return new WorkTime(doughMaking, stagesWork, seasoning, timeToBake);
	}

	private WorkTime(final Duration doughMaking, final Duration[] stagesWork, final Duration seasoning, final LocalTime timeToBake){
		Objects.requireNonNull(timeToBake, "Time to bake not set");

		this.timeToBake = timeToBake;
		this.doughMaking = doughMaking;
		this.stagesWork = stagesWork;
		this.seasoning = seasoning;
	}

}
